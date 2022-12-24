package uk.co.real_logic.sbe.generation.rust.templatemodels;

import java.util.List;

import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder;

public class Composite {
    public String filename;
    public String encoderName;
    public int encodedLength;
    public List<FieldEncoder> fieldEncoders;

    public String decoderName;
    public List<FieldDecoder> fieldDecoders;
}
