/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.awt.widgets;

// External imports
import java.awt.Label;

import org.ietf.uri.ResourceConnection;
import org.ietf.uri.URI;
import org.ietf.uri.event.ProgressEvent;
import org.ietf.uri.event.ProgressListener;

// Local imports
import org.j3d.util.ErrorReporter;

/**
 * An implementation of the URI progress listener for putting messages to
 * a status label.
 *  <p>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class AWTProgressListener implements ProgressListener
{
    /** The status label to put transient messages on */
    private Label statusLabel;

    /** Error handler for more serious messages */
    private ErrorReporter reporter;

    /**
     * Create a new listener that puts information in these different places.
     * Assumes that both references are non-null.
     *
     * @param status The status label to write to
     * @param rep The place for error messages
     */
    public AWTProgressListener(Label status, ErrorReporter rep) {
        statusLabel = status;
        reporter = rep;
    }

    //---------------------------------------------------------------
    // Methods defined by ProgressListener
    //---------------------------------------------------------------

    /**
     * A connection to the resource has been established. At this point, no data
     * has yet been downloaded.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void connectionEstablished(ProgressEvent evt) {
        statusLabel.setText(evt.getMessage());
    }

    /**
     * The header information reading and handshaking is taking place. Reading
     * and interpreting of the data (a download started event) should commence
     * shortly. When that begins, you will be given the appropriate event.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void handshakeInProgress(ProgressEvent evt) {
        statusLabel.setText(evt.getMessage());
    }

    /**
     * The download has started.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void downloadStarted(ProgressEvent evt) {
        statusLabel.setText(evt.getMessage());
    }

    /**
     * The download has updated its status.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void downloadUpdate(ProgressEvent evt) {
        ResourceConnection conn = evt.getSource();
        URI uri = conn.getURI();

        StringBuilder buf = new StringBuilder(uri.toExternalForm());
        buf.append(" (");
        buf.append(evt.getValue());
        buf.append(")");

        statusLabel.setText(buf.toString());
    }

    /**
     * The download has ended.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void downloadEnded(ProgressEvent evt) {
        ResourceConnection conn = evt.getSource();
        URI uri = conn.getURI();
        String msg = uri.toExternalForm() + " download complete.";
/*
        String msg2 = evt.getMessage();
        if (msg2 != null)
            msg = msg + msg2;
*/
        statusLabel.setText(msg);
        reporter.messageReport(msg);
    }

    /**
     * An error has occurred during the download.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void downloadError(ProgressEvent evt) {
        statusLabel.setText(evt.getMessage());
        reporter.errorReport(evt.getMessage(), null);
    }
}
