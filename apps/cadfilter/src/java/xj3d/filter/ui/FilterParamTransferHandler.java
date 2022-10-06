/*****************************************************************************
 *                        Web3d.org Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.ui;

// External imports
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.Transferable;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import java.io.IOException;

// Local imports
// none
class FilterParamTransferHandler extends TransferHandler {

    private DataFlavor flavor;

    private JList available;

    private JList<FilterParam> active;

    FilterParamTransferHandler(JList<FilterParam> available, JList<FilterParam> active) {
        this.available = available;
        this.active = active;
        this.flavor = FilterParamDataFlavor.instance();
    }

    @Override
    protected Transferable createTransferable(JComponent c) {

        FilterParamTransferable fpt = null;
        JList source = (JList) c;
        int srcIndex = source.getSelectedIndex();
        if (srcIndex >= 0) {
            DefaultListModel listModel = (DefaultListModel) source.getModel();
            FilterParam fp = (FilterParam) listModel.getElementAt(srcIndex);
            if (source == active) {
                listModel.removeElementAt(srcIndex);
            } else {
                fp = fp.clone();
            }
            fpt = new FilterParamTransferable(fp);
        }
        return fpt;
    }

    @Override
    public int getSourceActions(JComponent c) {
        int rval;
        if (c == available) {
            rval = COPY;
        } else if (c == active) {
            rval = MOVE;
        } else {
            rval = NONE;
        }
        return rval;
    }

    @Override
    public boolean importData(JComponent c, Transferable t) {

        boolean rval = false;
        if (canImport(c, t.getTransferDataFlavors())) {
            try {
                FilterParam fp = (FilterParam) t.getTransferData(flavor);
                int tgtIndex = active.getSelectedIndex();
                DefaultListModel<FilterParam> listModel = (DefaultListModel<FilterParam>) active.getModel();
                int max = listModel.getSize();
                if (tgtIndex < 0) {
                    tgtIndex = max;
                }
                listModel.add(tgtIndex, fp);
                rval = true;
            } catch (UnsupportedFlavorException | IOException ufe) {}
        }
        return rval;
    }

    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {}

    @Override
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        boolean rval = false;
        if (c == active) {
            for (DataFlavor flavor1 : flavors) {
                if (flavor.equals(flavor1)) {
                    rval = true;
                    break;
                }
            }
        }
        return rval;
    }
}
