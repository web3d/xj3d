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
package org.web3d.vrml.scripting.ecmascript.builtin;

// External imports
// none

// Local imports
import org.j3d.util.DefaultErrorReporter;
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Factory class for generating fields from a given node.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class ECMAFieldFactory implements FieldFactory {

    /**
     * Create a field given a name from the node.
     *
     * @param node The node to create the field from
     * @param name The name of the field to fetch
     * @param checkEventIn true if we should check for an event in
     * @return An instance of the field class representing the field
     */
    @Override
    public Object createField(VRMLNodeType node,
                             String name,
                             boolean checkEventIn) {

        int index = node.getFieldIndex(name);

        if(index == -1) {
            return null;
        }

        // Should check the VRML97 capabilities here
        VRMLFieldDeclaration decl = node.getFieldDeclaration(index);
        VRMLFieldData data = null;

        try {
            data = node.getFieldValue(index);
        } catch(FieldException fe) {
            // should never get to this position
            fe.printStackTrace(System.err);
        }

        Object ret_val = null;

        switch(decl.getFieldType())
        {
            case FieldConstants.SFBOOL:
                ret_val = data.booleanValue ? Boolean.TRUE : Boolean.FALSE;
                break;
            case FieldConstants.SFCOLOR:
                ret_val = new SFColor(data.floatArrayValues);
                break;
            case FieldConstants.SFCOLORRGBA:
                ret_val = new SFColorRGBA(data.floatArrayValues);
                break;
            case FieldConstants.SFFLOAT:
                ret_val = (double) data.floatValue;
                break;
            case FieldConstants.SFDOUBLE:
                ret_val = data.doubleValue;
                break;
            case FieldConstants.SFIMAGE:
                ret_val = new SFImage(data.intArrayValues, data.numElements);
                break;
            case FieldConstants.SFINT32:
                ret_val = data.intValue;
                break;
            case FieldConstants.SFNODE:
                ret_val = new SFNode(node,
                                     index,
                                     (VRMLNodeType)data.nodeValue);
                break;
            case FieldConstants.SFROTATION:
                ret_val = new SFRotation(data.floatArrayValues);
                break;
            case FieldConstants.SFSTRING:
                ret_val = data.stringValue;
                break;
            case FieldConstants.SFTIME:
                ret_val = data.doubleValue;
                break;
            case FieldConstants.SFVEC2F:
                ret_val = new SFVec2f(data.floatArrayValues);
                break;
            case FieldConstants.SFVEC3F:
                ret_val = new SFVec3f(data.floatArrayValues);
                break;
            case FieldConstants.SFVEC2D:
                ret_val = new SFVec2d(data.doubleArrayValues);
                break;
            case FieldConstants.SFVEC3D:
                ret_val = new SFVec3d(data.doubleArrayValues);
                break;
            case FieldConstants.MFBOOL:
                ret_val = new MFBool(data.booleanArrayValues,
                                     data.numElements);
                break;
            case FieldConstants.MFCOLOR:
                ret_val = new MFColor(data.floatArrayValues,
                                      data.numElements * 3);
                break;
            case FieldConstants.MFCOLORRGBA:
                ret_val = new MFColorRGBA(data.floatArrayValues,
                                      data.numElements * 4);
                break;
            case FieldConstants.MFFLOAT:
                ret_val = new MFFloat(data.floatArrayValues,
                                      data.numElements);
                break;
            case FieldConstants.MFDOUBLE:
                ret_val = new MFDouble(data.doubleArrayValues,
                                      data.numElements);
                break;
            case FieldConstants.MFINT32:
                ret_val = new MFInt32(data.intArrayValues,
                                      data.numElements);
                break;
            case FieldConstants.MFIMAGE:
                ret_val = new MFImage(data.intArrayValues,
                                      data.numElements);
                break;
            case FieldConstants.MFNODE:
                ret_val = new MFNode(node,
                                     index, data.nodeArrayValues,
                                     data.numElements);
                break;
            case FieldConstants.MFROTATION:
                ret_val = new MFRotation(data.floatArrayValues,
                                      data.numElements * 4);
                break;
            case FieldConstants.MFSTRING:
                ret_val = new MFString(data.stringArrayValues,
                                      data.numElements);
                break;
            case FieldConstants.MFTIME:
                ret_val = new MFTime(data.doubleArrayValues,
                                      data.numElements);
                break;
            case FieldConstants.MFVEC2F:
                ret_val = new MFVec2f(data.floatArrayValues,
                                      data.numElements * 2);
                break;
            case FieldConstants.MFVEC3F:
                ret_val = new MFVec3f(data.floatArrayValues,
                                      data.numElements * 3);
                break;
            case FieldConstants.MFVEC2D:
                ret_val = new MFVec2d(data.doubleArrayValues,
                                      data.numElements * 2);
                break;
            case FieldConstants.MFVEC3D:
                ret_val = new MFVec3d(data.doubleArrayValues,
                                      data.numElements * 3);
                break;
        }

        if(checkEventIn &&
           (ret_val instanceof FieldScriptableObject) &&
           (decl.getAccessType() == FieldConstants.EVENTIN)) {

           ((FieldScriptableObject)ret_val).setReadOnly();
        }

        return ret_val;
    }

    /**
     * Update a field given a name from the node.  Will return an updated
     * field or a new object as needed.
     *
     * @param field The field to update
     * @param node The node to create the field from
     * @param name The name of the field to fetch
     * @param checkEventIn true if we should check for an event in
     * @return An instance of the field class representing the field
     */
    @Override
    public Object updateField(Object field,
                             VRMLNodeType node,
                             String name,
                             boolean checkEventIn) {

        int index = node.getFieldIndex(name);

        if(index == -1) {
            System.out.println("ECMAFieldFactory: illegal node index -1");
            return null;
        }

        // Should check the VRML97 capabilities here
        VRMLFieldDeclaration decl = node.getFieldDeclaration(index);
        VRMLFieldData data = null;

        try {
            data = node.getFieldValue(index);
        } catch(FieldException fe) {
            // should never get to this position
            fe.printStackTrace(System.err);
        }

        // Default the return value to the value they handed us. Only overwrite if
        // needed by the update process.
        Object ret_val = field;

        switch(decl.getFieldType())
        {
            case FieldConstants.SFBOOL:
                ret_val = data.booleanValue ? Boolean.TRUE : Boolean.FALSE;
                break;
            case FieldConstants.SFCOLOR:
                ((SFColor)field).setRawData(data.floatArrayValues);
                break;
            case FieldConstants.SFCOLORRGBA:
                ((SFColorRGBA)field).setRawData(data.floatArrayValues);
                break;
            case FieldConstants.SFFLOAT:
                if(((Number)field).floatValue() != data.floatValue)
                    ret_val = (double) data.floatValue;
                break;
            case FieldConstants.SFIMAGE:
                ret_val = new SFImage(data.intArrayValues, data.numElements);
                break;
            case FieldConstants.SFINT32:
                if(((Number)field).intValue() != data.intValue)
                    ret_val = data.intValue;
                break;
            case FieldConstants.SFNODE:
                SFNode n = (SFNode) field;
                if (n.getImplNode() == data.nodeValue)
                    return field;
                else
                    ret_val = new SFNode(node,
                                         index,
                                         (VRMLNodeType)data.nodeValue);
                break;
            case FieldConstants.SFROTATION:
                ((SFRotation)field).setRawData(data.floatArrayValues);
                break;
            case FieldConstants.SFSTRING:
                ret_val = data.stringValue;
                break;
            case FieldConstants.SFTIME:
                if(((Number)field).doubleValue() != data.doubleValue)
                    ret_val = data.doubleValue;
                break;
            case FieldConstants.SFVEC2F:
                ((SFVec2f)field).setRawData(data.floatArrayValues);
                break;
            case FieldConstants.SFVEC3F:
                if (data.floatArrayValues == null)
                {
                    DefaultErrorReporter.getDefaultReporter().messageReport("ECMAFieldFactory null data value, reset to default");
                    data.floatArrayValues = new float[] {0.0f, 0.0f, 0.0f};
                    // TODO diagnose and fix root cause rather than many bandaids here
                }
                ((SFVec3f)field).setRawData(data.floatArrayValues);
                break;
            case FieldConstants.SFVEC4F:
                ((SFVec4f)field).setRawData(data.floatArrayValues);
                break;
            case FieldConstants.SFVEC2D:
                ((SFVec2d)field).setRawData(data.doubleArrayValues);
                break;
            case FieldConstants.SFVEC3D:
                ((SFVec3d)field).setRawData(data.doubleArrayValues);
                break;
            case FieldConstants.SFVEC4D:
                ((SFVec4d)field).setRawData(data.doubleArrayValues);
                break;
            case FieldConstants.SFMATRIX3F:
            case FieldConstants.SFMATRIX4F:
            case FieldConstants.SFMATRIX3D:
            case FieldConstants.SFMATRIX4D:
                break;
            case FieldConstants.MFBOOL:
                ((MFBool)field).updateRawData(data.booleanArrayValues,
                                              data.numElements);
                break;
            case FieldConstants.MFCOLOR:
                ((MFColor)field).updateRawData(data.floatArrayValues,
                                               data.numElements * 3);
                break;
            case FieldConstants.MFCOLORRGBA:
                ((MFColorRGBA)field).updateRawData(data.floatArrayValues,
                                                   data.numElements * 4);
                break;
            case FieldConstants.MFDOUBLE:
                ((MFDouble)field).updateRawData(data.doubleArrayValues,
                                                data.numElements);
                break;
            case FieldConstants.MFFLOAT:
                ((MFFloat)field).updateRawData(data.floatArrayValues,
                                               data.numElements);
                break;
            case FieldConstants.MFIMAGE:
                ((MFImage)field).updateRawData(data.intArrayValues,
                                               data.numElements);
                break;
            case FieldConstants.MFINT32:
                ((MFInt32)field).updateRawData(data.intArrayValues,
                                               data.numElements);
                break;
            case FieldConstants.MFNODE:
                ((MFNode)field).updateRawData(data.nodeArrayValues,
                                              data.numElements);
                break;
            case FieldConstants.MFROTATION:
                ((MFRotation)field).updateRawData(data.floatArrayValues,
                                                  data.numElements * 4);
                break;
            case FieldConstants.MFSTRING:
                ((MFString)field).updateRawData(data.stringArrayValues,
                                                data.numElements);
                break;
            case FieldConstants.MFTIME:
                ((MFTime)field).updateRawData(data.doubleArrayValues,
                                              data.numElements);
                break;
            case FieldConstants.MFVEC2F:
                ((MFVec2f)field).updateRawData(data.floatArrayValues,
                                               data.numElements * 2);
                break;
            case FieldConstants.MFVEC3F:
                ((MFVec3f)field).updateRawData(data.floatArrayValues,
                                               data.numElements * 3);
                break;
            case FieldConstants.MFVEC4F:
                ((MFVec4f)field).updateRawData(data.floatArrayValues,
                                               data.numElements * 4);
                break;
            case FieldConstants.MFVEC2D:
                ((MFVec2d)field).updateRawData(data.doubleArrayValues,
                                               data.numElements * 2);
                break;
            case FieldConstants.MFVEC3D:
                ((MFVec3d)field).updateRawData(data.doubleArrayValues,
                                               data.numElements * 3);
                break;
            case FieldConstants.MFVEC4D:
                ((MFVec4d)field).updateRawData(data.doubleArrayValues,
                                               data.numElements * 4);
                break;
            case FieldConstants.MFMATRIX3F:
            case FieldConstants.MFMATRIX4F:
            case FieldConstants.MFMATRIX3D:
            case FieldConstants.MFMATRIX4D:
                break;
        }

        if(checkEventIn &&
           (ret_val instanceof FieldScriptableObject) &&
           (decl.getAccessType() == FieldConstants.EVENTIN)) {

           ((FieldScriptableObject)ret_val).setReadOnly();
        }

        return ret_val;
    }
}
