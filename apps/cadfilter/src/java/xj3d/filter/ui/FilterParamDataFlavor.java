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

// Local imports
// none

class FilterParamDataFlavor extends DataFlavor {

    private static FilterParamDataFlavor flavor;

    private FilterParamDataFlavor() {
        super(new FilterParam(null, null).getClass(), "FilterParam");
    }

    static FilterParamDataFlavor instance() {
        if (flavor == null) {
            flavor = new FilterParamDataFlavor();
        }
        return (flavor);
    }
}
