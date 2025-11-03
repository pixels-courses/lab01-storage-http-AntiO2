#!/bin/bash
set -e

POM_FILE="${JAVA_DIR}/pom.xml"

if [ ! -f "$POM_FILE" ]; then
    echo "❌ Error: $POM_FILE not found!"
    exit 1
fi

mvn -f "${POM_FILE}" install -DskipTests
COMPILE_RESULT=$?
if [ $COMPILE_RESULT -ne 0 ]; then
    echo "Install Project Failed"
    echo "[SCORE] 0"
    exit 0
fi


if grep -q "<skipTests>true</skipTests>" "$POM_FILE"; then
    echo "🔧 Updating <skipTests>true</skipTests> → <skipTests>false</skipTests> ..."
    sed -i.bak 's|<skipTests>true</skipTests>|<skipTests>false</skipTests>|g' "$POM_FILE"
    echo "✅ Updated successfully!"
else
    echo "ℹ️ No <skipTests>true</skipTests> found in $POM_FILE, nothing to change."
fi

STORAGE_HTTP=${JAVA_DIR}/mini-storage-http
LAB_POM_FILE="${STORAGE_HTTP}/pom.xml"

echo "========== Running Basic Test =========="
mvn -f "${LAB_POM_FILE}" -Dtest=TestHttpStream -Dsurefire.jvm.args="${MAVEN_OPTS}" test
BASIC_RESULT=$?
echo "Basic Test Done"
echo "BASIC_RESULT=$BASIC_RESULT"

if [ $BASIC_RESULT -ne 0 ]; then
    echo "Basic test failed"
    echo "[SCORE] 0"
    exit 0
fi

echo "========== Running Perf Test =========="
echo "Running performance test..."
PERF_OUTPUT=$(mvn -f "${LAB_POM_FILE}" -Dtest=TestHttpStreamPerf#testPhysicalReaderAndWriterPerf -Dsurefire.jvm.args="${MAVEN_OPTS}" test)
echo $PERF_OUTPUT
SCORE=$(echo "$PERF_OUTPUT" | grep "\[SCORE\]" | awk '{print $2}')
echo "[SCORE] $SCORE"
