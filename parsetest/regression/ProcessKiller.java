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

 /**
  * A stone cold killer.  It destroy's a process after a specified
  * amount of time.
  *
  * @author Alan Hudson, Eric Fickenscher
  * @version $Revision: 1.0 $
  */
 public class ProcessKiller extends Thread {

    /** The process to manage */
    private Process process;

    /** The amount of time to wait */
    private long wait;

    /** TRUE if this processKiller thread is still live
     * and we still want to kill the associated Process.
     * Set to FALSE by calling exit() if we no longer want
     * to kill the  associated process. */
    private boolean killThread;

    /** Was this process destroyed */
    private boolean destroyed;

    /**
     * @param processToKill Process to be killed
     * @wait integer value - number of milliseconds to wait
     * before killing the {@link #process}.
     */
    public ProcessKiller(Process processToKill, int millisecondsToWait) {
        process = processToKill;
        wait = millisecondsToWait;

        killThread = true;
        destroyed = false;
    }

    public void run() {
        wait += System.currentTimeMillis();

        while( killThread && System.currentTimeMillis() < wait) {
            try {
                Thread.sleep(500);
            } catch(Exception e) {}

            //System.out.println("Waiting...");
        }

        if ( killThread ) {
            System.out.println("***Destroying process");
            destroyed = true;
            process.destroy();
        }

        process = null;
    }

    /**
     * Exit this watcher.  Call this method if you no longer
     * want to kill {@link #process}.
     */
    public void exit() {
        killThread = false;
    }

    /**
     * Was the process destroyed.
     *
     * @return Whether this process was destroyed
     */
    public boolean isDestroyed() {
        return destroyed;
    }
}
