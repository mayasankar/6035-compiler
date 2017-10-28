#!/bin/sh

runcompiler() {
    $(git rev-parse --show-toplevel)/run.sh --opt=all --target=assembly -o $2 $1
}

exit=0;
fail=0;
count=0;

if ! gcc -v 2>&1 |grep -q '^Target: x86_64-linux-gnu'; then
  echo "Refusing to run cross-compilation on non-64-bit architechure."
  exit 0;
fi

for file in `dirname $0`/input/*.dcf; do
  asm=`mktemp --suffix=.s`
  msg=""
  if runcompiler $file $asm; then
    binary=`mktemp`
    if gcc -o $binary -L `dirname $0`/lib -l6035 $asm; then
      output=`mktemp`
      echo "Running file $file"
      if timeout 10 $binary > $output; then
        diffout=`mktemp`
        if ! diff -u $output `dirname $0`/output/`basename $file`.out > $diffout; then
          msg="File $file output mismatch.";
        fi
      else
        msg="Program failed to run or exited unexpectedly.";
      fi
    else
      msg="Program failed to assemble.";
    fi
  else
    msg="Program failed to generate assembly.";
  fi
  if [ ! -z "$msg" ]; then
    fail=$(($fail+1))
    exit=1;
    echo $file
    if [ ! -z "$diffout" ]; then
      cat $diffout
    elif [ ! -z "$output" ]; then
      cat $output
    fi
    echo $msg
  else
    echo "Test $file passed"
  fi
  count=$((count+1));
  rm -f $diffout $output $binary $asm;
done

echo "Failed $fail tests out of $count";
exit $exit;
