/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package xj3d.cdfviewer;

// External imports
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.tree.*;

// Local imports
import org.web3d.x3d.sai.*;

/**
 * A UI interface to leverage the CDFFilter
 *
 * @author Alan Hudson
 * @version $Id: CDFViewer.java 12458 2015-10-16 06:58:23Z tnorbraten $
 */
public class CDFViewer extends JFrame implements MouseListener {

    private JTree tree;

    /**
     * Constructor for the demo.
     * @param filename
     */
    public CDFViewer(String filename) {
        super("CDF Viewer");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container content_pane = getContentPane();

        // Setup browser parameters
        Map<String, Object> requestedParameters=new HashMap<>();
        requestedParameters.put("Xj3D_ShowConsole",Boolean.TRUE);

        // Create an SAI component
        // Bugfix 615
        System.setProperty("x3d.sai.factory.class", "org.xj3d.ui.awt.browser.ogl.X3DOGLBrowserFactoryImpl");
        final X3DComponent x3d_comp = BrowserFactory.createX3DComponent(requestedParameters);

        // Add the component to the UI
        content_pane.add((Component) x3d_comp, BorderLayout.CENTER);

        // Get an external browser
        ExternalBrowser x3dBrowser = x3d_comp.getBrowser();

        DefaultMutableTreeNode top = new DefaultMutableTreeNode("CADLayers");

        tree = new JTree(top) {

            @Override
            public Insets getInsets() {
                return new Insets(5,5,5,5);
            }
        };

        JScrollPane treePane = new JScrollPane(tree);
        treePane.setPreferredSize(new Dimension(350,600));
        content_pane.add(treePane, BorderLayout.WEST);

        setSize(900,600);

        long startTime = System.currentTimeMillis();

        // Create an X3D scene by loading a file
        X3DScene mainScene = x3dBrowser.createX3DFromURL(new String[] { filename });
        System.out.println("creation time: " + (System.currentTimeMillis() - startTime));

        // Replace the current world with the new one
        x3dBrowser.replaceWorld(mainScene);
        System.out.println("replace time: " + (System.currentTimeMillis() - startTime));

        X3DNode roots[] = mainScene.getRootNodes();

        findLayers("CADLayer", roots, top);

        int len = top.getChildCount();

        if (len > 0) {
            TreeNode[] path = ((DefaultMutableTreeNode)top.getChildAt(0)).getPath();
            tree.scrollPathToVisible(new TreePath(path));
        }

        tree.setShowsRootHandles(false);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(null);
        tree.setCellRenderer(renderer);
        tree.addMouseListener(CDFViewer.this);

        Runnable run = new Runnable() {

            @Override
            public void run() {
                tree.setRootVisible(true);
                setVisible(true);
            }
        };
        SwingUtilities.invokeLater(run);
    }

    //-------------------------------------------------------------------------
    // Methods for MouseListener
    //-------------------------------------------------------------------------

    @Override
    public void mousePressed(MouseEvent e) {
         int selRow = tree.getRowForLocation(e.getX(), e.getY());

         TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
         if(selRow != -1) {
             DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selPath.getLastPathComponent();
             if(e.getClickCount() > 0) {

                 if (!(selectedNode.getUserObject() instanceof TreeValue)) {return;}

                 TreeValue node = (TreeValue) selectedNode.getUserObject();
                 node.flipVisible();
//                 System.out.println("node sel: " + node);

                 // Get parent, flip this childs visibility
                 int len = selPath.getPathCount();

                 Object obj = selPath.getPathComponent(len-2);

//                 System.out.println("parent obj: " + obj);

                 if (obj instanceof DefaultMutableTreeNode) {
                     DefaultMutableTreeNode parent = (DefaultMutableTreeNode) obj;

//                     System.out.println("parent user: " + parent.getUserObject());
                     obj = parent.getUserObject();

                     if (obj instanceof TreeValue) {
                         TreeValue parentTree = (TreeValue) parent.getUserObject();

//                         System.out.println("parent sel: " + parentTree);
                         int idx = parent.getIndex(selectedNode);

                         X3DNode x3dNode = parentTree.node;
                         MFBool visibleField = (MFBool) x3dNode.getField("visible");
                         boolean currVal = visibleField.get1Value(idx);

//                         System.out.println("Current value: " + currVal);

                         visibleField.set1Value(idx, !currVal);
                     } else {
                        // What to do with top level?
                     }
                 } else {
                    System.out.println("Close tree?");
                 }
             }
         }
     }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    //-------------------------------------------------------------------------
    //Local Methods
    //-------------------------------------------------------------------------

    /**
     * Walk the SG looking for nodes.  Places the result in layerNodes.
     *
     * @param nodeName The node to look for.
     * @param nodes The nodes to look at.
     */
    private void findLayers(String nodeName, X3DNode[] nodes, DefaultMutableTreeNode tree) {

        X3DFieldDefinition[] decls;
        DefaultMutableTreeNode treeNode = null;
        SFString name;

        for(X3DNode node : nodes) {

            if (node.getNodeName().equals(nodeName)) {
                name = (SFString) node.getField("name");
                treeNode = new DefaultMutableTreeNode(new TreeValue(name.getValue(), node));
                tree.add(treeNode);

                MFNode childrenField = (MFNode) node.getField("children");

                MFBool visibleField = (MFBool) node.getField("visible");
                int size = childrenField.getSize();
                boolean[] currVals = new boolean[size];
                for(int j=0; j < size; j++) {
                    currVals[j] = true;
                }

                visibleField.setValue(size,currVals);

            }

            if (treeNode == null)
                treeNode = tree;

            decls = node.getFieldDefinitions();
            int ftype;
            int atype;
            MFNode mfnode;

            X3DNode[] snodes;

            for(X3DFieldDefinition decl : decls) {
                ftype = decl.getFieldType();
                atype = decl.getAccessType();

                if ((atype == X3DFieldTypes.INPUT_OUTPUT || atype == X3DFieldTypes.INITIALIZE_ONLY)
                    &&  ftype == X3DFieldTypes.MFNODE) {
                    mfnode = (MFNode) node.getField(decl.getName());
                    snodes = new X3DNode[mfnode.getSize()];

                    mfnode.getValue(snodes);

                    findLayers(nodeName, snodes, treeNode);
                } else if (ftype == X3DFieldTypes.SFNODE) {
                    //X3DNode
                }
            }
        }
    }

    /**
     * Main method.
     *
     * @param args None handled
     */
    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("usage: CDFViewer <filename>");
            return;
        }

        new CDFViewer(args[0]);
    }
}


class TreeValue {
    public String name;
    public X3DNode node;
    private boolean visible;

    public TreeValue(String name, X3DNode node) {
        this.name = name;
        this.node = node;
        visible = true;
    }

    public void flipVisible() {
        visible = !visible;
    }

    @Override
    public String toString() {
        String ret_val;

        if (visible)
            ret_val = "+ " + name;
        else
            ret_val = "- " + name;

        return ret_val;
    }

}
