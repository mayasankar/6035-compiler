#!/bin/sh

runcompiler() {
  $(git rev-parse --show-toplevel)/run.sh --target=assembly -o $2 $1
}

runcompiler_opt() {
  $(git rev-parse --show-toplevel)/run.sh --opt=all --target=assembly -o $2 $1
}

fail=0

if ! gcc -v 2>&1 |grep -q '^Target: x86_64-linux-gnu'; then
  echo "Refusing to run cross-compilation on non-64-bit architecture."
  exit 0;
fi

  cd `dirname $0`
  orig_pwd=$PWD

  workingdir=`mktemp -d`
  file="./input/derby.dcf"
  progname=derby
  orig_input="./data/input.ppm"
  input="${workingdir}/input.ppm"
  golden="${orig_pwd}/output/golden.ppm"
  binary="${workingdir}/${progname}"
  asm="${workingdir}/${progname}.s"
  output="${workingdir}/output.ppm"
  timing_gcc="${workingdir}/${progname}_gcc_opt.timing"
  timing_dcf_unopt="${workingdir}/${progname}_dcf_unopt.timing"
  timing_dcf_fullopt="${workingdir}/${progname}_dcf_fullopt.timing"

  echo "Unoptimized"
  cp $orig_input $input;
  msg=""
  if runcompiler $file $asm; then
    if gcc -o $binary -L./lib $asm -l6035 -lpthread; then
      cd $workingdir
      if $binary > $timing_dcf_unopt; then
        if ! diff -q $output $golden > /dev/null; then
          msg="File $file output mismatch.";
        fi
      else
        msg="Program failed to run.";
      fi
    else
      msg="Program failed to assemble.";
    fi
  else
    msg="Program failed to generate assembly.";
  fi
  echo $msg

  cd "$orig_pwd";
  echo "Fullopt"
  cp $orig_input $input;
  msg=""
  if runcompiler_opt $file $asm; then
    if gcc -o $binary -L./lib $asm -l6035 -lpthread; then
      cd $workingdir
      if $binary > $timing_dcf_fullopt; then
        if ! diff -q $output $golden > /dev/null; then
          msg="File $file output mismatch.";
        fi
      else
        msg="Program failed to run.";
      fi
    else
      msg="Program failed to assemble.";
    fi
  else
    msg="Program failed to generate assembly.";
  fi
  echo $msg

  cd "$orig_pwd";
  cp $orig_input $input;
  cd $workingdir
  $orig_pwd/derby_gcc > $timing_gcc

    if [ ! -z "`cat $timing_gcc`" ]; then
      echo -n "GCC: "
      gcc=`cat $timing_gcc|awk '{print($2)}'`
      echo "${gcc} usec"
    fi
    if [ ! -z "`cat $timing_dcf_unopt`" ]; then
      echo -n "Unoptimized: "
      dcf_unopt=`cat $timing_dcf_unopt|awk '{print($2)}'`
      echo "${dcf_unopt} usec"
  int_speedup=$(($dcf_unopt / $gcc))
  dec_speedup=$((($dcf_unopt * 1000) / $gcc - ($int_speedup * 1000)))
  printf "%d.%03dx slower than gcc\n" $int_speedup ${dec_speedup}
    fi
    if [ ! -z "`cat $timing_dcf_fullopt`" ]; then
      echo -n "Fullopt: "
      dcf_fullopt=`cat $timing_dcf_fullopt|awk '{print($2)}'`
      echo "${dcf_fullopt} usec"
  int_speedup=$(($dcf_fullopt / $gcc))
  dec_speedup=$((($dcf_fullopt * 1000) / $gcc - ($int_speedup * 1000)))
  printf "%d.%03dx slower than gcc\n" $int_speedup ${dec_speedup}
    fi
  #fi

  cd "$orig_pwd";
  rm -r -f $workingdir;

exit $fail;
