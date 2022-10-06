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

// Local imports
// none

class FilterParam {

    public final String name;

    public final String className;

    public String options;

    FilterParam(String name, String className) {
        this.name = name;
        this.className = className;
    }

    @Override
    public FilterParam clone() {
        FilterParam fp = new FilterParam(name, className);
        fp.options = this.options;
        return (fp);
    }

    @Override
    public String toString() {
        return (name);
    }
}
