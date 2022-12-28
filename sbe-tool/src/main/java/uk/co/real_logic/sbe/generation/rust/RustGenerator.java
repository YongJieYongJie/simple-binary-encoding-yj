/*
 * Copyright 2013-2022 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.generation.rust;

import static java.util.stream.Collectors.toMap;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.characterEncoding;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.codecModName;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.decoderName;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.encoderName;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.formatFunctionName;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.formatStructName;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.generateRustLiteral;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.rustTypeName;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.toLowerSnakeCase;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectFields;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectGroups;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectVarData;
import static uk.co.real_logic.sbe.ir.Signal.BEGIN_ENUM;
import static uk.co.real_logic.sbe.ir.Signal.BEGIN_SET;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.SpecMustacheFactory;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.agrona.Verify;
import org.agrona.generation.OutputManager;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.Generators;
import uk.co.real_logic.sbe.generation.java.JavaUtil;
import uk.co.real_logic.sbe.generation.rust.templatemodels.BitSet;
import uk.co.real_logic.sbe.generation.rust.templatemodels.CargoToml;
import uk.co.real_logic.sbe.generation.rust.templatemodels.Composite;
import uk.co.real_logic.sbe.generation.rust.templatemodels.Enum;
import uk.co.real_logic.sbe.generation.rust.templatemodels.Enum.EnumItem;
import uk.co.real_logic.sbe.generation.rust.templatemodels.LibRs;
import uk.co.real_logic.sbe.generation.rust.templatemodels.Message;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.GroupDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.VarDataDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder.BitSetDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder.CompositeDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder.EnumDecoderBasic;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder.EnumDecoderConstant;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder.PrimitiveDecoderArray;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder.PrimitiveDecoderConstant;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder.PrimitiveDecoderOptional;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder.PrimitiveDecoderRequired;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.GroupEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.VarDataEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.BitSetEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.CompositeEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.EnumEncoderBasic;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.EnumEncoderConstant;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.PrimitiveEncoderArray;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.PrimitiveEncoderArray.ArrayItems;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.PrimitiveEncoderBasic;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.PrimitiveEncoderConstant;
import uk.co.real_logic.sbe.ir.Encoding;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;
/** Generate codecs for the Rust programming language. */
public class RustGenerator implements CodeGenerator {
  static final String WRITE_BUF_TYPE = "WriteBuf";
  static final String READ_BUF_TYPE = "ReadBuf";
  private final Ir ir;
  private final RustOutputManager outputManager;

  /**
   * Create a new Rust language {@link CodeGenerator}.
   *
   * @param ir for the messages and types.
   * @param outputManager for generating the codecs to.
   */
  public RustGenerator(final Ir ir, final OutputManager outputManager) {
    Verify.notNull(ir, "ir");
    Verify.notNull(outputManager, "outputManager");

    this.ir = ir;
    this.outputManager = (RustOutputManager) outputManager;
  }

  static List<FieldEncoder> generateEncoderFields(final List<Token> tokens) {
    List<FieldEncoder> fieldEncoderValues = new ArrayList<>();
    Generators.forEachField(
        tokens,
        (fieldToken, typeToken) -> {
          final String name = fieldToken.name();
          switch (typeToken.signal()) {
            case ENCODING -> fieldEncoderValues.add(generateEncoderFields_primitives(typeToken, name));
            case BEGIN_ENUM -> fieldEncoderValues.add(generateEnumEncoder(fieldToken, typeToken, name));
            case BEGIN_SET -> fieldEncoderValues.add(generateBitSetEncoder(typeToken, name));
            case BEGIN_COMPOSITE -> fieldEncoderValues.add(generateCompositeEncoder(typeToken, name));
            default -> {
            }
          }
        });
    return fieldEncoderValues;
  }

  private static FieldEncoder generateEncoderFields_primitives(Token typeToken, String name) {
    if (typeToken.arrayLength() > 1) {
      return generatePrimitiveEncoderJSONArray(typeToken, name);
    }
    if (typeToken.encoding().presence() == Encoding.Presence.CONSTANT) {
      return generatePrimitiveEncoderJSONConstant(typeToken, name);
    }
    return generatePrimitiveEncoderJSONBasic(typeToken, name);
  }

