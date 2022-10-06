/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.util;

// External Imports
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

// Local imports
// None

/**
 * A set of tools for locating property values.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class PropertyTools {
    /**
     * Go looking for the named system property.
     *
     * @param propName The name of the property to read
     * @param def The default value if not found
     * @return
     */
    public static String fetchSystemProperty(final String propName,
                                           String def) {

        String prop = (String)AccessController.doPrivileged((PrivilegedAction<Object>) () -> System.getProperty(propName));

        if (prop == null) {
            return def;
        } else {
            System.out.println(propName + " set to: " + prop);
            return prop;
        }
    }

    /**
     * Go looking for the named system property.
     *
     * @param propName The name of the property to read
     * @param def The default value if not found
     * @return
     */
    public static boolean fetchSystemProperty(final String propName,
                                           boolean def) {
        boolean b;
        String prop = (String)AccessController.doPrivileged((PrivilegedAction<Object>) () -> System.getProperty(propName));

        if (prop == null)
            return def;
        else {
            b = Boolean.valueOf(prop);
            System.out.println(propName + " set to: " + b);
            return b;
        }
    }

    /**
     * Go looking for the named system property.
     *
     * @param propName The name of the property to read
     * @param def The default value if not found
     * @param map Mapping of the property string to values
     * @return
     */
    public static int fetchSystemProperty(final String propName,
                                           int def,
                                           Map<String, Integer> map) {

        Integer i;
        String prop = (String)AccessController.doPrivileged((PrivilegedAction<Object>) () -> System.getProperty(propName));

        if (prop == null)
            return def;
        else {
            i = map.get(prop);
            if (i == null) {
                System.out.println(propName + " invalid Property: " + prop);
                return def;
            }
            else {
                System.out.println(propName + " set to: " + i);
                return i;
            }
        }
    }

    /**
     * Go looking for the named system property.
     *
     * @param propName The name of the property to read
     * @param def The default value if not found
     * @return
     */
    public static int fetchSystemProperty(final String propName,
                                           final int def) {
        Integer prop = (Integer)AccessController.doPrivileged((PrivilegedAction<Object>) () -> Integer.getInteger(propName, def));

        int i = prop;
        if (i != def)
            System.out.println(propName + " set to: " + i);
        return i;
    }
}
