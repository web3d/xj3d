/*****************************************************************************
 *                        Web3d.org Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.filters;

// External imports
import java.util.HashMap;
import java.util.ArrayList;

// Local imports
import org.web3d.util.SimpleStack;

import org.web3d.vrml.sav.SAVException;

import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.NodeMarker;

import xj3d.filter.node.*;


/**
 * Filter for reducing IndexedTriangleSet, IndexedTriangleStripSet,
 * IndexedTriangleFanSet and IndexLineSet nodes to their most compact form.
 * This filter will remove duplicate coordinates from the coordinate point
 * array and recreate the index array to coorespond to the modified coordinate
 * points.
 * <p>
 * At present, handling of color, normal and texCoord fields of the indexed
 * geometry node set is not supported. If the coordinate array is already in
 * it's most reduced form (i.e. it is passed through unmodified), then the
 * color, normal and texCoord fields will be passed through unmodifed as well.
 * If the coordinate and index arrays are changed, the color, normal and
 * texCoord fields are dropped.
 *
 * @author Rex Melton
 * @version $Revision: 1.11 $
 */
public class ReindexFilter extends EncodedBaseFilter {
	
    /** Argument for epsilon */
    private static final String EPSILON = "-reindexEpsilon";

    /** Default value to use for epsilon calc */
    private static float EPSILON_DEFAULT = 0.0000000001f;

    /** The set of nodes that require translation */
    private static final String[] NODE = {
        "IndexedTriangleFanSet",
        "IndexedTriangleSet",
        "IndexedTriangleStripSet",
        "IndexedLineSet",
    };

    /** Indices into the NODE array, by type */
    private static final int TRIANGLE_FAN = 0;
    private static final int TRIANGLE = 1;
    private static final int TRIANGLE_STRIP = 2;
    private static final int LINE = 3;

    /** Flag indicating that we are processing a node that requires translation */
    private boolean intercept;

    /** Index into the NODE_* arrays for the node type that is being translated */
    private int interceptIndex;

    /** Geometry node wrapper converter */
    private GeometryConverter converter;

    /** The node that is being intercepted */
    private Encodable node;

    /** Map of DEF names to coord data */
    private HashMap<String, CoordinateProcessor> coordMap;

    /** Are we in a proto currently? */
    private boolean isProtoInstance;

    /** A stack of proto instance names */
    private SimpleStack protoStack;

    /** List of Proto and externProto declation names */
    private ArrayList<String> protoList;

    /** The epsilon to use */
    private float epsilon;

