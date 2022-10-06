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

/**
 * Validator which does nothing.  Useful in validating that
 * the resultant stream at least parses.
 *
 * @author Alan Hudson
 * @version
 */
public class IdentityFilterValidator extends NewAbstractFilter {
    private boolean fail;
    private String failMessage;

    public IdentityFilterValidator() {
    }

    @Override
    public void endDocument() {
        if (fail) {
            lastErrorCode = -1;
            FilterProcessingException fpe = new FilterProcessingException("Failed", -1);
            throw fpe;

            //throw new VRMLException("Told to fail");
        }
    }

    /**
     * Set the argument parameters to control the filter operation.
     *
     * @param arg The array of argument parameters.
     */
    @Override
    public void setArguments(String[] args) {

        super.setArguments(args);

        for( int i = 0; i< args.length; i++) {
            String argument = args[i];

            if (args[i].equals("-fail")) {
                fail = true;
            }
        }
    }
}
