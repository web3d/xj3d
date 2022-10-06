
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

/**
 * ***************************************************************************
 * Copyright North Dakota State University, 2001 Written By Bradley Vender
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
/**
 * Test whether the BrowserFactory is working, and nothing more.
 */
public class SceneGroupingTest {

    public static void main(String[] args) {
        boolean shouldQuit = true;
        Map<String, Object> requestedParameters = new HashMap<>();
        requestedParameters.put("Xj3D_ConsoleShown", Boolean.TRUE);
        requestedParameters.put("Xj3D_LocationShown", Boolean.FALSE);
        /*requestedParameters.put("Xj3D_NavbarShown",Boolean.TRUE);
         requestedParameters.put("Xj3D_LocationReadOnly",Boolean.TRUE);
         requestedParameters.put("Xj3D_LocationPosition","Top");
         requestedParameters.put("Xj3D_NavigationPosition","Bottom");*/
        final X3DComponent comp = BrowserFactory.createX3DComponent(requestedParameters);
        ExternalBrowser browser = comp.getBrowser();

        final Frame f = new Frame();
        f.setLayout(new BorderLayout());
        f.setBackground(Color.blue);
        f.add((Component) comp, BorderLayout.CENTER);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                f.setVisible(true);
            }
        };
        SwingUtilities.invokeLater(r);
        if (!shouldQuit) {
            f.addWindowListener(new java.awt.event.WindowAdapter() {
                /* Normal adapter to make dispose work. */
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    comp.shutdown();
                    f.dispose();
                }
            });
        } else {
            f.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
        }
        f.setSize(400, 400);
        System.out.println("Done setup");
        X3DScene s = browser.createX3DFromString(
                "PROFILE Interactive\n"
                + "Transform { translation -5 0 0}"
                + "ProximitySensor { size 1000000 1000000 1000000 enabled TRUE}"
                + "Transform { translation 5 0 0}"
                + "Viewpoint {}"
        );
        System.out.println("Done main");
        X3DScene fish = browser.createX3DFromURL(new String[]{"http://vcell.ndsu.nodak.edu/client/WRL/BlueBall.wrl"});
        System.out.println("Done fish");
        X3DScene spaceship = browser.createX3DFromURL(new String[]{"http://vcell.ndsu.nodak.edu/client/WRL/BlueBall.wrl"});
        System.out.println("Done spaceship");
        X3DNode root = s.getRootNodes()[0];
        X3DNode fishNodes[] = fish.getRootNodes();
        X3DNode spaceShip[] = spaceship.getRootNodes();
        for (X3DNode fishNode : fishNodes) {
            fish.removeRootNode(fishNode);
        }
        for (X3DNode spaceShip1 : spaceShip) {
            spaceship.removeRootNode(spaceShip1);
        }
        ((MFNode) (s.getRootNodes()[0].getField("children"))).setValue(fishNodes.length, fishNodes);
        ((MFNode) (s.getRootNodes()[2].getField("children"))).setValue(spaceShip.length, spaceShip);
        X3DNode proximitySensor = s.getRootNodes()[1];
        proximitySensor.getField("position_changed").addX3DEventListener(
                new GenericTestingX3DFieldListener()
        );
        proximitySensor.getField("orientation_changed").addX3DEventListener(
                new GenericTestingX3DFieldListener()
        );
        browser.replaceWorld(s);
    }
}
