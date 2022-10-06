
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
import org.web3d.x3d.sai.rendering.*;
import org.web3d.x3d.sai.shape.*;

import org.xj3d.sai.Xj3DBrowser;

/**
 * Test of constructing and an indexed line set using the SAI
 */
public class SAIIndexedFaceSetTest extends JFrame {

    static X3DComponent component;

    public SAIIndexedFaceSetTest() {
        super("SAIIndexedFaceSetTest");

        Container contentPane = this.getContentPane();
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
            browser.replaceWorld(scene);

            browser.beginUpdate();
            Group group = (Group) scene.createNode("Group");
            Shape shape = (Shape) scene.createNode("Shape");
            IndexedFaceSet ifs = (IndexedFaceSet) scene.createNode("IndexedFaceSet");
            Coordinate coord = (Coordinate) scene.createNode("Coordinate");
            coord.setPoint(new float[]{
                1, 1, 1,
                1, 1, -1,
                -1, 1, -1,
                -1, 1, 1,
                1, -1, 1,
                1, -1, -1,
                -1, -1, -1,
                -1, -1, 1,});
            ifs.setCoord(coord);
            ifs.setCoordIndex(new int[]{
                0, 1, 2, 3, -1, // #top > +y
                7, 6, 5, 4, -1, // #bottom > -y
                0, 4, 5, 1, -1, // #right > +x
                3, 2, 6, 7, -1, // #left > -x
                0, 3, 7, 4, -1, // #front > +z
                1, 5, 6, 2, -1, // #back > -z
            });
            ifs.setCcw(true);
            shape.setGeometry(ifs);

            Appearance appearance = (Appearance) scene.createNode("Appearance");
            Material material = (Material) scene.createNode("Material");
            material.setEmissiveColor(java.awt.Color.RED.getRGBComponents(null));
            appearance.setMaterial(material);

            shape.setAppearance(appearance);
            group.setChildren(new X3DNode[]{shape});
            scene.addRootNode(group);
            browser.endUpdate();

        } else {
            System.err.println("Concrete Node type interfaces not available!");
            System.exit(1);
        }
    }

    //

    public static void main(String[] args) {
        final SAIIndexedFaceSetTest frame = new SAIIndexedFaceSetTest();
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
