package uk.co.real_logic.sbe.generation.rust.templatemodels.encoders;

import java.util.List;
import uk.co.real_logic.sbe.generation.rust.templatemodels.SubGroupFormat;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.GroupDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.VarDataDecoder;

public class FieldEncoder {

  public PrimitiveEncoderArray primitiveEncoderArray;
  public PrimitiveEncoderBasic primitiveEncoderBasic;
  public PrimitiveEncoderConstant primitiveEncoderConstant;
  public EnumEncoderBasic enumEncoderBasic;
  public EnumEncoderConstant enumEncoderConstant;
  public CompositeEncoder compositeEncoder;
  public BitSetEncoder bitSetEncoder;

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

  public static class EncoderDecoderFormat {

      public String coderType;
      public String bufType;
      public String msgTypeName;
      public List<FieldEncoder> fieldEncoders;
      public List<GroupEncoder> groupEncoders;
      public List<VarDataEncoder> varDataEncoders;
      public List<SubGroupFormat> subgroups;
      public List<FieldDecoder> fieldDecoders;
      public List<GroupDecoder> groupDecoders;
      public List<VarDataDecoder> varDataDecoders;

      // below only for decoder
      public String blockLengthType;
      public String schemaVersionType;
  }

  public static class EnumEncoderBasic {

    public String rustPrimitiveType;
    public String functionName;
    public String enumType;
    public int offset;

  }

  public static class EnumEncoderConstant {

    public String name;
  }

  public static class PrimitiveEncoderArray {

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
      public int itemIndex;
    }
  }

  public static class PrimitiveEncoderBasic {

    public String name;
    public String encoding;
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
