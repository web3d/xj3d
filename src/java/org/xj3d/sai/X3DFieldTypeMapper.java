/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.sai;

import java.util.HashMap;
import java.util.Map;

import org.web3d.vrml.lang.FieldConstants;

import org.web3d.x3d.sai.X3DFieldTypes;

/**
 * A utility class for handling field type functions, Including:
 * <ul>
 * <li>Associating field type names with their enumerated type constants
 * and vice versa.</li>
 * <li>Translating between Xj3D internal FieldConstants types and spec
 * defined X3DFieldTypes.</li>
 * </ul>
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class X3DFieldTypeMapper {

    /** type translation from internal to external */
    private static final int[] fieldConstant = {

        FieldConstants.SFINT32, X3DFieldTypes.SFINT32,
        FieldConstants.MFINT32, X3DFieldTypes.MFINT32,
        FieldConstants.SFFLOAT, X3DFieldTypes.SFFLOAT,
        FieldConstants.MFFLOAT, X3DFieldTypes.MFFLOAT,
        FieldConstants.SFDOUBLE, X3DFieldTypes.SFDOUBLE,
        FieldConstants.MFDOUBLE, X3DFieldTypes.MFDOUBLE,
        FieldConstants.SFLONG, X3DFieldTypes.SFLONG,
        FieldConstants.MFLONG, X3DFieldTypes.MFLONG,
        FieldConstants.SFBOOL, X3DFieldTypes.SFBOOL,
        FieldConstants.MFBOOL, X3DFieldTypes.MFBOOL,
        FieldConstants.SFVEC2F, X3DFieldTypes.SFVEC2F,
        FieldConstants.MFVEC2F, X3DFieldTypes.MFVEC2F,
        FieldConstants.SFVEC2D, X3DFieldTypes.SFVEC2D,
        FieldConstants.MFVEC2D, X3DFieldTypes.MFVEC2D,
        FieldConstants.SFVEC3F, X3DFieldTypes.SFVEC3F,
        FieldConstants.MFVEC3F, X3DFieldTypes.MFVEC3F,
        FieldConstants.SFVEC3D, X3DFieldTypes.SFVEC3D,
        FieldConstants.MFVEC3D, X3DFieldTypes.MFVEC3D,
        FieldConstants.SFIMAGE, X3DFieldTypes.SFIMAGE,
        FieldConstants.MFIMAGE, X3DFieldTypes.MFIMAGE,
        FieldConstants.SFTIME, X3DFieldTypes.SFTIME,
        FieldConstants.MFTIME, X3DFieldTypes.MFTIME,
        FieldConstants.SFNODE, X3DFieldTypes.SFNODE,
        FieldConstants.MFNODE, X3DFieldTypes.MFNODE,
        FieldConstants.SFSTRING, X3DFieldTypes.SFSTRING,
        FieldConstants.MFSTRING, X3DFieldTypes.MFSTRING,
        FieldConstants.SFROTATION, X3DFieldTypes.SFROTATION,
        FieldConstants.MFROTATION, X3DFieldTypes.MFROTATION,
        FieldConstants.SFCOLOR, X3DFieldTypes.SFCOLOR,
        FieldConstants.MFCOLOR, X3DFieldTypes.MFCOLOR,
        FieldConstants.SFCOLORRGBA, X3DFieldTypes.SFCOLORRGBA,
        FieldConstants.MFCOLORRGBA, X3DFieldTypes.MFCOLORRGBA,

        // begin of 'undefined', but known types

        FieldConstants.SFVEC4F, UndefinedX3DFieldTypes.SFVEC4F,
        FieldConstants.MFVEC4F, UndefinedX3DFieldTypes.MFVEC4F,
        FieldConstants.SFVEC4D, UndefinedX3DFieldTypes.SFVEC4D,
        FieldConstants.MFVEC4D, UndefinedX3DFieldTypes.MFVEC4D,
        FieldConstants.SFMATRIX3F, UndefinedX3DFieldTypes.SFMATRIX3F,
        FieldConstants.MFMATRIX3F, UndefinedX3DFieldTypes.MFMATRIX3F,
        FieldConstants.SFMATRIX3D, UndefinedX3DFieldTypes.SFMATRIX3D,
        FieldConstants.MFMATRIX3D, UndefinedX3DFieldTypes.MFMATRIX3D,
        FieldConstants.SFMATRIX4F, UndefinedX3DFieldTypes.SFMATRIX4F,
        FieldConstants.MFMATRIX4F, UndefinedX3DFieldTypes.MFMATRIX4F,
        FieldConstants.SFMATRIX4D, UndefinedX3DFieldTypes.SFMATRIX4D,
        FieldConstants.MFMATRIX4D, UndefinedX3DFieldTypes.MFMATRIX4D,
    };

    /** The instance */
    private static X3DFieldTypeMapper instance;

    /** Abstract type map, key = (String)typeName, value = (Integer)constant */
    private Map<String,Integer> typeMap;

    /** Protected Constructor */
    protected X3DFieldTypeMapper( ) {
        initializeTypeMap( );
    }

    /**
     * Return the instance of the X3DFieldTypeMapper
     *
     * @return the instance of the X3DFieldTypeMapper
     */
    public static X3DFieldTypeMapper getInstance( ) {
        if ( instance == null ) {
            instance = new X3DFieldTypeMapper( );
        }
        return( instance );
    }

    /**
     * Return the field name that corresponds to the
     * X3DFieldTypes constant.
     *
     * @param type The X3DFieldTypes constant
     * @return The field type name. If the constant does not
     * correspond to a known type, null is returned.
     */
    public String getTypeName( int type ) {
        String typeName = null;
        for (Map.Entry<String,Integer> e : typeMap.entrySet( )) {
            if (type == e.getValue( )) {
                typeName = e.getKey( );
                break;
            }
        }
        return( typeName );
    }

    /**
     * Return the X3DFieldTypes constant that corresponds to the
     * field node type name.
     *
     * @param typeName The field type name.
     * @return the X3DFieldTypes constant that corresponds to the
     * named field type. If the named field type is unknown,
     * -1 is returned.
     */
    public int getX3DFieldType( String typeName ) {
        int type = -1;
        Integer typeVal = typeMap.get( typeName );
        if ( typeVal != null ) {
            type = typeVal;
        }
        return( type );
    }

    /**
     * Return the X3DFieldTypes constant that corresponds to the
     * FieldConstants type.
     *
     * @param internalType The FieldConstants type.
     * @return the X3DFieldTypes constant that corresponds.
     * If the field type is unknown, -1 is returned.
     */
    public int getX3DFieldType( int internalType ) {
        int type = -1;
        int length = fieldConstant.length;
        for ( int i = 0; i < length; i+=2 ) {
            if ( internalType == fieldConstant[i] ) {
                type = fieldConstant[i+1];
                break;
            }
        }
        return( type );
    }

    /**
     * Setup the map of type names to integer constant values.
     * This is done dynamically rather than statically as this
     * is only needed when Java SAI is being used.
     */
    private void initializeTypeMap( ) {
        typeMap = new HashMap<>( );

        typeMap.put("SFInt32", X3DFieldTypes.SFINT32);
        typeMap.put("MFInt32", X3DFieldTypes.MFINT32);
        typeMap.put("SFFloat", X3DFieldTypes.SFFLOAT);
        typeMap.put("MFFloat", X3DFieldTypes.MFFLOAT);
        typeMap.put("SFDouble", X3DFieldTypes.SFDOUBLE);
        typeMap.put("MFDouble", X3DFieldTypes.MFDOUBLE);
        typeMap.put("SFLong", X3DFieldTypes.SFLONG);
        typeMap.put("MFLong", X3DFieldTypes.MFLONG);
        typeMap.put("SFBool", X3DFieldTypes.SFBOOL);
        typeMap.put("MFBool", X3DFieldTypes.MFBOOL);
        typeMap.put("SFVec2f", X3DFieldTypes.SFVEC2F);
        typeMap.put("MFVec2f", X3DFieldTypes.MFVEC2F);
        typeMap.put("SFVec2d", X3DFieldTypes.SFVEC2D);
        typeMap.put("MFVec2d", X3DFieldTypes.MFVEC2D);
        typeMap.put("SFVec3f", X3DFieldTypes.SFVEC3F);
        typeMap.put("MFVec3f", X3DFieldTypes.MFVEC3F);
        typeMap.put("SFVec3d", X3DFieldTypes.SFVEC3D);
        typeMap.put("MFVec3d", X3DFieldTypes.MFVEC3D);
        typeMap.put("SFImage", X3DFieldTypes.SFIMAGE);
        typeMap.put("MFImage", X3DFieldTypes.MFIMAGE);
        typeMap.put("SFTime", X3DFieldTypes.SFTIME);
        typeMap.put("MFTime", X3DFieldTypes.MFTIME);
        typeMap.put("SFNode", X3DFieldTypes.SFNODE);
        typeMap.put("MFNode", X3DFieldTypes.MFNODE);
        typeMap.put("SFString", X3DFieldTypes.SFSTRING);
        typeMap.put("MFString", X3DFieldTypes.MFSTRING);
        typeMap.put("SFRotation", X3DFieldTypes.SFROTATION);
        typeMap.put("MFRotation", X3DFieldTypes.MFROTATION);
        typeMap.put("SFColor", X3DFieldTypes.SFCOLOR);
        typeMap.put("MFColor", X3DFieldTypes.MFCOLOR);
        typeMap.put("SFColorRGBA", X3DFieldTypes.SFCOLORRGBA);
        typeMap.put("MFColorRGBA", X3DFieldTypes.MFCOLORRGBA);

        // begin of 'undefined', but known types

        typeMap.put("SFVec4f", UndefinedX3DFieldTypes.SFVEC4F);
        typeMap.put("MFVec4f", UndefinedX3DFieldTypes.MFVEC4F);
        typeMap.put("SFVec4d", UndefinedX3DFieldTypes.SFVEC4D);
        typeMap.put("MFVec4d", UndefinedX3DFieldTypes.MFVEC4D);
        typeMap.put("SFMatrix3f", UndefinedX3DFieldTypes.SFMATRIX3F);
        typeMap.put("MFMatrix3f", UndefinedX3DFieldTypes.MFMATRIX3F);
        typeMap.put("SFMatrix3d", UndefinedX3DFieldTypes.SFMATRIX3D);
        typeMap.put("MFMatrix3d", UndefinedX3DFieldTypes.MFMATRIX3D);
        typeMap.put("SFMatrix4f", UndefinedX3DFieldTypes.SFMATRIX4F);
        typeMap.put("MFMatrix4f", UndefinedX3DFieldTypes.MFMATRIX4F);
        typeMap.put("SFMatrix4d", UndefinedX3DFieldTypes.SFMATRIX4D);
        typeMap.put("MFMatrix4d", UndefinedX3DFieldTypes.MFMATRIX4D);
    }
}
