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
package uk.co.real_logic.sbe.generation.rust.templatemodels.typedefs;

import uk.co.real_logic.sbe.generation.rust.templatemodels.encodingdecodingfns.*;

import java.util.List;

public class GroupCodecStruct
{
    public String numInGroupPrimitiveType;
    public String name;
    public int dimensionHeaderSize;
    public String blockLengthPrimitiveType;
    public int offset;
    public int encodedLength;
    public List<FieldEncoder> fieldEncoders;
    public List<GroupEncoder> groupEncoders;
    public List<VarDataEncoder> varDataEncoders;
    public String groupToken;
    public List<FieldDecoder> fieldDecoders;
    public List<GroupDecoder> groupDecoders;
    public List<VarDataDecoder> varDataDecoders;
}
