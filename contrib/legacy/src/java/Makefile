#*********************************************************************
#
#  (C) 2001 - 2006 Web3D Consortium
#    http://www.web3d.org/
#
# Author: Justin Couch
# Version: $Revision: 1.165 $
#
#*********************************************************************

# Names of packages to be compiled. These are declared in the order that
# they are to be compiled in. We create one at a time
BUILD_ORDER = \
  vrml.eai \
  vrml.eai.event \
  vrml.eai.field \
  org.web3d.util \
  org.web3d.util.spatial \
  org.web3d.vrml.lang \
  org.web3d.x3d.sai \
  org.web3d.x3d.sai.cadgeometry \
  org.web3d.x3d.sai.core \
  org.web3d.x3d.sai.dis \
  org.web3d.x3d.sai.environmentaleffects \
  org.web3d.x3d.sai.environmentalsensor \
  org.web3d.x3d.sai.eventutilities \
  org.web3d.x3d.sai.geometry2d \
  org.web3d.x3d.sai.geometry3d \
  org.web3d.x3d.sai.geospatial \
  org.web3d.x3d.sai.grouping \
  org.web3d.x3d.sai.hanim \
  org.web3d.x3d.sai.interpolation \
  org.web3d.x3d.sai.keydevicesensor \
  org.web3d.x3d.sai.layering \
  org.web3d.x3d.sai.lighting \
  org.web3d.x3d.sai.navigation \
  org.web3d.x3d.sai.networking \
  org.web3d.x3d.sai.particlesystems \
  org.web3d.x3d.sai.pickingsensor \
  org.web3d.x3d.sai.pointingdevicesensor \
  org.web3d.x3d.sai.rendering \
  org.web3d.x3d.sai.rigidbodyphysics \
  org.web3d.x3d.sai.scripting \
  org.web3d.x3d.sai.shape \
  org.web3d.x3d.sai.sound \
  org.web3d.x3d.sai.text \
  org.web3d.x3d.sai.texturing \
  org.web3d.x3d.sai.time \
  org.xj3d.sai \
  org.xj3d.io \
  org.web3d.vrml.sav \
  org.web3d.vrml.util \
  org.web3d.image \
  org.web3d.vrml.nodes \
  org.web3d.browser \
  org.web3d.vrml.parser \
  org.web3d.vrml.scripting \
  org.xj3d.core.loading \
  org.xj3d.core.eventmodel \
  org.web3d.vrml.nodes.proto \
  vrml \
  vrml.node \
  vrml.field \
  org.web3d.x3d.jaxp \
  org.xj3d.impl.core.loading \
  org.xj3d.impl.core.eventmodel \
  org.web3d.vrml.scripting.browser \
  org.web3d.vrml.scripting.jsai \
  org.web3d.vrml.scripting.ecmascript.builtin \
  org.web3d.vrml.scripting.ecmascript.x3d \
  org.web3d.vrml.scripting.ecmascript \
  org.web3d.vrml.scripting.sai \
  org.web3d.vrml.scripting.external.buffer \
  org.web3d.vrml.scripting.external.eai \
  org.web3d.vrml.scripting.external.neteai \
  org.web3d.vrml.scripting.external.sai \
  org.web3d.vrml.renderer \
  org.web3d.vrml.renderer.common.nodes \
  org.web3d.vrml.renderer.common.input \
  org.web3d.vrml.renderer.common.input.movie \
  org.web3d.vrml.renderer.common.input.dis \
  org.web3d.vrml.renderer.common.browser \
  org.web3d.vrml.renderer.common.geospatial \
  org.web3d.vrml.renderer.common.nodes.core \
  org.web3d.vrml.renderer.common.nodes.dis \
  org.web3d.vrml.renderer.common.nodes.environment \
  org.web3d.vrml.renderer.common.nodes.enveffects \
  org.web3d.vrml.renderer.common.nodes.eventutils \
  org.web3d.vrml.renderer.common.nodes.extensions \
  org.web3d.vrml.renderer.common.nodes.geom2d \
  org.web3d.vrml.renderer.common.nodes.geom3d \
  org.web3d.vrml.renderer.common.nodes.geospatial \
  org.web3d.vrml.renderer.common.nodes.group \
  org.web3d.vrml.renderer.common.nodes.hanim \
  org.web3d.vrml.renderer.common.nodes.interpolator \
  org.web3d.vrml.renderer.common.nodes.layering \
  org.web3d.vrml.renderer.common.nodes.lighting \
  org.web3d.vrml.renderer.common.nodes.marker \
  org.web3d.vrml.renderer.common.nodes.navigation \
  org.web3d.vrml.renderer.common.nodes.networking \
  org.web3d.vrml.renderer.common.nodes.nurbs \
  org.web3d.vrml.renderer.common.nodes.picking \
  org.web3d.vrml.renderer.common.nodes.particle \
  org.web3d.vrml.renderer.common.nodes.render \
  org.web3d.vrml.renderer.common.nodes.rigidphysics \
  org.web3d.vrml.renderer.common.nodes.scripting \
  org.web3d.vrml.renderer.common.nodes.sensor \
  org.web3d.vrml.renderer.common.nodes.shape \
  org.web3d.vrml.renderer.common.nodes.sound \
  org.web3d.vrml.renderer.common.nodes.text \
  org.web3d.vrml.renderer.common.nodes.surface \
  org.web3d.vrml.renderer.common.nodes.texture \
  org.web3d.vrml.renderer.common.nodes.time \
  org.web3d.vrml.renderer.norender.nodes \
  org.web3d.vrml.renderer.norender \
  org.web3d.vrml.renderer.norender.input \
  org.web3d.vrml.renderer.norender.browser \
  org.web3d.vrml.renderer.norender.nodes.core \
  org.web3d.vrml.renderer.norender.nodes.dis \
  org.web3d.vrml.renderer.norender.nodes.environment \
  org.web3d.vrml.renderer.norender.nodes.enveffects \
  org.web3d.vrml.renderer.norender.nodes.eventutils \
  org.web3d.vrml.renderer.norender.nodes.extensions \
  org.web3d.vrml.renderer.norender.nodes.geom2d \
  org.web3d.vrml.renderer.norender.nodes.geom3d \
  org.web3d.vrml.renderer.norender.nodes.geospatial \
  org.web3d.vrml.renderer.norender.nodes.group \
  org.web3d.vrml.renderer.norender.nodes.hanim \
  org.web3d.vrml.renderer.norender.nodes.interpolator \
  org.web3d.vrml.renderer.norender.nodes.lighting \
  org.web3d.vrml.renderer.norender.nodes.layering \
  org.web3d.vrml.renderer.norender.nodes.layout \
  org.web3d.vrml.renderer.norender.nodes.navigation \
  org.web3d.vrml.renderer.norender.nodes.networking \
  org.web3d.vrml.renderer.norender.nodes.nurbs \
  org.web3d.vrml.renderer.norender.nodes.particle \
  org.web3d.vrml.renderer.norender.nodes.picking \
  org.web3d.vrml.renderer.norender.nodes.render \
  org.web3d.vrml.renderer.norender.nodes.rigidphysics \
  org.web3d.vrml.renderer.norender.nodes.scripting \
  org.web3d.vrml.renderer.norender.nodes.sensor \
  org.web3d.vrml.renderer.norender.nodes.shape \
  org.web3d.vrml.renderer.norender.nodes.sound \
  org.web3d.vrml.renderer.norender.nodes.surface \
  org.web3d.vrml.renderer.norender.nodes.text \
  org.web3d.vrml.renderer.norender.nodes.texture \
  org.web3d.vrml.renderer.norender.nodes.time \
  org.web3d.vrml.renderer.ogl.nodes \
  org.web3d.vrml.renderer.ogl \
  org.web3d.vrml.renderer.ogl.input \
  org.web3d.vrml.renderer.ogl.browser \
  org.web3d.vrml.renderer.ogl.nodes.core \
  org.web3d.vrml.renderer.ogl.nodes.dis \
  org.web3d.vrml.renderer.ogl.nodes.enveffects \
  org.web3d.vrml.renderer.ogl.nodes.environment \
  org.web3d.vrml.renderer.ogl.nodes.eventutils \
  org.web3d.vrml.renderer.ogl.nodes.extensions \
  org.web3d.vrml.renderer.ogl.nodes.interpolator \
  org.web3d.vrml.renderer.ogl.nodes.group \
  org.web3d.vrml.renderer.ogl.nodes.geom2d \
  org.web3d.vrml.renderer.ogl.nodes.geom3d \
  org.web3d.vrml.renderer.ogl.nodes.geospatial \
  org.web3d.vrml.renderer.ogl.nodes.hanim \
  org.web3d.vrml.renderer.ogl.nodes.layering \
  org.web3d.vrml.renderer.ogl.nodes.lighting \
  org.web3d.vrml.renderer.ogl.nodes.marker \
  org.web3d.vrml.renderer.ogl.nodes.navigation \
  org.web3d.vrml.renderer.ogl.nodes.nurbs \
  org.web3d.vrml.renderer.ogl.nodes.networking \
  org.web3d.vrml.renderer.ogl.nodes.picking \
  org.web3d.vrml.renderer.ogl.nodes.scripting \
  org.web3d.vrml.renderer.ogl.nodes.render \
  org.web3d.vrml.renderer.ogl.nodes.rigidphysics \
  org.web3d.vrml.renderer.ogl.nodes.sensor \
  org.web3d.vrml.renderer.ogl.nodes.shape \
  org.web3d.vrml.renderer.ogl.nodes.sound \
  org.web3d.vrml.renderer.ogl.nodes.text \
  org.web3d.vrml.renderer.ogl.nodes.texture \
  org.web3d.vrml.renderer.ogl.nodes.time \
  org.web3d.net.content \
  org.web3d.net.protocol \
  org.web3d.net.resolve \
  org.xj3d.ui.awt.widgets \
  org.xj3d.ui.awt.net.content \
  org.xj3d.ui.awt.browser.ogl \
  org.xj3d.ui.awt.device \
  org.xj3d.ui.swt.util \
  org.xj3d.ui.swt.device \
  org.xj3d.ui.swt.net.content \
  org.xj3d.ui.swt.widgets \
  org.xj3d.ui.swt.browser.ogl \
  org.xj3d.ui.swt.view \
  org.xj3d.loaders.ogl \
  org.web3d.vrml.export.compressors \
  org.web3d.parser.vrml97 \
  org.web3d.parser.x3d \
  org.web3d.parser \
  org.web3d.vrml.export \
  org.web3d.sai.util \