    /**
     * Default Constructor
     */
    public ReindexFilter() {

        isProtoInstance = false;
        intercept = false;
        interceptIndex = -1;

        coordMap = new HashMap<>();
        protoStack = new SimpleStack();
        protoList = new ArrayList<>();

        epsilon = EPSILON_DEFAULT;
		
		// disable encoding, only encode nodes of the required types
		encode(false);
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------

    /**
     * Declaration of the start of the document. The parameters are all of the
     * values that are declared on the header line of the file after the
     * <CODE>#</CODE> start. The type string contains the representation of
     * the first few characters of the file after the #. This allows us to
     * work out if it is VRML97 or the later X3D spec.
     * <p>
     * Version numbers change from VRML97 to X3D and aren't logical. In the
     * first, it is <code>#VRML V2.0</code> and the second is
     * <code>#X3D V1.0</code> even though this second header represents a
     * later spec.
     *
     * @param uri The URI of the file.
     * @param url The base URL of the file for resolving relative URIs
     *    contained in the file
     * @param encoding The encoding of this document - utf8 or binary
     * @param type The bytes of the first part of the file header
     * @param version The VRML version of this document
     * @param comment Any trailing text on this line. If there is none, this
     *    is null.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void startDocument(String uri,
        String url,
        String encoding,
        String type,
        String version,
        String comment)
        throws SAVException, VRMLException {

        super.startDocument(uri, url, encoding, type, version, comment);

        converter = new GeometryConverter(factory);
    }

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void endDocument() throws SAVException, VRMLException {

        protoList.clear();
        coordMap.clear();

        super.endDocument();
    }

    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not
     *   given for this node.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        if (protoList.contains(name)) {
            protoStack.push(name);
            isProtoInstance = true;
        }

		if (!intercept) {
            // check if this is a node that must be translated
            for (int i = 0; i < NODE.length; i++) {
                if (name.equals(NODE[i])) {
                    intercept = true;
                    interceptIndex = i;
                    encode(true);
					suppressCalls(true);
                }
            }
        }
		super.startNode(name, defName);
    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endNode() throws SAVException, VRMLException {

		NodeMarker marker = (NodeMarker)nodeStack.peek();
		String nodeName = marker.nodeName;
		
        if (!protoStack.isEmpty()) {
            String protoName = (String)protoStack.peek();
            if (nodeName.equals(protoName)) {
                protoStack.pop();
                if (protoStack.isEmpty()) {
                    isProtoInstance = false;
                }
            }
        }
		if (intercept) {
            if (nodeName.equals(NODE[interceptIndex])) {
				// a node that we are responsible for has ended.
				
				// get it's encoding
            	node = (Encodable)encStack.peek();
				
				// clean up the super's state (before enabling again)
				super.endNode();
				
				// convert it
                reindex();
				
				// return to 'idle' mode
                intercept = false;
                interceptIndex = -1;
                node = null;
				encode(false);
				suppressCalls(false);
				
			} else {
				super.endNode();
			}
        } else {
            super.endNode();
        }
    }

    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void useDecl(String defName) throws SAVException, VRMLException {

		/////////////////////////////////////////////////////////
		// only nodes that we are intercepting are cached in the
		// encMap, if a use is declared for a node that we have 
		// not cached, but is specified in a node that we must 
		// convert - then the USE will not be declared in the
		// output file. this is probably a bug.....
		/////////////////////////////////////////////////////////
		Encodable use = encMap.get(defName);
        if (use != null) {
			boolean convert_type = false;
			String name = use.getNodeName();
			for (int i = 0; i < NODE.length; i++) {
                if (name.equals(NODE[i])) {
					// a node that we are responsible is being USE'ed
					
					// setup for the conversion
                    interceptIndex = i;
                    node = use.clone(true);
					// convert it
                	reindex();
					// clean up
                	interceptIndex = -1;
                	node = null;
					convert_type = true;
                }
            }
			if (!convert_type) {
				if (intercept) {
					// a child node that has been cached is being used,
					// clone it and set it
					Encodable enc = use.clone(true);
					if (enc instanceof Coordinate) {
						enc.setUseName(defName);
					}
					Encodable parent = (Encodable)encStack.peek();
					NodeMarker marker = (NodeMarker)nodeStack.peek();
					parent.setValue(marker.fieldName, enc);
				} else {
					// rem: not sure when this might happen. a def from inside
					// a cloned node is being used in -something- that this filter
					// is not handling. guessing that encoding the def'ed node
					// and passing it along will be the right thing to do
					use.encode();
				}
			}
		} else {
			super.useDecl(defName);
		}
    }

    //---------------------------------------------------------------
    // Methods defined by ProtoHandler
    //---------------------------------------------------------------

    /**
     * Notification of the start of an ordinary (inline) proto declaration.
     * The proto has the given node name.
     *
     * @param name The name of the proto
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void startProtoDecl(String name)
        throws SAVException, VRMLException {
		
        protoList.add(name);
        super.startProtoDecl(name);
    }

    /**
     * Notification of the start of an EXTERNPROTO declaration of the given
     * name. Between here and the matching {@link #endExternProtoDecl()} call
     * you should only receive {@link #protoFieldDecl} calls.
     *
     * @param name The node name of the extern proto
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void startExternProtoDecl(String name)
        throws SAVException, VRMLException {
		
        protoList.add(name);
        super.startExternProtoDecl(name);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Convert and reindex the node.
     */
    private void reindex() {

        BaseGeometry geometry = (BaseGeometry)node;
        if (geometry == null)
            return;

        float[] point = null;
        Coordinate coord = (Coordinate)geometry.getCoordinate();
        if (coord != null) {

            CoordinateProcessor cp = null;
            String useName = coord.useName;
            if (useName != null) {
                cp = coordMap.get(useName);
            } else {
                point = coord.point;
                if (point != null) {
                    cp = new CoordinateProcessor(point, errorHandler, epsilon);
                    String defName = coord.defName;
                    if (defName != null) {
                        coordMap.put(defName, cp);
                    }
                }
            }
            // It is possible that we don't have coordinate points, in the case
            // of a node with an empty coordinate list. So, make sure we just
            // ignore this step if we don't have a processor.
            if (cp != null && cp.hasDuplicates()) {
                int[] index = null;
                switch(interceptIndex) {
                    case TRIANGLE_FAN:
                        IndexedTriangleFanSet itfs = (IndexedTriangleFanSet)node;
                        index = itfs.index;
                        itfs.setColor(null);
                        itfs.setNormal(null);
                        itfs.setTextureCoordinate(null);
                        break;

                    case TRIANGLE:
                        IndexedTriangleSet its = (IndexedTriangleSet)node;
                        index = its.index;
                        its.setColor(null);
                        its.setNormal(null);
                        its.setTextureCoordinate(null);
                        break;

                    case TRIANGLE_STRIP:
                        IndexedTriangleStripSet itss = (IndexedTriangleStripSet)node;
                        index = itss.index;
                        itss.setColor(null);
                        itss.setNormal(null);
                        itss.setTextureCoordinate(null);
                        break;

                    case LINE:
                        IndexedLineSet ils = (IndexedLineSet)node;
                        index = ils.coordIndex;
                        ils.setColor(null);
                        //break;
                }
                cp.processIndices(index);
                coord.num_point = cp.getNumCoords();

                if (interceptIndex == TRIANGLE) {
                    // remove triangles from the index array that contain
                    // duplicate indices. i.e. they are degenerate.
                    IndexedTriangleSet its = (IndexedTriangleSet)node;
                    int num_index = its.num_index;
                    int num_tri = num_index / 3;
                    int num_valid_tri = 0;
                    int dst = 0;
                    for (int i = 0; i < num_tri; i++) {
                        int src = i * 3;
                        int idx0 = index[src];
                        int idx1 = index[src+1];
                        int idx2 = index[src+2];
                        if (!((idx0 == idx1) || (idx1 == idx2) || (idx2 == idx0))) {
                            // this tri is valid
                            if (src != dst) {
                                index[dst++] = idx0;
                                index[dst++] = idx1;
                                index[dst++] = idx2;
                            } else {
                                dst += 3;
                            }
                            num_valid_tri++;
                        }
                    }
                    its.num_index = num_valid_tri * 3;
                }
            }
        }
        node.encode();
    }

    /**
     * Set the argument parameters to control the filter operation.
     *
     * @param args The array of argument parameters.
     */
    @Override
    public void setArguments(String[] args) {

        super.setArguments(args);

        for (int i = 0; i< args.length; i++) {
            if (args[i].equals(EPSILON)) {
                if (i + 1 >= args.length){

                    throw new IllegalArgumentException(
                        "Not enough args for ReindexFilter.  " +
                        "Expecting one more for epsilon.");
                }
                epsilon = Float.parseFloat(args[i+1]);
            }
        }
    }
}
