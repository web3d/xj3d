/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.browser;

// External imports
// none

// Local imports
// none

/**
 * Defines the requirements for accessing profiling data.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface ProfilingInfo {

    /** Return the total time to render the scene in nanoseconds
     * @return the total time to render the scene in nanoseconds
     */
    long getSceneRenderTime();

    /** Return the time spent in the cull stage in nanoseconds
     * @return the time spent in the cull stage in nanoseconds
     */
    long getSceneCullTime();

    /** Return the time spent in the sort stage in nanoseconds
     * @return the time spent in the sort stage in nanoseconds
     */
    long getSceneSortTime();

    /** Return the time spent in the draw stage in nanoseconds
     * @return the time spent in the draw stage in nanoseconds
     */
    long getSceneDrawTime();
}
