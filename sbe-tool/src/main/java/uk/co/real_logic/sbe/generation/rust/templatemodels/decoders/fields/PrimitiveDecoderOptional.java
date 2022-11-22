package uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.fields;

public class PrimitiveDecoderOptional { // Only for decoder

  public String presence;
  public String applicableNullValue;
  public String characterEncoding;
  public String functionName;
  public String rustPrimitiveType;
  public int version;
  public int offset;
  public boolean isNAN;
  public RustLiteral literal;

  public static class RustLiteral {

    public String value;
  }

}
