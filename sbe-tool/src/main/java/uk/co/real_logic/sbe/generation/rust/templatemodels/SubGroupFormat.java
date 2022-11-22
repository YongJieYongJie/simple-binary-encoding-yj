package uk.co.real_logic.sbe.generation.rust.templatemodels;

import java.util.List;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.DecoderFields;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.GroupDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.VarDataDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.EncoderFields;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.VarDataEncoder;

public class SubGroupFormat {
  public String numInGroupPrimitiveType;
  public String name;
  public int dimensionHeaderSize;
  public String blockLengthPrimitiveType;
  public int offset;
  public int encodedLength;
  public List<EncoderFields> encoderFields;
  public Object encoderGroups;
  public List<VarDataEncoder> encoderVarData;
  public String groupToken; // Only for decoder
  public List<DecoderFields> decoderFields;
  public List<GroupDecoder> decoderGroups;
  public List<VarDataDecoder> decoderVarData;
}
