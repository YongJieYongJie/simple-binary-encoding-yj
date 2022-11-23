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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;
import uk.co.real_logic.sbe.generation.rust.templatemodels.LibRs;

/**
 * Generates `lib.rs` specific code.
 */
class LibRsGenerator
{

    /**
     * Create a new 'lib.rs' for the library being generated
     *
     * @param outputManager for generating the codecs to.
     */
    static LibRs generate(RustOutputManager outputManager) throws IOException
    {
        var libRsPojo = new LibRs();
        libRsPojo.filename = "lib";
        final ArrayList<String> modules = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(outputManager.getSrcDirPath()))
        {
            walk
                .filter(Files::isRegularFile)
                .map((path) -> path.getFileName().toString())
                .filter((fileName) -> fileName.endsWith(".rs"))
                .filter((fileName) -> !fileName.equals("lib.rs"))
                .map((fileName) -> fileName.substring(0, fileName.length() - 3))
                .forEach(modules::add);
        }
        libRsPojo.modules = modules;
        libRsPojo.modules.replaceAll(RustUtil::toLowerSnakeCase);
        return libRsPojo;
    }
}
