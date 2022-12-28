package uk.co.real_logic.sbe.generation.rust.templatemodels.encoders;

import java.util.List;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder.CompositeDecoder;

public class FieldEncoder {

  public PrimitiveEncoderArray primitiveEncoderArray;
  public PrimitiveEncoderBasic primitiveEncoderBasic;
  public PrimitiveEncoderConstant primitiveEncoderConstant;
  public EnumEncoderBasic enumEncoderBasic;
  public EnumEncoderConstant enumEncoderConstant;
  public CompositeEncoder compositeEncoder;
  public BitSetEncoder bitSetEncoder;

  public FieldEncoder(PrimitiveEncoderArray f) {
    primitiveEncoderArray = f;
  }

  public FieldEncoder(PrimitiveEncoderConstant f) {
    primitiveEncoderConstant = f;
  }

  public FieldEncoder(PrimitiveEncoderBasic f) {
    primitiveEncoderBasic = f;
  }

  public FieldEncoder(EnumEncoderConstant f) {
    enumEncoderConstant = f;
  }

  public FieldEncoder(EnumEncoderBasic f) {
    enumEncoderBasic = f;
  }

  public FieldEncoder(BitSetEncoder f) {
    bitSetEncoder = f;
  }

  public FieldEncoder(CompositeEncoder f) {
    compositeEncoder = f;
  }

  public static class BitSetEncoder {

    public String functionName;
    public String structTypeName;
    public int offset;
    public String rustPrimitiveType;
  }

  public static class CompositeEncoder {

    public String encoderFunctionName;
    public String encoderTypeName;
    public int offset;
  }

  public static class EnumEncoderBasic {

    public String functionName;
    public String enumType;
    public int offset;
    public String rustPrimitiveType;

  }

  public static class EnumEncoderConstant {

    public String name;
  }

  public static class PrimitiveEncoderArray {

    public String name;
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

  public static class PrimitiveEncoderBasic {

    public String name;
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

  public static class PrimitiveEncoderConstant {

    public String name;
  }
}
