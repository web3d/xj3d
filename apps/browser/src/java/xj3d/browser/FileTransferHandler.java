/*****************************************************************************
 *                        Shapeways Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.0
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.browser;

// External Imports
import java.awt.datatransfer.*;
import java.io.*;
import java.util.List;

import javax.swing.*;

// Internal
import org.web3d.util.FileHandler;

/**
 * Implement drag and drop for the main frame.
 *
 * @author Alan Hudson
 */
public class FileTransferHandler extends TransferHandler {

    /** The browser to handle the event */
    private FileHandler browser;

    public FileTransferHandler(FileHandler browser) {
        this.browser = browser;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        Transferable t = support.getTransferable();

        try {

            @SuppressWarnings("unchecked") // cast from Object type
            List<File> l =
                (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);

            if (l.size() > 0) {
                browser.loadURL(l.get(0).toString());
            }
        } catch (UnsupportedFlavorException | IOException e) {
            return false;
        }

        return true;
    }
}
