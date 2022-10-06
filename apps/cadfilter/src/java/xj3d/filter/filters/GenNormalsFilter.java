/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2010
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
import java.util.*;

import javax.vecmath.*;

// Local imports
import org.web3d.vrml.sav.SAVException;

import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.NodeMarker;

import xj3d.filter.node.*;

/**
 * Generates normals if none are specified.  The only shapes accepted are
 * IndexedTriangleSet, IndexedTriangleFanSet and IndexedTriangleStripSet.
 * <p>
 *
 * This filter will take the average of all the normals of a point
 * as the normal provided.
 *
 * @author Russell Dodds
 * @version $Revision: 1.8 $
 */
public class GenNormalsFilter extends EncodedBaseFilter {

    /** Number of triangles found in the set */
    private int polygonCount;

    /** Holder of number vertices per face during processing */
    private int[] rawVerticesPerFace;

    /** Flag indicating that we are processing a node that requires translation */
    private boolean intercept;

	/** Flag indicating that we are sidepocketing nodes, possibly for
	 *  USE'ing */
	private boolean interceptOther;
	
	/** Map for managing normal values */
	private HashMap<Integer, Vector3f> normalMap;
	
	/**
	 * Constructor
	 */
    public GenNormalsFilter() {
		
        normalMap = new HashMap<>();
		
		intercept = false;
		// disable encoding, only encode nodes of the required types
		encode(false);
    }

    //----------------------------------------------------------
    // methods defined by ContentHandler
    //----------------------------------------------------------

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
		
        switch (name) {
            case "IndexedTriangleSet":
            case "IndexedTriangleFanSet":
            case "IndexedTriangleStripSet":
                intercept = true;
                encode(true);
                suppressCalls(true);
                break;
            case "Coordinate":
            case "Normal":
            case "Color":
            case "TextureCoordinate":
            case "MultiTextureCoordinate":
                if (!intercept && (defName != null)) {
                    // sidepocket def'ed nodes that might be used
                    // by the geometry that is our target
                    interceptOther = true;
                    encode(true);
                }
                break;
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
		
		if (intercept && nodeName.equals("IndexedTriangleSet") ||
			nodeName.equals("IndexedTriangleFanSet") ||
			nodeName.equals("IndexedTriangleStripSet")) {
			
			// get it's encoded object
			Encodable enc = (Encodable)encStack.peek();
			
			// clean up the super's state (before enabling again)
			super.endNode();
			
            switch (nodeName) {
                case "IndexedTriangleSet":
                    IndexedTriangleSet its = (IndexedTriangleSet)enc;
                    processIndexedTriangleSet(its);
                    break;
                case "IndexedTriangleFanSet":
                    IndexedTriangleFanSet itfs = (IndexedTriangleFanSet)enc;
                    processIndexedTriangleFanSet(itfs);
                    break;
                case "IndexedTriangleStripSet":
                    IndexedTriangleStripSet itss = (IndexedTriangleStripSet)enc;
                    processIndexedTriangleStripSet(itss);
                    break;
            }
			// push it along....
			enc.encode();
			
			// return to 'idle' mode
			intercept = false;
			encode(false);
			suppressCalls(false);
			
		} else if (interceptOther) {
			
			// a node of interest outside the scope of an intercepted
			// geometry node has ended, stop encoding
			interceptOther = false;
			encode(false);
			super.endNode();
			
		}  else {
			super.endNode();
		}
    }

    //-----------------------------------------------------------------------
    // Local Methods
    //-----------------------------------------------------------------------
	
