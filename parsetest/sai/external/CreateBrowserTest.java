
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.X3DComponent;

/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

/**
 * Test whether the BrowserFactory is working, and nothing more.
 */
public class CreateBrowserTest {

    public static void main(String[] args) {
        boolean shouldQuit = true;
        Map<String, Object> requestedParameters = new HashMap<>();
        requestedParameters.put("Xj3D_ConsoleShown", Boolean.TRUE);
        requestedParameters.put("Xj3D_LocationShown", Boolean.TRUE);
        requestedParameters.put("Xj3D_NavbarShown", Boolean.TRUE);
        requestedParameters.put("Xj3D_LocationReadOnly", Boolean.TRUE);
        requestedParameters.put("Xj3D_LocationPosition", "Top");
        requestedParameters.put("Xj3D_NavigationPosition", "Bottom");
        final X3DComponent comp = BrowserFactory.createX3DComponent(requestedParameters);
//        ExternalBrowser browser = comp.getBrowser();

        final Frame f = new Frame();
        f.setLayout(new BorderLayout());
        f.setBackground(Color.blue);
        f.add((Component) comp, BorderLayout.CENTER);
        Runnable r = () -> {
            f.setVisible(true);
        };
        SwingUtilities.invokeLater(r);
        if (!shouldQuit) {
            f.addWindowListener(new java.awt.event.WindowAdapter() {
                /* Normal adapter to make dispose work. */
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    e.getWindow().hide();
                    e.getWindow().dispose();
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

    }
}
