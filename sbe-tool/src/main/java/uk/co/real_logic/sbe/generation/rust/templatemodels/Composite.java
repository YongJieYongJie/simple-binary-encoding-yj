package uk.co.real_logic.sbe.generation.rust.templatemodels;

import java.util.List;

import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.DecoderFields;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.EncoderFields;

public class Composite {
    public String filename;
    public String encoderName;
    public int encodedLength;
    public List<EncoderFields> encoderFields;

    public String decoderName;
    public List<DecoderFields> decoderFields;
}
