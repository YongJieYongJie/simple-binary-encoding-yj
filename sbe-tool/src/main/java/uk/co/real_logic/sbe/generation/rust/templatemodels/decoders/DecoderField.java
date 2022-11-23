package uk.co.real_logic.sbe.generation.rust.templatemodels.decoders;

import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.fields.BitSetDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.fields.CompositeDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.fields.EnumDecoderBasic;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.fields.EnumDecoderConstant;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.fields.PrimitiveDecoderArray;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.fields.PrimitiveDecoderConstant;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.fields.PrimitiveDecoderOptional;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.fields.PrimitiveDecoderRequired;

public class DecoderField {

  public PrimitiveDecoderArray primitiveDecoderArray;
  public PrimitiveDecoderConstant primitiveDecoderConstant;
  public BitSetDecoder bitSetDecoder;
  public PrimitiveDecoderOptional primitiveDecoderOptional;
  public PrimitiveDecoderRequired primitiveDecoderRequired;
  public EnumDecoderConstant enumDecoderConstant;
  public EnumDecoderBasic enumDecoderBasic;
  public CompositeDecoder compositeDecoder;
}
