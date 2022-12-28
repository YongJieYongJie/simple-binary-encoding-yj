package uk.co.real_logic.sbe.generation.rust.templatemodels.decoders;

import java.util.List;

public class FieldDecoder {

  public PrimitiveDecoderRequired primitiveDecoderRequired;
  public PrimitiveDecoderOptional primitiveDecoderOptional;
  public PrimitiveDecoderConstant primitiveDecoderConstant;
  public PrimitiveDecoderArray primitiveDecoderArray;
  public EnumDecoderBasic enumDecoderBasic;
  public EnumDecoderConstant enumDecoderConstant;
  public BitSetDecoder bitSetDecoder;
  public CompositeDecoder compositeDecoder;

  public FieldDecoder(CompositeDecoder f) {
    compositeDecoder = f;
  }

  public FieldDecoder(BitSetDecoder f) {
    bitSetDecoder = f;
  }

  public FieldDecoder(PrimitiveDecoderArray f) {
    primitiveDecoderArray = f;
  }

  public FieldDecoder(PrimitiveDecoderConstant f) {
    primitiveDecoderConstant = f;
  }

  public FieldDecoder(PrimitiveDecoderOptional f) {
    primitiveDecoderOptional = f;
  }

  public FieldDecoder(PrimitiveDecoderRequired f) {
    primitiveDecoderRequired = f;
  }

  public FieldDecoder(EnumDecoderConstant f) {
    enumDecoderConstant = f;
  }

  public FieldDecoder(EnumDecoderBasic f) {
    enumDecoderBasic = f;
  }

  public static class BitSetDecoder {
    public String functionName;
    public String structTypeName;
    public boolean versionGreaterThanZero;
    public int version;
    public String rustPrimitiveType;
    public int offset;
  }

  public static class CompositeDecoder {
    public boolean versionGreaterThanZero;
    public String decoderName;
    public String decoderTypeName;
    public int version;
    public int offset;
  }

  public static class EnumDecoderBasic {
    public String functionName;
    public String enumType;
    public boolean versionGreaterThanZero;
    public int version;
    public String rustPrimitiveType;
    public int offset;
  }

  public static class EnumDecoderConstant {

    public String enumType;
    public String constValueName;
    public String functionName;
  }

  public static class PrimitiveDecoderArray {

    public String functionName;
    public String rustPrimitiveType;
    public int arrayLength;
    public boolean versionGreaterThanZero;
    public int version;
    public String applicableNullValue;
    public List<ArrayItems> arrayItems;

    public static class ArrayItems {

      public String rustPrimitiveType;
      public int baseOffset;
      public int itemOffset;
    }
  }

  public static class PrimitiveDecoderConstant {
    public String characterEncoding;
    public String functionName;
    public String returnValue;
    public String rawConstValue;
  }

  public static class PrimitiveDecoderOptional {
    public String applicableNullValue;
    public String characterEncoding;
    public String functionName;
    public String rustPrimitiveType;
    public boolean versionGreaterThanZero;
    public int version;
    public int offset;
    public boolean isNAN;
    public String literal;
  }

  public static class PrimitiveDecoderRequired {

    public String characterEncoding;
    public String functionName;
    public String rustPrimitiveType;
    public boolean versionGreaterThanZero;
    public int version;
    public String rustLiteral;
    public int offset;
  }
}
