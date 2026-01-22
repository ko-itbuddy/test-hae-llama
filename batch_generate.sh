#!/bin/bash

# Configuration
ROOT_DIR="$(pwd)"
INPUT_PROJECT="${ROOT_DIR}/sample-projects/demo-app"
OUTPUT_PROJECT="${ROOT_DIR}/sample-projects/demo-app"
JAR_PATH="${ROOT_DIR}/common/build/libs/common-0.0.1-SNAPSHOT-boot.jar"

# Find all Java files, excluding DemoApplication
FILES=$(find "$INPUT_PROJECT/src/main/java" -name "*.java" | grep -v "DemoApplication.java")

mkdir -p "${ROOT_DIR}/batch_logs"

for FILE in $FILES; do
    echo "Processing $FILE..."
    FILE_NAME=$(basename "$FILE")
    LOG_FILE="${ROOT_DIR}/batch_logs/${FILE_NAME}.txt"
    
    # Run the generate command
    java -jar "$JAR_PATH" generate --input "$FILE" --output-project "$OUTPUT_PROJECT" > "$LOG_FILE" 2>&1
    
    if [ $? -eq 0 ]; then
        echo "✅ Finished $FILE_NAME"
    else
        echo "❌ Failed $FILE_NAME. See $LOG_FILE"
    fi
done