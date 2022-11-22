package uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.fields;

import java.util.List;

public class PrimitiveDecoderArray {

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
