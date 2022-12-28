package uk.co.real_logic.sbe.generation.rust.templatemodels.encoders;

import java.util.List;

public class FieldEncoder {

  public PrimitiveEncoder primitiveEncoder;
  public ConstantPrimitiveEncoder constantPrimitiveEncoder;
  public ArrayPrimitiveEncoder arrayPrimitiveEncoder;

  public EnumEncoder enumEncoder;
  public ConstantEnumEncoder constantEnumEncoder;

  public BitSetEncoder bitSetEncoder;

  public CompositeEncoder compositeEncoder;

  public FieldEncoder(PrimitiveEncoder encoder) {
    primitiveEncoder = encoder;
  }

  public FieldEncoder(ConstantPrimitiveEncoder encoder) {
    constantPrimitiveEncoder = encoder;
  }

  public FieldEncoder(ArrayPrimitiveEncoder encoder) {
    arrayPrimitiveEncoder = encoder;
  }

  public FieldEncoder(EnumEncoder encoder) {
    enumEncoder = encoder;
  }

  public FieldEncoder(ConstantEnumEncoder encoder) {
    constantEnumEncoder = encoder;
  }

  public FieldEncoder(BitSetEncoder encoder) {
    bitSetEncoder = encoder;
  }

  public FieldEncoder(CompositeEncoder encoder) {
    compositeEncoder = encoder;
  }

  public static class PrimitiveEncoder {
    public String fieldName;
    public String applicableMinValue;
    public String applicableMaxValue;
    public String applicableNullValue;
    public String characterEncoding;
    public String semanticType;
    public int offset;
    public int encodedLength;
    public String functionName;
    public String rustPrimitiveType;
  }

  public static class ConstantPrimitiveEncoder {
    public String fieldName;
  }

  public static class ArrayPrimitiveEncoder {
    public String fieldName;
    public String applicableMinValue;
    public String applicableMaxValue;
    public String applicableNullValue;
    public String characterEncoding;
    public String semanticType;
    public int offset;
    public int encodedLength;
    public int version;
    public String functionName;
    public String rustPrimitiveType;
    public int arrayLength;
    public List<ArrayItems> arrayItems;

    public static class ArrayItems {
      public String rustPrimitiveType;
      public int itemOffset;
      public int itemIndex;
    }
  }

  public static class EnumEncoder {
    public String functionName;
    public String enumTypeName;
    public int offset;
    public String rustPrimitiveType;
  }

  public static class ConstantEnumEncoder {
    public String fieldName;
  }

  public static class BitSetEncoder {
    public String functionName;
    public String bitSetTypeName;
    public int offset;
    public String rustPrimitiveType;
  }

  public static class CompositeEncoder {
    public String encoderFunctionName;
    public String encoderTypeName;
    public int offset;
  }
}
