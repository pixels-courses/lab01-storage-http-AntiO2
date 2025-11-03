#!/bin/bash
set -e

# 获取 entry.sh 所在目录的绝对路径
GRADING_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

export PIXLES_DIR="$(dirname "$(dirname ${GRADING_DIR})" )"
export JAVA_DIR=${PIXLES_DIR}/java
export MAVEN_OPTS="-Xmx6g"

if [ $# -lt 2 ]; then
    echo "Usage: $0 <coursename> <labname>"
    echo "Example: $0 2025-fall lab1"
    exit 1
fi

COURSE_NAME="$1"
LAB_NAME="$2"

TARGET="${GRADING_DIR}/${COURSE_NAME}/${LAB_NAME}.sh"

if [ ! -f "$TARGET" ]; then
    echo "❌ Lab script not found: $TARGET"
    exit 1
fi

# 执行目标脚本
echo "▶ Running $TARGET"
bash "$TARGET"


if [ $? -ne 0 ]; then
    echo "Test failed"
    echo "[SCORE] 0"
    exit 0
fi

echo "Test Done"

