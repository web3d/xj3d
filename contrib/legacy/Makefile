#
# Top Level makefile for the Xj3D codebase
#
#  (C) 2001 - 2007 Web3d Consortium
#    http://www.web3d.org/
#    http://www.xj3d.org
#
# This makefile is designed to build the entire library from scratch. It is
# not desigend as a hacking system. It is recommended that you use the normal
# javac/CLASSPATH setup for that. 
#
# Author: Justin Couch
# Version: $Revision: 1.7 $

ifndef PROJECT_ROOT
export PROJECT_ROOT=$(PWD)
endif

include $(PROJECT_ROOT)/make/Makefile.inc

# Default instruction is to print out the help list
help:
	$(PRINT) 
	$(PRINT) "                   The Xj3D Project"
	$(PRINT) 
	$(PRINT) "More information on this project can be found at http://www.xj3d.org"
	$(PRINT) 
	$(PRINT) "The following options are offered and will build the entire codebase:"
	$(PRINT) 
	$(PRINT) "parsers:     Build the parser java source files using javacc"
	$(PRINT) "class:       Compile just the classes (OGL-only). Don't make JAR files."
	$(PRINT) "bin:         Build parsers and classes"
	$(PRINT) "examples:    Build the examples. Doesn't build the core libraries (Doesn't work yet)"
	$(PRINT) "images:      Build the images."
	$(PRINT) "xml:         Build the xml dependencies."
	$(PRINT) "config:      Build the config file system."
	$(PRINT) "jar:         Make the java JAR file"
	$(PRINT) "parserdoc:   Generate BNF documentation for the parser"
	$(PRINT) "javadoc:     Generate the javadoc information"
	$(PRINT) "docs:        Generate both parser and javadoc files"
	$(PRINT) "all:         Build everything (including docs)"
	$(PRINT) "install:     Build everything then put the JARs in the right place"
	$(PRINT) "clean:       Blow all the library classes away"
	$(PRINT) "nuke:        Blow both lib and example code away"
	$(PRINT) 
	$(PRINT) "The following allow you to build only the files needed by a"
	$(PRINT) "specific renderer, without compiling the others."
	$(PRINT) 
	$(PRINT) "ogl:         OpenGL classes"
	$(PRINT) "ogl-jar:     OpenGL bin and jar files"
	$(PRINT) "ogl-javadoc: OpenGl javadoc"
	$(PRINT) 
	$(PRINT) "null:         Null renderer classes"
	$(PRINT) "null-jar:     Null renderer bin and jar files"
	$(PRINT) "null-javadoc: Null renderer javadoc"
	$(PRINT) 

all: examples jar docs javadoc

bin: parsers class properties
docs: parserdoc javadoc

class: ogl

#	make -f $(JAVA_DIR)/Makefile buildall

properties:
	make -f $(JAVA_DIR)/Makefile properties

parsers:
	make -f $(PARSER_DIR)/Makefile buildall

parserdoc:
	make -f $(PARSER_DIR)/Makefile docall

javadoc: ogl-javadoc

#	make -f $(JAVA_DIR)/Makefile javadoc
#
jar: ogl-jar

#jar: imagesjar xmljar configjar bin
#	make -f $(JAVA_DIR)/Makefile jar

examples:
	cd $(EXAMPLES_DIR) && make buildall

images:
	make -f $(IMAGES_DIR)/Makefile buildall

imagesjar: images
	make -f $(IMAGES_DIR)/Makefile jar

xml:
	make -f $(XML_DIR)/Makefile buildall

xmljar: xml
	make -f $(XML_DIR)/Makefile jar

config:
	make -f $(CONFIG_DIR)/Makefile buildall

configjar: config
	make -f $(CONFIG_DIR)/Makefile jar

clean:
	make -f $(JAVA_DIR)/Makefile clean


#
# OpenGL-specific renderer
#
ogl:
	make -f $(JAVA_DIR)/Makefile-opengl buildall

ogl-javadoc:
	make -f $(JAVA_DIR)/Makefile-opengl javadoc

ogl-jar: ogl images xml config
	make -f $(JAVA_DIR)/Makefile-opengl jar


#
# Null only-specific renderer
#
null:
	make -f $(JAVA_DIR)/Makefile-null buildall

null-javadoc:
	make -f $(JAVA_DIR)/Makefile-null javadoc

null-jar: null images xml config
	make -f $(JAVA_DIR)/Makefile-null jar