  static List<GroupEncoder> generateEncoderGroups(
      final List<Token> tokens, final GroupContainer parentElement) {
    List<GroupEncoder> groupEncoderValues = new ArrayList<>();
    for (int i = 0, size = tokens.size(); i < size; i++) {
      var e = new GroupEncoder();
      final Token groupToken = tokens.get(i);
      if (groupToken.signal() != Signal.BEGIN_GROUP) {
        throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
      }

      ++i;
      final int index = i;
      final int groupHeaderTokenCount = tokens.get(i).componentTokenCount();
      i += groupHeaderTokenCount;

      final List<Token> fields = new ArrayList<>();
      i = collectFields(tokens, i, fields);

      final List<Token> groups = new ArrayList<>();
      i = collectGroups(tokens, i, groups);

      final List<Token> varData = new ArrayList<>();
      i = collectVarData(tokens, i, varData);

      final String groupName = encoderName(formatStructName(groupToken.name()));
      final Token numInGroupToken = Generators.findFirst("numInGroup", tokens, index);
      final PrimitiveType numInGroupPrimitiveType = numInGroupToken.encoding().primitiveType();

      assert 4 == groupHeaderTokenCount;
      e.functionName = formatFunctionName(groupName);
      e.groupSizeType = rustTypeName(numInGroupPrimitiveType);
      e.groupTypeName = groupName;
      e.groupName = toLowerSnakeCase(groupName);

      final GroupGenerator groupGenerator = parentElement.addInnerGroup(groupName, groupToken);
      groupGenerator.generateEncoder(tokens, fields, groups, varData, index);

      groupEncoderValues.add(e);
    }
    return groupEncoderValues;
  }

  static List<VarDataEncoder> generateEncoderVarData(final List<Token> tokens) {
    List<VarDataEncoder> varDataEncoderValues = new ArrayList<>();
    for (int i = 0, size = tokens.size(); i < size; ) {
      var varDataPojo = new VarDataEncoder();
      final Token varDataToken = tokens.get(i);
      if (varDataToken.signal() != Signal.BEGIN_VAR_DATA) {
        throw new IllegalStateException(
            "tokens must begin with BEGIN_VAR_DATA: token=" + varDataToken);
      }

      final String characterEncoding = characterEncoding(tokens.get(i + 3).encoding());
      final Token lengthToken = tokens.get(i + 2);
      final PrimitiveType lengthType = lengthToken.encoding().primitiveType();

      if (JavaUtil.isUtf8Encoding(characterEncoding)) {
        varDataPojo.varDataType = "&str";
        varDataPojo.toBytesFn = ".as_bytes()";
      } else {
        varDataPojo.varDataType = "&[u8]";
        varDataPojo.toBytesFn = "";
      }
      varDataPojo.characterEncoding = characterEncoding;
      varDataPojo.propertyName = toLowerSnakeCase(varDataToken.name());
      varDataPojo.lengthTypeSize = lengthType.size();
      varDataPojo.lengthType = rustTypeName(lengthType);
      i += varDataToken.componentTokenCount();
      varDataEncoderValues.add(varDataPojo);
    }
    return varDataEncoderValues;
  }

  private static FieldEncoder generatePrimitiveEncoderJSONArray(
      final Token typeToken, final String name) {
    final Encoding encoding = typeToken.encoding();
    final PrimitiveType primitiveType = encoding.primitiveType();
    final String rustPrimitiveType = rustTypeName(primitiveType);
    final int arrayLength = typeToken.arrayLength();
    assert arrayLength > 1;
    var e = new PrimitiveEncoderArray();
    e.name = name;
    e.applicableMinValue = encoding.applicableMinValue().toString();
    e.applicableMaxValue = encoding.applicableMaxValue().toString();
    e.applicableNullValue = encoding.applicableNullValue().toString();
    e.characterEncoding = encoding.characterEncoding();
    e.semanticType = encoding.semanticType();
    e.offset = typeToken.offset();
    e.encodedLength = typeToken.encodedLength();
    e.version = typeToken.version();
    e.functionName = formatFunctionName(name);
    e.rustPrimitiveType = rustPrimitiveType;
    e.arrayLength = arrayLength;
    e.arrayItems = new ArrayList<>();
    for (int i = 0; i < arrayLength; i++) {
      var item = new ArrayItems();
      item.rustPrimitiveType = rustPrimitiveType;
      item.itemOffset = i * primitiveType.size();
      item.itemIndex = i;
      e.arrayItems.add(item);
    }
    return new FieldEncoder(e);
  }

