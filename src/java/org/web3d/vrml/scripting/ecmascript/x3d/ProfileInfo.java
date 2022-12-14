/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.ecmascript.x3d;

// Standard imports
import org.mozilla.javascript.Scriptable;

// Application specific imports
import org.j3d.util.HashSet;

import org.web3d.vrml.scripting.ecmascript.builtin.AbstractScriptableObject;

/**
 * ProfileInfo miscellaneous object.
 *  <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class ProfileInfo extends AbstractScriptableObject {

    /** Set of the valid property names for this object */
    private static final HashSet<String> propertyNames;

    /** The name of the profile */
    private final String name;

    /** The title string of the profile */
    private final String title;

    /** The provider URL of the profile */
    private final String url;

    /** The array of constituent components */
    private final ComponentInfoArray components;

    static {
        propertyNames = new HashSet<>();
        propertyNames.add("name");
        propertyNames.add("title");
        propertyNames.add("providerUrl");
        propertyNames.add("components");
    }

    /**
     * Construct a profile descriptor for the given information.
     *
     * @param name The name of this component
     * @param title An arbitrary title string
     * @param url An optional provider url
     * @param comps our Component info array
     */
    public ProfileInfo(String name,
                       String title,
                       String url,
                       ComponentInfoArray comps) {
        super("ProfileInfo");

        this.name = name;
        this.title = title;
        this.url = url;
        this.components = comps;
    }

    /**
     * Construct a component descriptor based on the internal representation
     * of same.
     *
     * @param info The description of the component to use
     */
    public ProfileInfo(org.web3d.vrml.lang.ProfileInfo info) {
        super("ProfileInfo");

        this.url = null;

        this.name = info.getName();
        this.title = info.getTitle();

        org.web3d.vrml.lang.ComponentInfo[] c_list = info.getComponents();
        ComponentInfo[] out_list = new ComponentInfo[c_list.length];

        for(int i = 0; i < c_list.length; i++)
            out_list[i] = new ComponentInfo(c_list[i]);

        components = new ComponentInfoArray(out_list);
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return propertyNames.contains(name);
    }

    /**
     * Get the value of the named function. If no function object is
     * registered for this name, the method will return null.
     *
     * @param name The variable name
     * @param start The object where the lookup began
     * @return the corresponding function object or null
     */
    @Override
    public Object get(String name, Scriptable start) {
        Object ret_val = NOT_FOUND;

        if(propertyNames.contains(name)) {
            char prop = name.charAt(0);

            switch(prop) {
                case 'n':
                    ret_val = name;
                    break;

                case 't':
                    ret_val = title;
                    break;

                case 'p':
                    ret_val = url;
                    break;

                case 'c':
                    ret_val = components;
                    break;
            }
        }

        return ret_val;
    }
}
