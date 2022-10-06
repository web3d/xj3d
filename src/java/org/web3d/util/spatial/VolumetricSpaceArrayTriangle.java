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

package org.web3d.util.spatial;

import java.util.*;

import toxi.geom.Vec3D;

/**
 *  A Volumetric Space which stores triangles.
 *
 * Code adapted from toxiclibs copyright Copyright(c) 2006-2011 Karsten Schmidt
 *
 * @author Alan Hudson
 */
public class VolumetricSpaceArrayTriangle extends VolumetricSpaceTriangle {

    protected Set<Triangle>[] data;

    @SuppressWarnings("unchecked") // generic array type HashSet[]
    public VolumetricSpaceArrayTriangle(Vec3D scale, int resX, int resY, int resZ) {
        super(scale, resX, resY, resZ);
        data = new HashSet[resX * resY * resZ];
    }

    @Override
    public void clear() {
        for (Set<Triangle> data1 : data) {
            data1.clear();
        }
    }

    public Set[] getData() {
        return data;
    }

    @Override
    public void insertAt(int x, int y, int z, Triangle tri) {
        int idx = x + y * resX + z * sliceRes;
        Set<Triangle> val = data[idx];

        if (val == null) {
            val = new HashSet<>(1);
            data[idx] = val;
        }
//System.out.println("Adding tri at: " + x + " " + y + " " + z);
        val.add(tri);
    }

    @Override
    public Set<Triangle> getVoxelAt(int index) {
        return data[index];
    }

    @Override
    public Set<Triangle> getVoxelAt(int x, int y, int z) {
        return data[x + y * resX + z * sliceRes];
    }
}
