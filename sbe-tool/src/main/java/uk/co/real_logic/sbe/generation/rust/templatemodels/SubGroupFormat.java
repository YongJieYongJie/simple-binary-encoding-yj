package uk.co.real_logic.sbe.generation.rust.templatemodels;

import java.util.List;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.GroupDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.VarDataDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.VarDataEncoder;

public class SubGroupFormat {
  public String numInGroupPrimitiveType;
  public String name;
  public int dimensionHeaderSize;
  public String blockLengthPrimitiveType;
  public int offset;
  public int encodedLength;
  public List<FieldEncoder> fieldEncoders;
  public Object encoderGroups;
  public List<VarDataEncoder> encoderVarData;
  public String groupToken; // Only for decoder
  public List<FieldDecoder> fieldDecoders;
  public List<GroupDecoder> decoderGroups;
  public List<VarDataDecoder> decoderVarData;
}
