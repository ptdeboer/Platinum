#!/bin/bash
##

deplogger() {
   file="$1"
   echo deplogging: "${file}"
   cat "${file}" | sed \
      -e "s/\(.*[a-z]*Printf.*\)\(%[a-z]\)/\1{}/g" \
      -e "s/\(.*[a-z]*Printf.*\)\(%[a-z]\)/\1{}/g" \
      -e "s/\(.*[a-z]*Printf.*\)\(%[a-z]\)/\1{}/g" \
      -e "s/\(.*[a-z]*Printf.*\)%\([0-9]*\)\([a-z]\)/\1{\2}/g" \
      -e "s/\(.*[a-z]*Printf.*\)%\([0-9]*\)\([a-z]\)/\1{\2}/g" \
      -e "s/\(.*[a-z]*Printf.*\)%\([0-9]*\)\([a-z]\)/\1{\2}/g" \
      -e "s/\(.*[a-z]*Printf.*\)\(\\\\n\"\)/\1\"/" \
      -e "s/{{/\[{/g" -e "s/}}/}\]/g" \
      -e "s/\(logger\.\)\(trace\|debug\|info\|warn\|error\)\(Printf(\)\(.*\)/log.\2(\4/" \
      -e "s/\(monitor\.logPrintf(\)/monitor.log(/" \
      -e "s/\(monitorLogPrintf(\)/monitorLog(/" \
      -e "s/\([ \t]*\)\([a-zA-Z]*\)\(Printf(\)\(.*\)/\1log.\2(\4/" \
      > "${file}.new"
}

# Arguments:

DIR="$1"
if [ -z "${DIR}" ]  ; then
    DIR="."
fi

# Find all
find "${DIR}" -name *.java -print | ( while read file ; do
    deplogger "${file}"
    diff "${file}" "${file}.new"
    RESULT=$?
    case "$RESULT" in
        0)
            echo "No changes. Continue ? Y(es)/q(uit)"
            DIFF=0
            ;;
        *)
            echo "Change ? Y(es)/s(kip)/q(uit)"
            DIFF=1
            ;;
    esac
    read ans < /dev/tty
    CHANGE=0
    case "$ans" in
         [Qq])
                exit 1;
                ;;
         [Cc])
                exit 1;
                ;;
         [NnSs])
                echo "*** Skipping:"$file
                CHANGE=0
                ;;
         ""|[Yy])
                CHANGE=${DIFF}
                ;;
          *)
                echo "*** Error"
                exit 1
                ;;
    esac
    if [ "$CHANGE" == "1" ] ; then 
                echo "*** Changing:"$file
		mv -vf "${file}" "${file}.old" 
                mv -vf "${file}.new" "${file}"
                echo "" 
    fi
done)
 