  private static FieldEncoder generatePrimitiveEncoderJSONConstant(
      final Token typeToken, final String name) {
    assert typeToken.encoding().presence() == Encoding.Presence.CONSTANT;
    var e = new PrimitiveEncoderConstant();
    e.name = name;
    return new FieldEncoder(e);
  }

  private static FieldEncoder generatePrimitiveEncoderJSONBasic(
      final Token typeToken, final String name) {
    final Encoding encoding = typeToken.encoding();
    final String rustPrimitiveType = rustTypeName(encoding.primitiveType());
    var e = new PrimitiveEncoderBasic();
    e.name = name;
    e.applicableMinValue = encoding.applicableMinValue().toString();
    e.applicableMaxValue = encoding.applicableMaxValue().toString();
    e.applicableNullValue = encoding.applicableNullValue().toString();
    e.characterEncoding = encoding.characterEncoding();
    e.semanticType = encoding.semanticType();
    e.offset = typeToken.offset();
    e.encodedLength = typeToken.encodedLength();
    e.functionName = formatFunctionName(name);
    e.rustPrimitiveType = rustPrimitiveType;
    return new FieldEncoder(e);
  }

  private static FieldEncoder generateEnumEncoder(
      final Token fieldToken, final Token typeToken, final String name) {
    if (fieldToken.isConstantEncoding()) {
      var e = new EnumEncoderConstant();
      e.name = name;
      return new FieldEncoder(e);
    }
    var e = new EnumEncoderBasic();
    e.rustPrimitiveType = rustTypeName(typeToken.encoding().primitiveType());
    e.functionName = formatFunctionName(name);
    e.enumType = formatStructName(typeToken.applicableTypeName());
    e.offset = typeToken.offset();
    return new FieldEncoder(e);
  }

  private static FieldEncoder generateBitSetEncoder(final Token bitsetToken, final String name)
  {
    var e = new BitSetEncoder();
    e.functionName = formatFunctionName(name);
    e.structTypeName = formatStructName(bitsetToken.applicableTypeName());
    e.offset = bitsetToken.offset();
    e.rustPrimitiveType = rustTypeName(bitsetToken.encoding().primitiveType());
    return new FieldEncoder(e);
  }

  private static FieldEncoder generateCompositeEncoder(
      final Token typeToken, final String name) {
    var e = new CompositeEncoder();
    e.encoderFunctionName = toLowerSnakeCase(encoderName(name));
    e.encoderTypeName = encoderName(formatStructName(typeToken.name()));
    e.offset = typeToken.offset();
    return new FieldEncoder(e);
  }

  static List<FieldDecoder> generateDecoderFields(final List<Token> tokens) {
    List<FieldDecoder> fieldDecoders = new ArrayList<>();
    Generators.forEachField(tokens, (fieldToken, typeToken) -> {
      final String name = fieldToken.name();
      final Encoding encoding = typeToken.encoding();

      switch (typeToken.signal()) {
        case ENCODING -> fieldDecoders.add(generatePrimitiveDecoder(typeToken, fieldToken, name, encoding));
        case BEGIN_ENUM -> fieldDecoders.add(generateEnumDecoder(fieldToken, typeToken, name));
        case BEGIN_SET -> fieldDecoders.add(generateBitSetDecoder(typeToken, name));
        case BEGIN_COMPOSITE -> fieldDecoders.add(generateCompositeDecoder(fieldToken, typeToken, name));
        default -> throw new UnsupportedOperationException("Unable to handle: " + typeToken);
      }
    });
    return fieldDecoders;
  }

  private static FieldDecoder generateCompositeDecoder(
      final Token fieldToken,
      final Token typeToken,
      final String name)
      {
    var e = new CompositeDecoder();
    e.decoderName = toLowerSnakeCase(decoderName(name));
    e.decoderTypeName = decoderName(formatStructName(typeToken.applicableTypeName()));
    e.versionGreaterThanZero = fieldToken.version() > 0;
    e.version = fieldToken.version();
    e.offset = fieldToken.offset();
    return new FieldDecoder(e);
  }

  private static FieldDecoder generateBitSetDecoder(final Token bitsetToken, final String name)
      {
    var e = new BitSetDecoder();
    e.functionName = formatFunctionName(name);
    e.structTypeName = formatStructName(bitsetToken.applicableTypeName());
    e.versionGreaterThanZero = bitsetToken.version() > 0;
    e.version = bitsetToken.version();
    e.rustPrimitiveType = rustTypeName(bitsetToken.encoding().primitiveType());
    e.offset = bitsetToken.offset();
    return new FieldDecoder(e);
  }

