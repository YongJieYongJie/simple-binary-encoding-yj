package uk.co.real_logic.sbe.generation.rust.templatemodels;

import java.util.List;

public class BitSet {

    public String filename;
    public String bitSetType;
    public String rustPrimitiveType;
    public List<Choice> choices;

    public static class Choice {

        public String choiceName;
        public String choiceBitIndex;
        public boolean isLast;

    }
}
