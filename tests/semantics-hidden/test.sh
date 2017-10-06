#!/bin/sh

runsemantics() {
  $(git rev-parse --show-toplevel)/run.sh -t inter $1;
}

exitcode=0;
fail=0;
count=0;

for file in `dirname $0`/illegal/*; do
  if runsemantics $file > /dev/null 2>&1; then
    echo "Illegal file $file semantic checked successfully.";
    exitcode=1;
    fail=$((fail+1));
  else
    echo "Test $file passed";
  fi
  count=$((count+1));
done

for file in `dirname $0`/legal/*; do
  if ! runsemantics $file; then
    echo "Legal file $file failed to pass semantic checks.";
    exitcode=1;
    fail=$((fail+1));
  else
    echo "Test $file passed";
  fi
  count=$((count+1));
done

echo "Failed $fail tests out of $count";
exit $fail;
