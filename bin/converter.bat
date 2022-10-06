@rem # Shell file to run Xj3D's CADFilter (format converter) using Apache Ant
@echo off
@echo Running Xj3D Format Converter
@echo cadfilter.args %1 %2 %3 %4 %5 %6 %7
ant -Dcadfilter.args="%1 %2 %3 %4 %5 %6" run.cadfilter