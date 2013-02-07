#!/bin/sh

runscanner() {
  java -jar `dirname $0`/../../dist/Compiler.jar \
    -target scan -compat $1
}

fail=0

echo "START"
for file in `dirname $0`/input/*; do
  echo $file
  output=`tempfile`
  echo "boop"
  runscanner $file > $output;
  echo "bop"
  if ! diff -u $output `dirname $0`/output/`basename $file`.out; then
    echo "File $file scanner output mismatch.";
    fail=1
  fi
#  rm $output;
  break
done

exit $fail;
