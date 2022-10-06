
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

/**
 * Test that the browser can bind viewpoints specified on url of the anchor
 */
public class AnchorViewpointBindTest {

    public AnchorViewpointBindTest() {
        ExternalBrowser b = SAITestFactory.getBrowser();

        String[] url = {"anchor_vptest.x3dv"};
        b.loadURL(url, null);
    }

    public static void main(String[] args) {
        new AnchorViewpointBindTest();
    }
}
