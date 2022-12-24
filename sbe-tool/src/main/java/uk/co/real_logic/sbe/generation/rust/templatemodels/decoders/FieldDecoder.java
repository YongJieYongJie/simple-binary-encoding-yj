package uk.co.real_logic.sbe.generation.rust.templatemodels.decoders;

import java.util.List;

public class FieldDecoder {

  public PrimitiveDecoderArray primitiveDecoderArray;
  public PrimitiveDecoderConstant primitiveDecoderConstant;
  public BitSetDecoder bitSetDecoder;
  public PrimitiveDecoderOptional primitiveDecoderOptional;
  public PrimitiveDecoderRequired primitiveDecoderRequired;
  public EnumDecoderConstant enumDecoderConstant;
  public EnumDecoderBasic enumDecoderBasic;
  public CompositeDecoder compositeDecoder;

  public static class BitSetDecoder {
    public String functionName;
    public String structTypeName;
    public int offset;
    public String rustPrimitiveType;
    public int version;
    public boolean versionGreaterThanZero;
  }

  public static class CompositeDecoder {
    public String decoderName;
    public String decoderTypeName;
    public int version;
    public int offset;
  public boolean versionGreaterThanZero;
  }

  public static class EnumDecoderBasic {
    public String functionName;
    public int version;
    public String enumType;
    public String rustPrimitiveType;
    public int offset;
    public boolean versionGreaterThanZero;
  }

  public static class EnumDecoderConstant {

    public String name;
    public String enumType;
    public String constValueName;
    public String functionName;
  }

  public static class PrimitiveDecoderArray {

    public String encoding;
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
    public String name;

    public static class ArrayItems {

      public String rustPrimitiveType;
      public int itemOffset;
      public int baseOffset; // only for decoder
    }
  }

  public static class PrimitiveDecoderConstant {
    public String characterEncoding;
    public String functionName;
    public String rawConstValue;
    public String rustPrimitiveType;
    public Object returnValue;
  }

  public static class PrimitiveDecoderOptional {
    public String presence;
    public String applicableNullValue;
    public String characterEncoding;
    public String functionName;
    public String rustPrimitiveType;
    public int version;
    public int offset;
    public boolean isNAN;
    public String literal;
    public boolean versionGreaterThanZero;
  }

  public static class PrimitiveDecoderRequired { // Only for decoder

    public String applicableNullValue;
    public String characterEncoding;
    public String functionName;
    public String rustPrimitiveType;
    public int version;
    public int offset;

    public VersionAboveZero versionAboveZero;

    public static class VersionAboveZero {

      public int version;
      public String rustLiteral;
      public String applicableNullValue;
    }
  }
}
