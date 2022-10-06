/*****************************************************************************
 *                        Yumetech Copyright (c) 2010
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * FileFilter implementation that filters on the file extension
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class ExtFilter implements FileFilter {

    /** The file extension string */
    private String ext;

    /**
     * Constructor
     *
     * @param ext The file extension string
     */
    public ExtFilter( String ext ) {
        this.ext = ext;
    }

    /**
     * Return whether the file path string ends with the specified extension
     *
     * @param file The File to be tested
     */
    @Override
    public boolean accept( File file ) {
        return( file.getName( ).endsWith( ext ) );
    }
}
