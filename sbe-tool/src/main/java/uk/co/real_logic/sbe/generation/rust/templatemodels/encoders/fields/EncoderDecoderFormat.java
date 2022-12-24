package uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.fields;

import java.util.List;
import uk.co.real_logic.sbe.generation.rust.templatemodels.SubGroupFormat;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.GroupDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.VarDataDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.GroupEncoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.VarDataEncoder;

public class EncoderDecoderFormat {

    public String coderType;
    public String bufType;
    public String msgTypeName;
    public List<FieldEncoder> fieldEncoders;
    public List<GroupEncoder> encoderGroups;
    public List<VarDataEncoder> encoderVarData;
    public List<SubGroupFormat> subgroups;
    public List<FieldDecoder> fieldDecoders;
    public List<GroupDecoder> decoderGroups;
    public List<VarDataDecoder> decoderVarData;

    // below only for decoder
    public String blockLengthType;
    public String schemaVersionType;
}
