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

package xj3d.filter;

/**
 * A dark and evil class who is very impatient.  If it isn't told to stop
 * in time it will kill the whole system.
 *
 * Used with captureViewpoints in case the browser hangs.  We want the
 * browser to still exit.
 *
 * @author Eric Fickenscher
 * @version $Revision: 1.0 $
 */
public class DeathTimer extends Thread {

    /** TRUE if we still want this DeathTimer thread
     * to terminate the application by calling System.exit();
     * Set to FALSE by calling exit() if we no longer want
     * to kill the whole system. */
    private boolean exitIfTimeExceeded;

    /** Allow the application to run this many milliseconds
     * before calling System.exit() */
    private long waitTime;

    /** Amount of time in milliseconds to sleep between timeout checks */
    private long sleepDuration;


    /**
     * Constructor
     * @param wait long value - number of milliseconds to wait
     * before calling System.exit().
     */
    public DeathTimer(long wait) {

        waitTime = wait;
        exitIfTimeExceeded = true;

        sleepDuration = 1_000;
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

            if ( System.currentTimeMillis() > waitTime) {
                System.out.println("Time exceeded, killing system");
                System.exit(FilterExitCodes.MAX_RUN_TIME_EXCEEDED);
            }

            try {
                sleep(sleepDuration);

            } catch(InterruptedException e) {
                // ignored
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