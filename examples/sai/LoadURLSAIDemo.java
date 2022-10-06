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

// External imports
import java.awt.*;
import java.util.HashMap;
import javax.swing.*;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;

import org.web3d.x3d.sai.*;

/**
 * A simple example of how to use SAI to load a scene and modify a value.
 *
 * @author Alan Hudson
 * @version
 */
public class LoadURLSAIDemo extends JFrame implements BrowserListener {

    /**
     * Constructor for the demo.
     */
    public LoadURLSAIDemo() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container contentPane = getContentPane();

        // Setup browser parameters
        Map<String, Object> requestedParameters=new HashMap<>();
        requestedParameters.put("Xj3D_ShowConsole",Boolean.TRUE);

        // Create an SAI component
        X3DComponent x3dComp = BrowserFactory.createX3DComponent(requestedParameters);

        // Add the component to the UI
        Component x3dPanel = (Component)x3dComp;
        contentPane.add(x3dPanel, BorderLayout.CENTER);

        // Get an external browser
        ExternalBrowser x3dBrowser = x3dComp.getBrowser();

        setSize(500,500);
        setVisible(true);

        x3dBrowser.addBrowserListener(this);

        // Create an X3D scene by loading a file
        String fileURL = null;

        try {
          fileURL = (new File("moving_box.x3dv")).toURI().toURL().toString();
        } catch (MalformedURLException ex) {
            ex.printStackTrace(System.err);
        }

//        x3dBrowser.loadURL(new String[] { "moving_box.x3dv" }, null);
        x3dBrowser.loadURL(new String[] { fileURL }, null);
    }

    @Override
    public void browserChanged(BrowserEvent event) {
        int id = event.getID();

        switch(id) {
            case BrowserEvent.INITIALIZED:
                System.out.println("World Initialized");
                break;
            case BrowserEvent.URL_ERROR:
                System.out.println("Error loading world");
                break;
        }
    }

    /**
     * Main method.
     *
     * @param args None handled
     */
    public static void main(String[] args) {
        
        Runnable r = () -> {
            new LoadURLSAIDemo();
        };

        SwingUtilities.invokeLater(r);
    }

}
