#!/usr/bin/env bash

#
# Script file name must be .jar or .zip Caused by Scala REPL limitation
#
echo "gen_shell.sh called"
cat digdag-shell/src/main/sh/selfrun.sh digdag-shell/target/scala-2.13/digdag-shell-assembly.jar > target/digdag-shell.jar
chmod +x target/digdag-shell.jar
