/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.importer;

import java.io.IOException;
import java.util.*;
import java.text.NumberFormat;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;

import org.j3d.loaders.ac3d.*;

// Local imports
import org.web3d.vrml.sav.*;

import org.j3d.util.ErrorReporter;
import org.web3d.util.StringArray;
import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.NonWeb3DFileParser;

/**
 * File parser implementation that reads AC3D files and generates an X3D stream
 * of events.
 * <p>
 *
 * @author Ben Yarger
 * @version 1.0
 */
public class AC3DFileParser implements NonWeb3DFileParser, Ac3dParseObserver
{

    /** The url of the current document */
    private String documentURL;

    /** Reference to the registered content handler if we have one */
    private ContentHandler contentHandler;

    /** Reference to the registered route handler if we have one */
    private RouteHandler routeHandler;

    /** Reference to the registered script handler if we have one */
    private ScriptHandler scriptHandler;

    /** Reference to the registered proto handler if we have one */
    private ProtoHandler protoHandler;

    /** Reference to the registered error handler if we have one */
    private ErrorReporter errorHandler;

    /** Reference to our Locator instance to hand to users */
    private Locator locator;

    /** The hash map of materials found in the .ac file being parsed. */
    private Map<Integer, String> materialHM;

    /** The hash map of original and generated object names. */
    private Map<String, String> objectHM;

    /** The object stack that maintains the hierarchy of objects found. */
    private Stack<Ac3dObject> objectStack;

    /** The index number to be added to the end of the generic name applied. */
    private int objectNameIndex;

    /** The index number to be added to the end of the generic name applied. */
    private int materialNameIndex;

    /** The maximum number of digits to round to when formatting data. */
    private static final int MAX_DIGITS = 10;

    /** Flag for 1 sided polygons with flat shading. */
    private static final int FLAT_1SIDED_POLY = 0;

    /** Flag for 1 sided poly line with flat shading. */
    private static final int FLAT_1SIDED_POLYLINE = 1;

    /** Flag for 1 sided line with flat shading. */
    private static final int FLAT_1SIDED_LINE = 2;

    /** Flag for 1 sided polygons with smooth shading. */
    private static final int SMOOTH_1SIDED_POLY = 16;

    /** Flag for 1 sided poly line with smooth shading. */
    private static final int SMOOTH_1SIDED_POLYLINE = 17;

    /** Flag for 1 sided line with smooth shading. */
    private static final int SMOOTH_1SIDED_LINE = 18;

    /** Flag for 2 sided polygons with flat shading. */
    private static final int FLAT_2SIDED_POLY = 32;

    /** Flag for 2 sided poly line with flat shading. */
    private static final int FLAT_2SIDED_POLYLINE = 33;

    /** Flag for 2 sided line with smooth shading. */
    private static final int FLAT_2SIDED_LINE = 34;

    /** Flag for 2 sided polygon with smooth shading. */
    private static final int SMOOTH_2SIDED_POLY = 48;

    /** Flag for 2 sided poly line with smooth shading. */
    private static final int SMOOTH_2SIDED_POLYLINE = 49;

    /** Flag for 2 sided line with smooth shading. */
    private static final int SMOOTH_2SIDED_LINE = 50;

    /**
     * Create a new instance of this parser.
     */
    public AC3DFileParser()
    {
        materialHM = new HashMap<>();
        objectHM = new HashMap<>();
        objectStack = new Stack<>();
        objectNameIndex = 0;
        materialNameIndex = 0;
    }

    //---------------------------------------------------------------
    // Methods defined by NonWeb3DFileParser
    //---------------------------------------------------------------

    /**
     * Initialise the internals of the parser at start up. If you are not using
     * the detailed constructors, this needs to be called to ensure that all
     * internal states are correctly set up.
     */
    @Override
    public void initialize()
    {
        materialHM = new HashMap<>();
        objectHM = new HashMap<>();
        objectStack = new Stack<>();
        objectNameIndex = 0;
        materialNameIndex = 0;
    }

    /**
     * Set the base URL of the document that is about to be parsed. Users
     * should always call this to make sure we have correct behaviour for the
     * ContentHandler's <code>startDocument()</code> call.
     * <p>
     * The URL is cleared at the end of each document run. Therefore it is
     * imperative that it gets called each time you use the parser.
     *
     * @param url The document url to set
     */
    @Override
    public void setDocumentUrl(String url)
    {
        documentURL = url;
    }

