#!/bin/sh
# Shell file to run Xj3D using Apache Ant
SCRIPTDIR=`dirname $0`
cd $SCRIPTDIR
ant -Dscene=$1 run
