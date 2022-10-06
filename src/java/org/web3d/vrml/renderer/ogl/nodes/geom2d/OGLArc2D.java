/*
Copyright (c) 1995-2014 held by the author(s).  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer
      in the documentation and/or other materials provided with the
      distribution.
    * Neither the names of the Naval Postgraduate School (NPS)
      Modeling Virtual Environments and Simulation (MOVES) Institute
      (http://www.nps.edu and http://www.movesinstitute.org)
      nor the names of its contributors may be used to endorse or
      promote products derived from this software without specific
      prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/
package org.web3d.vrml.renderer.ogl.nodes.geom2d;

import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.LineStripArray;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.VertexGeometry;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.geom2d.BaseArc2D;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;

/** OpenGL implementation of an Arc2D
 *
 * @author <a href="mailto:tdnorbra@nps.edu?subject=org.web3d.vrml.renderer.ogl.nodes.geom2d.OGLArc2D">Terry Norbraten, NPS MOVES</a>
 * @version $Id: OGLArc2D.java 12221 2015-07-27 19:15:15Z tnorbraten $
 */
public class OGLArc2D extends BaseArc2D implements OGLGeometryNodeType {

    /** The geometric impl for this class */
    private VertexGeometry implGeom;

    /** Construct a new OGLArc2D instance that contains no child nodes */
    public OGLArc2D () {}

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLArc2D(VRMLNodeType node) {
        super(node);
    }

    //-------------------------------------------------------------
    // Methods defined by OGLGeometryNodeType
    //-------------------------------------------------------------

    @Override
    public Geometry getGeometry() {
        return implGeom;
    }

    @Override
    public SceneGraphObject getSceneGraphObject() {
        return implGeom;
    }

    @Override
    public void setupFinished() {

        if (!inSetup) {
            return;
        }

        super.setupFinished();

        // Implement the correct rendering for an Arc2D.  The below was
        // borrowed from Disk2D to get this going, however, NX was cut in half
        // to render Math.PI/2 radians, which is a quarter circle (arc),
        // correctly
        int NP = 12;
        int NX = 2 * NP + 1;
        int NVert = 1 + NX;
        float[] vertices = new float[3 * NVert];
        float[] uc = new float[2 * NX];
        double wedge_a = (vfEndAngle - vfStartAngle) / (2 * NP);
        double angle;

        // Generate the arc sweep array
        for (int i = 0; i < (2 * NP); ++i) {
            angle = i * wedge_a;
            uc[2 * i] = (float) Math.cos(angle);
            uc[2 * i + 1] = (float) Math.sin(angle);
        }

        // Ensure symmetry at the end of this sweep array
        uc[4*NP] = -1.0f;
        uc[4*NP+1] = 0.0f;

        for (int i = 0; i < (NVert - 1); ++i) {
            vertices[3 * i + 3] = vfRadius * uc[2 * i];
            vertices[3 * i + 4] = vfRadius * uc[2 * i + 1];
            vertices[3 * i + 5] = 0.0f;
        }

        // Strip out the first vertice (origin) so that the final line segment
        // of this arc is not drawn
        float[] tempVertices = new float[vertices.length-3];
        System.arraycopy(vertices, 3, tempVertices, 0, tempVertices.length);

        LineStripArray lsa = new LineStripArray();
        lsa.setVertices(LineStripArray.COORDINATE_3, tempVertices, NVert-1);
        int[] counts = {NVert-1};
        lsa.setStripCount(counts, 1);
        implGeom = lsa;
    }

} // end class file OGLArc2D.java
