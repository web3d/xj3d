/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.node;

// External imports
import java.util.ArrayList;

// Local imports
import xj3d.filter.FieldValueHandler;

/**
 * Wrapper for the X3D MultiTextureCoordinate node.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class MultiTextureCoordinate extends BaseEncodable implements ITextureCoordinate {
    
    /** The TextureCoordinate nodes */
    private ArrayList<Encodable> texCoord;
    
    /**
     * Constructor
     */
    public MultiTextureCoordinate() {
        this(null);
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public MultiTextureCoordinate(String defName) {
        super("MultiTextureCoordinate", defName);
        texCoord = new ArrayList<>();
    }
    
    //----------------------------------------------------------
    // Methods defined by Encodable
    //----------------------------------------------------------
    
    /**
     * Clear the node fields to their initial values
     */
    @Override
    public void clear() {
        super.clear();
        texCoord.clear();
    }
    
    /**
     * Push the node contents to the ContentHandler.
     */
    @Override
    public void encode() {
        
        if (handler != null) {
            if (useName == null) {
                handler.startNode(nodeName, defName);
                
                super.encode();
                
                for (Encodable e : texCoord) {
                    e.encode();
                }
                
                handler.endNode();
            } else {
                handler.useDecl(useName);
            }
        }
    }
    
    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     */
    @Override
    public void setValue(String name, Object value) {
        
        if (name.equals("texCoord")) {
            if (value instanceof ITexture) {
                texCoord.add((Encodable)value);
            } else if (value instanceof Encodable[]) {
                Encodable[] enc = (Encodable[])value;
                for (Encodable enc1 : enc) {
                    if (enc1 instanceof ITexture) {
                        texCoord.add(enc1);
                    }
                }
            }
        } else {
            super.setValue(name, value);
        }
    }
    
    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     * @param len The number of values in the array.
     */
    @Override
    public void setValue(String name, Object value, int len) {
        
        if (name.equals("texCoord")) {
            if (value instanceof Encodable[]) {
                Encodable[] enc = (Encodable[])value;
                for (int i = 0; i < len; i++) {
                    if (enc[i] instanceof ITextureCoordinate) {
                        texCoord.add(enc[i]);
                    }
                }
            }
        } else {
            super.setValue(name, value, len);
        }
    }
    
    /**
     * Create and return a copy of this object.
     *
     * @param full true if the clone should contain a copy of
     * the complete contents of this node and it's children,
     * false returns a new instance of this node type.
     * @return a copy of this.
     */
    @Override
    public Encodable clone(boolean full) {
        MultiTextureCoordinate mtc = new MultiTextureCoordinate();
        copy(mtc, full);
        if (full) {
            for (Encodable e : texCoord) {
                mtc.addTextureCoordinate(e.clone(true));
            }
        }
        return(mtc);
    }
    
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    
    /**
     * Clear and set the TextureCoordinates of this node. 
     *
     * @param enc The TextureCoordinate nodes to set.
     * A value of null just performs a clear.
     */
    public void setTextureCoordinate(Encodable[] enc) {
        texCoord.clear();
        if (enc != null) {
            for (Encodable enc1 : enc) {
                if (enc1 instanceof ITextureCoordinate) {
                    texCoord.add(enc1);
                }
            }
        }
    }
    
    /**
     * Return the TextureCoordinates of this node. 
     *
     * @return The TextureCoordinate nodes of this node.
     */
    public Encodable[] getTextureCoordinate() {
        return(texCoord.toArray(new Encodable[texCoord.size()]));
    }
    
    /**
     * Add TextureCoordinates to this node. 
     *
     * @param enc The TextureCoordinate nodes to add to this node.
     */
    public void addTextureCoordinate(Encodable[] enc) {
        if (enc != null) {
            for (Encodable enc1 : enc) {
                if (enc1 instanceof ITextureCoordinate) {
                    texCoord.add(enc1);
                }
            }
        }
    }
    
    /**
     * Add a TextureCoordinate to this node. 
     *
     * @param enc A TextureCoordinate node to add to this node.
     */
    public void addTextureCoordinate(Encodable enc) {
        if ((enc != null) && (enc instanceof ITextureCoordinate)) {
            texCoord.add(enc);
        }
    }
}
