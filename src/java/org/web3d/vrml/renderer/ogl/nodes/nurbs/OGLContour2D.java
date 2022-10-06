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

import org.web3d.vrml.renderer.common.nodes.nurbs.BaseContour2D;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.j3d.aviatrix3d.SceneGraphObject;

import net.jgeom.nurbs.TrimCurve;
import net.jgeom.nurbs.UVCoord2f;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * OpenGL rendering implementation of Contour2D
 *
 * Uses classes from the net.jgeom package to implement
 * trimmed nurbs surface tesselation
 *
 * This class presents methods to retrieve the geometry
 * of a trimming loop for a nurbs patch as an instance of 
 * net.jgeom.nurbs.TrimCurve
 *
 * @author Vincent Marchetti
 * @version $Revision: 1.17 $
 * 
 */

public class OGLContour2D extends BaseContour2D implements OGLVRMLNode{

    /** implementation of trimming geometry */
    private TrimCurve vTrimCurve;
    
    /** 
      stored value of sense, the direction of the trimming loop
      True for ccw direction (with convention that (s,t) and normal
      vectors form right handed system
    */
    
    
    private boolean vSense;
    
    /** 
      stored value of sense, the direction of the trimming loop
      
      True for ccw direction (with convention that (s,t) and normal
      vectors form right handed system
    */
    
    /** 
      Value specifying quality or precision of representation
      
      A value of 0 will correspond to representing circles as
      16-sided polygons, and every increase by 1 will double quality
    */

    private float qTesselation;
    
    /**
    */
    public OGLContour2D() {
        super();
        vTrimCurve=null;
        vSense=false;
        qTesselation = 0.0f;
    }

    /**
    Returns representation of trimming loop
    
    @return triming geometry as net.jgeom object
    */
    public TrimCurve getTrimCurve(){
        if (vTrimCurve == null) generateTrimCurve();
        return vTrimCurve;
    }

    /**
    Returns direction of this loop
    
    Considering s parameter axis as equivalent to x axis, and
    t parameter as y axis, return value of true corresponds to a 
    ccw loop, suitable for an outer boundary trimming
    
    @return sense 
    */
    
    public boolean getSense(){
        if (vTrimCurve == null) generateTrimCurve();
        return vSense;
    }
    
    public void setQuality(float q){
        qTesselation = q;
        vTrimCurve=null;
    }
    
    private void generateTrimCurve(){
        if (vfChildren.size() == 1){
            TrimSegment seg = (TrimSegment) vfChildren.get(0);
            vTrimCurve= new TrimCurve( seg.getPoints() );
        }
        else{
            UVCoord2f[][] segments = new UVCoord2f[vfChildren.size()][];
            int Npoints = 1;
            for (int i=0; i<vfChildren.size(); ++i){
                TrimSegment seg = (TrimSegment) vfChildren.get(i);
                segments[i] = seg.getPoints();
                Npoints += (segments[i].length - 1);
            }
            UVCoord2f[] total = new UVCoord2f[Npoints];
            for (int i=0, k=0; i<segments.length; ++i)
                for (int j=0; j<segments[i].length-1; ++j,++k)
                    total[k] = segments[i][j];
            total[Npoints-1] = total[0];
            
            vTrimCurve= new TrimCurve( total );
        }
        
        
        float sum = 0.0f;
        UVCoord2f[] lines = vTrimCurve.lines;
        int NP = lines.length;
        
        for (int i=0; i < NP; ++i){
            int inext = (i+1) % NP;
            sum = sum + lines[i].x*lines[inext].y - lines[i].y * lines[inext].x;
        }
        vSense = (sum > 0.0f);
        
        /* info output */
        if (false){
            System.out.println(String.format(
                "OGLCountour2D.generateTrimCurve: %d points", lines.length
            ));
            for (int i=0; i < vTrimCurve.lines.length; ++i){
                UVCoord2f p = vTrimCurve.lines[i];
                String msg = String.format(":%2d: (%.4f,%.4f)", i, p.x,p.y);
                System.out.println(msg);
            }
            System.out.println(String.format("OGLCountour2D.generateTrimCurve:evaluated sense: %b",vSense));
        }
        
        
    } 
    
	@Override
	public void setupFinished() {
	    setQuality(qTesselation);
	    
	    for (VRMLNodeType vNode: vfChildren){
	        TrimSegment tc = (TrimSegment) vNode;
	        tc.setQuality(qTesselation);
	    }
	    
        super.setupFinished(); 
        /*
        There's no necessary reason to generate the TrimCurve
        right away except that it may be easier to debug if 
        the first evaluation is done at a easily specified
        point in execution
        */
        generateTrimCurve();
    }

    /**
     * got the idea of returning null from OGLCoordinate
     * Get the OGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The null representation.
     */
    @Override
     public SceneGraphObject getSceneGraphObject() {
         return null;
     }

}