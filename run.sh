#!/bin/sh
gitroot=$(git rev-parse --show-toplevel)
java_tool_options="$JAVA_TOOL_OPTIONS"
unset JAVA_TOOL_OPTIONS
java $java_tool_options -jar $gitroot/dist/Compiler.jar "$@"
