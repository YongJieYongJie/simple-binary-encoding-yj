package uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.fields;

public class PrimitiveDecoderRequired { // Only for decoder

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
