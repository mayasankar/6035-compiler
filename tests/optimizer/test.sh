#!/bin/sh

runcompiler_opt() {
  $(git rev-parse --show-toplevel)/run.sh --opt=all --target=assembly -o $2 $1
}

runcompiler_unopt() {
  $(git rev-parse --show-toplevel)/run.sh --target=assembly -o $2 $1
}

fail=0

if ! gcc -v 2>&1 |grep -q '^Target: x86_64-linux-gnu'; then
  echo "Refusing to run cross-compilation on non-64-bit architecture."
  exit 0;
fi

cd `dirname $0`
orig_pwd=$PWD
for file in $PWD/input/*.dcf; do
  workingdir=`mktemp -d`
  progname=`basename $file .dcf`
  input_filename="`echo $progname|cut -d_ -f1`.pgm"
  orig_input="${orig_pwd}/data/$input_filename"
  input="${workingdir}/$input_filename"
  golden="${orig_pwd}/output/${progname}.pgm"
  binary="${workingdir}/${progname}"
  asm="${workingdir}/${progname}.s"
  output="${workingdir}/${progname}.pgm"
  timing_opt="${workingdir}/${progname}_opt.timing"
  timing_unopt="${workingdir}/${progname}_unopt.timing"

  cp $orig_input $input;
  msg=""
  if runcompiler_opt $file $asm; then
    if gcc -o $binary -L${orig_pwd}/lib $asm -l6035 -lpthread; then
      cd $workingdir
      if timeout -sKILL 10 $binary > $timing_opt; then
        if ! diff -q $output $golden > /dev/null; then
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
  cd "$orig_pwd";
  if runcompiler_unopt $file $asm; then
    if gcc -o $binary -L${orig_pwd}/lib $asm -l6035 -lpthread; then
      cd $workingdir
      if timeout -sKILL 10 $binary > $timing_unopt; then
        if ! diff -q $output $golden > /dev/null; then
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
  echo $file
  if [ ! -z "$msg" ]; then
    fail=$(($fail+1))
    echo $msg
  else
    if [ ! -z "`cat $timing_unopt`" ]; then
      echo -n "Unoptimized: "
      unopt=`cat $timing_unopt|awk '{print($2)}'`
      echo "${unopt} usec"
    fi
    if [ ! -z "`cat $timing_opt`" ]; then
      echo -n "Optimized: "
      opt=`cat $timing_opt|awk '{print($2)}'`
      echo "${opt} usec"
    fi
  fi
  int_speedup=$(($unopt / $opt)) 
  dec_speedup=$((($unopt * 1000) / $opt - ($int_speedup * 1000))) 
  printf "%d.%03dx speedup\n" $int_speedup ${dec_speedup}

  cd "$orig_pwd";
  rm -r -f $workingdir;
done

exit $fail;
