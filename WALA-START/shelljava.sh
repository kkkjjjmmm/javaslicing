#!/bin/sh
echo "---------start---------"
# ./shelljava.sh abc def
# $2 -> def
OUTPUT_FOLDER="parser_output"
TARGET="$1"
FILENAME="$(basename $1)"
LIBS="src/main/java/kkkjjjmmm/slicer/ProbUtil.java"
DESTINATION="src/main/java/kkkjjjmmm/modified/${FILENAME}"

#MAINCLASS=$2 # "Lkkkjjjmmm/test/Example"
#SRCCALLER=$3 # "SliceL"
#SRCCALLEE=$4 # "fake"

rm -r $OUTPUT_FOLDER
mkdir $OUTPUT_FOLDER
mkdir -p "$OUTPUT_FOLDER/kkkjjjmmm/slicer"
cp $LIBS "$OUTPUT_FOLDER/kkkjjjmmm/slicer/ProbUtil.java"

java -jar doparser.jar $TARGET $OUTPUT_FOLDER

cd $OUTPUT_FOLDER
find . -name "*.java" | xargs javac  
jar -cvf transformed.jar *

echo "--------finished-------"

