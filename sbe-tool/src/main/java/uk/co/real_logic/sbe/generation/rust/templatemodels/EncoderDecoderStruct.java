package uk.co.real_logic.sbe.generation.rust.templatemodels;

import java.util.List;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.GroupDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.VarDataDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.GroupEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.VarDataEncoder;

public class EncoderDecoderStruct {

  public String msgTypeName;
  public List<FieldEncoder> fieldEncoders;
  public List<GroupEncoder> groupEncoders;
  public List<VarDataEncoder> varDataEncoders;
  public List<GroupEncoderDecoderStruct> groupEncoderDecoders;
  public List<FieldDecoder> fieldDecoders;
  public List<GroupDecoder> groupDecoders;
  public List<VarDataDecoder> varDataDecoders;

  // below only for decoder
  public String blockLengthType;
  public String schemaVersionType;
}
