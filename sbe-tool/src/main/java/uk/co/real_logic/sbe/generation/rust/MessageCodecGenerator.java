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
import uk.co.real_logic.sbe.generation.rust.templatemodels.typedefs.GroupCodecStruct;
import uk.co.real_logic.sbe.generation.rust.templatemodels.typedefs.MessageCodecStruct;
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

    static MessageCodecStruct generateEncoder(
            final Ir ir,
            final Token msgToken,
            final List<Token> fields,
            final List<Token> groups,
            final List<Token> varData) {
        return new MessageCodecGenerator().generate(fields, groups, varData, ir, msgToken, Encoder);
    }

    static MessageCodecStruct generateDecoder(
            final Ir ir,
            final Token msgToken,
            final List<Token> fields,
            final List<Token> groups,
            final List<Token> varData) {
        return new MessageCodecGenerator().generate(fields, groups, varData, ir, msgToken, Decoder);
    }

    MessageCodecStruct generate(
            final List<Token> fields,
            final List<Token> groups,
            final List<Token> varData,
            final Ir ir,
            final Token msgToken,
            final CodecType codecType
    ) {
        var messageCodec = new MessageCodecStruct();

        // i.e. <name>Decoder or <name>Encoder
        messageCodec.msgTypeName = formatStructName(msgToken.name()) + codecType.name();
        messageCodec.blockLengthType = rustTypeName(ir.headerStructure().blockLengthType());
        messageCodec.schemaVersionType = rustTypeName(ir.headerStructure().schemaVersionType());

        if (codecType == Decoder) {
            messageCodec.fieldDecoders = RustGenerator.generateDecoderFields(fields);
            messageCodec.groupDecoders = RustGenerator.generateDecoderGroups(groups, this);
            messageCodec.varDataDecoders = RustGenerator.generateDecoderVarData(varData, false);
        } else {
            messageCodec.fieldEncoders = RustGenerator.generateEncoderFields(fields);
            messageCodec.groupEncoders = RustGenerator.generateEncoderGroups(groups, this);
            messageCodec.varDataEncoders = RustGenerator.generateEncoderVarData(varData);
        }

        List<GroupCodecStruct> subGroups = new ArrayList<>();

        // Unwrap subgroups in a breadth-first manner (i.e., groups that are generated directly by the
        // message will be processed first, followed by any subgroups these initial groups generates.
        List<GroupGenerator> nextGroupGenerators = new ArrayList<>();
        Set<String> alreadyAdded = new HashSet<>();
        while (!groupGenerators.isEmpty()) {
            for (GroupGenerator groupGenerator : groupGenerators) {
                if (!groupGenerator.hasSubGroup()) continue;
                if (alreadyAdded.contains(groupGenerator.subGroup.name)) continue;
                alreadyAdded.add(groupGenerator.subGroup.name);
                subGroups.add(groupGenerator.subGroup);
                nextGroupGenerators.addAll(groupGenerator.groupGenerators);
            }
            groupGenerators.clear();
            groupGenerators.addAll(nextGroupGenerators);
            nextGroupGenerators.clear();
        }
        messageCodec.groupCodecStructs = subGroups;

        return messageCodec;
    }

    public GroupGenerator addSubGroup(final String name, final Token groupToken) {
        final GroupGenerator groupGenerator = new GroupGenerator(name, groupToken);
        groupGenerators.add(groupGenerator);
        return groupGenerator;
    }
}
