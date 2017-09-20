#!/bin/bash
OS="`uname`"
ROOT="$(git rev-parse --show-toplevel)/tests";
case $OS in
  Linux*|Darwin*)
    echo "Scanner Tests";
    $ROOT/scanner/test.sh;
    echo "Parser Tests";
    $ROOT/parser/test.sh;
    echo "Hidden Scanner Tests";
    $ROOT/scanner-hidden/test.sh;
    echo "Hidden Parser Tests";
    $ROOT/parser-hidden/test.sh;
    ;;
  CYGWIN*|Windows*)
    cd $ROOT;
    echo "Scanner Tests";
    bash scanner/test.sh;
    echo "Parser Tests";
    bash parser/test.sh;
    echo "Hidden Scanner Tests";
    bash scanner-hidden/test.sh;
    echo "Hidden Parser Tests";
    bash parser-hidden/test.sh;
    ;;
  *)
    echo "Error: Unknown $OS";
    ;;
esac