# Names of packages that we want included in the javadoc
JAVADOC_PACKAGES = \
  vrml \
  vrml.node \
  vrml.field \
  vrml.eai \
  vrml.eai.event \
  vrml.eai.field \
  org.web3d.image \
  org.web3d.util \
  org.web3d.browser \
  org.web3d.parser \
  org.web3d.net.content \
  org.web3d.net.protocol \
  org.web3d.net.resolve \
  org.web3d.sai.util \
  org.xj3d.device \
  org.web3d.vrml.lang \
  org.web3d.vrml.nodes \
  org.web3d.vrml.nodes.proto \
  org.web3d.vrml.renderer \
  org.web3d.vrml.renderer.common.nodes \
  org.web3d.vrml.renderer.common.input \
  org.web3d.vrml.renderer.common.browser \
  org.web3d.vrml.renderer.common.input.movie \
  org.web3d.vrml.renderer.common.input.dis \
  org.web3d.vrml.renderer.common.geospatial \
  org.web3d.vrml.renderer.norender \
  org.web3d.vrml.renderer.norender.nodes \
  org.web3d.vrml.renderer.norender.input \
  org.web3d.vrml.renderer.norender.browser \
  org.web3d.vrml.renderer.ogl \
  org.web3d.vrml.renderer.ogl.nodes \
  org.web3d.vrml.renderer.ogl.input \
  org.web3d.vrml.renderer.ogl.browser \
  org.web3d.vrml.sav \
  org.web3d.vrml.parser \
  org.web3d.vrml.scripting \
  org.web3d.vrml.scripting.browser \
  org.web3d.vrml.scripting.jsai \
  org.web3d.vrml.scripting.sai \
  org.web3d.vrml.scripting.ecmascript \
  org.web3d.vrml.scripting.external.sai \
  org.web3d.vrml.scripting.external.eai \
  org.web3d.vrml.scripting.external.buffer \
  org.web3d.vrml.util \
  org.web3d.x3d.dom.swing \
  org.web3d.x3d.sai \
  org.web3d.x3d.jaxp \
  org.web3d.vrml.export.compressors \
  org.web3d.vrml.export \
  org.xj3d.core.eventmodel \
  org.xj3d.core.loading \
  org.xj3d.impl.core.eventmodel \
  org.xj3d.impl.core.loading \
  org.xj3d.io \
  org.xj3d.sai \
  org.xj3d.ui.awt.widgets \
  org.xj3d.ui.awt.net.content \
  org.xj3d.ui.awt.browser.ogl \
  org.xj3d.ui.awt.device \
  org.xj3d.ui.swt.util \
  org.xj3d.ui.swt.device \
  org.xj3d.ui.swt.device.keyboard \
  org.xj3d.ui.swt.device.ogl.mouse \
  org.xj3d.ui.swt.net.content \
  org.xj3d.ui.swt.widgets \
  org.xj3d.ui.swt.browser.ogl \
  org.xj3d.ui.swt.view \
  org.xj3d.ui.common.device \
  org.xj3d.loaders.ogl

