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
package uk.co.real_logic.sbe.generation.rust;

import static uk.co.real_logic.sbe.generation.rust.RustUtil.rustTypeName;

import java.util.ArrayList;
import java.util.List;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.Generators;
import uk.co.real_logic.sbe.generation.rust.RustGenerator.SubGroupContainer;
import uk.co.real_logic.sbe.generation.rust.templatemodels.SubGroupFormat;
import uk.co.real_logic.sbe.ir.Token;

class SubGroup implements SubGroupContainer
{

    public final List<SubGroup> subGroups = new ArrayList<>();
    private final String name;
    private final Token groupToken;
    public SubGroupFormat subGroupValue;

    SubGroup(final String name, final Token groupToken)
    {
        this.name = name;
        this.groupToken = groupToken;
    }

    public SubGroup addSubGroup(final String name, final Token groupToken)
    {
        final SubGroup subGroup = new SubGroup(name, groupToken);
        subGroups.add(subGroup);

        return subGroup;
    }

    void generateEncoder(
        final List<Token> tokens,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final int index) {
        var subGroupPojo = new SubGroupFormat();
        final Token blockLengthToken = Generators.findFirst("blockLength", tokens, index);
        final Token numInGroupToken = Generators.findFirst("numInGroup", tokens, index);
        subGroupPojo.numInGroupPrimitiveType = rustTypeName(numInGroupToken.encoding().primitiveType());
        subGroupPojo.name = name;
        subGroupPojo.dimensionHeaderSize = tokens.get(index).encodedLength();
        subGroupPojo.blockLengthPrimitiveType = rustTypeName(blockLengthToken.encoding().primitiveType());
        subGroupPojo.offset = numInGroupToken.offset();
        subGroupPojo.encodedLength = this.groupToken.encodedLength();
        subGroupPojo.encoderFields = RustGenerator.generateEncoderFields(fields);
        subGroupPojo.encoderGroups = RustGenerator.generateEncoderGroups(groups, this);
        subGroupPojo.encoderVarData = RustGenerator.generateEncoderVarData(varData);
        subGroupValue = subGroupPojo;
    }

    void generateDecoder(
        final List<Token> tokens,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final int index)
    {
        var subGroupPojo = new SubGroupFormat();
        final Token blockLengthToken = Generators.findFirst("blockLength", tokens, index);
        final PrimitiveType blockLengthPrimitiveType = blockLengthToken.encoding().primitiveType();

        final Token numInGroupToken = Generators.findFirst("numInGroup", tokens, index);
        final PrimitiveType numInGroupPrimitiveType = numInGroupToken.encoding().primitiveType();

        subGroupPojo.numInGroupPrimitiveType = rustTypeName(numInGroupPrimitiveType);
        subGroupPojo.name = name;
        subGroupPojo.dimensionHeaderSize = tokens.get(index).encodedLength();

        subGroupPojo.blockLengthPrimitiveType = rustTypeName(blockLengthPrimitiveType);
        subGroupPojo.offset = numInGroupToken.offset();
        subGroupPojo.groupToken = groupToken.toString();

        subGroupPojo.decoderFields = RustGenerator.generateDecoderFields(fields);
        subGroupPojo.decoderGroups = RustGenerator.generateDecoderGroups(groups, this);
        subGroupPojo.decoderVarData = RustGenerator.generateDecoderVarData(varData, true);
        subGroupValue = subGroupPojo;
    }
}
