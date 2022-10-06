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
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.IOException;

// Local imports
// none
class FilterParamTransferable implements Transferable {

    private static DataFlavor flavor;

    private FilterParam param;

    FilterParamTransferable(FilterParam param) {
        this.param = param;
        flavor = FilterParamDataFlavor.instance();
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return (new DataFlavor[]{flavor});
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return ((flavor instanceof FilterParamDataFlavor));
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws
            UnsupportedFlavorException, IOException {

        if (!(flavor instanceof FilterParamDataFlavor)) {
            throw (new UnsupportedFlavorException(flavor));
        }
        return (param);
    }
}
