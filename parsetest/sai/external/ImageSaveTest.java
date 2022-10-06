
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

// External imports
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.image.BufferedImage;

// Local imports
import org.web3d.x3d.sai.ExternalBrowser;

import org.xj3d.sai.Xj3DScreenCaptureListener;
import org.xj3d.sai.Xj3DBrowser;

/**
 * Test that the browser can grab single images from the browser.
 */
public class ImageSaveTest
        implements ActionListener, Xj3DScreenCaptureListener {

    /**
     * Our browser reference
     */
    private Xj3DBrowser browser;

    /**
     * Label that we put the captured image on
     */
    private JLabel imagePanel;

    public ImageSaveTest() {

        JButton b = new JButton("Capture now");
        b.addActionListener(ImageSaveTest.this);

        imagePanel = new JLabel("Nothing captured yet");

        JPanel base_panel = new JPanel(new BorderLayout());
        base_panel.add(imagePanel, BorderLayout.CENTER);
        base_panel.add(b, BorderLayout.SOUTH);

        ExternalBrowser eb = SAITestFactory.getBrowser();

        String[] url = {"anchor_test.x3dv"};
        eb.loadURL(url, null);

        browser = (Xj3DBrowser) eb;
        browser.setScreenCaptureListener(ImageSaveTest.this);
    }

    /**
     * Process the action event from the button.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        browser.captureFrames(1);
    }

    /**
     * Notification of a new screen capture presented as an image. A new image
     * instance will be generated for each frame.
     *
     * @param img The screen captured image
     */
    @Override
    public void screenCaptured(BufferedImage img) {
        ImageIcon icon = new ImageIcon(img);
        imagePanel.setIcon(icon);

    }

    public static void main(String[] args) {
        new ImageSaveTest();
    }
}
