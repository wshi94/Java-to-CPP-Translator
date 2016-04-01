#!/bin/sh
# This script currently compiles each java source file, runs it,
# and saves the output to file.

echo "-------------------- Starting Script --------------------"
echo "------------------- Generating output -------------------"

# change to script directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
OUTPUT="/output/java/"
cd $DIR

# set up output location
mkdir -p output/java/
cd src/test/java/inputs


echo "-------------------- Starting input set 1 --------------------"

#Loop for every input java file in the format testxxx.java
for var in {0..20}
do
cd "test$(printf %03d $var)"

# compile java source files
javac Test$(printf %03d $var).java
cd ..
cd ..
FILE="test$(printf %03d $var).txt"

# run java program and store output to file
java inputs.test$(printf %03d $var).Test$(printf %03d $var) > $FILE

# move file to output directory
mv $FILE "$DIR$OUTPUT"
cd inputs
done


echo "-------------------- Starting input set 2 --------------------"

# input set 2 is in a different folder, must navigate to that
cd ..
cd inputs2

for var in {21..50}
do
cd "test$(printf %03d $var)"

# compile java source file
javac Test$(printf %03d $var).java
cd ..
cd ..
FILE="test$(printf %03d $var).txt"

# run java program and store output to file
java inputs2.test$(printf %03d $var).Test$(printf %03d $var) > $FILE

# move file to output directory
mv $FILE "$DIR$OUTPUT"
cd inputs2
done

echo "-------------------- Finished script --------------------"