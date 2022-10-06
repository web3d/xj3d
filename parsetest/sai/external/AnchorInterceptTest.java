
/**
 * ***************************************************************************
 * Web3d.org Copyright (c) 2001 - 2007 Java Source
 *
 * This source is licensed under the BSD-style license Please read
 * http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any purpose.
 * Use it at your own risk. If there's a problem you get to fix it.
 *
 ***************************************************************************
 */

import org.web3d.x3d.sai.ExternalBrowser;

import org.xj3d.sai.Xj3DAnchorListener;
import org.xj3d.sai.Xj3DBrowser;

/**
 * Test that the browser can intercept anchor events
 */
public class AnchorInterceptTest implements Xj3DAnchorListener {

    public AnchorInterceptTest() {
        ExternalBrowser b = SAITestFactory.getBrowser();

        String[] url = {"anchor_test.x3dv"};
        b.loadURL(url, null);
        Xj3DBrowser browser = (Xj3DBrowser) b;

        browser.setAnchorListener(AnchorInterceptTest.this);
    }

    /**
     * Notification that the given link has been activated. If your code wants
     * to process this URL then return a value of true and the browser will not
     * do anything further. If you do not wish to process this URL in order to
     * let the browser handle it, then return false
     *
     * @param url The value of the URL field of the Anchor node that was
     * selected
     * @param params The value of the parameter field of the Anchor node that
     * was selected
     * @return true if the browser should not perform any further processing,
     * false when the browser should continue normal functionality
     */
    @Override
    public boolean processLinkActivation(String[] url, String[] params) {
        System.out.println("Got link activation " + url[0]);
        return true;
    }

    public static void main(String[] args) {
        new AnchorInterceptTest();
    }
}
