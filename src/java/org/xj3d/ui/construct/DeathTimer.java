/*****************************************************************************
 *                    Yumetech, Inc Copyright (c) 2010
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.construct;

/**
 * A dark and evil class who is very impatient.  If it isn't told to stop
 * in time it will kill the whole system.
 *
 * Used with captureViewpoints in case the browser hangs.  We want the
 * browser to still exit.
 *
 * @author Alan Hudson, Eric Fickenscher
 * @version $Revision: 1.0 $
 */
public class DeathTimer extends Thread {

    /** The exit code to throw when calling System.exit() */
    private static final int SYSTEM_TIMEOUT_EXIT_CODE = -1;

    /** TRUE if we still want this DeathTimer thread
     * to terminate the application by calling System.exit().
     *
     * Set to FALSE by calling exit() if we no longer want
     * to kill the whole system.
     */
    private boolean exitIfTimeExceeded;

    /** Amount of milliseconds to wait until we call System.exit() */
    private long waitTime;

    /**
     * @param wait integer value - number of milliseconds to wait
     * before calling System.exit().
     */
    public DeathTimer(int wait) {
        exitIfTimeExceeded = true;
        waitTime = wait;
    }

    /**
     * Continue to call Thread.sleep while {@link #exitIfTimeExceeded} is
     * TRUE.  Shutdown the application if {@link #waitTime} is
     * exceeded.
     */
    @Override
    public void run() {

        waitTime += System.currentTimeMillis();

        while ( exitIfTimeExceeded ) {

            try {
                Thread.sleep(500);

            } catch(InterruptedException e) {
                // ignored
            }

            if (System.currentTimeMillis() > waitTime) {
                System.out.println("Time exceeded, killing system");
                System.exit(SYSTEM_TIMEOUT_EXIT_CODE);
            }
        }
    }

    /**
     * Exit this watcher.  Call this method if you no longer
     * want to terminate the application.
     */
    public void exit() {
        exitIfTimeExceeded = false;
    }
}
