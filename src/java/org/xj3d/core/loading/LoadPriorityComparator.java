/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.core.loading;

// External imports
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

// Local imports
// none

/**
 * Comparator implementation that takes items from the load queue and sorts
 * them according to their priority.
 * <p>
 *
 * The priority is based on the value of the system property
 * <code>org.xj3d.core.loading.sort.order</code>. The default load order is
 * set by {@link LoadConstants#DEFAULT_SORT_ORDER}.
 * <p>
 *
 * The priority order is read from the current system property. If the user
 * needs to change the load order, they can reset the property value and then
 * ask this class to reload the values.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
class LoadPriorityComparator implements Comparator<LoadRequest> {

    /**
     * Mapping of the sort type string to the Integer representing its
     * priority.
     */
    private Map<String, Integer> priorityMap;

    /**
     * Create a new instance of this comparator using the default sort order.
     */
    LoadPriorityComparator() {
        priorityMap = new HashMap<>();

        setupPriorities();
    }

    //---------------------------------------------------------
    // Methods defined by Comparator
    //---------------------------------------------------------

    /**
     * Compares its two arguments for order. Returns a negative integer, zero,
     * or a positive integer as the first argument is less than, equal to, or
     * greater than the second.
     *
     * @param o1 The first object to be compared
     * @param o2 The second object to be compared
     * @return a negative integer, zero, or a positive integer as the first
     *   argument is less than, equal to, or greater than the second
     */
    @Override
    public int compare(LoadRequest o1, LoadRequest o2) {
        LoadRequest l1 = o1;
        LoadRequest l2 = o2;


        Integer i1 = priorityMap.get(l1.type);
        Integer i2 = priorityMap.get(l2.type);

        // Integer priorities. If we find something not in the map (should be
        // impossible, but let's just be sure, then set it to load last.
        int p1 = (i1 == null) ? 1_000 : i1;
        int p2 = (i2 == null) ? 1_000 : i2;

        if(p1 != p2)
            return p1 < p2 ? -1 : 1;

        // same priority? Then prefer the one that has the most nodes that
        // have to send the URL to. Also, take the size right now as, since
        // this is multithreaded, then these sizes may change underneath us
        // if someone dumps something onto the queue as we're sorting this
        // item pair.

// TODO: This sort criteria makes the tree not work.  contains and remove depend
//       on compare to return the same this over time.  loadList.size changes

/*
        int s1 = l1.loadList.size();
        int s2 = l2.loadList.size();
        if(s1 != s2)
            return s1 < s2 ? s1 : s2;
*/
        // Number of items are the same? Well, let's now go for the one with
        // the shortest URL list.

        if(l1.url.length != l2.url.length)
            return l1.url.length < l2.url.length ? -1 : 1;

        // They're the same length (not unusual as most nodes like image
        // textures will only have a single URL listed) then just compare
        // the two strings. It's pretty arbitrary at this point, but we don't
        // really care anymore as we have the big-picture ordering that we
        // need now, so that's all that matters. Once we're down to the
        // individual nodes, we are not so picky about load ordering.
        //
        // Note that we could add other priority issues here - such as distance
        // from the currently bound viewpoint, or what is in view of the
        // current view frustum, but that's way too much work for now. Just let
        // it go for now. Maybe we'll look at something later.

        if (l1.url[0] == null)
            return 1;
        else if (l2.url[0] == null)
            return -1;

        return l1.url[0].compareTo(l2.url[0]);
    }

    /**
     * Compares another objects for equality to this comparator. The
     * comparators are equal if they have the same sort order.
     *
     * @param o The object to compare against this one
     * @return true if this is equal to this class
     */
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof LoadPriorityComparator))
            return false;

        return priorityMap.equals(((LoadPriorityComparator)o).priorityMap);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Instruct the comparator to re-read the priority list now. This will take
     * the current value of the property and replace the existing set of
     * priorities with the new list.
     */
    void updatePriorities() {
        priorityMap.clear();
        setupPriorities();
    }

    /**
     * Process the reading of the system property that defines the current
     * priorities.
     */
    private void setupPriorities() {
        String prop = (String)AccessController.doPrivileged(
            new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    // privileged code goes here, for example:
                    return System.getProperty(LoadConstants.SORT_ORDER_PROP,
                                              LoadConstants.DEFAULT_SORT_ORDER);
                }
            }
        );

        // Now tear the string apart. Comma separated list.
        List<String> all_tokens = new ArrayList<>();
        all_tokens.add(LoadConstants.SORT_SCRIPT);
        all_tokens.add(LoadConstants.SORT_INLINE);
        all_tokens.add(LoadConstants.SORT_TEXTURE);
        all_tokens.add(LoadConstants.SORT_CREATE);
        all_tokens.add(LoadConstants.SORT_PROTO);
        all_tokens.add(LoadConstants.SORT_AUDIO);
        all_tokens.add(LoadConstants.SORT_MOVIE);
        all_tokens.add(LoadConstants.SORT_SHADER);
        all_tokens.add(LoadConstants.SORT_OTHER);

        StringTokenizer strtok = new StringTokenizer(prop, ",");
        int priority = 1;

        priorityMap.put(LoadConstants.SORT_LOAD_URL, 0);

        while(strtok.hasMoreTokens()) {
            String type = strtok.nextToken();

            if(type.equals(LoadConstants.SORT_LOAD_URL))
                continue;

            priorityMap.put(type, priority++);
            all_tokens.remove(type);
        }

        // Anything left, just add it onto the list of priorities
        for (String all_token : all_tokens) {
            priorityMap.put(all_token, priority++);
        }
    }
}
