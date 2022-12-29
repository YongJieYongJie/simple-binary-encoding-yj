package uk.co.real_logic.sbe.generation.rust.templatemodels.typedefs;

import uk.co.real_logic.sbe.generation.rust.templatemodels.encodingdecodingfns.FieldDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encodingdecodingfns.FieldEncoder;

import java.util.List;

public class Composite {
    public String filename;
    public boolean encodedLengthGreaterThanZero;
    public int encodedLength;
    public String encoderName;
    public List<FieldEncoder> fieldEncoders;

    public String decoderName;
    public List<FieldDecoder> fieldDecoders;
}
