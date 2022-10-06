
/**
 * ***************************************************************************
 * Yumetech, Inc Copyright (c) 2007 Java Source
 *
 * This source is licensed under the BSD license. Please read docs/BSD.txt for
 * the text of the license.
 *
 *
 * This software comes with the standard NO WARRANTY disclaimer for any purpose.
 * Use it at your own risk. If there's a problem you get to fix it.
 *
 ***************************************************************************
 */

// External imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

// Local imports
import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.ProfileInfo;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

import org.web3d.x3d.sai.grouping.*;
import org.web3d.x3d.sai.geometry3d.*;
import org.web3d.x3d.sai.interpolation.*;
import org.web3d.x3d.sai.shape.*;
import org.web3d.x3d.sai.time.*;

import org.xj3d.sai.Xj3DBrowser;

/**
 * watch the cone change color
 */
public class SAIColorInterpolatorTest extends JFrame {

    static X3DComponent component;

    static final float[] COLOR_VALUE = new float[]{
        1, 0, 0,
        0, 1, 0,
        0, 0, 1,
        1, 0, 0,};

    static final float[] KEY = new float[]{
        0,
        0.33f,
        0.67f,
        1,};

    int color_index = 0;

    public SAIColorInterpolatorTest() {
        super("SAIColorInterpolatorTest");

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        Map<String, Object> params = new HashMap<>();
        params.put("Xj3D_LocationShown", Boolean.FALSE);
        params.put("Xj3D_OpenButtonShown", Boolean.FALSE);
        params.put("Xj3D_ReloadButtonShown", Boolean.FALSE);

        component = BrowserFactory.createX3DComponent(params);
        contentPane.add((Component) component, BorderLayout.CENTER);

        ExternalBrowser browser = component.getBrowser();
        ((Xj3DBrowser) browser).setMinimumFrameInterval(20);

        Map<String, Object> props = browser.getBrowserProperties();
        if (props.get("CONCRETE_NODES").equals(Boolean.TRUE)) {

            ProfileInfo profile = browser.getProfile("Immersive");
            X3DScene scene = browser.createScene(profile, null);

            // This is key to rendering a pure SAI scene.  Get the created scene
            // loaded first, then modify as below.
            browser.replaceWorld(scene);

            Group group = (Group) scene.createNode("Group");
            Shape shape = (Shape) scene.createNode("Shape");

            Cone cone = (Cone) scene.createNode("Cone");
            shape.setGeometry(cone);

            Appearance appearance = (Appearance) scene.createNode("Appearance");
            Material material = (Material) scene.createNode("Material");
            material.setDiffuseColor(new float[]{0.5f, 0.5f, 0.5f});
            appearance.setMaterial(material);

            shape.setAppearance(appearance);

            TimeSensor timeSensor = (TimeSensor) scene.createNode("TimeSensor");
            scene.addRootNode(timeSensor);
            timeSensor.setLoop(true);
            timeSensor.setCycleInterval(10);

            ColorInterpolator colorInterpolator = (ColorInterpolator) scene.createNode("ColorInterpolator");
            scene.addRootNode(colorInterpolator);
            colorInterpolator.setFraction(0);
            colorInterpolator.setKey(KEY);
            colorInterpolator.setKeyValue(COLOR_VALUE);

            scene.addRoute(timeSensor, "fraction_changed", colorInterpolator, "set_fraction");
            scene.addRoute(colorInterpolator, "value_changed", material, "emissiveColor");

            group.setChildren(new X3DNode[]{shape});
            scene.addRootNode(group);

        } else {
            System.err.println("Concrete Node type interfaces not available!");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        final SAIColorInterpolatorTest frame = new SAIColorInterpolatorTest();
        frame.setSize(512, 512);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                frame.setVisible(true);
            }
        };
        SwingUtilities.invokeLater(r);
    }
}
