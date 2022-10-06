/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/
package org.web3d.parser.x3d;

/**
 * X3D Binary constants.  Align values with ISO specification.
 *
 * @author Alan Hudson
 * @version $Id: $
 */
public interface X3DBinaryConstants {
    String EXTERNAL_VOCABULARY_URI_STRING_OLD = "urn:external-vocabulary";
    String EXTERNAL_VOCABULARY_URI_STRING = "urn:web3d:x3d:fi-vocabulary-3.2";

    int QUANTIZED_FLOAT_ARRAY_ALGORITHM_ID = 32;
    int DELTA_ZLIB_INT_ARRAY_ALGORITHM_ID = 33;
    int QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID = 34;  // Original Xj3D impl
    int QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID2 = 35;
    int ZLIB_FLOAT_ARRAY_ALGORITHM_ID2 = 36;
    int QUANTIZED_DOUBLE_ARRAY_ALGORITHM_ID = 37;
    int ZLIB_DOUBLE_ARRAY_ALGORITHM_ID2 = 38;
    int QUANTIZED_ZLIB_DOUBLE_ARRAY_ALGORITHM_ID = 39;
    int RANGE_INT_ARRAY_ALGORITHM_ID = 40;

    int BYTE_ALGORITHM_ID = 50;  // unspeced prototype
}