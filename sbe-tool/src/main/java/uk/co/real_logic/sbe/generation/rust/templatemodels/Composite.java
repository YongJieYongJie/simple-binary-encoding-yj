package uk.co.real_logic.sbe.generation.rust.templatemodels;

import java.util.List;

import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.FieldDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder;

public class Composite {
    public String filename;
    public boolean encodedLengthGreaterThanZero;
    public int encodedLength;
    public String encoderName;
    public List<FieldEncoder> fieldEncoders;

    public String decoderName;
    public List<FieldDecoder> fieldDecoders;
}
