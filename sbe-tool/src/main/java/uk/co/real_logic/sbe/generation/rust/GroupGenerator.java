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
import uk.co.real_logic.sbe.generation.rust.RustGenerator.GroupContainer;
import uk.co.real_logic.sbe.generation.rust.templatemodels.GroupEncoderDecoderStruct;
import uk.co.real_logic.sbe.ir.Token;

class GroupGenerator implements GroupContainer
{

    public final List<GroupGenerator> groupGenerators = new ArrayList<>();
    private final String name;
    private final Token groupToken;
    public GroupEncoderDecoderStruct innerGroup;

    GroupGenerator(final String name, final Token groupToken)
    {
        this.name = name;
        this.groupToken = groupToken;
    }

    public boolean hasInnerGroup() {
        return innerGroup != null;
    }

    public GroupGenerator addInnerGroup(final String name, final Token groupToken)
    {
        final GroupGenerator groupGenerator = new GroupGenerator(name, groupToken);
        groupGenerators.add(groupGenerator);

        return groupGenerator;
    }

    void generateEncoder(
        final List<Token> tokens,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final int index) {
        var groupEncoderStruct = new GroupEncoderDecoderStruct();
        final Token blockLengthToken = Generators.findFirst("blockLength", tokens, index);
        final Token numInGroupToken = Generators.findFirst("numInGroup", tokens, index);
        groupEncoderStruct.numInGroupPrimitiveType = rustTypeName(numInGroupToken.encoding().primitiveType());
        groupEncoderStruct.name = name;
        groupEncoderStruct.dimensionHeaderSize = tokens.get(index).encodedLength();
        groupEncoderStruct.blockLengthPrimitiveType = rustTypeName(blockLengthToken.encoding().primitiveType());
        groupEncoderStruct.offset = numInGroupToken.offset();
        groupEncoderStruct.encodedLength = this.groupToken.encodedLength();
        groupEncoderStruct.fieldEncoders = RustGenerator.generateEncoderFields(fields);
        groupEncoderStruct.groupEncoders = RustGenerator.generateEncoderGroups(groups, this);
        groupEncoderStruct.varDataEncoders = RustGenerator.generateEncoderVarData(varData);
        innerGroup = groupEncoderStruct;
    }

    void generateDecoder(
        final List<Token> tokens,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final int index)
    {
        var groupDecoderStruct = new GroupEncoderDecoderStruct();
        final Token blockLengthToken = Generators.findFirst("blockLength", tokens, index);
        final PrimitiveType blockLengthPrimitiveType = blockLengthToken.encoding().primitiveType();

        final Token numInGroupToken = Generators.findFirst("numInGroup", tokens, index);
        final PrimitiveType numInGroupPrimitiveType = numInGroupToken.encoding().primitiveType();

        groupDecoderStruct.numInGroupPrimitiveType = rustTypeName(numInGroupPrimitiveType);
        groupDecoderStruct.name = name;
        groupDecoderStruct.dimensionHeaderSize = tokens.get(index).encodedLength();

        groupDecoderStruct.blockLengthPrimitiveType = rustTypeName(blockLengthPrimitiveType);
        groupDecoderStruct.offset = numInGroupToken.offset();
        groupDecoderStruct.groupToken = groupToken.toString();

        groupDecoderStruct.fieldDecoders = RustGenerator.generateDecoderFields(fields);
        groupDecoderStruct.groupDecoders = RustGenerator.generateDecoderGroups(groups, this);
        groupDecoderStruct.varDataDecoders = RustGenerator.generateDecoderVarData(varData, true);
        innerGroup = groupDecoderStruct;
    }
}
