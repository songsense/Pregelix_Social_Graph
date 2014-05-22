#!/bin/bash
#
# Read given number of lines from a large file.

if [ $# != 1 ]; then
    echo 'Usage: readline.sh "line_number" < inputfile > outputfile'
    exit
fi

i=0
while read LINE
do
    echo $LINE
    i=$((i+1))
    if [ $i -ge $1 ]; then
        exit
    fi
done
