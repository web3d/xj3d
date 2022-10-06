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

// External Imports
import junit.framework.TestCase;

import java.io.*;

import java.util.*;

// Internal Imports
// None

/**
 * Base class for testing filters.
 *
 * @author Alan Hudson
 * @version
 */
public abstract class BaseTestFilter extends TestCase {

    /**
     * Execute a filter.
     *
     * @param filterList The list of filters to apply
     * @param argList The list of arguments
     * @param file The file to process
     * @param baseURL The base url
     * @param encoding The output encoding to use.
     * @param validator
     * @param validArgs
     * @return
     * @throws java.io.IOException
     */
    public int executeFilter(List<String> filterList, List<String> argList,
            String file, String baseURL, String encoding,
            String validator, List<String> validArgs) throws IOException {

        String[] filters = new String[filterList.size()];
        filters = filterList.toArray(filters);
        String[] params = new String[argList.size()];
        params = argList.toArray(params);

        String[] args = new String[filters.length + params.length + 2];

        int idx = 0;

        for (String filter : filters) {
            args[idx++] = filter;
        }

        File dest = File.createTempFile("xj3d_filter", encoding);
        dest.deleteOnExit();

        File src = new File(file);

        args[idx++] = src.getAbsolutePath();
        args[idx++] = dest.getAbsolutePath();

        for (String param : params) {
            args[idx++] = param;
        }

        int err_code = CDFFilter.executeFilters(args, false, null, encoding);

        if (err_code == 0 && validator != null) {
            File dest2 = File.createTempFile("xj3d_validator", encoding);
            dest2.deleteOnExit();
            int validSize = (validArgs == null) ? 0 : validArgs.size();
            args = new String[5 + validSize];
            idx = 0;
            args[idx++] = validator;
            args[idx++] = dest.getAbsolutePath();
            args[idx++] = dest2.getAbsolutePath();
            args[idx++] = "-loglevel";
            args[idx++] = "NONE";

            if (validArgs != null) {
                params = new String[validArgs.size()];
                params = validArgs.toArray(params);

                for (String param : params) {
                    args[idx++] = param;
                }
            }

            int err_code2 = CDFFilter.executeFilters(args, false, null, encoding);

            assertEquals("Error code not 0", 0, err_code2);
        }

        return err_code;
    }
}
