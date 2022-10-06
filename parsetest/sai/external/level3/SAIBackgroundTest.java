
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

import java.util.Map;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

// Local imports
import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.ProfileInfo;
import org.web3d.x3d.sai.MFColor;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

import org.web3d.x3d.sai.environmentaleffects.*;
import org.web3d.x3d.sai.grouping.*;

import org.xj3d.sai.Xj3DBrowser;

public class SAIBackgroundTest extends JFrame {

    static X3DComponent component;

    public SAIBackgroundTest() {
        super("SAIBackgroundTest");

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

        ProfileInfo profile = browser.getProfile("Immersive");
        X3DScene scene = browser.createScene(profile, null);
        browser.replaceWorld(scene);

        Map<String, Object> props = browser.getBrowserProperties();
        if (props.get("CONCRETE_NODES").equals(Boolean.TRUE)) {

            browser.beginUpdate();
            Group group = (Group) scene.createNode("Group");

            Background background = (Background) scene.createNode("Background");
            background.setSkyColor(java.awt.Color.GRAY.getRGBComponents(null));
            background.setGroundColor(java.awt.Color.GRAY.getRGBComponents(null));

            // uncomment to initialize the node so it will work
            String[] empty = new String[]{""};
            background.setBackUrl(empty);
            background.setFrontUrl(empty);
            background.setRightUrl(empty);
            background.setLeftUrl(empty);
            background.setTopUrl(empty);
            background.setBottomUrl(empty);

            group.setChildren(new X3DNode[]{background});
            background.setBind(true);
            scene.addRootNode(group);
            browser.endUpdate();
        } else {

            X3DNode group = scene.createNode("Group");

            X3DNode background = scene.createNode("Background");
            MFColor sky = (MFColor) background.getField("skyColor");
            sky.setValue(1, java.awt.Color.GRAY.getRGBComponents(null));
            MFColor ground = (MFColor) background.getField("groundColor");
            ground.setValue(1, java.awt.Color.GRAY.getRGBComponents(null));

            MFNode children = (MFNode) group.getField("children");
            children.setValue(1, new X3DNode[]{background});

            // uncomment to initialize the node so it will work
            /*
             String[] empty = new String[]{ "" };
             MFString backUrl = (MFString)background.getField( "backUrl" );
             backUrl.setValue( 1, empty );
             MFString frontUrl = (MFString)background.getField( "frontUrl" );
             frontUrl.setValue( 1, empty );
             MFString rightUrl = (MFString)background.getField( "rightUrl" );
             rightUrl.setValue( 1, empty );
             MFString leftUrl = (MFString)background.getField( "leftUrl" );
             leftUrl.setValue( 1, empty );
             MFString topUrl = (MFString)background.getField( "topUrl" );
             topUrl.setValue( 1, empty );
             MFString bottomUrl = (MFString)background.getField( "bottomUrl" );
             bottomUrl.setValue( 1, empty );
             */
            SFBool set_bind = (SFBool) background.getField("set_bind");
            set_bind.setValue(true);

            scene.addRootNode(group);
        }

    }

    public static void main(String[] args) {
        final SAIBackgroundTest frame = new SAIBackgroundTest();
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
