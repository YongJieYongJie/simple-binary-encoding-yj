package uk.co.real_logic.sbe.generation.rust.templatemodels;

import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.fields.EncoderFormat;

public class Message {
    public String blockLengthType;
    public int blockLength;
    public String templateIdType;
    public int templateId;
    public String schemaIdType;
    public int schemaId;
    public String schemaVersionType;
    public int schemaVersion;
    public EncoderFormat encoder;
    public EncoderFormat decoder;
    public String filename;
}