  private static FieldDecoder generatePrimitiveArrayDecoderJson(
      final Token fieldToken, final Token typeToken, final String name) {
    var e = new PrimitiveDecoderArray();
    Encoding encoding = typeToken.encoding();
    final PrimitiveType primitiveType = encoding.primitiveType();
    final String rustPrimitiveType = rustTypeName(primitiveType);

    final int arrayLength = typeToken.arrayLength();
    assert arrayLength > 1;

    e.functionName = formatFunctionName(name);
    e.rustPrimitiveType = rustPrimitiveType;
    e.arrayLength = arrayLength;

    e.versionGreaterThanZero = fieldToken.version() > 0;
    e.version = fieldToken.version();
    e.applicableNullValue = encoding.applicableNullValue().toString();

    List<PrimitiveDecoderArray.ArrayItems> arrayItems = new ArrayList<>();
    for (int i = 0; i < arrayLength; i++) {
      var arrayItem = new PrimitiveDecoderArray.ArrayItems();
      arrayItem.rustPrimitiveType = rustPrimitiveType;
      arrayItem.itemOffset = i * primitiveType.size();
      arrayItem.baseOffset = typeToken.offset();
      arrayItems.add(arrayItem);
    }
    e.arrayItems = arrayItems;
    return new FieldDecoder(e);
  }

  private static FieldDecoder generatePrimitiveConstantDecoderJson(
      final String name, final Encoding encoding) {
    var e = new PrimitiveDecoderConstant();
    assert encoding.presence() == Encoding.Presence.CONSTANT;
    final String rustPrimitiveType = rustTypeName(encoding.primitiveType());
    final String characterEncoding = encoding.characterEncoding();
    final String rawConstValue = encoding.constValue().toString();
    e.characterEncoding = characterEncoding;
    if (characterEncoding == null) {
      e.returnValue = rustPrimitiveType;
      e.rawConstValue = rawConstValue;
    } else if (JavaUtil.isAsciiEncoding(characterEncoding)) {
      e.returnValue = "&'static [u8]";
      e.rawConstValue = "b\"" + rawConstValue + "\"";
    } else if (JavaUtil.isUtf8Encoding(characterEncoding)) {
      e.returnValue = "&'static str";
      e.rawConstValue = rawConstValue;
    } else {
      throw new IllegalArgumentException("Unsupported encoding: " + characterEncoding);
    }
    e.functionName = formatFunctionName(name);

    return new FieldDecoder(e);
  }

  private static FieldDecoder generatePrimitiveOptionalDecoderJson(
      final Token fieldToken,
      final String name,
      final Encoding encoding) {
    var e = new PrimitiveDecoderOptional();
    assert encoding.presence() == Encoding.Presence.OPTIONAL;
    final PrimitiveType primitiveType = encoding.primitiveType();
    final String characterEncoding = encoding.characterEncoding();
    e.applicableNullValue = encoding.applicableNullValue().toString();
    if (characterEncoding != null) {
      e.characterEncoding = characterEncoding;
    }
    e.functionName = formatFunctionName(name);
    e.rustPrimitiveType = rustTypeName(primitiveType);
    e.versionGreaterThanZero = fieldToken.version() > 0;
    e.version = fieldToken.version();
    e.offset = fieldToken.offset();

    final String literal =
        generateRustLiteral(primitiveType, encoding.applicableNullValue().toString());
    if (literal.endsWith("::NAN")) {
      e.isNAN = true;
    } else {
      e.literal = literal;
    }
    return new FieldDecoder(e);
  }

  private static FieldDecoder generatePrimitiveRequiredDecoderJson(
      final Token fieldToken,
      final String name,
      final Encoding encoding) {
    assert encoding.presence() == Encoding.Presence.REQUIRED;
    var e = new PrimitiveDecoderRequired();
    e.characterEncoding = encoding.characterEncoding();
    e.functionName = formatFunctionName(name);
    e.rustPrimitiveType = rustTypeName(encoding.primitiveType());
    e.versionGreaterThanZero = fieldToken.version() > 0;
    e.version = fieldToken.version();
    e.rustLiteral = generateRustLiteral(encoding.primitiveType(), encoding.applicableNullValue().toString());
    e.offset = fieldToken.offset();
    return new FieldDecoder(e);
  }

