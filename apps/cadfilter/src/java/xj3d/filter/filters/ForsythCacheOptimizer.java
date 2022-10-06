/*****************************************************************************
 *                        Yumetech Copyright (c) 2011
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

import java.util.ArrayList;

/**
 * Vertex cache optimization.  Order vertices in the best order to
 * optimize usage of the post transform vertex cache.
 *
 * Uses Forsyth method.  See this posting about it:
 *    http://home.comcast.net/~tom_forsyth/papers/fast_vert_cache_opt.html
 */
public class ForsythCacheOptimizer {
	private static final float CACHE_DECAY_POWER = 1.5f;
	private static final float LAST_TRI_SCORE = 0.75f;
	private static final float VALENCE_BOOST_SCALE = 2.0f;
	private static final float VALENCE_BOOST_POWER = 0.5f;

	private int kMaxVertexCacheSize = 64;
	private int kMaxPrecomputedVertexValenceScores = 64;
	private float[][] s_vertexCacheScores = new float[kMaxVertexCacheSize+1][kMaxVertexCacheSize];
	private float[] s_vertexValenceScores = new float[kMaxPrecomputedVertexValenceScores];

	/**
	 * Cmputer the vertex cache score.
	 * Code for computing vertex score was taken, as much as possible
	 * directly from the original publication(Bogomjakov and Hoppe).
	 */
	private float computeVertexCacheScore(int cachePosition, int vertexCacheSize) {
		float score = 0.0f;

		if (cachePosition < 0) {
			// Vertex is not in FIFO cache - no score.
		} else {
			if (cachePosition < 3) {
				// This vertex was used in the last triangle,
				// so it has a fixed score, whichever of the three
				// it's in. Otherwise, you can get very different
				// answers depending on whether you add
				// the triangle 1,2,3 or 3,1,2 - which is silly.
				score = LAST_TRI_SCORE;
			} else {
				// Points for being high in the cache.
				float scaler = 1.0f / ( vertexCacheSize - 3 );
				score = 1.0f - (cachePosition - 3) * scaler;
				score = (float) Math.pow(score, CACHE_DECAY_POWER);
			}
		}

		return score;
	}

	private float computeVertexValenceScore(int numActiveFaces) {
		float score = 0.f;

		// Bonus points for having a low number of tris still to
		// use the vert, so we get rid of lone verts quickly.
		float valenceBoost = (float) Math.pow(numActiveFaces,-VALENCE_BOOST_POWER);
		score += VALENCE_BOOST_SCALE * valenceBoost;

		return score;
	}

	private float findVertexCacheScore(int cachePosition, int maxSizeVertexCache) {
		return s_vertexCacheScores[maxSizeVertexCache][cachePosition];
	}

	private float findVertexValenceScore(int numActiveTris) {
		return s_vertexValenceScores[numActiveTris];
	}

	private float findVertexScore(int numActiveFaces, int cachePosition, int vertexCacheSize) {
		if (numActiveFaces == 0) {
			// No tri needs this vertex!
			return -1.0f;
		}

		float score = 0.f;

		if (cachePosition < vertexCacheSize) {
			score += s_vertexCacheScores[vertexCacheSize][cachePosition];
		}

		if (numActiveFaces < kMaxPrecomputedVertexValenceScores) {
			score += s_vertexValenceScores[numActiveFaces];
		} else {
			score += computeVertexValenceScore(numActiveFaces);
		}

		return score;
	}

