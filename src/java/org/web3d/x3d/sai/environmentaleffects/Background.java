/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai.environmentaleffects;

import org.web3d.x3d.sai.X3DBackgroundNode;

/**
 * Defines the requirements of an X3D Background node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface Background extends X3DBackgroundNode {

    /**
     * Return the number of MFString items in the backUrl field.
     *
     * @return the number of MFString items in the backUrl field.
     */
    int getNumBackUrl();

    /**
     * Return the backUrl value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getBackUrl(String[] val);

    /**
     * Set the backUrl field.
     *
     * @param val The String[] to set.
     */
    void setBackUrl(String[] val);

    /**
     * Return the number of MFString items in the frontUrl field.
     *
     * @return the number of MFString items in the frontUrl field.
     */
    int getNumFrontUrl();

    /**
     * Return the frontUrl value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getFrontUrl(String[] val);

    /**
     * Set the frontUrl field.
     *
     * @param val The String[] to set.
     */
    void setFrontUrl(String[] val);

    /**
     * Return the number of MFString items in the leftUrl field.
     *
     * @return the number of MFString items in the leftUrl field.
     */
    int getNumLeftUrl();

    /**
     * Return the leftUrl value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getLeftUrl(String[] val);

    /**
     * Set the leftUrl field.
     *
     * @param val The String[] to set.
     */
    void setLeftUrl(String[] val);

    /**
     * Return the number of MFString items in the rightUrl field.
     *
     * @return the number of MFString items in the rightUrl field.
     */
    int getNumRightUrl();

    /**
     * Return the rightUrl value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getRightUrl(String[] val);

    /**
     * Set the rightUrl field.
     *
     * @param val The String[] to set.
     */
    void setRightUrl(String[] val);

    /**
     * Return the number of MFString items in the bottomUrl field.
     *
     * @return the number of MFString items in the bottomUrl field.
     */
    int getNumBottomUrl();

    /**
     * Return the bottomUrl value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getBottomUrl(String[] val);

    /**
     * Set the bottomUrl field.
     *
     * @param val The String[] to set.
     */
    void setBottomUrl(String[] val);

    /**
     * Return the number of MFString items in the topUrl field.
     *
     * @return the number of MFString items in the topUrl field.
     */
    int getNumTopUrl();

    /**
     * Return the topUrl value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getTopUrl(String[] val);

    /**
     * Set the topUrl field.
     *
     * @param val The String[] to set.
     */
    void setTopUrl(String[] val);
}
