#!/bin/sh
# ******************************************************************
# dos2unix.sh
# This script performs a recursive stripping of carriage return
# characters, but preserves the '\n' line feed character
#
# Line ending characters = Win: \r\n  Mac: \r  Unix: \n
# ******************************************************************

echo "*****************"
echo "* DOS 2 UNIX .1 *"
echo "*****************"

#for DIR in $(find . -type f -name Root -or -name Entries -or -name Entries.\* -or -name Entries.\*.\* -or -name Repository)
for DIR in $(find . -type f -path $1 -print0)
  do
    cat $DIR | tr -d '\r'
  done

echo " *** DOS 2 UNIX Complete! *** "