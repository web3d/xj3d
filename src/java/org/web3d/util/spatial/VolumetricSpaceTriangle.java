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
import toxi.math.MathUtils;

/**
 *  A Volumetric space.
 *
 * Code adapted from toxiclibs copyright Copyright(c) 2006-2011 Karsten Schmidt
 *
 * @author Alan Hudson
 */
public abstract class VolumetricSpaceTriangle {

    public final int resX, resY, resZ;
    public final int resX1, resY1, resZ1;

    public final int sliceRes;

    public final Vec3D scale = new Vec3D();
    public final Vec3D halfScale = new Vec3D();
    public final Vec3D voxelSize = new Vec3D();

    public final int numCells;

    public VolumetricSpaceTriangle(Vec3D scale, int resX, int resY, int resZ) {
        this.resX = resX;
        this.resY = resY;
        this.resZ = resZ;
        resX1 = resX - 1;
        resY1 = resY - 1;
        resZ1 = resZ - 1;
        sliceRes = resX * resY;
        numCells = sliceRes * resZ;
        setScale(scale);
    }

    public abstract void clear();

    public void closeSides() {
        throw new UnsupportedOperationException(
                "This VolumetricSpace implementation does not support closeSides()");
    }

    public final int getIndexFor(int x, int y, int z) {
        x = MathUtils.clip(x, 0, resX1);
        y = MathUtils.clip(y, 0, resY1);
        z = MathUtils.clip(z, 0, resZ1);
        return x + y * resX + z * sliceRes;
    }

    public final Vec3D getResolution() {
        return new Vec3D(resX, resY, resZ);
    }

    /**
     * @return the scale
     */
    public final Vec3D getScale() {
        return scale.copy();
    }

    /**
     * @param scale
     *            the scale to set
     */
    public final void setScale(Vec3D scale) {
        this.scale.set(scale);
        this.halfScale.set(scale.scale(0.5f));
        voxelSize.set(scale.x / resX, scale.y / resY, scale.z / resZ);

        System.out.println("VoxelSize: " + voxelSize);
    }

    public void insertAt(int x, int y, int z, Triangle tri) {
        throw new UnsupportedOperationException(
                "This VolumetricSpace implementation does not support insert()");
    }

    public abstract Set<Triangle> getVoxelAt(int index);

    public abstract Set<Triangle> getVoxelAt(int x, int y, int z);
}