# A version number we tack onto the JAR file name for eclipse versioning
# purposes.
JAR_VERSION=2.0.0

# Names of JAR files we want to create
JARS = xj3d-core.jar \
  xj3d-config.jar \
  xj3d-sav.jar \
  xj3d-parser.jar \
  xj3d-script-base.jar \
  xj3d-net.jar \
  xj3d-runtime.jar \
  xj3d-jsai.jar \
  xj3d-java-sai.jar \
  xj3d-external-sai.jar \
  xj3d-ecmascript.jar \
  xj3d-eai.jar \
  xj3d-norender.jar \
  xj3d-xml-util.jar \
  xj3d-render.jar \
  xj3d-common.jar \
  xj3d-ogl.jar \
  xj3d-sai.jar \
  xj3d-jaxp.jar \
  xj3d-images.jar \
  xj3d-xml.jar \
  xj3d-immersive-applet-av3d.jar \
  xj3d-interactive-applet-av3d.jar \
  xj3d-interchange-applet-av3d.jar \
  xj3d-interchange-loader-av3d.jar \
  xj3d-all.jar \
  org.xj3d.libs.jar \
  org.xj3d.browser.jar \

# 3rd party libraries that need including for JAVADOC generation
JARS_JAVADOC=\
  gt2-main-2.4.4.jar \
  gt2-referencing-2.4.4.jar \
  geoapi-nogenerics-2.1.0.jar \

# 3rd party libraries that need including.
JARS_3RDPARTY=j3d-org-all_2.0.0.jar j3d-org-java3d_0.9.0.jar uri.jar

# The list of other files we need to copy from this directory to the classes
# directory when we are making JAR files.
EXTRA =

#*********************************************************************
#
# Javadoc
#
#*********************************************************************

OVERVIEW    = overview.html
WINDOWTITLE = 'Xj3D 2.0 Code Library'
DOCTITLE    = 'Xj3D 2.0 VRML/X3D Code API'
HEADER      = 'Xj3D 2.0 VRML/X3D Code API'
BOTTOM      = '<font size=2>Copyright &copy; 2001 - 2006 \
               <a href=\"http://www.web3d.org\">Web3D Consortium</a> \
               </font>'

# A list of the local directories that contain other Javadoc that we want
# to link to. For example we link to the JDK, JAXP, SAI and JUnit Javadocs
# here. Note that these *must* be full URLs and not just local directory
# references. If your directory path contains a space in it, you may be in
# trouble. We haven't tested it with that. Try using double quotes inside
# the single quotes to get the space treated in the file name
LINK_URLS   =

include $(PROJECT_ROOT)/make/Makefile.jav
