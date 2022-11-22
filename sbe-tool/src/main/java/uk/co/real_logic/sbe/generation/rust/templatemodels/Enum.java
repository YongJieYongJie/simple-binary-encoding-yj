package uk.co.real_logic.sbe.generation.rust.templatemodels;

import java.util.List;

public class Enum {

    public String primitiveType;
    public String enumRustName;
    public List<EnumItem> enumItems;
    public List<EnumItem> enumFromItems;
    public String filename;
    public static class EnumItem {

        public String name;
        public String literal;

    }
}