     /**
     * Fetch the locator used by this parser. This is here so that the user of
     * this parser can ask for it and set it before calling startDocument().
     * Once the scene has started parsing in this class it is too late for the
     * locator to be set. This parser does set it internally when asked for a
     * {@link org.web3d.parser.x3d.X3DRelaxedParser#Scene()} but there may be other times when it is not set.
     *
     * @return The locator used for syntax errors
     */
    @Override
    public Locator getDocumentLocator()
    {
        return locator;
    }

    /**
     * Set the content handler instance.
     *
     * @param ch The content handler instance to use
     */
    @Override
    public void setContentHandler(ContentHandler ch)
    {
        contentHandler = ch;
    }

    /**
     * Set the route handler instance.
     *
     * @param rh The route handler instance to use
     */
    @Override
    public void setRouteHandler(RouteHandler rh)
    {
        routeHandler = rh;
    }

    /**
     * Set the script handler instance.
     *
     * @param sh The script handler instance to use
     */
    @Override
    public void setScriptHandler(ScriptHandler sh)
    {
        scriptHandler = sh;
    }

    /**
     * Set the proto handler instance.
     *
     * @param ph The proto handler instance to use
     */
    @Override
    public void setProtoHandler(ProtoHandler ph)
    {
        protoHandler = ph;
    }

    /**
     * Set the error handler instance.
     *
     * @param eh The error handler instance to use
     */
    @Override
    public void setErrorHandler(ErrorHandler eh)
    {
        errorHandler = eh;

        if(eh != null)
            eh.setDocumentLocator(getDocumentLocator());
    }

    /**
     * Set the error reporter instance. If this is also an ErrorHandler
     * instance, the document locator will also be set.
     *
     * @param eh The error handler instance to use
     */
    @Override
    public void setErrorReporter(ErrorReporter eh)
    {
        if(eh instanceof ErrorHandler)
            setErrorHandler((ErrorHandler)eh);
        else
            errorHandler = eh;
    }


    //---------------------------------------------------------------
    // Methods defined by Ac3dParseObserver
    //---------------------------------------------------------------

