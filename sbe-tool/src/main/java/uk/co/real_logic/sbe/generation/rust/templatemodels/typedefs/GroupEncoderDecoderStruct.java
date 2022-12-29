package uk.co.real_logic.sbe.generation.rust.templatemodels.typedefs;

import uk.co.real_logic.sbe.generation.rust.templatemodels.encodingdecodingfns.*;

import java.util.List;

public class GroupEncoderDecoderStruct {
    public String numInGroupPrimitiveType;
    public String name;
    public int dimensionHeaderSize;
    public String blockLengthPrimitiveType;
    public int offset;
    public int encodedLength; // only for encoder
    public List<FieldEncoder> fieldEncoders;
    public List<GroupEncoder> groupEncoders;
    public List<VarDataEncoder> varDataEncoders;
    public String groupToken; // Only for decoder
    public List<FieldDecoder> fieldDecoders;
    public List<GroupDecoder> groupDecoders;
    public List<VarDataDecoder> varDataDecoders;
}
