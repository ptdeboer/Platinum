#!/bin/bash 
###
# (C) 2005-2011 Virtual Laboratory for e-Science (VL-e)
# (C) 2012-2014 Netherlands eScience Center
# (C) 2015-2020 Piter.NL
#
# file  : vbrowser.sh: 
# info  : VBrowser startup script. 
# author: Piter T. de Boer
#

##
# settings 
# defaults:
JAVA=java
VBROWSER_CLASS=nl.esciencecenter.ptk.vbrowser.ui.StartVBrowser

##
# VBROWSER_SYSCONFDIR 
# Set the following variable when the configuration files or NOT under $VBROWSER_INSTALL/etc ! 
# This is automatically done when performing a binary installation 
### Autoconfiguration START ###
# VBROWSER_SYSCONFDIR=@VBROWSER_SYSCONFDIR@
### Autoconfiguration END ###

## 
# Bootstrap check for installation

# No ${VBROWSER_SYSCONFDIR} => default startup from ${INSTALL_DIR}/bin/
if [ -z "${VBROWSER_SYSCONFDIR}" ] ; then
   DIRNAME=`dirname "$0"`
   BASE_DIR=`cd "${DIRNAME}/.."; pwd`
else
   DIRNAME=`dirname "$0"`
   BASE_DIR=`cd "${DIRNAME}"/ ; pwd`
fi

# Bootstrap if not provided -> assume startup from bin/ directory
VBROWSER_SYSCONFDIR="${VBROWSER_SYSCONFDIR-${BASE_DIR}/etc}"

# Check configuration environment: VBROWSER_SYSCONFDIR overrules VBROWSER_INSTALL which overrules BASE_DIR 
if [ -f "$VBROWSER_SYSCONFDIR/vbrowser_env.sh" ] ; then
	source "$VBROWSER_SYSCONFDIR/vbrowser_env.sh"
elif [ -f "$VBROWSER_INSTALL/etc/vbrowser_env.sh" ] ; then
	source "$VBROWSER_INSTALL/etc/vbrowser_env.sh"
elif [ -f "$BASE_DIR/etc/vbrowser_env.sh" ] ; then
	source "$BASE_DIR/etc/vbrowser_env.sh"
else
   echo "Not using etc/vbrowser_env.sh (file not found)"
   # Continue with defaults 
fi 

# Defaults for etc/, bin/, lib/
VBROWSER_INSTALL="${VBROWSER_INSTALL-${BASE_DIR}}"
VBROWSER_LIBDIR="${VBROWSER_LIBDIR-${BASE_DIR}/lib}"
VBROWSER_BINDIR="${VBROWSER_BINDIR-${BASE_DIR}/bin}"
VBROWSER_SYSCONFDIR="${VBROWSER_SYSCONFDIR-${BASE_DIR}/etc}"

CLASSPATH="${VBROWSER_INSTALL}:${VBROWSER_SYSCONFDIR}:${VBROWSER_BINDIR}:${VBROWSER_LIBDIR}:${VBROWSER_LIBDIR}/*"

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
echo "VBROWSER_LIBDIR      ="$VBROWSER_LIBDIR 
echo "VBROWSER_SYSCONFDIR  ="$VBROWSER_SYSCONFDIR 
echo "BOOTSTRAP_JAR        ="$BOOTSTRAP_JAR 
echo "JAVA_HOME            ="$JAVA_HOME 
echo "CLASSPATH            ="$CLASSPATH
echo "VBROWSER_CLASS       ="$VBROWSER_CLASS
echo "Command line options ="$OPTS

# bootstrap class sets up real environment, classpath is optional:
echo "$JAVA" -cp "$CLASSPATH" -Dvbrowser.install.sysconfdir="$VBROWSER_SYSCONFDIR" "$VBROWSER_CLASS" $OPTS
"$JAVA" -cp "$CLASSPATH" -Dvbrowser.install.sysconfdir="$VBROWSER_SYSCONFDIR" "$VBROWSER_CLASS" $OPTS

