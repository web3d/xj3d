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

import toxi.geom.AABB;
import toxi.geom.Triangle3D;
import toxi.geom.Vec3D;
import toxi.geom.mesh.Face;
import toxi.geom.mesh.Mesh3D;
import toxi.math.MathUtils;
import toxi.math.ScaleMap;

public class MeshVoxelizerTriangle {

    protected VolumetricSpaceTriangle volume;
    protected int wallThickness = 0;

    public MeshVoxelizerTriangle(int res) {
        this(res, res, res);
    }

    public MeshVoxelizerTriangle(int resX, int resY, int resZ) {
        volume =
                new VolumetricSpaceArrayTriangle(new Vec3D(1, 1, 1), resX, resY, resZ);
    }

    public MeshVoxelizerTriangle(VolumetricSpaceTriangle vol) {
        volume = vol;
    }

    public MeshVoxelizerTriangle clear() {
        volume.clear();
        return this;
    }

    /**
     * @return the volume
     */
    public VolumetricSpaceTriangle getVolume() {
        return volume;
    }

    /**
     * @return the wallThickness
     */
    public int getWallThickness() {
        return wallThickness;
    }

    protected void insertTriangleAt(int x, int y, int z, Triangle tri) {
        int mix = MathUtils.max(x - wallThickness, 0);
        int miy = MathUtils.max(y - wallThickness, 0);
        int miz = MathUtils.max(z - wallThickness, 0);
        int max = MathUtils.min(x + wallThickness, volume.resX1);
        int may = MathUtils.min(y + wallThickness, volume.resY1);
        int maz = MathUtils.min(z + wallThickness, volume.resZ1);
        for (z = miz; z <= maz; z++) {
            for (y = miy; y <= may; y++) {
                for (x = mix; x <= max; x++) {
                    volume.insertAt(x, y, z, tri);
                }
            }
        }
    }

    /**
     * @param wallThickness
     *            the wallThickness to set
     * @return
     */
    public MeshVoxelizerTriangle setWallThickness(int wallThickness) {
        this.wallThickness = wallThickness;
        return this;
    }

    public VolumetricSpaceTriangle voxelizeMesh(Mesh3D mesh, double voxelSize) {
        return voxelizeMesh(mesh, 1f, voxelSize, new double[] {0,0,0});
    }

    public VolumetricSpaceTriangle voxelizeMesh(Mesh3D mesh, double scale, double voxelSize, double[] aaDir) {
        AABB box = mesh.getBoundingBox();
        Vec3D bmin = box.getMin();
        Vec3D bmax = box.getMax();
//System.out.println("mesh min: " + bmin);
//System.out.println("mesh max: " + bmax);

        ScaleMap wx = new ScaleMap(bmin.x, bmax.x, 1, volume.resX - 2);
        ScaleMap wy = new ScaleMap(bmin.y, bmax.y, 1, volume.resY - 2);
        ScaleMap wz = new ScaleMap(bmin.z, bmax.z, 1, volume.resZ - 2);
        ScaleMap gx = new ScaleMap(1, volume.resX - 2, bmin.x, bmax.x);
        ScaleMap gy = new ScaleMap(1, volume.resY - 2, bmin.y, bmax.y);
        ScaleMap gz = new ScaleMap(1, volume.resZ - 2, bmin.z, bmax.z);

        //volume.setScale(box.getExtent().scale(2f));
        volume.setScale(new Vec3D((float)scale,(float)scale,(float)scale));
        Triangle3D tri = new Triangle3D();
System.out.println("extent: " + volume.voxelSize.scale(0.5f));
        AABB voxel = new AABB(new Vec3D(), volume.voxelSize.scale(0.5f));

        int marked = 0;
        int skipped = 0;

        // Only worry about whole voxel shifts
        int xoff = 1;
        int yoff = 1;
        int zoff = 1;

//System.out.println("aaDir off: " + aaDir[0] + " " + aaDir[1] + " " + aaDir[2]);
//System.out.println("aaDir off: " + xoff + " " + yoff + " " + zoff);
        int id = 0;
        for (Face f : mesh.getFaces()) {
            tri.a = f.a;
            tri.b = f.b;
            tri.c = f.c;
            AABB bounds = tri.getBoundingBox();
            Vec3D min = bounds.getMin();
            Vec3D max = bounds.getMax();

            min =
                    new Vec3D((int) wx.getClippedValueFor(min.x),
                            (int) wy.getClippedValueFor(min.y),
                            (int) wz.getClippedValueFor(min.z));
            max =
                    new Vec3D((int) wx.getClippedValueFor(max.x),
                            (int) wy.getClippedValueFor(max.y),
                            (int) wz.getClippedValueFor(max.z));


// TODO: try this, shit this widens the answers
if (min.x >= 1)
    min.x--;
if (min.y >= 1)
    min.y--;
if (min.z >= 1)
    min.z--;
/*   // This doesn't change it.  hmmm bet there is an edge condition someplace
if (min.x <= volume.resX)
    max.x++;
if (min.y <= volume.resY)
    max.y++;
if (min.z <= volume.resZ)
    max.z++;
*/
            float[] coords = new float[9];
            coords[0] = tri.a.x;
            coords[1] = tri.a.y;
            coords[2] = tri.a.z;
            coords[3] = tri.b.x;
            coords[4] = tri.b.y;
            coords[5] = tri.b.z;
            coords[6] = tri.c.x;
            coords[7] = tri.c.y;
            coords[8] = tri.c.z;


            System.out.println("Tri: " + java.util.Arrays.toString(coords));
            System.out.println("Min Bounds: x: " + min.x + " y: " + min.y + " z: " + min.z);
            System.out.println("Max Bounds: x: " + max.x + " y: " + max.y + " z: " + max.z);

            // TODO: is id what we really want?
            Triangle t = new Triangle(coords, id);

            for (int z = (int) min.z; z <= max.z; z++) {
                for (int y = (int) min.y; y <= max.y; y++) {
                    for (int x = (int) min.x; x <= max.x; x++) {
                        if (x < volume.resX1 && y < volume.resY1
                                && z < volume.resZ1) {

                            voxel.set((float) gx.getClippedValueFor(x),
                                    (float) gy.getClippedValueFor(y),
                                    (float) gz.getClippedValueFor(z));


                            // Damn, this test uses the ABB of triangle
                            if (voxel.intersectsTriangle(tri)) {
                                // Convert to offset applied grid

//System.out.println("mark voxel(orig): " + (x) + " " + (y) + " " + (z));
System.out.println("mark voxel: " + (x*xoff) + " " + (y*yoff) + " " + (z*zoff));
                                marked++;
                                insertTriangleAt(x * xoff, y * yoff, z * zoff, t);
                            } else {
                                skipped++;
//System.out.println("skip voxel: " + x + " " + y + " " + z);
                            }
                        }
                    }
                }
            }

            id++;
        }

        System.out.println("Marked: " + marked + " Skipped: " + skipped + " %saved: " + ((float) skipped / (marked + skipped)));
        return volume;
    }
}