    /**
     * A material block has been read from the file. Callback to handle
     * materials as they are found.
     *
     * @param mat The material definition
     * @return true if to keep reading
     */
    @Override
    public boolean materialComplete(Ac3dMaterial mat)
    {
        if(contentHandler instanceof BinaryContentHandler)
        {
            BinaryContentHandler bch = (BinaryContentHandler)contentHandler;
            materialHM.put(mat.getIndex(), ("material"+materialNameIndex));

            contentHandler.startNode("Transform", null);
            contentHandler.startField("children");
            contentHandler.startNode("Shape", null);
            contentHandler.startField("appearance");
            contentHandler.startNode("Appearance", null);
            contentHandler.startField("material");
            contentHandler.startNode("Material", ("material"+materialNameIndex));
            materialNameIndex++;

            contentHandler.startField("diffuseColor");
            float[] diffuse = new float[3];
            mat.getRGBColor(diffuse);
            bch.fieldValue(diffuse, 3);

            contentHandler.startField("ambientIntensity");
            float[] ambient = new float[3];
            mat.getAmbientColor(ambient);
            float ambientValue = (ambient[0] + ambient[1] + ambient[2])/3.0f;
            bch.fieldValue(ambientValue);

            contentHandler.startField("emissiveColor");
            float[] emissive = new float[3];
            mat.getEmissiveColor(emissive);
            bch.fieldValue(emissive, 3);

            contentHandler.startField("shininess");
            float shininessFloat = mat.getShininess()/128.0f;
            bch.fieldValue(shininessFloat);

            contentHandler.startField("specularColor");
            float[] specular = new float[3];
            mat.getSpecularColor(specular);
            bch.fieldValue(specular, 3);

            contentHandler.startField("transparency");
            float transparencyFloat = mat.getTransparency();
            bch.fieldValue(transparencyFloat);

            contentHandler.endNode();   // Material
            //contentHandler.endField();    // material
            contentHandler.endNode();   // Appearance
            //contentHandler.endField();    //apppearance
            contentHandler.endNode();   // Shape
            contentHandler.endField();  // children
            contentHandler.endNode();   // Transform
        }
        else
        {
            StringContentHandler sch = (StringContentHandler)contentHandler;
            materialHM.put(mat.getIndex(), ("material"+materialNameIndex));
            StringArray output = new StringArray();
            NumberFormat formatter = NumberFormat.getInstance();
            formatter.setMaximumFractionDigits(MAX_DIGITS);

            contentHandler.startNode("Transform", null);
            contentHandler.startField("children");
            contentHandler.startNode("Shape", null);
            contentHandler.startField("appearance");
            contentHandler.startNode("Appearance", null);
            contentHandler.startField("material");
            contentHandler.startNode("Material", ("material"+materialNameIndex));
            materialNameIndex++;

            output.clear();
            contentHandler.startField("diffuseColor");
            float[] diffuse = new float[3];
            mat.getRGBColor(diffuse);
            output.add(formatter.format(diffuse[0]));
            output.add(formatter.format(diffuse[1]));
            output.add(formatter.format(diffuse[2]));
            sch.fieldValue(output.toArray());

            contentHandler.startField("ambientIntensity");
            float[] ambient = new float[3];
            mat.getAmbientColor(ambient);
            float ambientFloat = (ambient[0] + ambient[1] + ambient[2])/3.0f;
            String ambientStr = formatter.format(ambientFloat);
            sch.fieldValue(ambientStr);

            output.clear();
            contentHandler.startField("emissiveColor");
            float[] emissive = new float[3];
            mat.getEmissiveColor(emissive);
            output.add(formatter.format(emissive[0]));
            output.add(formatter.format(emissive[1]));
            output.add(formatter.format(emissive[2]));
            sch.fieldValue(output.toArray());

            contentHandler.startField("shininess");
            float shininessFloat = mat.getShininess()/128.0f;
            String shininessString = formatter.format(shininessFloat);
            sch.fieldValue(shininessString);

            output.clear();
            contentHandler.startField("specularColor");
            float[] specular = new float[3];
            mat.getSpecularColor(specular);
            output.add(formatter.format(specular[0]));
            output.add(formatter.format(specular[1]));
            output.add(formatter.format(specular[2]));
            sch.fieldValue(output.toArray());

            contentHandler.startField("transparency");
            float transparencyFloat = mat.getTransparency();
            String transparencyString = formatter.format(transparencyFloat);
            sch.fieldValue(transparencyString);

            contentHandler.endNode();   // Material
            //contentHandler.endField();    // material
            contentHandler.endNode();   // Appearance
            //contentHandler.endField();    // appearance
            contentHandler.endNode();   // Shape
            contentHandler.endField();  // children
            contentHandler.endNode();   // Transform
        }

        return true;
    }

    /**
     * Callback for when an object has been loaded by the parser.Checks the
     * object stack to create the correct parent child relationship and then
     * passes the object along for processing.
     *
     * @param parent The parent object that contains this surface
     * @param obj The object that was just read
     * @return true if to keep reading
     */
    @Override
    public boolean objectComplete(Ac3dObject parent, Ac3dObject obj)
    {
        if(parent == null)          // Ignoring scene equivalent object
        {
            while(!objectStack.empty())
            {
                closeObjectTag(objectStack.pop());
            }

            return true;
        }

        if (objectStack.empty())
        {
                objectStack.push(obj);
        }
        else
        {
            boolean continueProcess = true;

            while(!objectStack.empty() && continueProcess)
            {
                if(objectStack.peek().getName() == null && parent.getName() == null)
                {
                    continueProcess = false;
                }
                else if(objectStack.peek().getName() == null || parent.getName() == null)
                {
                    closeObjectTag(objectStack.pop());
                }
                else if(objectStack.peek().getName().compareToIgnoreCase(parent.getName()) != 0)
                {
                    closeObjectTag(objectStack.pop());
                }
                else
                {
                    continueProcess = false;
                }
            }

            objectStack.push(obj);
        }

        createObjectTag(obj);

        return true;
    }

    /**
     * Callback for when a surface definition from the parser has been read.
     *
     * @param obj The parent object that contains this surface
     * @param surf The surface object that has been read
     * @return true if to keep reading
     */
    @Override
    public boolean surfaceComplete(Ac3dObject obj, Ac3dSurface surf)
    {
        return true;
    }

    //---------------------------------------------------------------
    // Methods defined by AC3DFileParser
    //---------------------------------------------------------------

