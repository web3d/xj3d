#!/bin/sh
# Shell file to run Xj3D's CADFilter (format converter) using Apache Ant
SCRIPTDIR=`dirname $0`
cd $SCRIPTDIR
echo cadfilter.args $1 $2 $3 $4 $5 $6 $7
ant -Dcadfilter.args="$1 $2 $3 $4 $5 $6" run.cadfilter