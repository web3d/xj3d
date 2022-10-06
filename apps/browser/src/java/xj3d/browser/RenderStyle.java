/*****************************************************************************
 *                        Web3d.org Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.0
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.browser;

// Standard library imports

// Application specific imports

/**
 * Common interface for render styles.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */

interface RenderStyle {
    /**
     * Sets this style to non active.
     */
    void reset();

    /**
     * Set the render styles linked to this one.  If one is enabled
     * the others will be disabled.
     *
     * @param linked The linked styles
     */
    void setLinkedStyles(RenderStyle[] linked);
}