    /**
     * Creates transform data.
     *
     * @param obj The object definition.
     */
    public void createTransformTag(Ac3dObject obj)
    {
        if(contentHandler instanceof BinaryContentHandler)
        {
            BinaryContentHandler bch = (BinaryContentHandler)contentHandler;

            float[] position = obj.getLocation();
            float[] rotation = obj.getRotation();

            Matrix3f rotationMatrix = new Matrix3f(rotation);
            AxisAngle4f rotationAxisAngle = new AxisAngle4f();
            rotationAxisAngle.set(rotationMatrix);

            float[] angleAxis = new float[4];
            rotationAxisAngle.get(angleAxis);

            contentHandler.startNode("Transform", null);
            contentHandler.startField("translation");
            bch.fieldValue(position, 3);

            contentHandler.startField("rotation");
            bch.fieldValue(angleAxis, 4);

            contentHandler.startField("children");
        }
        else
        {
            StringContentHandler sch = (StringContentHandler)contentHandler;
            StringArray output = new StringArray();
            NumberFormat formatter = NumberFormat.getInstance();
            formatter.setMaximumFractionDigits(MAX_DIGITS);

            float[] position = obj.getLocation();
            float[] rotation = obj.getRotation();

            Matrix3f rotationMatrix = new Matrix3f(rotation);
            AxisAngle4f rotationAxisAngle = new AxisAngle4f();
            rotationAxisAngle.set(rotationMatrix);

            float[] angleAxis = new float[4];
            rotationAxisAngle.get(angleAxis);

            output.clear();
            contentHandler.startNode("Transform", null);
            contentHandler.startField("translation");
            output.add(formatter.format(position[0]));
            output.add(formatter.format(position[1]));
            output.add(formatter.format(position[2]));
            sch.fieldValue(output.toArray());

            output.clear();
            contentHandler.startField("rotation");
            output.add(formatter.format(angleAxis[0]));
            output.add(formatter.format(angleAxis[1]));
            output.add(formatter.format(angleAxis[2]));
            output.add(formatter.format(angleAxis[3]));
            sch.fieldValue(output.toArray());

            contentHandler.startField("children");
        }
    }

    /**
     * Adds closing transform tags.
     */
    public void closeTransformTag()
    {
        contentHandler.endField();  // children
        contentHandler.endNode();   // Transform
    }

    /**
     * Create opening tags for the basic object types.
     *
     * @param obj The Object definition.
     * @return
     */
    public boolean createObjectTag(Ac3dObject obj)
    {
//      StringContentHandler sch = (StringContentHandler)contentHandler;
//      StringArray output = new StringArray();
//      StringArray texCoordIndexSA = new StringArray();
//      NumberFormat formatter = NumberFormat.getInstance();
//      formatter.setMaximumFractionDigits(MAX_DIGITS);
        switch (obj.getType()) {
            case "world":
                return true;
            case "group":
                createTransformTag(obj);
                contentHandler.startNode("Group", ("group"+objectNameIndex));
                objectHM.put(obj.getName(), ("group"+objectNameIndex));
                objectNameIndex++;
                contentHandler.startField("children");
                break;
            case "poly":
                if(obj.getNumSurfaces() > 0)
                {
                    createTransformTag(obj);

                    contentHandler.startNode("Shape", ("shape"+objectNameIndex));
                    objectHM.put(obj.getName(), ("shape"+objectNameIndex));
                    objectNameIndex++;

                    createMaterial(obj);

                    int flagValue = obj.getSurface(0).getFlags();

                    switch(flagValue)
                    {
                        case FLAT_1SIDED_POLY:
                        case FLAT_2SIDED_POLY:
                        case SMOOTH_1SIDED_POLY:
                        case SMOOTH_2SIDED_POLY:
                            createPolyGeometry(obj);
                            break;
                        case FLAT_1SIDED_POLYLINE:
                        case FLAT_2SIDED_POLYLINE:
                        case SMOOTH_1SIDED_POLYLINE:
                        case SMOOTH_2SIDED_POLYLINE:
                        case FLAT_1SIDED_LINE:
                        case FLAT_2SIDED_LINE:
                        case SMOOTH_1SIDED_LINE:
                        case SMOOTH_2SIDED_LINE:
                            createPolyLineGeometry(obj);
                            break;
                        default:
                            System.out.println("Unrecognized flag value specified.");
                            return false;
                    }
                    //////////////////////////////////////////////////
                    // rem: this was not here, think it should be
                    contentHandler.endNode();   // Shape
                    //////////////////////////////////////////////////

                    closeTransformTag();
                }
                else
                {
                    System.out.println("Shape without any surfaces: " +obj.getName());
                    return false;
                }   break;
            default:
                System.out.println("Unknown object type returned: " + obj.getType());
            return false;
        }

        return true;
    }

