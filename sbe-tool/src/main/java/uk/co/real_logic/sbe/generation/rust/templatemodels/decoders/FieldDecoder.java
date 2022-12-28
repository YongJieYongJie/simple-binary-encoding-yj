package uk.co.real_logic.sbe.generation.rust.templatemodels.decoders;

import java.util.List;

public class FieldDecoder {

  public RequiredPrimitiveDecoder requiredPrimitiveDecoder;
  public OptionalPrimitiveDecoder optionalPrimitiveDecoder;
  public ConstantPrimitiveDecoder constantPrimitiveDecoder;
  public ArrayPrimitiveDecoder arrayPrimitiveDecoder;

  public EnumDecoder enumDecoder;
  public ConstantEnumDecoder constantEnumDecoder;

  public BitSetDecoder bitSetDecoder;

  public CompositeDecoder compositeDecoder;

  public FieldDecoder(RequiredPrimitiveDecoder decoder) {
    requiredPrimitiveDecoder = decoder;
  }

  public FieldDecoder(OptionalPrimitiveDecoder decoder) {
    optionalPrimitiveDecoder = decoder;
  }

  public FieldDecoder(ConstantPrimitiveDecoder decoder) {
    constantPrimitiveDecoder = decoder;
  }

  public FieldDecoder(ArrayPrimitiveDecoder decoder) {
    arrayPrimitiveDecoder = decoder;
  }

  public FieldDecoder(EnumDecoder decoder) {
    enumDecoder = decoder;
  }

  public FieldDecoder(ConstantEnumDecoder decoder) {
    constantEnumDecoder = decoder;
  }

  public FieldDecoder(BitSetDecoder decoder) {
    bitSetDecoder = decoder;
  }

  public FieldDecoder(CompositeDecoder decoder) {
    compositeDecoder = decoder;
  }

  public static class RequiredPrimitiveDecoder {

    public String characterEncoding;
    public String functionName;
    public String rustPrimitiveType;
    public boolean versionAboveZero;
    public int version;
    public String rustLiteral;
    public int offset;
  }

  public static class OptionalPrimitiveDecoder {
    public String applicableNullValue;
    public String characterEncoding;
    public String functionName;
    public String rustPrimitiveType;
    public boolean versionAboveZero;
    public int version;
    public int offset;
    public boolean isNAN;
    public String literal;
  }

  public static class ConstantPrimitiveDecoder {
    public String characterEncoding;
    public String functionName;
    public String returnValue;
    public String rawConstValue;
  }

  public static class ArrayPrimitiveDecoder {

    public String functionName;
    public String rustPrimitiveType;
    public int arrayLength;
    public boolean versionAboveZero;
    public int version;
    public String applicableNullValue;
    public List<ArrayItems> arrayItems;

    public static class ArrayItems {

      public String rustPrimitiveType;
      public int baseOffset;
      public int itemOffset;
    }
  }

  public static class EnumDecoder {
    public String functionName;
    public String enumTypeName;
    public boolean versionAboveZero;
    public int version;
    public String rustPrimitiveType;
    public int offset;
  }

  public static class ConstantEnumDecoder {

    public String functionName;
    public String enumTypeName;
    public String constValueName;
  }

  public static class BitSetDecoder {
    public String functionName;
    public String bitSetTypeName;
    public boolean versionAboveZero;
    public int version;
    public String rustPrimitiveType;
    public int offset;
  }

  public static class CompositeDecoder {
    public boolean versionAboveZero;
    public String functionName;
    public String decoderTypeName;
    public int version;
    public int offset;
  }
}
