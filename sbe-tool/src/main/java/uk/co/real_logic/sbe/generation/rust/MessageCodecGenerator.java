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

import static uk.co.real_logic.sbe.generation.rust.RustGenerator.CodecType.Decoder;
import static uk.co.real_logic.sbe.generation.rust.RustGenerator.CodecType.Encoder;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.formatStructName;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.rustTypeName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.co.real_logic.sbe.generation.rust.RustGenerator.CodecType;
import uk.co.real_logic.sbe.generation.rust.RustGenerator.SubGroupContainer;
import uk.co.real_logic.sbe.generation.rust.templatemodels.SubGroupFormat;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encoders.FieldEncoder.EncoderDecoderFormat;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Token;

class MessageCodecGenerator implements SubGroupContainer
{

    private final List<SubGroup> subGroups = new ArrayList<>();

    MessageCodecGenerator() {}

    EncoderDecoderFormat generate(
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final Ir ir,
        final Token msgToken,
        final CodecType codecType
    )
    {
        var messageValues = new EncoderDecoderFormat();
        messageValues.coderType = codecType.toString().toLowerCase();

        // i.e. <name>Decoder or <name>Encoder
        messageValues.msgTypeName = formatStructName(msgToken.name()) + codecType.name();
        messageValues.bufType = codecType.bufType();
        messageValues.blockLengthType = rustTypeName(ir.headerStructure().blockLengthType());
        messageValues.schemaVersionType = rustTypeName(ir.headerStructure().schemaVersionType());

        if (codecType == Decoder)
        {
            messageValues.fieldDecoders = RustGenerator.generateDecoderFields(fields);
            messageValues.groupDecoders = RustGenerator.generateDecoderGroups(groups, this);
            messageValues.varDataDecoders = RustGenerator.generateDecoderVarData(varData, false);
        }
        else
        {
            messageValues.fieldEncoders = RustGenerator.generateEncoderFields(fields);
            messageValues.groupEncoders = RustGenerator.generateEncoderGroups(groups, this);
            messageValues.varDataEncoders = RustGenerator.generateEncoderVarData(varData);
        }

        List<SubGroupFormat> subGroupValues = new ArrayList<>();

        // Unwrap subgroups in a breadth-first manner (i.e., groups that are generated directly by the
        // message will be processed first, followed by any subgroups these initial groups generates.
        List<SubGroup> nextSubGroups = new ArrayList<>();
        Set<String> alreadyAdded = new HashSet<>();
        while (!subGroups.isEmpty()) {
            for (SubGroup subGroup : subGroups) {
                if (subGroup.subGroupValue == null) continue;
                if (alreadyAdded.contains(subGroup.subGroupValue.name)) continue;
                alreadyAdded.add(subGroup.subGroupValue.name);
                subGroupValues.add(subGroup.subGroupValue);
                nextSubGroups.addAll(subGroup.subGroups);
            }
            subGroups.clear();
            subGroups.addAll(nextSubGroups);
            nextSubGroups.clear();
        }
        messageValues.subgroups = subGroupValues;

        return messageValues;
    }

    public SubGroup addSubGroup(final String name, final Token groupToken)
    {
        final SubGroup subGroup = new SubGroup(name, groupToken);
        subGroups.add(subGroup);
        return subGroup;
    }

    static EncoderDecoderFormat generateEncoder(
        final Ir ir,
        final Token msgToken,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData)
    {
        final MessageCodecGenerator coderDef = new MessageCodecGenerator();
        return coderDef.generate(fields, groups, varData, ir, msgToken, Encoder);
    }

    static EncoderDecoderFormat generateDecoder(
        final Ir ir,
        final Token msgToken,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData)
    {
        final MessageCodecGenerator coderDef = new MessageCodecGenerator();
        return coderDef.generate(fields, groups, varData, ir, msgToken, Decoder);
    }
}