  private static FieldDecoder generateEnumDecoder(
      final Token fieldToken,
      final Token typeToken,
      final String name)
       {
    final String enumType = formatStructName(typeToken.applicableTypeName());

    if (fieldToken.isConstantEncoding()) {
      var enumDecoderConstant = new EnumDecoderConstant();
      final String rawConstValueName = fieldToken.encoding().constValue().toString();
      final int indexOfDot = rawConstValueName.indexOf('.');

      enumDecoderConstant.enumType = enumType;
      enumDecoderConstant.constValueName =
          -1 == indexOfDot ? rawConstValueName : rawConstValueName.substring(indexOfDot + 1);
      enumDecoderConstant.functionName = formatFunctionName(name);
      return new FieldDecoder(enumDecoderConstant);
    } else {
      var enumDecoderBasic = new EnumDecoderBasic();
      enumDecoderBasic.functionName = formatFunctionName(name);
      enumDecoderBasic.versionGreaterThanZero = fieldToken.version() > 0;
      enumDecoderBasic.version = fieldToken.version();
      enumDecoderBasic.enumType = enumType;
      enumDecoderBasic.rustPrimitiveType = rustTypeName(typeToken.encoding().primitiveType());
      enumDecoderBasic.offset = typeToken.offset();
      return new FieldDecoder(enumDecoderBasic);
    }
  }

  static List<GroupDecoder> generateDecoderGroups(
      final List<Token> tokens, final GroupContainer parentElement)
      {
    List<GroupDecoder> groupDecoders = new ArrayList<>();
    for (int i = 0, size = tokens.size(); i < size; i++) {
      final Token groupToken = tokens.get(i);
      if (groupToken.signal() != Signal.BEGIN_GROUP) {
        throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
      }

      ++i;
      final int index = i;
      final int groupHeaderTokenCount = tokens.get(i).componentTokenCount();
      i += groupHeaderTokenCount;

      final List<Token> fields = new ArrayList<>();
      i = collectFields(tokens, i, fields);

      final List<Token> groups = new ArrayList<>();
      i = collectGroups(tokens, i, groups);

      final List<Token> varData = new ArrayList<>();
      i = collectVarData(tokens, i, varData);

      final String groupName = decoderName(formatStructName(groupToken.name()));
      assert 4 == groupHeaderTokenCount;

      final GroupGenerator groupGenerator = parentElement.addInnerGroup(groupName, groupToken);
      groupGenerator.generateDecoder(tokens, fields, groups, varData, index);

      var groupDecoder = new GroupDecoder();
      groupDecoder.versionGreaterThanZero = groupToken.version() > 0;
      groupDecoder.functionName = formatFunctionName(groupName);
      groupDecoder.groupName = groupName;
      groupDecoder.version = groupToken.version();
      groupDecoders.add(groupDecoder);
    }
    return groupDecoders;
  }

  static List<VarDataDecoder> generateDecoderVarData(
      final List<Token> tokens, final boolean isNestedGroup) {
    List<VarDataDecoder> varDataDecoders = new ArrayList<>();
    for (int i = 0, size = tokens.size(); i < size; ) {
      var varDataPojo = new VarDataDecoder();
      varDataPojo.isNestedGroup = isNestedGroup;
      final Token varDataToken = tokens.get(i);
      if (varDataToken.signal() != Signal.BEGIN_VAR_DATA) {
        throw new IllegalStateException(
            "tokens must begin with BEGIN_VAR_DATA: token=" + varDataToken);
      }
      final Token lengthToken = tokens.get(i + 2);
      final PrimitiveType lengthType = lengthToken.encoding().primitiveType();

      varDataPojo.characterEncoding = characterEncoding(tokens.get(i + 3).encoding());
      varDataPojo.propertyName = toLowerSnakeCase(varDataToken.name());
      if (varDataToken.version() > 0) {
          varDataPojo.versionGreaterThanZero = true;
          varDataPojo.version = varDataToken.version();
      }
      varDataPojo.lengthType = rustTypeName(lengthType);
      varDataPojo.lengthTypeSize = lengthType.size();

      i += varDataToken.componentTokenCount();
      varDataDecoders.add(varDataPojo);
    }
    return varDataDecoders;
  }