	/**
	 *
	 */
	private void processIndexedTriangleSet(IndexedTriangleSet its) {
		
		int[] indices = its.index;
			
		float[] coordinates = null;
		Coordinate coord = (Coordinate)its.getCoordinate();
		if (coord != null) {
			coordinates = coord.point;
		}
		
		float[] normals = null;
		Normal normal_node = (Normal)its.getNormal();
		if (normal_node != null) {
			normals = normal_node.vector;
		}
		
		if ((normals == null) && (indices != null) && (coordinates != null)) {
			
			// process the index
			int num_index = its.num_index;
			int maxIndex = 0;
			for (int i = 0; i < num_index; i++) {
				if (indices[i] > maxIndex) {
					maxIndex = indices[i];
				}
			}
			
			int c1, c2, c3;
			float x, y, z;
			int len = indices.length;
			
			normalMap.clear();
			
			// create the faces
			for (int i = 0; i < len; i++) {
				
				// get all 3 coordinates
				c1 = indices[i++];
				c2 = indices[i++];
				c3 = indices[i];
				
				// create the vectors
				x = coordinates[c1*3] - coordinates[c2*3];
				y = coordinates[c1*3 + 1] - coordinates[c2*3 + 1];
				z = coordinates[c1*3 + 2] - coordinates[c2*3 + 2];
				Vector3f vec1 = new Vector3f(x, y, z);
				
				x = coordinates[c3*3] - coordinates[c2*3];
				y = coordinates[c3*3 + 1] - coordinates[c2*3 + 1];
				z = coordinates[c3*3 + 2] - coordinates[c2*3 + 2];
				Vector3f vec2 = new Vector3f(x, y, z);
				
				// perform cross product to create the normal
				Vector3f normal1 = new Vector3f();
				Vector3f normal2;
				
				normal1.cross(vec2, vec1);
				
				if (normal1.x == 0 && normal1.y == 0 && normal1.z == 0) {
					// Triangle is so small it has a 0 cross product, just use 0,0,1 as the normal
					
					normal1.z = 1;
				} else {
					normal1.normalize();
				}
				
				// average normals for c1
				if (normalMap.containsKey(c1)) {
					
					normal2 = normalMap.get(c1);
					normal2.add(normal1);
				} else {
					
					normal2 = new Vector3f(normal1.x, normal1.y, normal1.z);
					
				}
				normalMap.put(c1, normal2);
				
				
				// average normals for c2
				if (normalMap.containsKey(c2)) {
					
					normal2 = normalMap.get(c2);
					normal2.add(normal1);
				} else {
					
					normal2 = new Vector3f(normal1.x, normal1.y, normal1.z);
				}
				normalMap.put(c2, normal2);
				
				// average normals for c3
				if (normalMap.containsKey(c3)) {
					
					normal2 = normalMap.get(c3);
					normal2.add(normal1);
					
				} else {
					
					normal2 = new Vector3f(normal1.x, normal1.y, normal1.z);
				}
				normalMap.put(c3, normal2);
			}
			
			//len = inIndex.length;
			normals = new float[(maxIndex + 1) * 3];
			
			int idx = 0;
			for (int i = 0; i <= maxIndex; i++) {
				
				Vector3f normal = normalMap.get(i);
				idx = i * 3;
				if (normal == null) {
					
					// float form
					normals[idx] = 0f;
					normals[idx + 1] = 0f;
					normals[idx + 2] = 1f;
					
				} else {
					
					if (normal.length() == 0) {
						normal.z = 1;
					} else {
						normal.normalize();
					}
					// float form
					normals[idx] = normal.x;
					normals[idx + 1] = normal.y;
					normals[idx + 2] = normal.z;
				}
			}
			if (normal_node == null) {
				normal_node = (Normal)factory.getEncodable("Normal", null);
				its.setNormal(normal_node);
			}
			normal_node.setValue("vector", normals, normals.length);
		}
	}

