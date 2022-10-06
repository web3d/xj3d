/*
Copyright (c) 1995-2014 held by the author(s).  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer
      in the documentation and/or other materials provided with the
      distribution.
    * Neither the names of the Naval Postgraduate School (NPS)
      Modeling Virtual Environments and Simulation (MOVES) Institute
      (http://www.nps.edu and http://www.movesinstitute.org)
      nor the names of its contributors may be used to endorse or
      promote products derived from this software without specific
      prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/
package org.web3d.vrml.renderer.common.nodes.geom2d;

import java.util.HashMap;
import java.util.Map;
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.LocalColorsListener;
import org.web3d.vrml.nodes.TexCoordGenModeListener;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLGeometryNodeType;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/** An abstract implementation of the BaseArc2D
 *
 * @author <a href="mailto:tdnorbra@nps.edu?subject=org.web3d.vrml.renderer.common.nodes.geom2d.BaseArc2D">Terry Norbraten, NPS MOVES</a>
 * @version $Id: BaseArc2D.java 12346 2015-09-04 01:03:17Z brutzman $
 */
public class BaseArc2D extends AbstractNode implements VRMLGeometryNodeType {

    /** Index of the start angle */
    protected static final int FIELD_START_ANGLE = LAST_NODE_INDEX + 1;

    /** Index of the end angle */
    protected static final int FIELD_END_ANGLE = LAST_NODE_INDEX + 2;

    /** Index of the arc radius */
    protected static final int FIELD_RADIUS = LAST_NODE_INDEX + 3;

    /** Last field declaration in this node */
    private static final int LAST_ARC2D_INDEX = FIELD_RADIUS;

    /** The number of fields in this node */
    private static final int NUM_FIELDS = LAST_ARC2D_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    protected float vfStartAngle, vfEndAngle, vfRadius;

    /**
     * Static constructor sets up the field declarations
     */
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_START_ANGLE,
            FIELD_END_ANGLE,
            FIELD_RADIUS,
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");

        fieldDecl[FIELD_START_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "startAngle");

        fieldDecl[FIELD_END_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "endAngle");

        fieldDecl[FIELD_RADIUS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "radius");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("startAngle", FIELD_START_ANGLE);
        fieldMap.put("endAngle", FIELD_END_ANGLE);
        fieldMap.put("radius", FIELD_RADIUS);
    }

     /**
     * Construct a default instance of this class with the bind flag set to
     * false and no time information set (effective value of zero).
     */
    protected BaseArc2D() {
        super("Arc2D");

        vfStartAngle = 0.0f;
        vfEndAngle = (float) (Math.PI/2f);
        vfRadius = 1.0f;

        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseArc2D(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {

            int index = node.getFieldIndex("startAngle");
            VRMLFieldData field = node.getFieldValue(index);
            vfStartAngle = field.floatValue;

            index = node.getFieldIndex("endAngle");
            field = node.getFieldValue(index);
            vfEndAngle = field.floatValue;

            index = node.getFieldIndex("radius");
            field = node.getFieldValue(index);
            vfRadius = field.floatValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    @Override
    public int getPrimaryType() {
        return TypeConstants.GeometryNodeType;
    }

    @Override
    public int getFieldIndex(String fieldName) {
        Integer index = fieldMap.get(fieldName);

        return (index == null) ? -1 : index;
    }

    @Override
    public int getNumFields() {
        return fieldDecl.length;
    }

    @Override
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if (index < 0  || index > LAST_ARC2D_INDEX)
            return null;

        return fieldDecl[index];
    }

    //----------------------------------------------------------
    // Methods defined by VRMLGeometryNodeType
    //----------------------------------------------------------

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean isCCW() {
        return true;
    }

    // NOTE: arcs may require emissive lighting
    @Override
    public boolean isLightingEnabled() {
        return true;
    }

    @Override
    public void setTextureCount(int count) {
        // default implementation does nothing
    }

    @Override
    public int getNumSets() {
        return 0;
    }

    @Override
    public String getTexCoordGenMode(int setNum) {
        return null;
    }

    @Override
    public boolean hasLocalColors() {
        return false;
    }

    @Override
    public boolean hasLocalColorAlpha() {
        return false;
    }

    @Override
    public void addLocalColorsListener(LocalColorsListener l) {
        // do nothing
    }

    @Override
    public void removeLocalColorsListener(LocalColorsListener l) {
        // do nothing
    }

    @Override
    public void addTexCoordGenModeChanged(TexCoordGenModeListener l) {
        // do nothing
    }

    @Override
    public void removeTexCoordGenModeChanged(TexCoordGenModeListener l) {
        // do nothing
    }

    @Override
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_START_ANGLE:
                if(!inSetup)
                    throwInitOnlyWriteException("size");
                vfStartAngle = value;
                break;

            case FIELD_END_ANGLE:
                if(!inSetup)
                    throwInitOnlyWriteException("size");
                vfEndAngle = value;
                break;

            case FIELD_RADIUS:
               if(!inSetup)
                    throwInitOnlyWriteException("size");
                vfRadius = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    @Override
    public VRMLFieldData getFieldValue(int index)
        throws InvalidFieldException {

        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_START_ANGLE:
                fieldData.clear();
                fieldData.floatValue = vfStartAngle;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_END_ANGLE:
                fieldData.clear();
                fieldData.floatValue = vfEndAngle;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_RADIUS:
                fieldData.clear();
                fieldData.floatValue = vfRadius;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

} // end class file BaseArc2D.java
