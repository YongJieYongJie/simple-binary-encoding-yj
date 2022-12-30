# Simple Binary Encoding (SBE) with some modifications

## Additions to upstream

1. Added dependency on [mustache templating language](mustache) (Java implementation [here](mustache-java)).

   _Benefits_:
    1. Makes the generation code (written in Java) clearer because the template-related code blocks are moved into
       the `.mustache` template files. What remains are the logic that translates SBE tokens into string literals
       relevant for the target output (_e.g._, function names, type names, _etc._).
    2. Enables some modification to the generated output without changing the generation code that is written in Java.
       This is useful as users of the generated code might not be familiar with Java.

   _Drawbacks_:
    1. Necessitates learning of mustache templating syntax. That said, mustache has a very limited syntax and supports
       minimal logic.

[mustache]: https://mustache.github.io/
[mustache-java]: https://github.com/spullara/mustache.java

2. Generally refactor Java code for generating Rust output.

## Usage

1. Download the latest jar file from GitHub releases.
2. Generate the Rust codec the same way as the upstream sbe-tool, for example:
    ```shell
    pathToDownloadedJarFile="path/to/downloaded/jar/file/sbe-all-yj-1.27.0.jar"
    pathToSchemaFile="./sbe-samples/src/main/resources/example-schema.xml"

    java -Dsbe.xinclude.aware=true \
        -Dsbe.generate.ir=true \
        -Dsbe.target.language=Rust \
        -Dsbe.target.namespace=sbe \
        -Dsbe.output.dir=generated/rust \
        -Dsbe.errorLog=yes \
        -jar "$pathToDownloadedJarFile" \
        "pathToSchemaFile"
    ```

## More information

See upstream for main documentation: https://github.com/real-logic/simple-binary-encoding.

See also earlier decision not to use external templating
engine: https://github.com/real-logic/simple-binary-encoding/issues/28.

## TODO

- [ ] Add zero-copy implementation for Go. See issue: https://github.com/real-logic/simple-binary-encoding/issues/765
- [ ] Consider rewriting the Rust generation code from ground up. Currently, the code is based on the previous
  implementation, with is in turn based on the Java implementation, which is not template-based (at least not with
  mustache in mind).
- [ ] Publish artifact to Maven.