    /**
     *
     */
    private void processIndexedTriangleFanSet(IndexedTriangleFanSet itfs) {

		int[] indices = itfs.index;
			
		float[] coordinates = null;
		Coordinate coord = (Coordinate)itfs.getCoordinate();
		if (coord != null) {
			coordinates = coord.point;
		}
		
		float[] normals = null;
		Normal normal_node = (Normal)itfs.getNormal();
		if (normal_node != null) {
			normals = normal_node.vector;
		}
		
		if ((normals == null) && (indices != null) && (coordinates != null)) {
			
			// process the index
			int num_index = itfs.num_index;
			int maxIndex = 0;
			for (int i = 0; i < num_index; i++) {
				if (indices[i] > maxIndex) {
					maxIndex = indices[i];
				}
			}
			
            // Check the output and adjust accordingly. For max size 3 then just
            // drop the coordinates out now and not do any processing. For anything
            // more than three we need to go through and triangulate whatever we
            // find.
            int max_poly_size = checkMaxPolySize(indices);

            // do nothing, no coordinates provided
            if (max_poly_size == 0)
                return;

            int c1, c2, c3;
            float x, y, z;
            int input_idx = 0;
            Vector3f normal1, normal2;

            normalMap.clear();

            for(int i = 0; i < polygonCount; i++) {

                if (rawVerticesPerFace[i] == 0) {

                    // do nothing, no coordinates provided
                    input_idx += rawVerticesPerFace[i] + 1;
                    continue;

                } else if (rawVerticesPerFace[i] == 1) {

                    //
                    normal1 = new Vector3f(0, 0, 1);
                    normalMap.put(indices[input_idx], normal1);

                    input_idx += rawVerticesPerFace[i] + 1;
                    continue;

                } else  if (rawVerticesPerFace[i] == 2) {

                    normal1 = new Vector3f(0, 0, 1);
                    normalMap.put(indices[input_idx], normal1);
                    normalMap.put(indices[input_idx + 1], normal1);

                    input_idx += rawVerticesPerFace[i] + 1;
                    continue;

                }

                // save the first 2 indecies
                c1 = indices[input_idx];
                c2 = indices[input_idx + 1];

                for(int j = 2; j < rawVerticesPerFace[i]; j++) {

                    // get the next index
                    c3 = indices[input_idx + j];

                    // perform the checks
                    x = coordinates[c1*3] - coordinates[c2*3];
                    y = coordinates[c1*3 + 1] - coordinates[c2*3 + 1];
                    z = coordinates[c1*3 + 2] - coordinates[c2*3 + 2];
                    Vector3f vec1 = new Vector3f(x, y, z);

                    x = coordinates[c3*3] - coordinates[c2*3];
                    y = coordinates[c3*3 + 1] - coordinates[c2*3 + 1];
                    z = coordinates[c3*3 + 2] - coordinates[c2*3 + 2];
                    Vector3f vec2 = new Vector3f(x, y, z);

                    // perform cross product to create the normal
                    normal1 = new Vector3f();
                    normal1.cross(vec2, vec1);

                    if (normal1.x == 0 && normal1.y == 0 && normal1.z == 0) {
                        // Triangle is so small it has a 0 cross product, just use 0,0,1 as the normal

                        normal1.z = 1;
                    } else {
                        normal1.normalize();
                    }

                    // average normals for c1
                    if (normalMap.containsKey(c1)) {

                        normal2 = normalMap.get(c1);
                        normal2.add(normal1);
                    } else {

                        normal2 = new Vector3f(normal1.x, normal1.y, normal1.z);
                    }
                    normalMap.put(c1, normal2);

                    // average normals for c2
                    if (normalMap.containsKey(c2)) {

                        normal2 = normalMap.get(c2);
                        normal2.add(normal1);
                    } else {

                        normal2 = new Vector3f(normal1.x, normal1.y, normal1.z);
                    }
                    normalMap.put(c2, normal2);

                    // average normals for c3
                    if (normalMap.containsKey(c3)) {

                        normal2 = normalMap.get(c3);
                        normal2.add(normal1);
                    } else {

                        normal2 = new Vector3f(normal1.x, normal1.y, normal1.z);
                    }
                    normalMap.put(c3, normal2);

                    // save the indices for the next check
                    c2 = c3;
                }

                // increament the base number, plus the -1 is accounted for
                input_idx += rawVerticesPerFace[i] + 1;
            }

            // now write out the normals
            normals = new float[(maxIndex + 1) * 3];

			int idx = 0;
            for (int i = 0; i <= maxIndex; i++) {

                Vector3f normal = normalMap.get(i);
				idx = i * 3;
                if (normal == null) {

                    normals[idx] = 1f;
                    normals[idx + 1] = 1f;
                    normals[idx + 2] = 1f;

                } else {
                    if (normal.length() == 0) {
                        normal.z = 1;
					} else {
                        normal.normalize();
					}

                    normals[idx] = normal.x;
                    normals[idx + 1] = normal.y;
                    normals[idx + 2] = normal.z;
                }
            }
			if (normal_node == null) {
				normal_node = (Normal)factory.getEncodable("Normal", null);
				itfs.setNormal(normal_node);
			}
			normal_node.setValue("vector", normals, normals.length);
        }
    }

