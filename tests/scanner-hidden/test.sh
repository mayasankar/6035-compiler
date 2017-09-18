#!/bin/sh

runscanner() {
    curdir=$PWD
    cd `dirname $1`
    $(git rev-parse --show-toplevel)/run.sh -t scan `basename $1`
    cd $curdir
}

exitcode=0
fail=0
count=0

for file in `dirname $0`/input/*; do
  output=`tempfile`
  runscanner $file > $output 2>&1;
  if ! diff -u $output `dirname $0`/output/`basename $file`.out; then
    echo "File $file scanner output mismatch.";
    exitcode=1
    fail=$((fail+1))
  fi
  count=$((count+1))
  rm $output;
done

echo "Failed $fail tests out of $count";
exit $exitcode;
