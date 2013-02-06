#!/bin/sh

base=`dirname $0`/..

echo 'Checking for indent width of two spaces.'
find $base/src -name "*.java" |
    xargs $base/lib/indent_finder.py | grep -v -E ': space 2$'

echo 'Detecting trailing whitespace.'
find $base/src -name "*.java" |
    xargs grep -n -E '\S+\s+$'

echo 'Detecting tabs.'
find $base/src -name "*.java" |
    xargs grep -n -E '	';

echo 'Detecting >80c.'
find $base/src -name "*.java" |
    xargs grep -n -E '.{81,}';

exit 0;
