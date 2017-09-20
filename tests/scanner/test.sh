#!/bin/sh

runscanner() {
    curdir=$PWD
    cd `dirname $1`
    $(git rev-parse --show-toplevel)/run.sh -t scan `basename $1`
    cd $curdir
}

tempfile() {
  TMPDIR=`pwd` mktemp -t tmp
}

exitcode=0
fail=0
count=0

output=`tempfile`
for file in `dirname $0`/input/*; do
  runscanner $file > $output 2>&1;
  if ! diff -u $output `dirname $0`/output/`basename $file`.out; then
    echo "File $file scanner output mismatch.";
    exitcode=1
    fail=$((fail+1))
    if [ $fail -gt 1 ]; then
      echo Got through $((count+1)) tests before quitting.
      exit 1
    fi
  fi
  count=$((count+1))
  rm $output;
done

echo "Failed $fail tests out of $count";
exit $exitcode;
