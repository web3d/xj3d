/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

/**
 * Browser interface that represents the additional abilities an external
 * application is granted to the VRML browser.
 * <p>
 * A number of the methods in this application can take strings representing URLs.
 * Relative URL strings contained in URL fields of nodes or these method
 * arguments are interpreted as follows:
 * <p>
 * Relative URLs are treated as per clause B.3.5 of the EAI Java Bindings
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public interface ExternalBrowser extends Browser {

    /**
     * Lock the output from the external interface to the browser as the code
     * is about to begin a series of updates. No events will be passed to the
     * VRML world. They will be buffered pending release due to a subsequent
     * call to endUpdate.
     * <p>
     * This call is a nesting call which means subsequent calls to beginUpdate
     * are kept on a stack. No events will be released to the VRML browser
     * until as many endUpdates have been called as beginUpdate.
     *
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @exception ConnectionException An error occurred in the connection to the
     *    browser.
     */
    void beginUpdate()
      throws InvalidBrowserException;

    /**
     * Release the output of events from the external interface into the
     * VRML browser. All events posted to this point from the last time that
     * beginUpdate was called are released into the VRML browser for
     * processing at the next available opportunity.
     * <p>
     * This call is a nesting call which means subsequent calls to beginUpdate
     * are kept on a stack. No events will be released to the VRML browser
     * until as many endUpdates have been called as beginUpdate.
     * <p>
     * If no beginUpdate has been called before calling this method, it has
     * no effect.
     *
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @exception ConnectionException An error occurred in the connection to the
     *    browser.
     */
    void endUpdate()
      throws InvalidBrowserException;

    /**
     * Add a listener for browser events. Any changes in the browser will be
     * sent to this listener. The order of calling listeners is not guaranteed.
     * Checking is performed on whether the nominated listener is already
     * registered to ensure that multiple registration cannot take place.
     * Therefore it is possible to multiply register the one class
     * instance while only receiving one event.
     *
     * @param l The listener to add.
     * @exception NullPointerException If the provided listener reference is
     *     null
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @exception ConnectionException An error occurred in the connection to the
     *    browser.
     */
    void addBrowserListener(BrowserListener l)
      throws InvalidBrowserException;

    /**
     * Remove a listener for browser events. After calling this method, the
     * listener will no longer receive events from this browser instance. If the
     * listener passed as an argument is not currently registered, the method
     * will silently exit.
     *
     * @param l The listener to remove
     * @exception NullPointerException If the provided listener reference is
     *     null
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @exception ConnectionException An error occurred in the connection to the
     *    browser.
     */
    void removeBrowserListener(BrowserListener l)
      throws InvalidBrowserException;

    /**
     * Dispose the resources that are used by this instance. Should be called
     * just prior to leaving the application.
     */
    void dispose();
}
