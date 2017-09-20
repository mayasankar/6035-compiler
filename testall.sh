#!/bin/bash

echo "Scanner Tests"
./tests/scanner/test.sh
echo "Parser Tests";
./tests/parser/test.sh
echo "Hidden Scanner Tests";
./tests/scanner-hidden/test.sh;
echo "Hidden Parser Tests";
./tests/parser-hidden/test.sh