    /**
     * Creates the appearance and material.
     *
     * @param obj The object definition.
     * @return
     */
    public boolean createMaterial(Ac3dObject obj)
    {
        StringContentHandler sch = (StringContentHandler)contentHandler;

        contentHandler.startField("appearance");
        contentHandler.startNode("Appearance", null);

        String materialDef = materialHM.get(obj.getSurface(0).getMaterial());
        contentHandler.useDecl(materialDef);

        if(obj.getTexture() != null)
        {
            contentHandler.startField("texture");
            contentHandler.startNode("ImageTexture", null);
            contentHandler.startField("url");
            sch.fieldValue(obj.getTexture());
            contentHandler.endNode();   // ImageTexture
            //contentHandler.endField();    // texture
        }

        contentHandler.endNode();   // Appearance
        //contentHandler.endField();    // appearance

        return true;
    }

    /**
     * Creates the geometry for poly objects.
     *
     * @param obj The object definition.
     * @return
     */
    public boolean createPolyGeometry(Ac3dObject obj)
    {
        if(contentHandler instanceof BinaryContentHandler)
        {
            BinaryContentHandler bch = (BinaryContentHandler)contentHandler;
            StringContentHandler sch = (StringContentHandler)contentHandler;

            contentHandler.startField("geometry");
            contentHandler.startNode("IndexedFaceSet", null);
            contentHandler.startField("coord");
            contentHandler.startNode("Coordinate", null);
            contentHandler.startField("point");

            float[] vertices = obj.getVertices();
            bch.fieldValue(vertices, vertices.length);

            contentHandler.endNode();   // Coordinate
            //contentHandler.endField();    // coord

            contentHandler.startField("coordIndex");

            ArrayList<Integer> texCoordIndexSA = new ArrayList<>();
            ArrayList<Integer> vertexIndexes = new ArrayList<>();
            int counter = 0;

            for(int i = 0; i < obj.getNumSurfaces(); i++)
            {
                Ac3dSurface surface = obj.getSurface(i);
                int[] verticeIndexes = surface.getVerticesIndex();

                for(int j = 0; j < verticeIndexes.length; j++)
                {
                    vertexIndexes.add(verticeIndexes[j]);
                    texCoordIndexSA.add(counter);
                    counter++;
                }

                vertexIndexes.add(-1);
                texCoordIndexSA.add(-1);
            }

            int[] vIndexes = new int[vertexIndexes.size()];

            for(int i = 0; i < vIndexes.length; i++)
            {
                vIndexes[i] = vertexIndexes.get(i);
            }

            bch.fieldValue(vIndexes, vIndexes.length);

            if(obj.getTexture() != null)
            {
                contentHandler.startField("texCoord");
                contentHandler.startNode("TextureCoordinate", null);
                contentHandler.startField("point");

                ArrayList<Float> texCoord = new ArrayList<>();

                for(int i = 0; i < obj.getNumSurfaces(); i++)
                {
                    Ac3dSurface surface = obj.getSurface(i);
                    float[] texCoordinates = surface.getTextureCoordinates();

                    for(int j = 0; j < texCoordinates.length; j++)
                    {
                        texCoord.add(texCoordinates[j]);
                    }
                }

                float[] texCoordArray = new float[texCoord.size()];

                for(int i = 0; i < texCoordArray.length; i++)
                {
                    texCoordArray[i] = texCoord.get(i);
                }

                bch.fieldValue(texCoordArray, texCoordArray.length);

                contentHandler.endNode();   // TextureCoordinate
                //contentHandler.endField();    // texCoord

                contentHandler.startField("texCoordIndex");

                int[] texCoordIndexArray = new int[texCoordIndexSA.size()];

                for(int i = 0; i < texCoordIndexArray.length; i++)
                {
                    texCoordIndexArray[i] = texCoordIndexSA.get(i);
                }

                bch.fieldValue(texCoordIndexArray, texCoordIndexArray.length);
            }

            contentHandler.startField("creaseAngle");
            double creaseAngle = obj.getCreaseAngle()*Math.PI/180;
            bch.fieldValue((float)creaseAngle);

            contentHandler.startField("solid");
            if(obj.getSurface(0).getFlags() == FLAT_1SIDED_POLY || obj.getSurface(0).getFlags() == SMOOTH_1SIDED_POLY)
            {
                sch.fieldValue("TRUE");
            }
            else
            {
                sch.fieldValue("FALSE");
            }
            //////////////////////////////////////////////////
            // rem: this was not here, think it should be
            contentHandler.endNode(); // IndexedFaceSet
            //contentHandler.endField();    // geometry
            //////////////////////////////////////////////////
        }
        else
        {
            StringContentHandler sch = (StringContentHandler)contentHandler;
            StringArray output = new StringArray();
            StringArray texCoordIndexSA = new StringArray();
            NumberFormat formatter = NumberFormat.getInstance();
            formatter.setMaximumFractionDigits(MAX_DIGITS);

            contentHandler.startField("geometry");
            contentHandler.startNode("IndexedFaceSet", null);
            contentHandler.startField("coord");
            contentHandler.startNode("Coordinate", null);
            contentHandler.startField("point");

            output.clear();
            float[] vertices = obj.getVertices();

            for(int i = 0; i < vertices.length; i++)
            {
                output.add(formatter.format(vertices[i]));
            }

            sch.fieldValue(output.toArray());
            contentHandler.endNode();   // Coordinate
            //contentHandler.endField();    // coord

            contentHandler.startField("coordIndex");
            output.clear();
            texCoordIndexSA.clear();
            int counter = 0;

            for(int i = 0; i < obj.getNumSurfaces(); i++)
            {
                Ac3dSurface surface = obj.getSurface(i);
                int[] verticeIndexes = surface.getVerticesIndex();

                for(int j = 0; j < verticeIndexes.length; j++)
                {
                    output.add(Integer.toString(verticeIndexes[j]));
                    texCoordIndexSA.add(Integer.toString(counter));
                    counter++;
                }

                output.add(Integer.toString(-1));
                texCoordIndexSA.add(Integer.toString(-1));
            }

            sch.fieldValue(output.toArray());

            if(obj.getTexture() != null)
            {
                contentHandler.startField("texCoord");
                contentHandler.startNode("TextureCoordinate", null);
                contentHandler.startField("point");

                output.clear();

                for(int i = 0; i < obj.getNumSurfaces(); i++)
                {
                    Ac3dSurface surface = obj.getSurface(i);
                    float[] texCoordinates = surface.getTextureCoordinates();

                    for(int j = 0; j < texCoordinates.length; j++)
                    {
                        output.add(formatter.format(texCoordinates[j]));
                    }
                }

                sch.fieldValue(output.toArray());

                contentHandler.endNode();   // TextureCoordinate
                //contentHandler.endField();    // texCoord

                contentHandler.startField("texCoordIndex");
                sch.fieldValue(texCoordIndexSA.toArray());
            }

            contentHandler.startField("creaseAngle");
            double creaseAngle = obj.getCreaseAngle()*Math.PI/180;
            String creaseAngleS = formatter.format(creaseAngle);
            sch.fieldValue(creaseAngleS);

            contentHandler.startField("solid");
            if(obj.getSurface(0).getFlags() == FLAT_1SIDED_POLY || obj.getSurface(0).getFlags() == SMOOTH_1SIDED_POLY)
            {
                sch.fieldValue("TRUE");
            }
            else
            {
                sch.fieldValue("FALSE");
            }
            //////////////////////////////////////////////////
            // rem: this was not here, think it should be
            contentHandler.endNode(); // IndexedFaceSet
            //contentHandler.endField();    // geometry
            //////////////////////////////////////////////////
        }

        return true;
    }

