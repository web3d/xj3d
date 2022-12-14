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

/**
 * A simple listener for application exit requests.
 * <p>
 *
 * The method gets called when the caller thinks it is time to close the
 * application.
 */
interface ExitListener
{
    /**
     * Request that the application be closed.
     */
    void exitNow();
}
