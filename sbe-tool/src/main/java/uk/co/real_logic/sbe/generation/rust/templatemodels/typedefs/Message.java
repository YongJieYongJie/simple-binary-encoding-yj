package uk.co.real_logic.sbe.generation.rust.templatemodels.typedefs;

public class Message {
    public String filename;
    public String blockLengthType;
    public int blockLength;
    public String templateIdType;
    public int templateId;
    public String schemaIdType;
    public int schemaId;
    public String schemaVersionType;
    public int schemaVersion;
    public MessageCodecStruct encoderStruct;
    public MessageCodecStruct decoderStruct;
}