    /**
     * Creates the geometry for poly line and line objects.
     *
     * @param obj The object definition.
     * @return
     */
    public boolean createPolyLineGeometry(Ac3dObject obj)
    {
        if(contentHandler instanceof BinaryContentHandler)
        {
            BinaryContentHandler bch = (BinaryContentHandler)contentHandler;

            contentHandler.startField("geometry");
            contentHandler.startNode("IndexedLineSet", null);
            contentHandler.startField("coord");
            contentHandler.startNode("Coordinate", null);
            contentHandler.startField("point");

            float[] vertices = obj.getVertices();
            bch.fieldValue(vertices, vertices.length);

            contentHandler.endNode();   // Coordinate
            //contentHandler.endField();    // coord

            contentHandler.startField("coordIndex");

            List<Integer> vertexIndexes = new ArrayList<>();

            for(int i = 0; i < obj.getNumSurfaces(); i++)
            {
                Ac3dSurface surface = obj.getSurface(i);
                int[] verticeIndexes = surface.getVerticesIndex();

                for(int j = 0; j < verticeIndexes.length; j++)
                {
                    vertexIndexes.add(verticeIndexes[j]);
                }

                vertexIndexes.add(-1);
            }

            int[] vIndexes = new int[vertexIndexes.size()];

            for(int i = 0; i < vIndexes.length; i++)
            {
                vIndexes[i] = vertexIndexes.get(i);
            }

            bch.fieldValue(vIndexes, vIndexes.length);

            //////////////////////////////////////////////////
            // rem: this was not here, think it should be
            contentHandler.endNode(); // IndexedLineSet
            //contentHandler.endField();    // geometry
            //////////////////////////////////////////////////
        }
        else
        {
            StringContentHandler sch = (StringContentHandler)contentHandler;
            StringArray output = new StringArray();
            NumberFormat formatter = NumberFormat.getInstance();
            formatter.setMaximumFractionDigits(MAX_DIGITS);

            contentHandler.startField("geometry");
            contentHandler.startNode("IndexedLineSet", null);
            contentHandler.startField("coord");
            contentHandler.startNode("Coordinate", null);
            contentHandler.startField("point");

            output.clear();
            float[] vertices = obj.getVertices();

            for(int i = 0; i < vertices.length; i++)
            {
                output.add(formatter.format(vertices[i]));
            }

            sch.fieldValue(output.toArray());
            contentHandler.endNode(); // Coordinate
            //contentHandler.endField(); // coord

            contentHandler.startField("coordIndex");
            output.clear();

            for(int i = 0; i < obj.getNumSurfaces(); i++)
            {
                Ac3dSurface surface = obj.getSurface(i);
                int[] verticeIndexes = surface.getVerticesIndex();

                for(int j = 0; j < verticeIndexes.length; j++)
                {
                    output.add(formatter.format(verticeIndexes[j]));
                }

                output.add(formatter.format(-1));
            }

            sch.fieldValue(output.toArray());

            //////////////////////////////////////////////////
            // rem: this was not here, think it should be
            contentHandler.endNode(); // IndexedLineSet
            //contentHandler.endField();    // geometry
            //////////////////////////////////////////////////
        }

        return true;
    }

