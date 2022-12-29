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

import uk.co.real_logic.sbe.generation.rust.RustGenerator.CodecType;
import uk.co.real_logic.sbe.generation.rust.RustGenerator.GroupContainer;
import uk.co.real_logic.sbe.generation.rust.templatemodels.typedefs.GroupEncoderDecoderStruct;
import uk.co.real_logic.sbe.generation.rust.templatemodels.typedefs.MessageEncoderDecoderStruct;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Token;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.co.real_logic.sbe.generation.rust.RustGenerator.CodecType.Decoder;
import static uk.co.real_logic.sbe.generation.rust.RustGenerator.CodecType.Encoder;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.formatStructName;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.rustTypeName;

class MessageCodecGenerator implements GroupContainer {

    private final List<GroupGenerator> groupGenerators = new ArrayList<>();

    MessageCodecGenerator() {
    }

    static MessageEncoderDecoderStruct generateEncoder(
            final Ir ir,
            final Token msgToken,
            final List<Token> fields,
            final List<Token> groups,
            final List<Token> varData) {
        return new MessageCodecGenerator().generate(fields, groups, varData, ir, msgToken, Encoder);
    }

    static MessageEncoderDecoderStruct generateDecoder(
            final Ir ir,
            final Token msgToken,
            final List<Token> fields,
            final List<Token> groups,
            final List<Token> varData) {
        return new MessageCodecGenerator().generate(fields, groups, varData, ir, msgToken, Decoder);
    }

    MessageEncoderDecoderStruct generate(
            final List<Token> fields,
            final List<Token> groups,
            final List<Token> varData,
            final Ir ir,
            final Token msgToken,
            final CodecType codecType
    ) {
        var messageEncoderDecoder = new MessageEncoderDecoderStruct();

        // i.e. <name>Decoder or <name>Encoder
        messageEncoderDecoder.msgTypeName = formatStructName(msgToken.name()) + codecType.name();
        messageEncoderDecoder.blockLengthType = rustTypeName(ir.headerStructure().blockLengthType());
        messageEncoderDecoder.schemaVersionType = rustTypeName(ir.headerStructure().schemaVersionType());

        if (codecType == Decoder) {
            messageEncoderDecoder.fieldDecoders = RustGenerator.generateDecoderFields(fields);
            messageEncoderDecoder.groupDecoders = RustGenerator.generateDecoderGroups(groups, this);
            messageEncoderDecoder.varDataDecoders = RustGenerator.generateDecoderVarData(varData, false);
        } else {
            messageEncoderDecoder.fieldEncoders = RustGenerator.generateEncoderFields(fields);
            messageEncoderDecoder.groupEncoders = RustGenerator.generateEncoderGroups(groups, this);
            messageEncoderDecoder.varDataEncoders = RustGenerator.generateEncoderVarData(varData);
        }

        List<GroupEncoderDecoderStruct> innerGroups = new ArrayList<>();

        // Unwrap subgroups in a breadth-first manner (i.e., groups that are generated directly by the
        // message will be processed first, followed by any subgroups these initial groups generates.
        List<GroupGenerator> nextGroupGenerators = new ArrayList<>();
        Set<String> alreadyAdded = new HashSet<>();
        while (!groupGenerators.isEmpty()) {
            for (GroupGenerator groupGenerator : groupGenerators) {
                if (!groupGenerator.hasInnerGroup()) continue;
                if (alreadyAdded.contains(groupGenerator.innerGroup.name)) continue;
                alreadyAdded.add(groupGenerator.innerGroup.name);
                innerGroups.add(groupGenerator.innerGroup);
                nextGroupGenerators.addAll(groupGenerator.groupGenerators);
            }
            groupGenerators.clear();
            groupGenerators.addAll(nextGroupGenerators);
            nextGroupGenerators.clear();
        }
        messageEncoderDecoder.groupEncoderDecoders = innerGroups;

        return messageEncoderDecoder;
    }

    public GroupGenerator addInnerGroup(final String name, final Token groupToken) {
        final GroupGenerator groupGenerator = new GroupGenerator(name, groupToken);
        groupGenerators.add(groupGenerator);
        return groupGenerator;
    }
}
