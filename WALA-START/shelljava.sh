#!/bin/sh
echo "---------start---------"
# ./shelljava.sh abc def
# $2 -> def
OUTPUT_FOLDER="parser_output"
TARGET="$1"
FILENAME="$(basename $1)"
LIBS="src/main/java/kkkjjjmmm/slicer/ProbUtil.java"
LIBS1="src/main/java/org/apache/commons/math3"
#LIBS1="commons-math3-3.6.1-src/src/main/java/org/apache/commons/math3/distribution"
#LIBS2="commons-math3-3.6.1-src/src/main/java/org/apache/commons/math3/exception"
#LIBS3="commons-math3-3.6.1-src/src/main/java/org/apache/commons/math3/util"
#DESTINATION="src/main/java/kkkjjjmmm/modified/${FILENAME}"

rm -r $OUTPUT_FOLDER
mkdir $OUTPUT_FOLDER
mkdir -p "$OUTPUT_FOLDER/kkkjjjmmm/slicer"
mkdir -p "$OUTPUT_FOLDER/org/apache/commons"
#mkdir -p "$OUTPUT_FOLDER/org/apache/commons/math3"
cp $LIBS "$OUTPUT_FOLDER/kkkjjjmmm/slicer/ProbUtil.java"
cp -r $LIBS1 "$OUTPUT_FOLDER/org/apache/commons/math3"
#cp -r $LIBS1 "$OUTPUT_FOLDER/org/apache/commons/math3/distribution"
#cp -r $LIBS2 "$OUTPUT_FOLDER/org/apache/commons/math3/exception"
#cp -r $LIBS3 "$OUTPUT_FOLDER/org/apache/commons/math3/util"
java -jar doparser.jar $TARGET $OUTPUT_FOLDER

cd $OUTPUT_FOLDER
find . -name "*.java" | xargs javac  
jar -cf transformed.jar *

echo "--------finished-------"