    /**
     * Closes the appropriate x3d tags at the end of object processing.
     *
     * @param obj The object definition.
     * @return
     */
    public boolean closeObjectTag(Ac3dObject obj)
    {
        switch (obj.getType()) {
            case "poly":
                break;
            case "group":
                closeTransformTag();
                contentHandler.endField();  // children
                contentHandler.endNode();   // Group
                break;
            case "world":
                return true;
            default:
                System.err.println("Error:");
                System.err.println("Unable to apply closing tags to unknown object type: " + obj.getType());
                System.err.println("Acceptable types are: poly, group and world.");
                return false;
        }

        return true;
    }

    /**
     * Begins processing the input stream from the parser.
     *
     * @param input The stream to read from
     * @param style The style or null or no styling
     * @return Null if no parsing issues or a messages detailing the issues
     * @throws IOException An I/O error while reading the stream
     * @throws VRMLParseException A parsing error occurred in the file
     * @throws SAVNotSupportedException The input file is not VRML97 UTF8
     *    encoded.
     */
    @Override
    public List<String> parse(InputSource input, String[] style)
        throws IOException, VRMLException
    {
        System.out.println("Begin parsing AC3D file.");

        objectNameIndex = 0;
        materialNameIndex = 0;

        contentHandler.startDocument(input.getURL(),
                                     input.getBaseURL(),
                                     "utf8",
                                     "#X3D",
                                     "V3.0",
                                     "Auto converted AC3D file");

        contentHandler.profileDecl("Interchange");
        contentHandler.componentDecl("Rendering:3");

        Ac3dParser parser = new Ac3dParser(input.getCharacterStream());
        parser.setParseObserver(this);
        parser.parse(true);

        while(!objectStack.empty())
        {
            closeObjectTag(objectStack.pop());
        }

        contentHandler.endDocument();

        return null;
    }
}