    /**
     *
     */
    private void processIndexedTriangleStripSet(IndexedTriangleStripSet itss) {

		int[] indices = itss.index;
			
		float[] coordinates = null;
		Coordinate coord = (Coordinate)itss.getCoordinate();
		if (coord != null) {
			coordinates = coord.point;
		}
		
		float[] normals = null;
		Normal normal_node = (Normal)itss.getNormal();
		if (normal_node != null) {
			normals = normal_node.vector;
		}
		
		if ((normals == null) && (indices != null) && (coordinates != null)) {
			
			// process the index
			int num_index = itss.num_index;
			int maxIndex = 0;
			for (int i = 0; i < num_index; i++) {
				if (indices[i] > maxIndex) {
					maxIndex = indices[i];
				}
			}
			
            // Check the output and adjust accordingly. For max size 3 then just
            // drop the coordinates out now and not do any processing. For anything
            // more than three we need to go through and triangulate whatever we
            // find.
            int max_poly_size = checkMaxPolySize(indices);

            // do nothing, no coordinates provided
            if (max_poly_size == 0)
                return;

            int c1, c2, c3;
            float x, y, z;
            int input_idx = 0;
            Vector3f normal1, normal2;

            normalMap.clear();

            for (int i = 0; i < polygonCount; i++) {

                if (rawVerticesPerFace[i] == 0) {

                    // do nothing, no coordinates provided
                    input_idx += rawVerticesPerFace[i] + 1;
                    continue;

                } else if (rawVerticesPerFace[i] == 1) {

                    //
                    normal1 = new Vector3f(0, 0, 1);
                    normalMap.put(indices[input_idx], normal1);

                    input_idx += rawVerticesPerFace[i] + 1;
                    continue;

                } else if (rawVerticesPerFace[i] == 2) {

                    normal1 = new Vector3f(0, 0, 1);
                    normalMap.put(indices[input_idx], normal1);
                    normalMap.put(indices[input_idx + 1], normal1);

                    input_idx += rawVerticesPerFace[i] + 1;
                    continue;

                }

                // save the first 2 indecies
                c1 = indices[input_idx];
                c2 = indices[input_idx + 1];

                for(int j = 2; j < rawVerticesPerFace[i]; j++) {

                    // get the next index
                    c3 = indices[input_idx + j];

                    // perform the checks
                    x = coordinates[c1*3] - coordinates[c2*3];
                    y = coordinates[c1*3 + 1] - coordinates[c2*3 + 1];
                    z = coordinates[c1*3 + 2] - coordinates[c2*3 + 2];
                    Vector3f vec1 = new Vector3f(x, y, z);

                    x = coordinates[c3*3] - coordinates[c2*3];
                    y = coordinates[c3*3 + 1] - coordinates[c2*3 + 1];
                    z = coordinates[c3*3 + 2] - coordinates[c2*3 + 2];
                    Vector3f vec2 = new Vector3f(x, y, z);

                    // perform cross product to create the normal
                    normal1 = new Vector3f();

                    if (j % 2 == 0) {
                        normal1.cross(vec2, vec1);
                    } else {
                        normal1.cross(vec1, vec2);
                    }

                    if (normal1.x == 0 && normal1.y == 0 && normal1.z == 0) {
                        // Triangle is so small it has a 0 cross product, just use 0,0,1 as the normal

                        normal1.z = 1;
                    } else {
                        normal1.normalize();
                    }

                    // average normals for c1
                    if (normalMap.containsKey(c1)) {

                        normal2 = normalMap.get(c1);
                        normal2.add(normal1);
                    } else {

                        normal2 = new Vector3f(normal1.x, normal1.y, normal1.z);
                    }
                    normalMap.put(c1, normal2);

                    // average normals for c2
                    if (normalMap.containsKey(c2)) {

                        normal2 = normalMap.get(c2);
                        normal2.add(normal1);
                    } else {

                        normal2 = new Vector3f(normal1.x, normal1.y, normal1.z);
                    }
                    normalMap.put(c2, normal2);

                    // average normals for c3
                    if (normalMap.containsKey(c3)) {

                        normal2 = normalMap.get(c3);
                        normal2.add(normal1);
                    } else {

                        normal2 = new Vector3f(normal1.x, normal1.y, normal1.z);
                    }
                    normalMap.put(c3, normal2);

                    // save the indices for the next check
                    c1 = c2;
                    c2 = c3;
                }

                // increament the base number, plus the -1 is accounted for
                input_idx += rawVerticesPerFace[i] + 1;
            }

            // now write out the normals
            normals = new float[(maxIndex + 1) * 3];
			
			int idx = 0;
            for(int i = 0; i <= maxIndex; i++) {

                Vector3f normal = normalMap.get(i);
				idx = i * 3;
                if (normal == null) {

                    normals[idx] = 1f;
                    normals[idx + 1] = 1f;
                    normals[idx + 2] = 1f;

                } else {
                    if (normal.length() == 0) {
                        normal.z = 1;
					} else {
                        normal.normalize();
					}
                    normals[idx] = normal.x;
                    normals[idx + 1] = normal.y;
                    normals[idx + 2] = normal.z;
                }
            }
			if (normal_node == null) {
				normal_node = (Normal)factory.getEncodable("Normal", null);
				itss.setNormal(normal_node);
			}
			normal_node.setValue("vector", normals, normals.length);
        }
    }

    /**
     * Go through the coordIndex array and work out what the maximum polygon
     * size will be before we've done any processing. It does not define the
     * current maxPolySize variable.
     *
     * @return The maximum size that this check found
     */
    private int checkMaxPolySize(int[] indices) {

        int length = indices.length;

        int cur_size = 0;
        int max_size = 0;
        polygonCount = 0;

        for(int i = 0; i < length; i++) {
            if(indices[i] == -1) {
                if(cur_size > max_size)
                    max_size = cur_size;

                cur_size = 0;
                polygonCount++;
            } else {
                cur_size++;
            }
        }

        // One last check on the last index. The spec allows the user to not
        // need to specify -1 as the last value. If we don't check for this,
        // the max size would never be set.
        if((length != 0) && (indices[length - 1] != -1)) {
            if(cur_size > max_size)
                max_size = cur_size;

            polygonCount++;
        }

        rawVerticesPerFace = new int[polygonCount];
        int current_face = 0;

        for(int i = 0; i < length; i++) {
            if(indices[i] != -1) {
                rawVerticesPerFace[current_face]++;
            } else {
                current_face++;
                if(current_face < polygonCount)
                    rawVerticesPerFace[current_face] = 0;
            }
        }

        return max_size;
    }
}
