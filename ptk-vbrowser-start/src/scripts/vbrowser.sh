#!/bin/bash 
###
# (C) 2012-2014 Netherlands eScience Center
# (C) 2015 Piter.NL 
#
# file  : vbrowser.sh: 
# info  : VBrowser startup script. 
# author: P.T. de Boer
#

##
# settings 

# defaults: 
VERSION="1.1.0-SNAPSHOT" 
BOOTSTRAP_JAR="ptk-vbrowser-start-${VERSION}.jar"
JAVA=java
VBROWSER_CLASS=nl.esciencecenter.ptk.vbrowser.ui.StartVRSBrowser

##
# VBROWSER_SYSCONFDIR 
# Set the following variable when the configuration files or NOT under $VBROWSER_INSTALL/etc ! 
# This is automaticaly done when performing a binary installation 
#VBROWSER_SYSCONFDIR=/etc/vbrowser

## 
# check bootstrap startup directory:
DIRNAME=`dirname "$0"`
BASE_DIR=`cd "$DIRNAME"/ ; pwd`
 
# Check configuration environment: VBROWSER_SYSCONFDIR overrules VBROWSER_INSTALL which overrules BASE_DIR 
if [ -f "$VBROWSER_SYSCONFDIR/etc/vbrowser_env.sh" ] ; then
	source "$VBROWSER_SYSCONFDIR/etc/vbrowser_env.sh"
elif [ -f "$VBROWSER_INSTALL/etc/vbrowser_env.sh" ] ; then
	source "$VBROWSER_INSTALL/etc/vbrowser_env.sh"
elif [ -f "$BASE_DIR/etc/vbrowser_env.sh" ] ; then
	source "$BASE_DIR/etc/vbrowser_env.sh"
else
   echo "Not using etc/vbrowser_env.sh (file not found)"
   # Continue with defaults 
fi 

###
# Startup 

#default startup command line: currently none:

if [ -z "$@" ] ;then
  echo "No arguments"
  # start with userhome
  #OPTS="file://$HOME" 
  #if [ $cygwin == "true" ] ; then 
  #   OPTS="file://"`cygpath -m $HOME` 
  #fi
else
  OPTS="$@"
fi

# explicit exports

export VBROWSER_INSTALL VBROWSER_SYSCONFDIR 

###
# default classpath:
#

echo "VBROWSER_INSTALL     ="$VBROWSER_INSTALL 
echo "VBROWSER_SYSCONFDIR  ="$VBROWSER_SYSCONFDIR 
echo "BOOTSTRAP_JAR        ="$BOOTSTRAP_JAR 
echo "JAVA_HOME            ="$JAVA_HOME 
echo "CLASSPATH            ="$CLASSPATH
echo "VBROWSER_CLASS       ="$VBROWSER_CLASS
echo "Command line options ="$OPTS

# bootstrap class sets up real enviromment: 
echo "$JAVA" -cp "$CLASSPATH" -Dvbrowser.install.sysconfdir="$VBROWSER_SYSCONFDIR" -jar "${BASE_DIR}/lib/${BOOTSTRAP_JAR}" "$VBROWSER_CLASS" $OPTS
"$JAVA" -cp "$CLASSPATH" -Dvbrowser.install.sysconfdir="$VBROWSER_SYSCONFDIR" -jar "${BASE_DIR}/lib/${BOOTSTRAP_JAR}" "$VBROWSER_CLASS" $OPTS

