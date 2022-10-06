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

// Local imports
import org.web3d.vrml.sav.SAVException;

import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.NodeMarker;

import xj3d.filter.node.*;

/**
 * Converts a IndexedFaceSet to an IndexedTriangleSet if it can.  Otherwise
 * it will stay as an IFS.
 *
 * Right now this code assumes the IFS is all triangles.  Later versions will
 * actually triangulate the code for you.  Must have trailing -1 on coordIndex.
 *
 * @author Alan Hudson
 * @version $Revision: 1.6 $
 */
public class IFSToITSFilter extends EncodedBaseFilter {
	/** Flag indicating that we are processing a node that requires translation */
	private boolean intercept;

	/** Flag indicating that we are sidepocketing Coordinate nodes, possibly for
	 *  USE'ing */
	private boolean interceptCoordinate;

	private int cacheLength = 3;

	/**
	 * Create a new default filter for the conversion
	 */
	public IFSToITSFilter() {

		intercept = false;
		interceptCoordinate = false;
		// disable encoding, only encode nodes of the required types
		encode(false);
	}

	//----------------------------------------------------------
	// Methods defined by ContentHandler
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
                case "IndexedFaceSet":
                    intercept = true;
                    encode(true);
                    suppressCalls(true);
                    break;
                case "Coordinate":
                    if (!intercept && (defName != null)) {
                        // sidepocket def'ed Coordinate nodes that are
                        // outside the scope of an IFS - in case the
                        // IFS will use
                        interceptCoordinate = true;
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

		if (intercept) {

			NodeMarker marker = (NodeMarker)nodeStack.peek();
			String nodeName = marker.nodeName;

			if (nodeName.equals("IndexedFaceSet")) {
				// a node that we are responsible for has ended.

				// get it's encoded object
            	IndexedFaceSet ifs = (IndexedFaceSet)encStack.peek();

				// clean up the super's state (before enabling again)
				super.endNode();

				int[] coordIndex = ifs.coordIndex;
				int[] normalIndex = ifs.normalIndex;
				int[] texCoordIndex = ifs.texCoordIndex;
				Coordinate coord = (Coordinate)ifs.getCoordinate();
				Normal normal = (Normal)ifs.getNormal();
				TextureCoordinate texCoord = (TextureCoordinate)ifs.getTextureCoordinate();

				int idx = 0;
				int[] index = null;

				if (coordIndex != null) {

					int len = coordIndex.length;
					index = new int[len];

					int p1, p2;
					int start = coordIndex[0];
					for (int i = 0; i < len; i++) {

						if (i + 2 >= len) {
							break;
						}
						p1 = coordIndex[i + 1];
						p2 = coordIndex[i + 2];


						if (p1 == -1) {
							start = p2;
						}
						if (p2 == -1) {
							if (i + 3 >= len) {
								break;
							}
							start = coordIndex[i + 3];
							i = i + 2;
							continue;
						}

						index[idx++] = start;
						index[idx++] = p1;
						index[idx++] = p2;
					}
				}

				boolean reorder = true;

				if (ifs.normalPerVertex != true || ifs.colorPerVertex != true) {
					reorder = false;
				}

				if (normal != null && normal.defName != null) {
					if (findUSE(normal.defName))
						reorder = false;
				}

				if (texCoord != null && texCoord.defName != null) {
					if (findUSE(texCoord.defName))
						reorder = false;
				}

				//calcACMR(index, idx, cacheLength);

				if (reorder) {
					reorderIndexForCacheCoherency(index, idx, coord.point, cacheLength);

					//calcACMR(index, idx, cacheLength);

					Normal newNormal = null;

					// Remap other arrays as needed
					if (normal.vector != null) {
						int[] origIndex = null;

						if (normalIndex != null)
							origIndex = normalIndex;
						else
							origIndex = coordIndex;

						float[] origVector = normal.vector;
						float[] newVector = new float[coord.point.length];

						int len = index.length;
						int newIdx;
						int oldIdx;
						int cnt = 0;

						for(int i=0; i < idx; i++) {
							newIdx = index[i];
							oldIdx = normalIndex[cnt++];

							// skip -1's
							if (oldIdx == -1)
								oldIdx = origIndex[cnt++];

							newVector[newIdx * 3 + 0] = origVector[oldIdx * 3 + 0];
							newVector[newIdx * 3 + 1] = origVector[oldIdx * 3 + 1];
							newVector[newIdx * 3 + 2] = origVector[oldIdx * 3 + 2];
						}

						newNormal = (Normal)factory.getEncodable("Normal", null);
						newNormal.vector = newVector;
						newNormal.num_vector = newVector.length / 3;
					}

					TextureCoordinate newTexCoord = null;

					// Remap other arrays as needed
					if (texCoord != null) {

						int[] origIndex = null;

						if (texCoordIndex != null)
							origIndex = texCoordIndex;
						else
							origIndex = coordIndex;

						float[] origPoint = texCoord.point;
						float[] newPoint = new float[coord.point.length];

						int len = index.length;
						int newIdx;
						int oldIdx;
						int cnt = 0;

						for(int i=0; i < idx; i++) {
							newIdx = index[i];
							oldIdx = texCoordIndex[cnt++];

							// skip -1's
							if (oldIdx == -1)
								oldIdx = origIndex[cnt++];

							newPoint[newIdx * 2 + 0] = origPoint[oldIdx * 2 + 0];
							newPoint[newIdx * 2 + 1] = origPoint[oldIdx * 2 + 1];
						}

						newTexCoord = (TextureCoordinate)factory.getEncodable("TextureCoordinate", null);
						newTexCoord.point = newPoint;
						newTexCoord.num_point = newPoint.length / 3;
					}

					IndexedTriangleSet its = (IndexedTriangleSet)factory.getEncodable(
						"IndexedTriangleSet",
						ifs.defName);
					its.setValue("index", index, idx);
					its.setCoordinate(coord);
					if (newNormal != null)
						its.setNormal(newNormal);
					if (newTexCoord != null)
						its.setTextureCoordinate(newTexCoord);

					// push it along....
					its.encode();
				} else {
					// push old one along
					ifs.encode();
				}
				// return to 'idle' mode
				intercept = false;
				encode(false);
				suppressCalls(false);
			} else {
				super.endNode();
			}
		} else if (interceptCoordinate) {
			// a coordinate node outside the scope of an ifs has ended,
			// stop encoding
			interceptCoordinate = false;
			encode(false);
			super.endNode();

		} else {
            super.endNode();
        }
	}

