#!/bin/bash

echo "Scanner Tests";
./scanner/test.sh;
echo "Parser Tests";
./parser/test.sh
echo "Hidden Scanner Tests";
./scanner-hidden/test.sh;
echo "Hidden Parser Tests";
./parser-hidden/test.sh