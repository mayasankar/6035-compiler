#!/bin/sh
gitroot=$(git rev-parse --show-toplevel)
java -jar $gitroot/dist/Compiler.jar "$@"
