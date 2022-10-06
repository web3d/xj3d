/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007 - 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.replica;

// External imports
// none

// Local imports
// none

/**
 * Image capture constants.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface ReplicaConstants {

    /** Exit code for normal JVM exit */
    int NORMAL_JVM_EXIT = 0;

    /** Exit code for input file not found condition */
    int INPUT_FILE_NOT_FOUND = 1;

    /** Exit code for invalid input file format condition */
    int INVALID_INPUT_FILE_FORMAT = 2;

    /** Exit code for output file write error condition */
    int OUTPUT_FILE_ERROR = 5;

    /** Exit code for invalid filter argument condition */
    int INVALID_ARGUMENTS = 6;

    /** Exit code for out of memory */
    int OUT_OF_MEMORY_ERROR = 102;

    /* Exit code for an unhandled exception in a filter */
    int UNHANDLED_EXCEPTION = 101;

    /* Exit code for an unhandled Error being thrown */
    int UNHANDLED_ERROR = 103;

}
