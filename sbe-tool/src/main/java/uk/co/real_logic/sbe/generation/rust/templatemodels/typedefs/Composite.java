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

import uk.co.real_logic.sbe.generation.rust.templatemodels.encodingdecodingfns.FieldDecoder;
import uk.co.real_logic.sbe.generation.rust.templatemodels.encodingdecodingfns.FieldEncoder;

import java.util.List;

public class Composite
{
    public String filename;
    public boolean encodedLengthGreaterThanZero;
    public int encodedLength;
    public String encoderName;
    public List<FieldEncoder> fieldEncoders;
    public String decoderName;
    public List<FieldDecoder> fieldDecoders;
}
