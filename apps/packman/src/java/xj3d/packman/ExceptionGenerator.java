/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.packman;

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.sav.SAVException;

// External imports
// None

// Local imports
// None

/**
 * A filter which generates deliberate exceptions
 *
 */
public class ExceptionGenerator extends AbstractFilter {

	/** Flag to generate OutOfMemoryError */
	private boolean memory=false;

	/** Flag to generate exception/error in endDocument */
	private boolean endDocError=false;

	/** Flag to generate exception/error in endNode */
	private boolean endNodeError=false;

	/** Flag to generate exception/error in setArgs */
	private boolean setArgsError=false;

    /**
     * Create an instance of the filter.
     */
    public ExceptionGenerator() {
    }


    //----------------------------------------------------------
    // Overrides of AbstractFilter
    //----------------------------------------------------------


    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void endDocument() throws SAVException, VRMLException {
    	if (memory)
    		throw new OutOfMemoryError("Deliberate memory exhaustion in endField.");
    	if (endDocError)
    		throw new NullPointerException("Deliberate NPE exception in endDocuemnt");
    	else super.endDocument();
    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void endNode() throws SAVException, VRMLException {
    	if (memory)
    		throw new OutOfMemoryError("Deliberate memory exhaustion in endNode.");
    	if (endNodeError)
    		throw new IllegalArgumentException("Deliberate IAE exception in endNode");
    	else
    		super.endNode();

    }

    /**
     * Notification of the end of a field declaration. This is called only at
     * the end of an MFNode declaration. All other fields are terminated by
     * either {@link #useDecl(String)} or {@link #fieldValue(String)}.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void endField() throws SAVException, VRMLException {
    	if (memory)
    		throw new OutOfMemoryError("Deliberate memory exhaustion in endField.");
    }

    /**
     * Set the argument parameters to control the filter operation
     *
     * @param arg The array of argument parameters.
     */
    public void setArguments(String[] arg) {
    	super.setArguments(arg);
            for (String arg1 : arg) {
                if (null != arg1) {
                    switch (arg[i]) {
                        case "endDocError":
                            endDocError=true;
                            break;
                        case "endNodeError":
                            endNodeError=true;
                            break;
                        case "setArgsError":
                            setArgsError=true;
                            break;
                        case "memory":
                            memory=true;
                            break;
                    }
                }
            }
    	if (setArgsError)
    		throw new RuntimeException("Deliberate RuntimeException exception in setArgs");
    	throw new OutOfMemoryError("Testing");
    }
}
