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
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder.EnumDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder.ConstantEnumDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder.ArrayPrimitiveDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder.ConstantPrimitiveDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder.OptionalPrimitiveDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder.RequiredPrimitiveDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.GroupEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.VarDataEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.BitSetEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.CompositeEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.EnumEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.ConstantEnumEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.ArrayPrimitiveEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.ArrayPrimitiveEncoder.ArrayItems;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.PrimitiveEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.ConstantPrimitiveEncoder;
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
    List<FieldEncoder> fieldEncoders = new ArrayList<>();
    Generators.forEachField(
        tokens,
        (fieldToken, typeToken) -> {
          final String name = fieldToken.name();
          switch (typeToken.signal()) {
            case ENCODING -> fieldEncoders.add(generateEncoderFields_primitives(typeToken, name));
            case BEGIN_ENUM -> fieldEncoders.add(generateEnumEncoder(fieldToken, typeToken, name));
            case BEGIN_SET -> fieldEncoders.add(generateBitSetEncoder(typeToken, name));
            case BEGIN_COMPOSITE -> fieldEncoders.add(generateCompositeEncoder(typeToken, name));
            default -> {
            }
          }
        });
    return fieldEncoders;
  }

  private static FieldEncoder generateEncoderFields_primitives(Token typeToken, String name) {
    if (typeToken.arrayLength() > 1) {
      return generateArrayPrimitiveEncoder(typeToken, name);
    }
    if (typeToken.encoding().presence() == Encoding.Presence.CONSTANT) {
      return generateConstantPrimitiveEncoder(typeToken, name);
    }
    return generatePrimitiveEncoder(typeToken, name);
  }

  static List<GroupEncoder> generateEncoderGroups(
      final List<Token> tokens, final GroupContainer parentElement) {
    List<GroupEncoder> groupEncoders = new ArrayList<>();
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

      groupEncoders.add(e);
    }
    return groupEncoders;
  }

  static List<VarDataEncoder> generateEncoderVarData(final List<Token> tokens) {
    List<VarDataEncoder> varDataEncoders = new ArrayList<>();
    for (int i = 0, size = tokens.size(); i < size; ) {
      var varDataEncoder = new VarDataEncoder();
      final Token varDataToken = tokens.get(i);
      if (varDataToken.signal() != Signal.BEGIN_VAR_DATA) {
        throw new IllegalStateException(
            "tokens must begin with BEGIN_VAR_DATA: token=" + varDataToken);
      }

      final String characterEncoding = characterEncoding(tokens.get(i + 3).encoding());
      final Token lengthToken = tokens.get(i + 2);
      final PrimitiveType lengthType = lengthToken.encoding().primitiveType();

      if (JavaUtil.isUtf8Encoding(characterEncoding)) {
        varDataEncoder.varDataType = "&str";
        varDataEncoder.toBytesFn = ".as_bytes()";
      } else {
        varDataEncoder.varDataType = "&[u8]";
        varDataEncoder.toBytesFn = "";
      }
      varDataEncoder.characterEncoding = characterEncoding;
      varDataEncoder.propertyName = toLowerSnakeCase(varDataToken.name());
      varDataEncoder.lengthTypeSize = lengthType.size();
      varDataEncoder.lengthType = rustTypeName(lengthType);
      i += varDataToken.componentTokenCount();
      varDataEncoders.add(varDataEncoder);
    }
    return varDataEncoders;
  }

  private static FieldEncoder generateArrayPrimitiveEncoder(
      final Token typeToken, final String name) {
    final Encoding encoding = typeToken.encoding();
    final PrimitiveType primitiveType = encoding.primitiveType();
    final String rustPrimitiveType = rustTypeName(primitiveType);
    final int arrayLength = typeToken.arrayLength();
    assert arrayLength > 1;
    var encoder = new ArrayPrimitiveEncoder();
    encoder.fieldName = name;
    encoder.applicableMinValue = encoding.applicableMinValue().toString();
    encoder.applicableMaxValue = encoding.applicableMaxValue().toString();
    encoder.applicableNullValue = encoding.applicableNullValue().toString();
    encoder.characterEncoding = encoding.characterEncoding();
    encoder.semanticType = encoding.semanticType();
    encoder.offset = typeToken.offset();
    encoder.encodedLength = typeToken.encodedLength();
    encoder.version = typeToken.version();
    encoder.functionName = formatFunctionName(name);
    encoder.rustPrimitiveType = rustPrimitiveType;
    encoder.arrayLength = arrayLength;
    encoder.arrayItems = new ArrayList<>();
    for (int i = 0; i < arrayLength; i++) {
      var item = new ArrayItems();
      item.rustPrimitiveType = rustPrimitiveType;
      item.itemOffset = i * primitiveType.size();
      item.itemIndex = i;
      encoder.arrayItems.add(item);
    }
    return new FieldEncoder(encoder);
  }

  private static FieldEncoder generateConstantPrimitiveEncoder(
      final Token typeToken, final String name) {
    assert typeToken.encoding().presence() == Encoding.Presence.CONSTANT;
    var encoder = new ConstantPrimitiveEncoder();
    encoder.fieldName = name;
    return new FieldEncoder(encoder);
  }

  private static FieldEncoder generatePrimitiveEncoder(
      final Token typeToken, final String name) {
    final Encoding encoding = typeToken.encoding();
    final String rustPrimitiveType = rustTypeName(encoding.primitiveType());
    var encoder = new PrimitiveEncoder();
    encoder.fieldName = name;
    encoder.applicableMinValue = encoding.applicableMinValue().toString();
    encoder.applicableMaxValue = encoding.applicableMaxValue().toString();
    encoder.applicableNullValue = encoding.applicableNullValue().toString();
    encoder.characterEncoding = encoding.characterEncoding();
    encoder.semanticType = encoding.semanticType();
    encoder.offset = typeToken.offset();
    encoder.encodedLength = typeToken.encodedLength();
    encoder.functionName = formatFunctionName(name);
    encoder.rustPrimitiveType = rustPrimitiveType;
    return new FieldEncoder(encoder);
  }

  private static FieldEncoder generateEnumEncoder(
      final Token fieldToken, final Token typeToken, final String name) {
    if (fieldToken.isConstantEncoding()) {
      var encoder = new ConstantEnumEncoder();
      encoder.fieldName = name;
      return new FieldEncoder(encoder);
    }
    var encoder = new EnumEncoder();
    encoder.rustPrimitiveType = rustTypeName(typeToken.encoding().primitiveType());
    encoder.functionName = formatFunctionName(name);
    encoder.enumTypeName = formatStructName(typeToken.applicableTypeName());
    encoder.offset = typeToken.offset();
    return new FieldEncoder(encoder);
  }

  private static FieldEncoder generateBitSetEncoder(final Token bitsetToken, final String name)
  {
    var encoder = new BitSetEncoder();
    encoder.functionName = formatFunctionName(name);
    encoder.bitSetTypeName = formatStructName(bitsetToken.applicableTypeName());
    encoder.offset = bitsetToken.offset();
    encoder.rustPrimitiveType = rustTypeName(bitsetToken.encoding().primitiveType());
    return new FieldEncoder(encoder);
  }

  private static FieldEncoder generateCompositeEncoder(
      final Token typeToken, final String name) {
    var encoder = new CompositeEncoder();
    encoder.encoderFunctionName = toLowerSnakeCase(encoderName(name));
    encoder.encoderTypeName = encoderName(formatStructName(typeToken.name()));
    encoder.offset = typeToken.offset();
    return new FieldEncoder(encoder);
  }

  static List<FieldDecoder> generateDecoderFields(final List<Token> tokens) {
    List<FieldDecoder> fieldDecoders = new ArrayList<>();
    Generators.forEachField(tokens, (fieldToken, typeToken) -> {
      final String name = fieldToken.name();
      final Encoding encoding = typeToken.encoding();

      switch (typeToken.signal()) {
        case ENCODING -> fieldDecoders.add(
            generateSpecificPrimitiveDecoder(typeToken, fieldToken, name, encoding));
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
    var decoder = new CompositeDecoder();
    decoder.functionName = toLowerSnakeCase(decoderName(name));
    decoder.decoderTypeName = decoderName(formatStructName(typeToken.applicableTypeName()));
    decoder.versionAboveZero = fieldToken.version() > 0;
    decoder.version = fieldToken.version();
    decoder.offset = fieldToken.offset();
    return new FieldDecoder(decoder);
  }

  private static FieldDecoder generateBitSetDecoder(final Token bitsetToken, final String name)
      {
    var decoder = new BitSetDecoder();
    decoder.functionName = formatFunctionName(name);
    decoder.bitSetTypeName = formatStructName(bitsetToken.applicableTypeName());
    decoder.versionAboveZero = bitsetToken.version() > 0;
    decoder.version = bitsetToken.version();
    decoder.rustPrimitiveType = rustTypeName(bitsetToken.encoding().primitiveType());
    decoder.offset = bitsetToken.offset();
    return new FieldDecoder(decoder);
  }

  private static FieldDecoder generateArrayPrimitiveDecoder(
      final Token fieldToken, final Token typeToken, final String name) {
    var decoder = new ArrayPrimitiveDecoder();
    Encoding encoding = typeToken.encoding();
    final PrimitiveType primitiveType = encoding.primitiveType();
    final String rustPrimitiveType = rustTypeName(primitiveType);

    final int arrayLength = typeToken.arrayLength();
    assert arrayLength > 1;

    decoder.functionName = formatFunctionName(name);
    decoder.rustPrimitiveType = rustPrimitiveType;
    decoder.arrayLength = arrayLength;

    decoder.versionAboveZero = fieldToken.version() > 0;
    decoder.version = fieldToken.version();
    decoder.applicableNullValue = encoding.applicableNullValue().toString();

    List<ArrayPrimitiveDecoder.ArrayItems> arrayItems = new ArrayList<>();
    for (int i = 0; i < arrayLength; i++) {
      var arrayItem = new ArrayPrimitiveDecoder.ArrayItems();
      arrayItem.rustPrimitiveType = rustPrimitiveType;
      arrayItem.itemOffset = i * primitiveType.size();
      arrayItem.baseOffset = typeToken.offset();
      arrayItems.add(arrayItem);
    }
    decoder.arrayItems = arrayItems;
    return new FieldDecoder(decoder);
  }

  private static FieldDecoder generateConstantPrimitiveDecoder(
      final String name, final Encoding encoding) {
    var decoder = new ConstantPrimitiveDecoder();
    assert encoding.presence() == Encoding.Presence.CONSTANT;
    final String rustPrimitiveType = rustTypeName(encoding.primitiveType());
    final String characterEncoding = encoding.characterEncoding();
    final String rawConstValue = encoding.constValue().toString();
    decoder.characterEncoding = characterEncoding;
    if (characterEncoding == null) {
      decoder.returnValue = rustPrimitiveType;
      decoder.rawConstValue = rawConstValue;
    } else if (JavaUtil.isAsciiEncoding(characterEncoding)) {
      decoder.returnValue = "&'static [u8]";
      decoder.rawConstValue = "b\"" + rawConstValue + "\"";
    } else if (JavaUtil.isUtf8Encoding(characterEncoding)) {
      decoder.returnValue = "&'static str";
      decoder.rawConstValue = rawConstValue;
    } else {
      throw new IllegalArgumentException("Unsupported encoding: " + characterEncoding);
    }
    decoder.functionName = formatFunctionName(name);

    return new FieldDecoder(decoder);
  }

  private static FieldDecoder generateOptionalPrimitiveDecoder(
      final Token fieldToken,
      final String name,
      final Encoding encoding) {
    var decoder = new OptionalPrimitiveDecoder();
    assert encoding.presence() == Encoding.Presence.OPTIONAL;
    final PrimitiveType primitiveType = encoding.primitiveType();
    final String characterEncoding = encoding.characterEncoding();
    decoder.applicableNullValue = encoding.applicableNullValue().toString();
    if (characterEncoding != null) {
      decoder.characterEncoding = characterEncoding;
    }
    decoder.functionName = formatFunctionName(name);
    decoder.rustPrimitiveType = rustTypeName(primitiveType);
    decoder.versionAboveZero = fieldToken.version() > 0;
    decoder.version = fieldToken.version();
    decoder.offset = fieldToken.offset();

    final String literal =
        generateRustLiteral(primitiveType, encoding.applicableNullValue().toString());
    if (literal.endsWith("::NAN")) {
      decoder.isNAN = true;
    } else {
      decoder.literal = literal;
    }
    return new FieldDecoder(decoder);
  }

  private static FieldDecoder generateRequiredPrimitiveDecoder(
      final Token fieldToken,
      final String name,
      final Encoding encoding) {
    assert encoding.presence() == Encoding.Presence.REQUIRED;
    var decoder = new RequiredPrimitiveDecoder();
    decoder.characterEncoding = encoding.characterEncoding();
    decoder.functionName = formatFunctionName(name);
    decoder.rustPrimitiveType = rustTypeName(encoding.primitiveType());
    decoder.versionAboveZero = fieldToken.version() > 0;
    decoder.version = fieldToken.version();
    decoder.rustLiteral = generateRustLiteral(encoding.primitiveType(), encoding.applicableNullValue().toString());
    decoder.offset = fieldToken.offset();
    return new FieldDecoder(decoder);
  }

  private static FieldDecoder generateEnumDecoder(
      final Token fieldToken,
      final Token typeToken,
      final String name)
       {
    final String enumType = formatStructName(typeToken.applicableTypeName());

    if (fieldToken.isConstantEncoding()) {
      var decoder = new ConstantEnumDecoder();
      final String rawConstValueName = fieldToken.encoding().constValue().toString();
      final int indexOfDot = rawConstValueName.indexOf('.');

      decoder.enumTypeName = enumType;
      decoder.constValueName =
          -1 == indexOfDot ? rawConstValueName : rawConstValueName.substring(indexOfDot + 1);
      decoder.functionName = formatFunctionName(name);
      return new FieldDecoder(decoder);
    } else {
      var decoder = new EnumDecoder();
      decoder.functionName = formatFunctionName(name);
      decoder.versionAboveZero = fieldToken.version() > 0;
      decoder.version = fieldToken.version();
      decoder.enumTypeName = enumType;
      decoder.rustPrimitiveType = rustTypeName(typeToken.encoding().primitiveType());
      decoder.offset = typeToken.offset();
      return new FieldDecoder(decoder);
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
      groupDecoder.versionAboveZero = groupToken.version() > 0;
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
      var varDataDecoder = new VarDataDecoder();
      varDataDecoder.isSubGroup = isNestedGroup;
      final Token varDataToken = tokens.get(i);
      if (varDataToken.signal() != Signal.BEGIN_VAR_DATA) {
        throw new IllegalStateException(
            "tokens must begin with BEGIN_VAR_DATA: token=" + varDataToken);
      }
      final Token lengthToken = tokens.get(i + 2);
      final PrimitiveType lengthType = lengthToken.encoding().primitiveType();

      varDataDecoder.characterEncoding = characterEncoding(tokens.get(i + 3).encoding());
      varDataDecoder.propertyName = toLowerSnakeCase(varDataToken.name());
      if (varDataToken.version() > 0) {
          varDataDecoder.versionAboveZero = true;
          varDataDecoder.version = varDataToken.version();
      }
      varDataDecoder.lengthType = rustTypeName(lengthType);
      varDataDecoder.lengthTypeSize = lengthType.size();

      i += varDataToken.componentTokenCount();
      varDataDecoders.add(varDataDecoder);
    }
    return varDataDecoders;
  }

  private static List<BitSet> generateBitSets(final Ir ir) {
    return ir.types().stream()
        .filter(tokens -> !tokens.isEmpty() && tokens.get(0).signal() == BEGIN_SET)
        .map(RustGenerator::generateBitSet)
        .toList();
  }

  private static BitSet generateBitSet(final List<Token> tokens) {
    final Token beginToken = tokens.get(0);
    final String bitSetType = formatStructName(beginToken.applicableTypeName());

    var bitSetStructDef = new BitSet();
    bitSetStructDef.filename = bitSetType;
    bitSetStructDef.bitSetType = bitSetType;
    bitSetStructDef.rustPrimitiveType = rustTypeName(beginToken.encoding().primitiveType());
    bitSetStructDef.choices = new ArrayList<>();
    for (final Token token : tokens) {
      if (Signal.CHOICE != token.signal()) continue;
      var choice = new BitSet.Choice();
      choice.choiceName = formatFunctionName(token.name());
      choice.choiceBitIndex = token.encoding().constValue().toString();
      bitSetStructDef.choices.add(choice);
    }
    bitSetStructDef.choices.get(bitSetStructDef.choices.size() - 1).isLast = true;
    return bitSetStructDef;
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

    var enumTypeDef = new Enum();
    enumTypeDef.filename = originalEnumName;
    enumTypeDef.primitiveType = rustTypeName(messageBody.get(0).encoding().primitiveType());
    enumTypeDef.enumRustName = formatStructName(originalEnumName);
    enumTypeDef.enumItems = new ArrayList<>();

    for (final Token token : messageBody) {
      final Encoding encoding = token.encoding();
      final String literal =
          generateRustLiteral(encoding.primitiveType(), encoding.constValue().toString());
      var enumItem = new EnumItem();
      enumItem.name = token.name();
      enumItem.literal = literal;
      enumTypeDef.enumItems.add(enumItem);
    }
    final Encoding encoding = messageBody.get(0).encoding();
    final String nullVal =
        generateRustLiteral(encoding.primitiveType(), encoding.applicableNullValue().toString());
    var enumItem = new EnumItem();
    enumItem.name = "NullVal";
    enumItem.literal = nullVal;
    enumTypeDef.enumItems.add(enumItem);

    enumTypeDef.enumFromItems = new ArrayList<>();
    for (final Token token : messageBody) {
      enumItem = new EnumItem();
      enumItem.literal =
          generateRustLiteral(
              token.encoding().primitiveType(), token.encoding().constValue().toString());
      enumItem.name = token.name();
      enumTypeDef.enumFromItems.add(enumItem);
    }
    var enumFromItemNull = new EnumItem();
    enumFromItemNull.literal = "_";
    enumFromItemNull.name = "NullVal";
    enumTypeDef.enumFromItems.add(enumFromItemNull);

    return enumTypeDef;
  }

  private static List<Composite> generateComposites(final Ir ir) {
    return ir.types().stream()
        .filter(tokens -> !tokens.isEmpty() && tokens.get(0).signal() == Signal.BEGIN_COMPOSITE)
        .map(RustGenerator::generateComposite)
        .toList();
  }

  private static Composite generateComposite(final List<Token> tokens) {
    var compositeEncoderDecoder = new Composite();
    final String compositeName = tokens.get(0).applicableTypeName();

    compositeEncoderDecoder.filename = codecModName(compositeName);
    compositeEncoderDecoder.encodedLength = tokens.get(0).encodedLength();
    compositeEncoderDecoder.encodedLengthGreaterThanZero = compositeEncoderDecoder.encodedLength > 0;
    compositeEncoderDecoder.encoderName = encoderName(compositeName);
    compositeEncoderDecoder.fieldEncoders = generateCompositeFieldsEncoder(tokens);
    compositeEncoderDecoder.decoderName = decoderName(compositeName);
    compositeEncoderDecoder.fieldDecoders = generateCompositeFieldsDecoder(tokens);
    return compositeEncoderDecoder;
  }

  private static List<FieldEncoder> generateCompositeFieldsEncoder(final List<Token> tokens) {
    List<FieldEncoder> fieldEncoders = new ArrayList<>();
    for (int i = 1, end = tokens.size() - 1; i < end; ) {
      final Token encodingToken = tokens.get(i);

      var name = encodingToken.name();
      switch (encodingToken.signal()) {
        case ENCODING -> fieldEncoders.add(generateSpecificPrimitiveEncoder(encodingToken, name));
        case BEGIN_ENUM -> fieldEncoders.add(generateEnumEncoder(encodingToken, encodingToken, name));
        case BEGIN_SET -> fieldEncoders.add(generateBitSetEncoder(encodingToken, name));
        case BEGIN_COMPOSITE -> fieldEncoders.add(generateCompositeEncoder(encodingToken, name));
        default -> {
        }
      }
      i += encodingToken.componentTokenCount();
    }
    return fieldEncoders;
  }

  private static FieldEncoder generateSpecificPrimitiveEncoder(Token typeToken, String name) {
    if (typeToken.arrayLength() > 1) {
      return generateArrayPrimitiveEncoder(typeToken, name);
    }
    if (typeToken.encoding().presence() == Encoding.Presence.CONSTANT) {
      return generateConstantPrimitiveEncoder(typeToken, name);
    }
    return generatePrimitiveEncoder(typeToken, name);
  }

  private static List<FieldDecoder> generateCompositeFieldsDecoder(final List<Token> tokens) {
    List<FieldDecoder> fieldDecoders = new ArrayList<>();
    for (int i = 1, end = tokens.size() - 1; i < end; ) {
      final Token encodingToken = tokens.get(i);

      var name = encodingToken.name();
      var encoding = encodingToken.encoding();
      switch (encodingToken.signal()) {
        case ENCODING -> fieldDecoders.add(
            generateSpecificPrimitiveDecoder(encodingToken, encodingToken, name, encoding));
        case BEGIN_ENUM -> fieldDecoders.add(generateEnumDecoder(encodingToken, encodingToken, name));
        case BEGIN_SET -> fieldDecoders.add(generateBitSetDecoder(encodingToken, name));
        case BEGIN_COMPOSITE ->
            fieldDecoders.add(generateCompositeDecoder(encodingToken, encodingToken, name));
        default -> {
        }
      }
      i += encodingToken.componentTokenCount();
    }
    return fieldDecoders;
  }

  private static FieldDecoder generateSpecificPrimitiveDecoder(Token typeToken,
      Token fieldToken, String name, Encoding encoding) {
    if (typeToken.arrayLength() > 1) {
      return generateArrayPrimitiveDecoder(fieldToken, typeToken, name);
    }
    if (encoding.presence() == Encoding.Presence.CONSTANT) {
      return generateConstantPrimitiveDecoder(name, encoding);
    }
    if (encoding.presence() == Encoding.Presence.OPTIONAL) {
      return generateOptionalPrimitiveDecoder(fieldToken, name, encoding);
    }
    return generateRequiredPrimitiveDecoder(fieldToken, name, encoding);
  }

  @Override
  public void generate() throws IOException {
    SpecMustacheFactory mf = new SpecMustacheFactory();
    mf.compilePartial("encoder-fields.mustache");
    mf.compilePartial("decoder-fields.mustache");
    mf.compilePartial("encoder-group.mustache");
    mf.compilePartial("decoder-group.mustache");

    var cargoToml= new CargoToml();
    final String packageName = toLowerSnakeCase(ir.packageName()).replaceAll("[.-]", "_");
    cargoToml.namespace =
        (ir.namespaceName() == null || ir.namespaceName().equalsIgnoreCase(packageName))
            ? packageName.toLowerCase()
            : (ir.namespaceName() + "_" + packageName).toLowerCase();
    cargoToml.description = ir.description();
    Writer cargoFileWriter = outputManager.createCargoToml();
    Mustache cargoTomlTemplate = mf.compile("cargo-toml-template.mustache");
    cargoTomlTemplate.execute(cargoFileWriter, cargoToml).flush();

    List<Enum> enumTypeDefs = generateEnums(ir);
    Mustache enumTemplate = mf.compile("enum-template.mustache");
    for (final var enumTypeDef : enumTypeDefs) {
      Writer fileWriter = outputManager.createOutput(enumTypeDef.filename);
      enumTemplate.execute(fileWriter, enumTypeDef).flush();
    }

    List<BitSet> bitSetStructDefs = generateBitSets(ir);
    Mustache bitSetTemplate = mf.compile("bitset-template.mustache");
    for (final var bitSetStructDef : bitSetStructDefs) {
      Writer fileWriter = outputManager.createOutput(bitSetStructDef.filename);
      bitSetTemplate.execute(fileWriter, bitSetStructDef).flush();
    }

    List<Composite> compositeEncoderDecoders = generateComposites(ir);
    Mustache compositeTemplate = mf.compile("composite-template.mustache");
    for (final var compositeEncoderDecoder : compositeEncoderDecoders) {
      Writer fileWriter = outputManager.createOutput(compositeEncoderDecoder.filename);
      compositeTemplate.execute(fileWriter, compositeEncoderDecoder).flush();
    }

    List<Message> messageEncoderDecoders = new ArrayList<>();
    for (final List<Token> tokens : ir.messages()) {
      var messageEncoderDecoder = new Message();
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

      messageEncoderDecoder.filename = codecModName;
      messageEncoderDecoder.blockLengthType = blockLengthType();
      messageEncoderDecoder.blockLength = msgToken.encodedLength();
      messageEncoderDecoder.templateIdType = rustTypeName(ir.headerStructure().templateIdType());
      messageEncoderDecoder.templateId = msgToken.id();
      messageEncoderDecoder.schemaIdType = rustTypeName(ir.headerStructure().schemaIdType());
      messageEncoderDecoder.schemaId = ir.id();
      messageEncoderDecoder.schemaVersionType = schemaVersionType();
      messageEncoderDecoder.schemaVersion = ir.version();

      messageEncoderDecoder.encoderStruct = MessageCodecGenerator.generateEncoder(ir, msgToken, fields, groups, varData);
      messageEncoderDecoder.decoderStruct = MessageCodecGenerator.generateDecoder(ir, msgToken, fields, groups, varData);

      messageEncoderDecoders.add(messageEncoderDecoder);
    }
     var mapper = new ObjectMapper();
     mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
     mapper.setSerializationInclusion(Include.NON_NULL);

    Mustache messageTemplate = mf.compile("message-template.mustache");
    for (final var messageEncoderDecoder : messageEncoderDecoders) {
      Writer fileWriter = outputManager.createOutput(messageEncoderDecoder.filename);
      messageTemplate.execute(fileWriter, messageEncoderDecoder).flush();
    }

    // lib.rs
    LibRs libRs = LibRsGenerator.generate(outputManager);
    Mustache libRsTemplate = mf.compile("lib-rs-template.mustache");
    Writer fileWriter = outputManager.createOutput(libRs.filename);
    libRsTemplate.execute(fileWriter, libRs).flush();
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
