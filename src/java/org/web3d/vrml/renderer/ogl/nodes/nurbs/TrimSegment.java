/*****************************************************************************
 *                        Web3d.org Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.nurbs;

import net.jgeom.nurbs.UVCoord2f;

public interface TrimSegment {

    void setQuality(float q);
    UVCoord2f[] getPoints();
}

