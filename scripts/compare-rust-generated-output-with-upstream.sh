#!/usr/bin/env bash

# This script performs the following actions:
# 1. Download upstream version of sbe-all-VERSION.jar (version is hardcoded)
# 2. Generate SBE codecs for Rust programming language using the upstream jar file
# 3. Generate SBE codecs for Rust programming language using the local sbe-all
#    jar file (running ./gradlew build if necessary)
# 4. Compares the output from steps 2 and 3 above, igonring trailing whitespace
#    and blank lines

set -euo pipefail

scriptDir=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
projectDir=$( dirname "$scriptDir" )

pushd "$scriptDir"

# Download upstream jar file
upstreamVersion="1.27.0"
jarFileName=sbe-all-$upstreamVersion.jar

[[ -f $jarFileName ]] || \
    curl -LO https://search.maven.org/remotecontent?filepath=uk/co/real-logic/sbe-all/$upstreamVersion/$jarFileName

# Generate upstream
java -Dsbe.xinclude.aware=true \
    -Dsbe.generate.ir=true \
    -Dsbe.target.language=Rust \
    -Dsbe.target.namespace=sbe \
    -Dsbe.output.dir="$projectDir"/generated/upstream/rust \
    -Dsbe.errorLog=yes \
    -jar sbe-all-1.27.0.jar \
    "$projectDir"/sbe-samples/src/main/resources/example-schema.xml \
    "$projectDir"/sbe-samples/src/main/resources/example-extension-schema.xml \
    "$projectDir"/sbe-benchmarks/src/main/resources/fix-message-samples.xml \
    "$projectDir"/sbe-benchmarks/src/main/resources/car.xml

# Build local
pushd "$projectDir"
[[ -f  "$projectDir"/sbe-all/build/libs/sbe-all-1.27.1-SNAPSHOT.jar ]] ||\
    ./gradlew build
popd

# Generate local
java -Dsbe.xinclude.aware=true \
    -Dsbe.generate.ir=true \
    -Dsbe.target.language=Rust \
    -Dsbe.target.namespace=sbe \
    -Dsbe.output.dir="$projectDir"/generated/local/rust \
    -Dsbe.errorLog=yes \
    -jar "$projectDir"/sbe-all/build/libs/sbe-all-1.27.1-SNAPSHOT.jar \
    "$projectDir"/sbe-samples/src/main/resources/example-schema.xml \
    "$projectDir"/sbe-samples/src/main/resources/example-extension-schema.xml \
    "$projectDir"/sbe-benchmarks/src/main/resources/fix-message-samples.xml \
    "$projectDir"/sbe-benchmarks/src/main/resources/car.xml

# Diff
find "$projectDir"/generated/upstream -regextype egrep -iregex '.*.rs|.*.toml' |\
    while read f
    do
        diff --ignore-trailing-space --ignore-blank-lines "$f" "${f/upstream/local}" ||\
            echo "Differences found between $f and ${f/upstream/local} (see above)"
    done

popd
