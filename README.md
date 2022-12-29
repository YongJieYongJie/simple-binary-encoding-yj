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