  private static List<BitSet> generateBitSets(final Ir ir) {
    return ir.types().stream()
        .filter(tokens -> !tokens.isEmpty() && tokens.get(0).signal() == BEGIN_SET)
        .map(RustGenerator::generateSingleBitSetJson)
        .toList();
  }

  private static BitSet generateSingleBitSetJson(final List<Token> tokens) {
    final Token beginToken = tokens.get(0);
    final String bitSetType = formatStructName(beginToken.applicableTypeName());

    var bitSetPojo = new BitSet();
    bitSetPojo.filename = bitSetType;
    bitSetPojo.bitSetType = bitSetType;
    bitSetPojo.rustPrimitiveType = rustTypeName(beginToken.encoding().primitiveType());
    bitSetPojo.choices = new ArrayList<>();
    for (final Token token : tokens) {
      if (Signal.CHOICE != token.signal()) continue;
      var choice = new BitSet.Choice();
      choice.choiceName = formatFunctionName(token.name());
      choice.choiceBitIndex = token.encoding().constValue().toString();
      bitSetPojo.choices.add(choice);
    }
    bitSetPojo.choices.get(bitSetPojo.choices.size() - 1).isLast = true;
    return bitSetPojo;
  }

  private static List<Enum> generateEnums(final Ir ir) {
    Map<String, List<Token>> tokensByTypename =
        ir.types().stream()
            .filter(tokens -> !tokens.isEmpty() && tokens.get(0).signal() == BEGIN_ENUM)
            .collect(toMap(tokens -> tokens.get(0).applicableTypeName(), Function.identity()));
    return tokensByTypename.values().stream().map(RustGenerator::generateEnum).toList();
  }

  private static Enum generateEnum(final List<Token> enumTokens) {
    final String originalEnumName = enumTokens.get(0).applicableTypeName();
    final List<Token> messageBody = enumTokens.subList(1, enumTokens.size() - 1);
    if (messageBody.isEmpty()) {
      throw new IllegalArgumentException("No valid values provided for enum " + originalEnumName);
    }

    var enumPojo = new Enum();
    enumPojo.filename = originalEnumName;
    enumPojo.primitiveType = rustTypeName(messageBody.get(0).encoding().primitiveType());
    enumPojo.enumRustName = formatStructName(originalEnumName);
    enumPojo.enumItems = new ArrayList<>();

    for (final Token token : messageBody) {
      System.out.println("[*] processing enum: " + originalEnumName);
      final Encoding encoding = token.encoding();
      final String literal =
          generateRustLiteral(encoding.primitiveType(), encoding.constValue().toString());
      var enumItem = new EnumItem();
      enumItem.name = token.name();
      enumItem.literal = literal;
      enumPojo.enumItems.add(enumItem);
    }
    final Encoding encoding = messageBody.get(0).encoding();
    final String nullVal =
        generateRustLiteral(encoding.primitiveType(), encoding.applicableNullValue().toString());
    var enumItem = new EnumItem();
    enumItem.name = "NullVal";
    enumItem.literal = nullVal;
    enumPojo.enumItems.add(enumItem);

    enumPojo.enumFromItems = new ArrayList<>();
    for (final Token token : messageBody) {
      enumItem = new EnumItem();
      enumItem.literal =
          generateRustLiteral(
              token.encoding().primitiveType(), token.encoding().constValue().toString());
      enumItem.name = token.name();
      enumPojo.enumFromItems.add(enumItem);
    }
    var enumFromItemNull = new EnumItem();
    enumFromItemNull.literal = "_";
    enumFromItemNull.name = "NullVal";
    enumPojo.enumFromItems.add(enumFromItemNull);

    return enumPojo;
  }

  private static List<Composite> generateComposites(final Ir ir) {
    return ir.types().stream()
        .filter(tokens -> !tokens.isEmpty() && tokens.get(0).signal() == Signal.BEGIN_COMPOSITE)
        .map(RustGenerator::generateComposite)
        .toList();
  }

  private static Composite generateComposite(final List<Token> tokens) {
    var compositePojo = new Composite();
    final String compositeName = tokens.get(0).applicableTypeName();

    compositePojo.filename = codecModName(compositeName);
    compositePojo.encodedLength = tokens.get(0).encodedLength();
    compositePojo.encodedLengthGreaterThanZero = compositePojo.encodedLength > 0;
    compositePojo.encoderName = encoderName(compositeName);
    compositePojo.fieldEncoders = generateCompositeFieldsEncoder(tokens);
    compositePojo.decoderName = decoderName(compositeName);
    compositePojo.fieldDecoders = generateCompositeFieldsDecoder(tokens);
    return compositePojo;
  }

