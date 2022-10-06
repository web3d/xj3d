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

package org.web3d.x3d.sai;

//Standard library imports
// None

// Local imports
// None

/**
 * Defines the requirements of an X3DTimeDependentNode abstract node type
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public interface X3DTimeDependentNode extends X3DChildNode {

    /**
     * Return the loop boolean value.
     *
     * @return The loop boolean value.
     */
    boolean getLoop();

    /**
     * Set the loop field.
     *
     * @param val The boolean to set.
     */
    void setLoop(boolean val);

    /**
     * Return the startTime double value.
     *
     * @return The startTime double value.
     */
    double getStartTime();

    /**
     * Set the startTime field.
     *
     * @param val The double to set.
     */
    void setStartTime(double val);

    /**
     * Return the stopTime double value.
     *
     * @return The stopTime double value.
     */
    double getStopTime();

    /**
     * Set the stopTime field.
     *
     * @param val The double to set.
     */
    void setStopTime(double val);

    /**
     * Return the pauseTime double value.
     *
     * @return The pauseTime double value.
     */
    double getPauseTime();

    /**
     * Set the pauseTime field.
     *
     * @param val The double to set.
     */
    void setPauseTime(double val);

    /**
     * Return the resumeTime double value.
     *
     * @return The resumeTime double value.
     */
    double getResumeTime();

    /**
     * Set the resumeTime field.
     *
     * @param val The double to set.
     */
    void setResumeTime(double val);

    /**
     * Return the elapsedTime double value.
     *
     * @return The elapsedTime double value.
     */
    double getElapsedTime();

    /**
     * Return the isActive boolean value.
     *
     * @return The isActive boolean value.
     */
    boolean getIsActive();

    /**
     * Return the isPaused boolean value.
     *
     * @return The isPaused boolean value.
     */
    boolean getIsPaused();
}
