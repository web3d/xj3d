/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.vrml.nodes;

/**
 * Geometry nodes produce renderable geometry and are contained by a Shape
 * node.
 * <p>
 *
 * All geometry has two common properties that indicate whether to render
 * both sides of the geometry (solid), and the winding of the vertices of
 * the triangle. By default, VRML97/X3D use counter-clockwise ordering, but
 * any of the polygonal nodes may elect to reverse the order.
 *
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 */
public interface VRMLGeometryNodeType extends VRMLNodeType {

    /**
     * Get the value of the solid field.
     *
     * @return true This object is solid (ie single sided)
     */
    boolean isSolid();

    /**
     * Get the value of the CCW field. If the node does not have one, this will
     * return true.
     *
     * @return true if the vertices are CCW ordered
     */
    boolean isCCW();

    /**
     * Specifies whether this node requires lighting.
     *
     * @return true Should lighting be enabled
     */
    boolean isLightingEnabled();

    /**
     * Set the number of textures that were found on the accompanying Appearance
     * node. Used to set the number of texture coordinates that need to be
     * passed in to the renderer when no explicit texture coordinates were
     * given.
     *
     * @param count The number of texture coordinate sets to add
     */
    void setTextureCount(int count);

    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    int getNumSets();

    /**
     * Get the texture coordinate generation mode.  The values are constants
     * defined in the X3D Spec under TextureCoordinateGenerator. NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers.
     * @return The mode or NULL
     */
    String getTexCoordGenMode(int setNum);

    /**
     * Specified whether this node has color information.  If so, then it
     * will be used for diffuse terms instead of materials.
     *
     * @return true Use local color information for diffuse lighting.
     */
    boolean hasLocalColors();

    /**
     * Specified whether this node has alpha values in the local colour
     * information. If so, then it will be used to override the material's
     * transparency value.
     *
     * @return true when the local color value has inbuilt alpha
     */
    boolean hasLocalColorAlpha();

    /**
     * Add a listener for local color changes.  Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    void addLocalColorsListener(LocalColorsListener l);

    /**
     * Remove a listener for local color changes.  Nulls will be ignored.
     *
     * @param l The listener.
     */
    void removeLocalColorsListener(LocalColorsListener l);

    /**
     * Add a listener for texture coordinate generation mode changes.
     * Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    void addTexCoordGenModeChanged(TexCoordGenModeListener l);

    /**
     * Remove a listener for texture coordinate generation mode changes.
     * Nulls will be ignored.
     *
     * @param l The listener.
     */
    void removeTexCoordGenModeChanged(TexCoordGenModeListener l);
}
