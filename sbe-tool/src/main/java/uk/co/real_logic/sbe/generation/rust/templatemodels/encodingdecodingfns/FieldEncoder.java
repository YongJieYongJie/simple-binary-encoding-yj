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

public class FieldEncoder
{

    public PrimitiveEncoder primitiveEncoder;
    public ConstantPrimitiveEncoder constantPrimitiveEncoder;
    public ArrayPrimitiveEncoder arrayPrimitiveEncoder;

    public EnumEncoder enumEncoder;
    public ConstantEnumEncoder constantEnumEncoder;

    public BitSetEncoder bitSetEncoder;

    public CompositeEncoder compositeEncoder;

    public FieldEncoder(final PrimitiveEncoder encoder)
    {
        primitiveEncoder = encoder;
    }

    public FieldEncoder(final ConstantPrimitiveEncoder encoder)
    {
        constantPrimitiveEncoder = encoder;
    }

    public FieldEncoder(final ArrayPrimitiveEncoder encoder)
    {
        arrayPrimitiveEncoder = encoder;
    }

    public FieldEncoder(final EnumEncoder encoder)
    {
        enumEncoder = encoder;
    }

    public FieldEncoder(final ConstantEnumEncoder encoder)
    {
        constantEnumEncoder = encoder;
    }

    public FieldEncoder(final BitSetEncoder encoder)
    {
        bitSetEncoder = encoder;
    }

    public FieldEncoder(final CompositeEncoder encoder)
    {
        compositeEncoder = encoder;
    }

    public static class PrimitiveEncoder
    {
        public String fieldName;
        public String applicableMinValue;
        public String applicableMaxValue;
        public String applicableNullValue;
        public String characterEncoding;
        public String semanticType;
        public int offset;
        public int encodedLength;
        public String functionName;
        public String rustPrimitiveType;
    }

    public static class ConstantPrimitiveEncoder
    {
        public String fieldName;
    }

    public static class ArrayPrimitiveEncoder
    {
        public String fieldName;
        public String applicableMinValue;
        public String applicableMaxValue;
        public String applicableNullValue;
        public String characterEncoding;
        public String semanticType;
        public int offset;
        public int encodedLength;
        public int version;
        public String functionName;
        public String rustPrimitiveType;
        public int arrayLength;
        public List<ArrayItems> arrayItems;

        public static class ArrayItems
        {
            public String rustPrimitiveType;
            public int itemOffset;
            public int itemIndex;
        }
    }

    public static class EnumEncoder
    {
        public String functionName;
        public String enumTypeName;
        public int offset;
        public String rustPrimitiveType;
    }

    public static class ConstantEnumEncoder
    {
        public String fieldName;
    }

    public static class BitSetEncoder
    {
        public String functionName;
        public String bitSetTypeName;
        public int offset;
        public String rustPrimitiveType;
    }

    public static class CompositeEncoder
    {
        public String encoderFunctionName;
        public String encoderTypeName;
        public int offset;
    }
}
