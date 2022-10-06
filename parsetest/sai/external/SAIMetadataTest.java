
/**
 * ***************************************************************************
 * Copyright North Dakota State University, 2005 Written By Bradley Vender
 * (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1 Please read
 * http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any purpose.
 * Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************
 */

import java.awt.*;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

import org.web3d.x3d.sai.*;
import org.web3d.x3d.sai.core.MetadataString;
import org.web3d.x3d.sai.grouping.Switch;

/**
 * Basic test that the Metadata setting and getting methods are working.
 */
public class SAIMetadataTest {

    public static void main(String[] args) {
        ExternalBrowser x3dBrowser;
        X3DScene mainScene;
        final JFrame testFrame = new JFrame();
        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container contentPane = testFrame.getContentPane();
        Map<String, Object> requestedParameters = new HashMap<>();
        final X3DComponent x3dComp = BrowserFactory.createX3DComponent(requestedParameters);
        Component x3dPanel = (Component) x3dComp;
        contentPane.add(x3dPanel, BorderLayout.CENTER);
        x3dBrowser = x3dComp.getBrowser();
        testFrame.setSize(600, 500);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                testFrame.setVisible(true);
            }
        };
        SwingUtilities.invokeLater(r);

        ProfileInfo profile = null;
        try {
            profile = x3dBrowser.getProfile("Immersive");
        } catch (NotSupportedException nse) {
            System.err.println("Immersive Profile not supported");
            System.exit(-1);
        }
        mainScene = x3dBrowser.createScene(profile, null);
        x3dBrowser.replaceWorld(mainScene);

        int[] iValues = {2, 1, 0, -1};
        float[][] mfValues = {{0, 0, 1}, {10, 10, 1}, {10, 0, 1}};

        x3dBrowser.beginUpdate();
        X3DNode node = mainScene.createNode("Shape");
        SFNode sChNode = (SFNode) (node.getField("geometry"));

        X3DNode indexedFaceSet = mainScene.createNode("IndexedFaceSet");
        X3DNode coordNode = mainScene.createNode("Coordinate");

        X3DField coord = indexedFaceSet.getField("coord");
        SFNode sCoord = (SFNode) coord;
        sCoord.setValue(coordNode);
        X3DField point = coordNode.getField("point");
        MFVec3f mPoint = (MFVec3f) point;
        mPoint.setValue(3, mfValues);

        X3DField coordIndex = indexedFaceSet.getField("coordIndex");
        MFInt32 mCoordIndex = (MFInt32) coordIndex;
        mCoordIndex.setValue(4, iValues);

        sChNode.setValue(indexedFaceSet);

        mainScene.addRootNode(node);
        x3dBrowser.endUpdate();

        System.out.println("Making and applying metadata");
        MetadataString stringData = (MetadataString) mainScene.createNode("MetadataString");
        stringData.setName("test string");
        stringData.setValue(new String[]{"test value"});

        X3DMetadataObject orginalMetadata = node.getMetadata();
        node.setMetadata(stringData);
        X3DMetadataObject newMetadata = node.getMetadata();
        System.out.println("Original metadata: " + orginalMetadata);
        System.out.println("New metadata" + newMetadata);

        X3DScene metaScene = x3dBrowser.createX3DFromString(
                "#VRML V3.0 utf8\n"
                + "PROFILE Immersive\n"
                + "DEF mySceneGroup Group {\n"
                + "    children ["
                + "		DEF firstTransform Transform {"
                + "			translation 5 0 0 "
                + "			children ["
                + "				DEF SW Switch {whichChoice 0"
                + "					metadata MetadataString {}"
                + "				}"
                + "			]"
                + "		}"
                + "    ]"
                + "}"
        );
        Switch switchNode = (Switch) metaScene.getNamedNode("SW");
        System.out.println("SW's metadata:" + switchNode.getMetadata());
        System.out.println("SW's metadata field:" + switchNode.getField("metadata"));
        System.out.println("SW's metadata read from field:" + ((SFNode) switchNode.getField("metadata")).getValue());
    }
}
