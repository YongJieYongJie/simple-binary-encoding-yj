/*
 * Copyright 2013-2022 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.generation.rust.templatemodels.encodingdecodingfns;

import java.util.List;

public class FieldDecoder
{

    public RequiredPrimitiveDecoder requiredPrimitiveDecoder;
    public OptionalPrimitiveDecoder optionalPrimitiveDecoder;
    public ConstantPrimitiveDecoder constantPrimitiveDecoder;
    public ArrayPrimitiveDecoder arrayPrimitiveDecoder;

    public EnumDecoder enumDecoder;
    public ConstantEnumDecoder constantEnumDecoder;

    public BitSetDecoder bitSetDecoder;

    public CompositeDecoder compositeDecoder;

    public FieldDecoder(final RequiredPrimitiveDecoder decoder)
    {
        requiredPrimitiveDecoder = decoder;
    }

    public FieldDecoder(final OptionalPrimitiveDecoder decoder)
    {
        optionalPrimitiveDecoder = decoder;
    }

    public FieldDecoder(final ConstantPrimitiveDecoder decoder)
    {
        constantPrimitiveDecoder = decoder;
    }

    public FieldDecoder(final ArrayPrimitiveDecoder decoder)
    {
        arrayPrimitiveDecoder = decoder;
    }

    public FieldDecoder(final EnumDecoder decoder)
    {
        enumDecoder = decoder;
    }

    public FieldDecoder(final ConstantEnumDecoder decoder)
    {
        constantEnumDecoder = decoder;
    }

    public FieldDecoder(final BitSetDecoder decoder)
    {
        bitSetDecoder = decoder;
    }

    public FieldDecoder(final CompositeDecoder decoder)
    {
        compositeDecoder = decoder;
    }

    public static class RequiredPrimitiveDecoder
    {
        public String characterEncoding;
        public String functionName;
        public String rustPrimitiveType;
        public boolean versionAboveZero;
        public int version;
        public String rustLiteral;
        public int offset;
    }

    public static class OptionalPrimitiveDecoder
    {
        public String applicableNullValue;
        public String characterEncoding;
        public String functionName;
        public String rustPrimitiveType;
        public boolean versionAboveZero;
        public int version;
        public int offset;
        public boolean isNAN;
        public String literal;
    }

    public static class ConstantPrimitiveDecoder
    {
        public String characterEncoding;
        public String functionName;
        public String returnValue;
        public String rawConstValue;
    }

    public static class ArrayPrimitiveDecoder
    {
        public String functionName;
        public String rustPrimitiveType;
        public int arrayLength;
        public boolean versionAboveZero;
        public int version;
        public String applicableNullValue;
        public List<ArrayItems> arrayItems;

        public static class ArrayItems
        {
            public String rustPrimitiveType;
            public int baseOffset;
            public int itemOffset;
        }
    }

    public static class EnumDecoder
    {
        public String functionName;
        public String enumTypeName;
        public boolean versionAboveZero;
        public int version;
        public String rustPrimitiveType;
        public int offset;
    }

    public static class ConstantEnumDecoder
    {
        public String functionName;
        public String enumTypeName;
        public String constValueName;
    }

    public static class BitSetDecoder
    {
        public String functionName;
        public String bitSetTypeName;
        public boolean versionAboveZero;
        public int version;
        public String rustPrimitiveType;
        public int offset;
    }

    public static class CompositeDecoder
    {
        public boolean versionAboveZero;
        public String functionName;
        public String decoderTypeName;
        public int version;
        public int offset;
    }
}
