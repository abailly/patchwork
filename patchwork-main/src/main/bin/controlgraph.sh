#!/bin/sh
# launcher shell script for Control Graph GUI
# environment variables expected:
# JAVA_HOME= Path to  JDK 

if [ z$JAVA_HOME = "z" ]; then
  echo "Variable JAVA_HOME is not set"
  exit 1
fi

if [ ! -x $JAVA_HOME/bin/java ]; then 
  echo "Cannot find java executable"
  exit 1
fi

# lookup directories
SCRIPT_DIR=`dirname $0`
LIB_DIR=$SCRIPT_DIR/../lib
JAR=$SCRIPT_DIR/../patchwork-main-${version}.jar

JAVA=$JAVA_HOME/bin/java

$JAVA "-Dlauncher.libdir=$LIB_DIR" -Dlauncher.main=oqube.patchwork.gui.Main -jar "$JAR" $@