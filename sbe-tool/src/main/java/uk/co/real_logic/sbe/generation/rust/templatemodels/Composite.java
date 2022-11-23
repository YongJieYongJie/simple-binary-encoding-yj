package uk.co.real_logic.sbe.generation.rust.templatemodels;

import java.util.List;

import uk.co.real_logic.sbe.generation.rust.templatemodels.decoders.DecoderField;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.EncoderField;

public class Composite {
    public String filename;
    public String encoderName;
    public int encodedLength;
    public List<EncoderField> encoderFields;

    public String decoderName;
    public List<DecoderField> decoderFields;
}