	/**
	 * Reorder an coordinate index list based on cache coherency for post transform
	 * lighting cache hits.
	 *
	 * @param index The index list
	 * @param len The number of valid entries in the index list
	 * @param data The coordinate data to reorder
	 * @param cacheLen The cache length to optimize for
	 */
	private void reorderIndexForCacheCoherency(int[] index, int len, float[] data, int cacheSize) {
		ForsythCacheOptimizer fco = new ForsythCacheOptimizer();
		int[] newIndex = new int[len];

//		System.out.println("input indices: " + java.util.Arrays.toString(index));

		fco.optimizeFaces(index, len, data.length - 1, newIndex, cacheSize);

//		System.out.println("new indices: " + java.util.Arrays.toString(newIndex));

		for(int i=0; i < len ; i++) {
			index[i] = newIndex[i];
		}
/*
		int cacheMisses = 0;
		int numTriangles = len / 3;
		float acmr;

		VertexCache cache = new VertexCache(cacheSize);
		Integer idx;

		for(int i=0; i < len; i++) {
			idx = new Integer(index[i]);
			if (!cache.contains(idx)) {
				cacheMisses++;
				cache.offer(idx);
			}
		}

		acmr = cacheMisses / (float) numTriangles;
//		System.out.println("Tris/Misses/ACMR:\t\t" + numTriangles + "\t" + cacheMisses + "\t" + acmr);
		System.out.println("\"" + numTriangles + "\",\"" + cacheMisses + "\",\"" + acmr + "\"");
*/
	}

	/**
	 * Reorder an coordinate index list based on cache coherency for post transform
	 * lighting cache hits.
	 *
	 * @param index The index list
	 * @param len The number of valid entries in the index list
	 * @param data The coordinate data to reorder
	 * @param cacheLen The cache length to optimize for
	 */
	private void calcACMR(int[] index, int len, int cacheSize) {
		int cacheMisses = 0;
		int numTriangles = len / 3;
		float acmr;

		VertexCache cache = new VertexCache(cacheSize);
		Integer idx;

		for(int i=0; i < len; i++) {
			idx = index[i];
			if (!cache.contains(idx)) {
				cacheMisses++;
				cache.offer(idx);
			}
		}

		acmr = cacheMisses / (float) numTriangles;
		System.out.println("Tris: " + numTriangles + "\tMisses:" + cacheMisses + "\tACMR:" + acmr);
	}

	/**
	 * Find whether a DEF name is used.
	 */
	private boolean findUSE(String defName) {
		return true;
	}

}

class VertexCache {
	private int cacheSize;
	private LinkedList<Integer> queue;

	public VertexCache(int cacheSize) {
		queue = new LinkedList<>();
		this.cacheSize = cacheSize;
	}

	public boolean offer(Integer o) {
		if (queue.size() < cacheSize) {
			queue.offer(o);
		} else {
			queue.poll();
			queue.offer(o);
		}

		return true;
	}

	public boolean contains(Integer o) {
		return queue.contains(o);
	}
}