  private static List<FieldEncoder> generateCompositeFieldsEncoder(final List<Token> tokens) {
    List<FieldEncoder> fieldEncoderValues = new ArrayList<>();
    for (int i = 1, end = tokens.size() - 1; i < end; ) {
      final Token encodingToken = tokens.get(i);

      var name = encodingToken.name();
      switch (encodingToken.signal()) {
        case ENCODING -> fieldEncoderValues.add(generatePrimitiveEncoder(encodingToken, name));
        case BEGIN_ENUM -> fieldEncoderValues.add(generateEnumEncoder(encodingToken, encodingToken, name));
        case BEGIN_SET -> fieldEncoderValues.add(generateBitSetEncoder(encodingToken, name));
        case BEGIN_COMPOSITE -> fieldEncoderValues.add(generateCompositeEncoder(encodingToken, name));
        default -> {
        }
      }
      i += encodingToken.componentTokenCount();
    }
    return fieldEncoderValues;
  }

  private static FieldEncoder generatePrimitiveEncoder(Token typeToken, String name) {
    if (typeToken.arrayLength() > 1) {
      return generatePrimitiveEncoderJSONArray(typeToken, name);
    }
    if (typeToken.encoding().presence() == Encoding.Presence.CONSTANT) {
      return generatePrimitiveEncoderJSONConstant(typeToken, name);
    }
    return generatePrimitiveEncoderJSONBasic(typeToken, name);
  }

  private static List<FieldDecoder> generateCompositeFieldsDecoder(final List<Token> tokens) {
    List<FieldDecoder> fieldDecoderValues = new ArrayList<>();
    for (int i = 1, end = tokens.size() - 1; i < end; ) {
      final Token encodingToken = tokens.get(i);

      var name = encodingToken.name();
      var encoding = encodingToken.encoding();
      switch (encodingToken.signal()) {
        case ENCODING -> fieldDecoderValues.add(
            generatePrimitiveDecoder(encodingToken, encodingToken, name, encoding));
        case BEGIN_ENUM -> fieldDecoderValues.add(generateEnumDecoder(encodingToken, encodingToken, name));
        case BEGIN_SET -> fieldDecoderValues.add(generateBitSetDecoder(encodingToken, name));
        case BEGIN_COMPOSITE ->
            fieldDecoderValues.add(generateCompositeDecoder(encodingToken, encodingToken, name));
        default -> {
        }
      }
      i += encodingToken.componentTokenCount();
    }
    return fieldDecoderValues;
  }

  private static FieldDecoder generatePrimitiveDecoder(Token typeToken,
      Token fieldToken, String name, Encoding encoding) {
    if (typeToken.arrayLength() > 1) {
      return generatePrimitiveArrayDecoderJson(fieldToken, typeToken, name);
    }
    if (encoding.presence() == Encoding.Presence.CONSTANT) {
      return generatePrimitiveConstantDecoderJson(name, encoding);
    }
    if (encoding.presence() == Encoding.Presence.OPTIONAL) {
      return generatePrimitiveOptionalDecoderJson(fieldToken, name, encoding);
    }
    return generatePrimitiveRequiredDecoderJson(fieldToken, name, encoding);
  }

