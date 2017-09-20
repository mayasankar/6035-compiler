#!/bin/bash

runparser() {
  $(git rev-parse --show-toplevel)/run.sh -t parse `pwd`/$1
}

fail=0

for file in `dirname $0`/illegal/*; do
  if runparser > /dev/null 2>&1 $file; then
    let "fail += 1"
    echo "Failed test $file"
  fi
done

for file in `dirname $0`/legal/*; do
  if ! runparser $file; then
    let "fail += 1"
    echo "Failed test $file"
  fi
done

exit $fail;
