package uk.co.real_logic.sbe.generation.rust.templatemodels.typedefs;

import uk.co.real_logic.sbe.generation.rust.templatemodels.encodingdecodingfns.*;

import java.util.List;

public class MessageEncoderDecoderStruct {

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