	/**
	 * Optimize faces.
	 *
     * @param indexList input index list
     * @param indexCount the number of indices in the list
     * @param vertexCount the largest index value in indexList
     * @param newIndexList  preallocated buffer the same size as indexList to hold the optimized index list
     * @param lruCacheSize  the size of the simulated post-transform cache (max:64)
     */
    public void optimizeFaces(int[] indexList, int indexCount, int vertexCount, int[] newIndexList, int lruCacheSize) {

		for (int cacheSize=0; cacheSize <= kMaxVertexCacheSize; ++cacheSize) {
			for (int cachePos=0; cachePos<cacheSize; ++cachePos) {
				s_vertexCacheScores[cacheSize][cachePos] = computeVertexCacheScore(cachePos, cacheSize);
			}
		}

		for (int valence=0; valence < kMaxPrecomputedVertexValenceScores; ++valence) {
			s_vertexValenceScores[valence] = computeVertexValenceScore(valence);
		}

		ArrayList<OptimizeVertexData> vertexDataList = new ArrayList<>(vertexCount);

		for(int i=0; i < vertexCount; i++) {
			vertexDataList.add(new OptimizeVertexData());
		}

        // compute face count per vertex
        for (int i=0; i<indexCount; ++i)
        {
            int index = indexList[i];
            OptimizeVertexData vertexData = vertexDataList.get(index);
            vertexData.activeFaceListSize++;
        }

        int kEvictedCacheIndex = Integer.MAX_VALUE;

		// allocate face list per vertex
		int curActiveFaceListPos = 0;
		for (int i=0; i < vertexCount; ++i) {
			OptimizeVertexData vertexData = vertexDataList.get(i);
			vertexData.cachePos0 = kEvictedCacheIndex;
			vertexData.cachePos1 = kEvictedCacheIndex;
			vertexData.activeFaceListStart = curActiveFaceListPos;
			curActiveFaceListPos += vertexData.activeFaceListSize;
			vertexData.score = findVertexScore(vertexData.activeFaceListSize, vertexData.cachePos0, lruCacheSize);
			vertexData.activeFaceListSize = 0;
		}

		ArrayList<Integer> activeFaceList = new ArrayList<>(curActiveFaceListPos);

		for(int i=0; i < curActiveFaceListPos; i++) {
			activeFaceList.add(0);
		}

        // fill out face list per vertex
        for (int i=0; i < indexCount; i += 3) {
            for (int j=0; j < 3; ++j) {
                int index = indexList[i+j];
                OptimizeVertexData vertexData = vertexDataList.get(index);
                activeFaceList.set(vertexData.activeFaceListStart + vertexData.activeFaceListSize,i);
                vertexData.activeFaceListSize++;
            }
        }


		ArrayList<Integer> processedFaceList = new ArrayList<>(indexCount);

		for(int i=0; i < indexCount; i++) {
			processedFaceList.add(0);
		}

		int[] cache0 = new int[(kMaxVertexCacheSize+3)];
		int[] cache1 = new int[(kMaxVertexCacheSize+3)];

        int entriesInCache0 = 0;

        int bestFace = 0;
        float bestScore = -1.0f;

        float maxValenceScore = findVertexScore(1, kEvictedCacheIndex, lruCacheSize) * 3.f;

        for (int i = 0; i < indexCount; i += 3) {
            if (bestScore < 0.f) {
                // no verts in the cache are used by any unprocessed faces so
                // search all unprocessed faces for a new starting point
                for (int j = 0; j < indexCount; j += 3) {
                    if (processedFaceList.get(j) == 0) {
                        int face = j;
                        float faceScore = 0.f;
                        for (int k=0; k < 3; ++k) {
                            int index = indexList[face+k];
                            OptimizeVertexData vertexData = vertexDataList.get(index);
                            faceScore += vertexData.score;
                        }

                        if (faceScore > bestScore) {
                            bestScore = faceScore;
                            bestFace = face;

                            if (bestScore >= maxValenceScore) {
                                break;
                            }
                        }
                    }
                }
            }

//System.out.println("bestScore: " + bestScore + " bestFace: " + bestFace);
            processedFaceList.set(bestFace, 1);
//printList("processed face list", processedFaceList);
            int entriesInCache1 = 0;

            // add bestFace to LRU cache and to newIndexList
            for (int v = 0; v < 3; ++v) {
                int index = indexList[bestFace + v];
                newIndexList[i+v] = index;

                OptimizeVertexData vertexData = vertexDataList.get(index);

                if (vertexData.cachePos1 >= entriesInCache1) {
					//System.out.println("Add index: " + index + " to cache pos " + entriesInCache1);

                    vertexData.cachePos1 = entriesInCache1;
                    cache1[entriesInCache1++] = index;

/*
System.out.println("VertexCache0:");
for(int ii=0; ii < entriesInCache0; ii++) {
   System.out.print(cache0[ii] + " ");
}

System.out.println("\nVertexCache1:");
for(int ii=0; ii < entriesInCache1; ii++) {
   System.out.print(cache1[ii] + " ");
}
System.out.println();
*/
                    if (vertexData.activeFaceListSize == 1) {
//System.out.println ("dec activeFaceList");
                        --vertexData.activeFaceListSize;
                        continue;
                    }
                }

//printList("active face list1", activeFaceList);

//System.out.println("best face: " + bestFace);
				int start = vertexData.activeFaceListStart;
				int end = vertexData.activeFaceListStart + vertexData.activeFaceListSize;
				int found = -1;

				for(int j=start; j < end; j++) {
					if (activeFaceList.get(j) == bestFace) {
						found = j;
						break;
					}
				}

//System.out.println("best face found at: " + found);
				int tmp = activeFaceList.get(found);
//System.out.println("***swap: " + found + " end: " + end + " tmp: " + tmp);

				activeFaceList.set(found, activeFaceList.get(end-1));
				activeFaceList.set(end-1, tmp);
//printList("active face list2", activeFaceList);

                --vertexData.activeFaceListSize;
                vertexData.score = findVertexScore(vertexData.activeFaceListSize, vertexData.cachePos1, lruCacheSize);

            }

            // move the rest of the old verts in the cache down and compute their new scores
            for (int c0 = 0; c0 < entriesInCache0; ++c0) {
                int index = cache0[c0];
                OptimizeVertexData vertexData = vertexDataList.get(index);

                if (vertexData.cachePos1 >= entriesInCache1) {
                    vertexData.cachePos1 = entriesInCache1;
                    cache1[entriesInCache1++] = index;
                    vertexData.score = findVertexScore(vertexData.activeFaceListSize, vertexData.cachePos1, lruCacheSize);
                }
            }

            // find the best scoring triangle in the current cache (including up to 3 that were just evicted)
            bestScore = -1.f;
            for (int c1 = 0; c1 < entriesInCache1; ++c1) {
                int index = cache1[c1];
                OptimizeVertexData vertexData = vertexDataList.get(index);
                vertexData.cachePos0 = vertexData.cachePos1;
                vertexData.cachePos1 = kEvictedCacheIndex;
                for (int j=0; j < vertexData.activeFaceListSize; ++j) {
                    int face = activeFaceList.get(vertexData.activeFaceListStart + j);
                    float faceScore = 0.f;
                    for (int v=0; v < 3; v++) {
                        int faceIndex = indexList[face+v];
                        OptimizeVertexData faceVertexData = vertexDataList.get(faceIndex);
                        faceScore += faceVertexData.score;
                    }

                    if (faceScore > bestScore) {
                        bestScore = faceScore;
                        bestFace = face;
                    }
                }
            }


			// swap cache0 and cache1
			// todo: dont realloc
			int len = cache0.length;
			int[] tmpCache = new int[len];
			for(int j=0; j < len; j++) {
				tmpCache[j] = cache1[j];
			}
			for(int j=0; j < len; j++) {
				cache1[j] = cache0[j];
			}

			for(int j=0; j < len; j++) {
				cache0[j] = tmpCache[j];
			}

            entriesInCache0 = Math.min(entriesInCache1, lruCacheSize);
        }
    }

    private void printList(String st, ArrayList list) {
    	System.out.println("Printing list: " + st + " len: " + list.size());
            for (Object list1 : list) {
                System.out.print(list1 + " ");
            }
    	System.out.println();
    }

}

class OptimizeVertexData {
	public float   score;
	public int activeFaceListStart;
	public int activeFaceListSize;
	public int cachePos0;
	public int cachePos1;

	public OptimizeVertexData() {
		score = 0f;
		activeFaceListStart = 0;
		cachePos0 = 0;
		cachePos1 = 0;
	}
};