  @Override
  public void generate() throws IOException {
    SpecMustacheFactory mf = new SpecMustacheFactory();
    mf.compilePartial("encoder-fields.mustache");
    mf.compilePartial("decoder-fields.mustache");
    mf.compilePartial("encoder-group.mustache");
    mf.compilePartial("decoder-group.mustache");

    var cargoTomlPojo = new CargoToml();
    final String packageName = toLowerSnakeCase(ir.packageName()).replaceAll("[.-]", "_");
    cargoTomlPojo.namespace =
        (ir.namespaceName() == null || ir.namespaceName().equalsIgnoreCase(packageName))
            ? packageName.toLowerCase()
            : (ir.namespaceName() + "_" + packageName).toLowerCase();
    cargoTomlPojo.description = ir.description();
    Writer cargoFileWriter = outputManager.createCargoToml();
    Mustache cargoTomlTemplate = mf.compile("cargo-toml-template.mustache");
    cargoTomlTemplate.execute(cargoFileWriter, cargoTomlPojo).flush();

    List<Enum> enumPojos = generateEnums(ir);
    Mustache enumTemplate = mf.compile("enum-template.mustache");
    for (final var enumPojo : enumPojos) {
      Writer fileWriter = outputManager.createOutput(enumPojo.filename);
      enumTemplate.execute(fileWriter, enumPojo).flush();
    }

    List<BitSet> bitSetPojos = generateBitSets(ir);
    Mustache bitSetTemplate = mf.compile("bitset-template.mustache");
    for (final var bitSetPojo : bitSetPojos) {
      Writer fileWriter = outputManager.createOutput(bitSetPojo.filename);
      bitSetTemplate.execute(fileWriter, bitSetPojo).flush();
    }

    List<Composite> compositePojos = generateComposites(ir);
    Mustache compositeTemplate = mf.compile("composite-template.mustache");
    for (final var compositePojo : compositePojos) {
      Writer fileWriter = outputManager.createOutput(compositePojo.filename);
      compositeTemplate.execute(fileWriter, compositePojo).flush();
    }

    List<Message> messagePojos = new ArrayList<>();
    for (final List<Token> tokens : ir.messages()) {
      var messagePojo = new Message();
      final Token msgToken = tokens.get(0);
      final String codecModName = codecModName(msgToken.name());
      final List<Token> messageBody = tokens.subList(1, tokens.size() - 1);

      int i = 0;
      final List<Token> fields = new ArrayList<>();
      i = collectFields(messageBody, i, fields);
      final List<Token> groups = new ArrayList<>();
      i = collectGroups(messageBody, i, groups);
      final List<Token> varData = new ArrayList<>();
      collectVarData(messageBody, i, varData);

      messagePojo.filename = codecModName;
      messagePojo.blockLengthType = blockLengthType();
      messagePojo.blockLength = msgToken.encodedLength();
      messagePojo.templateIdType = rustTypeName(ir.headerStructure().templateIdType());
      messagePojo.templateId = msgToken.id();
      messagePojo.schemaIdType = rustTypeName(ir.headerStructure().schemaIdType());
      messagePojo.schemaId = ir.id();
      messagePojo.schemaVersionType = schemaVersionType();
      messagePojo.schemaVersion = ir.version();

      messagePojo.encoder = MessageCodecGenerator.generateEncoder(ir, msgToken, fields, groups, varData);
      messagePojo.decoder = MessageCodecGenerator.generateDecoder(ir, msgToken, fields, groups, varData);

      messagePojos.add(messagePojo);
    }
     var mapper = new ObjectMapper();
     mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
     mapper.setSerializationInclusion(Include.NON_NULL);
     for (final var pojo : messagePojos) {
       if (pojo.filename.equals("mass_quote_codec")) {
//       if (pojo.filename.equals("market_data_incremental_refresh_codec")) {
         mapper.writeValue(System.out, pojo);
         break;
       }
     }

    Mustache messageTemplate = mf.compile("message-template.mustache");
    for (final var messagePojo : messagePojos) {
      Writer fileWriter = outputManager.createOutput(messagePojo.filename);
      messageTemplate.execute(fileWriter, messagePojo).flush();
    }

    // lib.rs
    LibRs libRsPojo = LibRsGenerator.generate(outputManager);
    Mustache libRsTemplate = mf.compile("lib-rs-template.mustache");
    Writer fileWriter = outputManager.createOutput(libRsPojo.filename);
    libRsTemplate.execute(fileWriter, libRsPojo).flush();
  }

  String blockLengthType() {
    return rustTypeName(ir.headerStructure().blockLengthType());
  }

  String schemaVersionType() {
    return rustTypeName(ir.headerStructure().schemaVersionType());
  }

  enum CodecType {
    Decoder {
      String bufType() {
        return READ_BUF_TYPE;
      }
    },

    Encoder {
      String bufType() {
        return WRITE_BUF_TYPE;
      }
    };

    abstract String bufType();
  }

  /**
   * Generator classes for SBE types that can contain inner groups (i.e. message and group) must
   * implement this interface. Code for such inner groups need to be generated after the other
   * fields in the main SBE type is generated, so we use the `addInnerGroup` method on this
   * interface to add the inner groups first, and generate later.
   */
  interface GroupContainer {
    GroupGenerator addInnerGroup(String name, Token groupToken);
  }
}
