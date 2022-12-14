/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
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

// External imports
// None

// Local imports
// None

/**
 * Sequencer nodes are designed for discrete animation.
 * <p>
 *
 * Sequencers are driven by an input key ranging [0..1] and produce
 * corresponding impulse output functions.
 * </p>
 *
 * This interface represents the X3D abstract node type X3DInterpolatorNode,
 * which is defined as:
 * <pre>
 *  X3DSequencerNode : X3DChildNode {
 *    SFBool       [in]     next
 *    SFBool       [in]     previous
 *    SFFloat      [in]     set_fraction       (-inf,inf)
 *    MFFloat      [in,out] key           []   (-inf,inf)
 *    MF&lt;type&gt;     [in,out] keyValue      []
 *    SFNode       [in,out] metadata      NULL [X3DMetadataObject]
 *    [S|M]F&lt;type&gt; [out]    value_changed
 *  }
 * </pre>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public interface VRMLSequencerNodeType extends VRMLChildNodeType {

    /**
     * Cause the next value to be generated on the output. This is equivalent
     * to sending a value to the next inputOnly field.
     */
    void setNext();

    /**
     * Cause the previous value to be generated on the output. This is equivalent
     * to sending a value to the previous inputOnly field.
     */
    void setPrevious();

    /**
     * Set a new value for the fraction field. This should cause an
     * output interpolation.
     *
     * @param fraction The new value for fraction
     */
    void setFraction(float fraction);

    /**
     * Get the current value of the fraction field.
     *
     * @return The current value for fraction
     */
    float getFraction();

    /**
     * Set a new value for the key field. A value of <code>null</code>
     * will delete all key values.
     *
     * @param keys The new key values
     * @param numValid The number of valid values to copy from the array
     */
    void setKey(float[] keys, int numValid);

    /**
     * Get current value of the key field. If no keys exist a zero-length
     * float[] will be returned. Use getNumKey to find out how many keys
     * need to be set.
     *
     * @return The current key values
     */
    float[] getKey();

    /**
     * Get the number of valid keys defined for this interpolator.
     *
     * @return a value &gt;= 0
     */
    int getNumKey();
}
