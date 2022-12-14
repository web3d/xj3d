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

package org.xj3d.sai.external.node.time;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.time.TimeSensor;

/**
 * A concrete implementation of the TimeSensor node interface
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAITimeSensor extends SAINode implements TimeSensor {

    /**
     * The loop inputOutput field
     */
    private SFBool loop;

    /**
     * The startTime inputOutput field
     */
    private SFTime startTime;

    /**
     * The stopTime inputOutput field
     */
    private SFTime stopTime;

    /**
     * The pauseTime inputOutput field
     */
    private SFTime pauseTime;

    /**
     * The resumeTime inputOutput field
     */
    private SFTime resumeTime;

    /**
     * The elapsedTime outputOnly field
     */
    private SFTime elapsedTime;

    /**
     * The cycleInterval inputOutput field
     */
    private SFTime cycleInterval;

    /**
     * The fraction_changed outputOnly field
     */
    private SFFloat fraction_changed;

    /**
     * The time outputOnly field
     */
    private SFTime time;

    /**
     * The cycleTime outputOnly field
     */
    private SFTime cycleTime;

    /**
     * The isActive outputOnly field
     */
    private SFBool isActive;

    /**
     * The isPaused outputOnly field
     */
    private SFBool isPaused;

    /**
     * The enabled inputOutput field
     */
    private SFBool enabled;

    /**
     * Constructor
     *
     * @param node
     * @param nodeFactory
     * @param fieldFactory
     * @param queue
     */
    public SAITimeSensor(
            VRMLNodeType node,
            SAINodeFactory nodeFactory,
            SAIFieldFactory fieldFactory,
            ExternalEventQueue queue) {
        super(node, nodeFactory, fieldFactory, queue);
    }

    /**
     * Return the loop boolean value.
     *
     * @return The loop boolean value.
     */
    @Override
    public boolean getLoop() {
        if (loop == null) {
            loop = (SFBool) getField("loop");
        }
        return (loop.getValue());
    }

    /**
     * Set the loop field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setLoop(boolean val) {
        if (loop == null) {
            loop = (SFBool) getField("loop");
        }
        loop.setValue(val);
    }

    /**
     * Return the startTime double value.
     *
     * @return The startTime double value.
     */
    @Override
    public double getStartTime() {
        if (startTime == null) {
            startTime = (SFTime) getField("startTime");
        }
        return (startTime.getValue());
    }

    /**
     * Set the startTime field.
     *
     * @param val The double to set.
     */
    @Override
    public void setStartTime(double val) {
        if (startTime == null) {
            startTime = (SFTime) getField("startTime");
        }
        startTime.setValue(val);
    }

    /**
     * Return the stopTime double value.
     *
     * @return The stopTime double value.
     */
    @Override
    public double getStopTime() {
        if (stopTime == null) {
            stopTime = (SFTime) getField("stopTime");
        }
        return (stopTime.getValue());
    }

    /**
     * Set the stopTime field.
     *
     * @param val The double to set.
     */
    @Override
    public void setStopTime(double val) {
        if (stopTime == null) {
            stopTime = (SFTime) getField("stopTime");
        }
        stopTime.setValue(val);
    }

    /**
     * Return the pauseTime double value.
     *
     * @return The pauseTime double value.
     */
    @Override
    public double getPauseTime() {
        if (pauseTime == null) {
            pauseTime = (SFTime) getField("pauseTime");
        }
        return (pauseTime.getValue());
    }

    /**
     * Set the pauseTime field.
     *
     * @param val The double to set.
     */
    @Override
    public void setPauseTime(double val) {
        if (pauseTime == null) {
            pauseTime = (SFTime) getField("pauseTime");
        }
        pauseTime.setValue(val);
    }

    /**
     * Return the resumeTime double value.
     *
     * @return The resumeTime double value.
     */
    @Override
    public double getResumeTime() {
        if (resumeTime == null) {
            resumeTime = (SFTime) getField("resumeTime");
        }
        return (resumeTime.getValue());
    }

    /**
     * Set the resumeTime field.
     *
     * @param val The double to set.
     */
    @Override
    public void setResumeTime(double val) {
        if (resumeTime == null) {
            resumeTime = (SFTime) getField("resumeTime");
        }
        resumeTime.setValue(val);
    }

    /**
     * Return the elapsedTime double value.
     *
     * @return The elapsedTime double value.
     */
    @Override
    public double getElapsedTime() {
        if (elapsedTime == null) {
            elapsedTime = (SFTime) getField("elapsedTime");
        }
        return (elapsedTime.getValue());
    }

    /**
     * Return the cycleInterval double value.
     *
     * @return The cycleInterval double value.
     */
    @Override
    public double getCycleInterval() {
        if (cycleInterval == null) {
            cycleInterval = (SFTime) getField("cycleInterval");
        }
        return (cycleInterval.getValue());
    }

    /**
     * Set the cycleInterval field.
     *
     * @param val The double to set.
     */
    @Override
    public void setCycleInterval(double val) {
        if (cycleInterval == null) {
            cycleInterval = (SFTime) getField("cycleInterval");
        }
        cycleInterval.setValue(val);
    }

    /**
     * Return the fraction_changed float value.
     *
     * @return The fraction_changed float value.
     */
    @Override
    public float getFraction() {
        if (fraction_changed == null) {
            fraction_changed = (SFFloat) getField("fraction_changed");
        }
        return (fraction_changed.getValue());
    }

    /**
     * Return the time double value.
     *
     * @return The time double value.
     */
    @Override
    public double getTime() {
        if (time == null) {
            time = (SFTime) getField("time");
        }
        return (time.getValue());
    }

    /**
     * Return the cycleTime double value.
     *
     * @return The cycleTime double value.
     */
    @Override
    public double getCycleTime() {
        if (cycleTime == null) {
            cycleTime = (SFTime) getField("cycleTime");
        }
        return (cycleTime.getValue());
    }

    /**
     * Return the isActive boolean value.
     *
     * @return The isActive boolean value.
     */
    @Override
    public boolean getIsActive() {
        if (isActive == null) {
            isActive = (SFBool) getField("isActive");
        }
        return (isActive.getValue());
    }

    /**
     * Return the isPaused boolean value.
     *
     * @return The isPaused boolean value.
     */
    @Override
    public boolean getIsPaused() {
        if (isPaused == null) {
            isPaused = (SFBool) getField("isPaused");
        }
        return (isPaused.getValue());
    }

    /**
     * Return the enabled boolean value.
     *
     * @return The enabled boolean value.
     */
    @Override
    public boolean getEnabled() {
        if (enabled == null) {
            enabled = (SFBool) getField("enabled");
        }
        return (enabled.getValue());
    }

    /**
     * Set the enabled field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setEnabled(boolean val) {
        if (enabled == null) {
            enabled = (SFBool) getField("enabled");
        }
        enabled.setValue(val);
    }

}
