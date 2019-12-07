#!/bin/bash
##
#

VERSION="$1"
SNAPSHOT="$2" 

usage()
{
   echo "usage: <RELEASE_VERSION> <NEW_SNAPSHOT>"
}

doMvn() {
 echo "MVN:$@"   
 mvn "$@"
}

doGit() { 
 echo "GIT:$@"   
 git "$@"
}

if [ -z "${VERSION}" ] ; then 
    usage
    exit 1 
fi

if [ -z "${SNAPSHOT}" ] ; then 
    usage
    exit 1 
fi

if [[ "${SNAPSHOT}" != *-SNAPSHOT ]] ; then 
    echo "SNAPSHOT version must end with '-SNAPSHOT'! but is:'"${SNAPSHOT}"'"
    exit 1
fi

doGit checkout develop
doMvn versions:set -DnewVersion=${VERSION}
doGit add pom.xml */pom.xml
doGit commit -m "New release version=${VERSION}"

doGit checkout release
doGit merge develop
doGit tag "v${VERSION}"
#doGit push origin release
#doGit push origin "v${VERSION}"

doGit checkout develop
doMvn versions:set -DnewVersion=${SNAPSHOT}
doGit add pom.xml */pom.xml
doGit commit -m "New develop version=${SNAPSHOT}"
#doGit push origin
