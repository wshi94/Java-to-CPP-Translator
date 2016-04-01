#!/bin/sh
# This script translates all 20 input files
# and checks if they output correctly

echo "-------------------- Starting Script --------------------"

#variables
PASSED=0
FAILED=0

# change to script directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

# compile into jar
rm -f translator-assembly-1.0.jar
sbt assembly
clear
mv target/scala-2.11/translator-assembly-1.0.jar .

# recreate cpp output
rm -f -r output/cpp/
mkdir -p output/cpp/

echo "------------------- Generating output -------------------"

# test each input
for var in {0..50};
do

# translation
if [ $var -le 20 ]; then
	java -cp translator-assembly-1.0.jar edu.nyu.oop.Boot -translate src/test/java/inputs/test$(printf %03d $var)/Test$(printf %03d $var).java
else
	java -cp translator-assembly-1.0.jar edu.nyu.oop.Boot -translate src/test/java/inputs2/test$(printf %03d $var)/Test$(printf %03d $var).java
fi
# compile translated c++ files
rm -f output/compile.out
g++ -Wall output/main.cpp output/java_lang.cpp output/output.cpp -o output/a.out &> output/compile.out

# check for differences
printf "Test%03d: " $var
#./a.out | ./input_diff java/test$var.txt

# run cpp translated program
output/a.out > output/cpp/test$(printf %03d $var).txt

# test 9, 13, 15, 17 prints a memory address so we just use wildcards for the output
if [ $var -eq 9 -o $var -eq 13 -o $var -eq 15 -o $var -eq 17 ]; then

	output_str=$(cat output/cpp/test$(printf %03d $var).txt)
	
	# check for compile errors
	if [ -s output/compile.out ]; then
		printf "Compile error\n"
		FAILED=$((FAILED+1))
	elif [[ $output_str == "inputs.test"* ]]; then
		printf "Test passed\n"
		PASSED=$((PASSED+1))
	else 
		printf "Test failed\n"
		FAILED=$((FAILED+1))
	fi

else
	# compare
	if [ -s output/compile.out ]; then
		printf "Compile error\n"
		FAILED=$((FAILED+1))
	elif (cmp -s output/java/test$(printf %03d $var).txt output/cpp/test$(printf %03d $var).txt);  then
		printf "Test passed\n"
		PASSED=$((PASSED+1))
	else	
		printf "Test failed\n"
		FAILED=$((FAILED+1))
		diff -y output/java/test$(printf %03d $var).txt output/cpp/test$(printf %03d $var).txt
	fi
fi

echo
done

printf "Passed tests: %d \t Failed tests: %d\n", $PASSED $FAILED

echo "-------------------- Finished script --------------------"
