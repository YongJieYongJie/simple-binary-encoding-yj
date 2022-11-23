package uk.co.real_logic.sbe.generation.rust.templatemodels.encoders;

import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.fields.BitSetEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.fields.CompositeEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.fields.EnumEncoderBasic;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.fields.EnumEncoderConstant;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.fields.PrimitiveEncoderArray;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.fields.PrimitiveEncoderBasic;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.fields.PrimitiveEncoderConstant;

public class EncoderField {

  public PrimitiveEncoderArray primitiveEncoderArray;
  public PrimitiveEncoderBasic primitiveEncoderBasic;
  public PrimitiveEncoderConstant primitiveEncoderConstant;
  public EnumEncoderBasic enumEncoderBasic;
  public EnumEncoderConstant enumEncoderConstant;
  public CompositeEncoder compositeEncoder;
  public BitSetEncoder bitSetEncoder;
}
