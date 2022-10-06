/*
 *   __               .__       .__  ._____.
 * _/  |_  _______  __|__| ____ |  | |__\_ |__   ______
 * \   __\/  _ \  \/  /  |/ ___\|  | |  || __ \ /  ___/
 *  |  | (  <_> >    <|  \  \___|  |_|  || \_\ \\___ \
 *  |__|  \____/__/\_ \__|\___  >____/__||___  /____  >
 *                   \/       \/             \/     \/
 *
 * Copyright (c) 2006-2011 Karsten Schmidt
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * http://creativecommons.org/licenses/LGPL/2.1/
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 */
package org.web3d.util.spatial;

import toxi.geom.*;

import java.util.Random;

import javax.xml.bind.annotation.XmlAttribute;

import toxi.math.InterpolateStrategy;
import toxi.math.MathUtils;
import toxi.math.ScaleMap;

/**
 * Comprehensive 3D vector class with additional basic intersection and
 * collision detection features.
 */
public class Vec3DDouble implements Comparable<ReadonlyVec3DDouble>, ReadonlyVec3DDouble {

    public static enum Axis {

        X(Vec3DDouble.X_AXIS), Y(Vec3DDouble.Y_AXIS), Z(Vec3DDouble.Z_AXIS);

        private final ReadonlyVec3DDouble vector;

        private Axis(ReadonlyVec3DDouble v) {
            this.vector = v;
        }

        public ReadonlyVec3DDouble getVector() {
            return vector;
        }
    }

    /** Defines positive X axis. */
    public static final ReadonlyVec3DDouble X_AXIS = new Vec3DDouble(1, 0, 0);

    /** Defines positive Y axis. */
    public static final ReadonlyVec3DDouble Y_AXIS = new Vec3DDouble(0, 1, 0);

    /** Defines positive Z axis. */
    public static final ReadonlyVec3DDouble Z_AXIS = new Vec3DDouble(0, 0, 1);

    /** Defines the zero vector. */
    public static final ReadonlyVec3DDouble ZERO = new Vec3DDouble();

    /**
     * Defines vector with all coords set to Double.MIN_VALUE. Useful for
     * bounding box operations.
     */
    public static final ReadonlyVec3DDouble MIN_VALUE = new Vec3DDouble(Double.MIN_VALUE,
            Double.MIN_VALUE, Double.MIN_VALUE);

    /**
     * Defines vector with all coords set to Double.MAX_VALUE. Useful for
     * bounding box operations.
     */
    public static final ReadonlyVec3DDouble MAX_VALUE = new Vec3DDouble(Double.MAX_VALUE,
            Double.MAX_VALUE, Double.MAX_VALUE);

    /**
     * Creates a new vector from the given angle in the XY plane. The Z
     * component of the vector will be zero.
     *
     * The resulting vector for theta=0 is equal to the positive X axis.
     *
     * @param theta
     *            the theta
     *
     * @return new vector in the XY plane
     */
    public static Vec3DDouble fromXYTheta(double theta) {
        return new Vec3DDouble(Math.cos(theta), Math.sin(theta), 0);
    }

    /**
     * Creates a new vector from the given angle in the XZ plane. The Y
     * component of the vector will be zero.
     *
     * The resulting vector for theta=0 is equal to the positive X axis.
     *
     * @param theta
     *            the theta
     *
     * @return new vector in the XZ plane
     */
    public static Vec3DDouble fromXZTheta(double theta) {
        return new Vec3DDouble(Math.cos(theta), 0, Math.sin(theta));
    }

    /**
     * Creates a new vector from the given angle in the YZ plane. The X
     * component of the vector will be zero.
     *
     * The resulting vector for theta=0 is equal to the positive Y axis.
     *
     * @param theta
     *            the theta
     *
     * @return new vector in the YZ plane
     */
    public static Vec3DDouble fromYZTheta(double theta) {
        return new Vec3DDouble(0, Math.cos(theta), Math.sin(theta));
    }

    /**
     * Constructs a new vector consisting of the largest components of both
     * vectors.
     *
     * @param b
     *            the b
     * @param a
     *            the a
     *
     * @return result as new vector
     */
    public static Vec3DDouble max(ReadonlyVec3DDouble a, ReadonlyVec3DDouble b) {
        return new Vec3DDouble(MathUtils.max(a.x(), b.x()), MathUtils.max(a.y(),
                b.y()), MathUtils.max(a.z(), b.z()));
    }

    /**
     * Constructs a new vector consisting of the smallest components of both
     * vectors.
     *
     * @param b
     *            comparing vector
     * @param a
     *            the a
     *
     * @return result as new vector
     */
    public static Vec3DDouble min(ReadonlyVec3DDouble a, ReadonlyVec3DDouble b) {
        return new Vec3DDouble(MathUtils.min(a.x(), b.x()), MathUtils.min(a.y(),
                b.y()), MathUtils.min(a.z(), b.z()));
    }

    /**
     * Static factory method. Creates a new random unit vector using the Random
     * implementation set as default for the {@link MathUtils} class.
     *
     * @return a new random normalized unit vector.
     */
    public static Vec3DDouble randomVector() {
        return randomVector(MathUtils.RND);
    }

    /**
     * Static factory method. Creates a new random unit vector using the given
     * Random generator instance. I recommend to have a look at the
     * https://uncommons-maths.dev.java.net library for a good choice of
     * reliable and high quality random number generators.
     *
     * @param rnd
     *            the rnd
     *
     * @return a new random normalized unit vector.
     */
    public static Vec3DDouble randomVector(Random rnd) {
        Vec3DDouble v = new Vec3DDouble(rnd.nextDouble() * 2 - 1, rnd.nextDouble() * 2 - 1,
                rnd.nextDouble() * 2 - 1);
        return v.normalize();
    }

    /** X coordinate. */
    @XmlAttribute(required = true)
    public double x;

    /** Y coordinate. */
    @XmlAttribute(required = true)
    public double y;

    /** Z coordinate. */
    @XmlAttribute(required = true)
    public double z;

    /**
     * Creates a new zero vector.
     */
    public Vec3DDouble() {
    }

    /**
     * Creates a new vector with the given coordinates.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     */
    public Vec3DDouble(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3DDouble(double[] v) {
        this.x = v[0];
        this.y = v[1];
        this.z = v[2];
    }

    /**
     * Creates a new vector with the coordinates of the given vector.
     *
     * @param v
     *            vector to be copied
     */
    public Vec3DDouble(ReadonlyVec3DDouble v) {
        this.x = v.x();
        this.y = v.y();
        this.z = v.z();
    }

    /**
     * Abs.
     *
     * @return the vec3 d
     */
    public final Vec3DDouble abs() {
        x = MathUtils.abs(x);
        y = MathUtils.abs(y);
        z = MathUtils.abs(z);
        return this;
    }

    @Override
    public final Vec3DDouble add(double a, double b, double c) {
        return new Vec3DDouble(x + a, y + b, z + c);
    }

    @Override
    public Vec3DDouble add(ReadonlyVec3DDouble v) {
        return new Vec3DDouble(x + v.x(), y + v.y(), z + v.z());
    }

    @Override
    public final Vec3DDouble add(Vec3DDouble v) {
        return new Vec3DDouble(x + v.x, y + v.y, z + v.z);
    }

    /**
     * Adds vector {a,b,c} and overrides coordinates with result.
     *
     * @param a
     *            X coordinate
     * @param b
     *            Y coordinate
     * @param c
     *            Z coordinate
     *
     * @return itself
     */
    public final Vec3DDouble addSelf(double a, double b, double c) {
        x += a;
        y += b;
        z += c;
        return this;
    }

    public final Vec3DDouble addSelf(ReadonlyVec3DDouble v) {
        x += v.x();
        y += v.y();
        z += v.z();
        return this;
    }

    /**
     * Adds vector v and overrides coordinates with result.
     *
     * @param v
     *            vector to add
     *
     * @return itself
     */
    public final Vec3DDouble addSelf(Vec3DDouble v) {
        x += v.x;
        y += v.y;
        z += v.z;
        return this;
    }

    @Override
    public final double angleBetween(ReadonlyVec3DDouble v) {
        return Math.acos(dot(v));
    }

    @Override
    public final double angleBetween(ReadonlyVec3DDouble v, boolean forceNormalize) {
        double theta;
        if (forceNormalize) {
            theta = getNormalized().dot(v.getNormalized());
        } else {
            theta = dot(v);
        }
        return Math.acos(theta);
    }

    /**
     * Sets all vector components to 0.
     *
     * @return itself
     */
    public final ReadonlyVec3DDouble clear() {
        x = y = z = 0;
        return this;
    }

    @Override
    public int compareTo(ReadonlyVec3DDouble v) {
        if (x == v.x() && y == v.y() && z == v.z()) {
            return 0;
        }
        double a = magSquared();
        double b = v.magSquared();
        if (a < b) {
            return -1;
        }
        return +1;
    }

    /**
     * Forcefully fits the vector in the given AABB.
     *
     * @param box
     *            the box
     *
     * @return itself
     */
    public final Vec3DDouble constrain(AABB box) {
        return constrain(box.getMin(), box.getMax());
    }

    /**
     * Forcefully fits the vector in the given AABB specified by the 2 given
     * points.
     *
     * @param min
     * @param max
     * @return itself
     */
    public final Vec3DDouble constrain(Vec3D min, Vec3D max) {
        x = MathUtils.clip(x, min.x, max.x);
        y = MathUtils.clip(y, min.y, max.y);
        z = MathUtils.clip(z, min.z, max.z);
        return this;
    }

    @Override
    public Vec3DDouble copy() {
        return new Vec3DDouble(this);
    }

    @Override
    public final Vec3DDouble cross(ReadonlyVec3DDouble v) {
        return new Vec3DDouble(y * v.z() - v.y() * z, z * v.x() - v.z() * x, x
                * v.y() - v.x() * y);
    }

    public final Vec3DDouble cross(Vec3DDouble v) {
        return new Vec3DDouble(y * v.z - v.y * z, z * v.x - v.z * x, x * v.y - v.x
                * y);
    }

    @Override
    public final Vec3DDouble crossInto(ReadonlyVec3DDouble v, Vec3DDouble result) {
        final double vx = v.x();
        final double vy = v.y();
        final double vz = v.z();
        result.x = y * vz - vy * z;
        result.y = z * vx - vz * x;
        result.z = x * vy - vx * y;
        return result;
    }

    /**
     * Calculates cross-product with vector v. The resulting vector is
     * perpendicular to both the current and supplied vector and overrides the
     * current.
     *
     * @param v
     *            the v
     *
     * @return itself
     */
    public final Vec3DDouble crossSelf(Vec3DDouble v) {
        final double cx = y * v.z - v.y * z;
        final double cy = z * v.x - v.z * x;
        z = x * v.y - v.x * y;
        y = cy;
        x = cx;
        return this;
    }

    @Override
    public final double distanceTo(ReadonlyVec3DDouble v) {
        if (v != null) {
            final double dx = x - v.x();
            final double dy = y - v.y();
            final double dz = z - v.z();
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        } else {
            return Double.NaN;
        }
    }

    @Override
    public final double distanceToSquared(ReadonlyVec3DDouble v) {
        if (v != null) {
            final double dx = x - v.x();
            final double dy = y - v.y();
            final double dz = z - v.z();
            return dx * dx + dy * dy + dz * dz;
        } else {
            return Double.NaN;
        }
    }

    @Override
    public final double dot(ReadonlyVec3DDouble v) {
        return x * v.x() + y * v.y() + z * v.z();
    }

    public final double dot(Vec3DDouble v) {
        return x * v.x + y * v.y + z * v.z;
    }

    /**
     * Returns true if the Object v is of type ReadonlyVec3DDouble and all of the data
     * members of v are equal to the corresponding data members in this vector.
     *
     * @param v
     *            the Object with which the comparison is made
     * @return true or false
     */
    @Override
    public boolean equals(Object v) {
        boolean retVal = false;
        if (v instanceof ReadonlyVec3DDouble)
            try {
                ReadonlyVec3DDouble vv = (ReadonlyVec3DDouble) v;
                retVal = (x == vv.x() && y == vv.y() && z == vv.z());
            } catch (NullPointerException | ClassCastException e) {
                System.err.println(e);
            }
        return retVal;
    }

    /**
     * Returns true if the Object v is of type ReadonlyVec3DDouble and all of the data
     * members of v are equal to the corresponding data members in this vector.
     *
     * @param v
     *            the vector with which the comparison is made
     * @return true or false
     */
    public boolean equals(ReadonlyVec3DDouble v) {
        try {
            return (x == v.x() && y == v.y() && z == v.z());
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public boolean equalsWithTolerance(ReadonlyVec3DDouble v, double tolerance) {
        try {
            double diff = x - v.x();
            if (Double.isNaN(diff)) {
                return false;
            }
            if ((diff < 0 ? -diff : diff) > tolerance) {
                return false;
            }
            diff = y - v.y();
            if (Double.isNaN(diff)) {
                return false;
            }
            if ((diff < 0 ? -diff : diff) > tolerance) {
                return false;
            }
            diff = z - v.z();
            if (Double.isNaN(diff)) {
                return false;
            }
            return (diff < 0 ? -diff : diff) <= tolerance;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Replaces the vector components with integer values of their current
     * values.
     *
     * @return itself
     */
    public final Vec3DDouble floor() {
        x = MathUtils.floor(x);
        y = MathUtils.floor(y);
        z = MathUtils.floor(z);
        return this;
    }

    /**
     * Replaces the vector components with the fractional part of their current
     * values.
     *
     * @return itself
     */
    public final Vec3DDouble frac() {
        x -= MathUtils.floor(x);
        y -= MathUtils.floor(y);
        z -= MathUtils.floor(z);
        return this;
    }

    @Override
    public final Vec3DDouble getAbs() {
        return new Vec3DDouble(this).abs();
    }

    @Override
    public Vec3DDouble getCartesian() {
        return copy().toCartesian();
    }

    @Override
    public final double getComponent(Axis id) {
        switch (id) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public final double getComponent(int id) {
        switch (id) {
            case 0:
                return x;
            case 1:
                return y;
            case 2:
                return z;
        }
        throw new IllegalArgumentException("index must be 0, 1 or 2");
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#getConstrained(toxi.geom.AABB)
     */
    @Override
    public final Vec3DDouble getConstrained(AABB box) {
        return new Vec3DDouble(this).constrain(box);
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#getFloored()
     */
    @Override
    public final Vec3DDouble getFloored() {
        return new Vec3DDouble(this).floor();
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#getFrac()
     */
    @Override
    public final Vec3DDouble getFrac() {
        return new Vec3DDouble(this).frac();
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#getInverted()
     */
    @Override
    public final Vec3DDouble getInverted() {
        return new Vec3DDouble(-x, -y, -z);
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#getLimited(double)
     */
    @Override
    public final Vec3DDouble getLimited(double lim) {
        if (magSquared() > lim * lim) {
            return getNormalizedTo(lim);
        }
        return new Vec3DDouble(this);
    }

    @Override
    public Vec3DDouble getMapped(ScaleMap map) {
        return new Vec3DDouble(map.getClippedValueFor(x),
                map.getClippedValueFor(y),
                map.getClippedValueFor(z));
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#getNormalized()
     */
    @Override
    public final Vec3DDouble getNormalized() {
        return new Vec3DDouble(this).normalize();
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#getNormalizedTo(double)
     */
    @Override
    public final Vec3DDouble getNormalizedTo(double len) {
        return new Vec3DDouble(this).normalizeTo(len);
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#getReciprocal()
     */
    @Override
    public final Vec3DDouble getReciprocal() {
        return copy().reciprocal();
    }

    @Override
    public final Vec3DDouble getReflected(ReadonlyVec3DDouble normal) {
        return copy().reflect(normal);
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#getRotatedAroundAxis(toxi.geom.Vec3DDouble, double)
     */
    @Override
    public final Vec3DDouble getRotatedAroundAxis(ReadonlyVec3DDouble axis, double theta) {
        return new Vec3DDouble(this).rotateAroundAxis(axis, theta);
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#getRotatedX(double)
     */
    @Override
    public final Vec3DDouble getRotatedX(double theta) {
        return new Vec3DDouble(this).rotateX(theta);
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#getRotatedY(double)
     */
    @Override
    public final Vec3DDouble getRotatedY(double theta) {
        return new Vec3DDouble(this).rotateY(theta);
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#getRotatedZ(double)
     */
    @Override
    public final Vec3DDouble getRotatedZ(double theta) {
        return new Vec3DDouble(this).rotateZ(theta);
    }

/*
    public Vec3DDouble getRoundedTo(double prec) {
        return copy().roundTo(prec);
    }
*/

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#getSignum()
     */
    @Override
    public final Vec3DDouble getSignum() {
        return new Vec3DDouble(this).signum();
    }

    @Override
    public Vec3DDouble getSpherical() {
        return copy().toSpherical();
    }

    /**
     * Returns a hash code value based on the data values in this object. Two
     * different Vec3DDouble objects with identical data values (i.e., Vec3DDouble.equals
     * returns true) will return the same hash code value. Two objects with
     * different data members may return the same hash value, although this is
     * not likely.
     *
     * @return the integer hash code value
     */
    @Override
    public int hashCode() {
        long bits = 1L;
        bits = 31L * bits + Double.doubleToLongBits(x);
        bits = 31L * bits + Double.doubleToLongBits(y);
        bits = 31L * bits + Double.doubleToLongBits(z);
        return (int) (bits ^ (bits >> 32));
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#headingXY()
     */
    @Override
    public final double headingXY() {
        return Math.atan2(y, x);
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#headingXZ()
     */
    @Override
    public final double headingXZ() {
        return Math.atan2(z, x);
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#headingYZ()
     */
    @Override
    public final double headingYZ() {
        return Math.atan2(y, z);
    }

    public ReadonlyVec3DDouble immutable() {
        return this;
    }

    @Override
    public final Vec3DDouble interpolateTo(ReadonlyVec3DDouble v, double f) {
        return new Vec3DDouble(x + (v.x() - x) * f, y + (v.y() - y) * f, z
                + (v.z() - z) * f);
    }

    @Override
    public final Vec3DDouble interpolateTo(ReadonlyVec3DDouble v, double f,
            InterpolateStrategy s) {
        return new Vec3DDouble(s.interpolate((float)x, (float)v.x(), (float)f), s.interpolate((float)y, (float)v.y(), (float)f), s.interpolate((float)z, (float)v.z(), (float)f));
    }

    public final Vec3DDouble interpolateTo(Vec3DDouble v, double f) {
        return new Vec3DDouble(x + (v.x - x) * f, y + (v.y - y) * f, z + (v.z - z)
                * f);
    }

    public final Vec3DDouble interpolateTo(Vec3DDouble v, double f, InterpolateStrategy s) {
        return new Vec3DDouble(s.interpolate((float)x, (float)v.x, (float)f), s.interpolate((float)y, (float)v.y, (float)f), s.interpolate((float)z, (float)v.z, (float)f));
    }

    /**
     * Interpolates the vector towards the given target vector, using linear
     * interpolation.
     *
     * @param v
     *            target vector
     * @param f
     *            interpolation factor (should be in the range 0..1)
     *
     * @return itself, result overrides current vector
     */
    public final Vec3DDouble interpolateToSelf(ReadonlyVec3DDouble v, double f) {
        x += (v.x() - x) * f;
        y += (v.y() - y) * f;
        z += (v.z() - z) * f;
        return this;
    }

    /**
     * Interpolates the vector towards the given target vector, using the given
     * {@link InterpolateStrategy}.
     *
     * @param v
     *            target vector
     * @param f
     *            interpolation factor (should be in the range 0..1)
     * @param s
     *            InterpolateStrategy instance
     *
     * @return itself, result overrides current vector
     */
    public final Vec3DDouble interpolateToSelf(ReadonlyVec3DDouble v, double f,
            InterpolateStrategy s) {
        x = s.interpolate((float)x, (float)v.x(), (float)f);
        y = s.interpolate((float)y, (float)v.y(), (float)f);
        z = s.interpolate((float)z, (float)v.z(), (float)f);
        return this;
    }

    /**
     * Scales vector uniformly by factor -1 ( v = -v ), overrides coordinates
     * with result.
     *
     * @return itself
     */
    public final Vec3DDouble invert() {
        x *= -1;
        y *= -1;
        z *= -1;
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3DDouble#isInAABB(toxi.geom.AABB)
     */
    @Override
    public boolean isInAABB(AABB box) {
        final Vec3D min = box.getMin();
        final Vec3D max = box.getMax();
        if (x < min.x || x > max.x) {
            return false;
        }
        if (y < min.y || y > max.y) {
            return false;
        }
        return !(z < min.z || z > max.z);
    }

    @Override
    public boolean isInAABB(Vec3DDouble boxOrigin, Vec3DDouble boxExtent) {
        double w = boxExtent.x;
        if (x < boxOrigin.x - w || x > boxOrigin.x + w) {
            return false;
        }
        w = boxExtent.y;
        if (y < boxOrigin.y - w || y > boxOrigin.y + w) {
            return false;
        }
        w = boxExtent.z;
        return !(z < boxOrigin.z - w || z > boxOrigin.z + w);
    }

    @Override
    public final boolean isMajorAxis(double tol) {
        double ax = MathUtils.abs(x);
        double ay = MathUtils.abs(y);
        double az = MathUtils.abs(z);
        double itol = 1 - tol;
        if (ax > itol) {
            if (ay < tol) {
                return (az < tol);
            }
        } else if (ay > itol) {
            if (ax < tol) {
                return (az < tol);
            }
        } else if (az > itol) {
            if (ax < tol) {
                return (ay < tol);
            }
        }
        return false;
    }

    @Override
    public final boolean isZeroVector() {
        return MathUtils.abs(x) < MathUtils.EPS
                && MathUtils.abs(y) < MathUtils.EPS
                && MathUtils.abs(z) < MathUtils.EPS;
    }

    /**
     * Add random jitter to the vector in the range -j ... +j using the default
     * {@link Random} generator of {@link MathUtils}.
     *
     * @param j
     *            the j
     *
     * @return the vec3 d
     */
    public final Vec3DDouble jitter(double j) {
        return jitter(j, j, j);
    }

    /**
     * Adds random jitter to the vector in the range -j ... +j using the default
     * {@link Random} generator of {@link MathUtils}.
     *
     * @param jx
     *            maximum x jitter
     * @param jy
     *            maximum y jitter
     * @param jz
     *            maximum z jitter
     *
     * @return itself
     */
    public final Vec3DDouble jitter(double jx, double jy, double jz) {
        x += MathUtils.normalizedRandom() * jx;
        y += MathUtils.normalizedRandom() * jy;
        z += MathUtils.normalizedRandom() * jz;
        return this;
    }

    public final Vec3DDouble jitter(Random rnd, double j) {
        return jitter(rnd, j, j, j);
    }

    public final Vec3DDouble jitter(Random rnd, double jx, double jy, double jz) {
        x += MathUtils.normalizedRandom(rnd) * jx;
        y += MathUtils.normalizedRandom(rnd) * jy;
        z += MathUtils.normalizedRandom(rnd) * jz;
        return this;
    }

    public final Vec3DDouble jitter(Random rnd, Vec3DDouble jitterVec) {
        return jitter(rnd, jitterVec.x, jitterVec.y, jitterVec.z);
    }

    /**
     * Adds random jitter to the vector in the range defined by the given vector
     * components and using the default {@link Random} generator of
     * {@link MathUtils}.
     *
     * @param jitterVec
     *            the jitter vec
     *
     * @return itself
     */
    public final Vec3DDouble jitter(Vec3DDouble jitterVec) {
        return jitter(jitterVec.x, jitterVec.y, jitterVec.z);
    }

    /**
     * Limits the vector's magnitude to the length given.
     *
     * @param lim
     *            new maximum magnitude
     *
     * @return itself
     */
    public final Vec3DDouble limit(double lim) {
        if (magSquared() > lim * lim) {
            return normalize().scaleSelf(lim);
        }
        return this;
    }

    @Override
    public final double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    @Override
    public final double magSquared() {
        return x * x + y * y + z * z;
    }

    /**
     * Max self.
     *
     * @param b
     *            the b
     *
     * @return the vec3 d
     */
    public final Vec3DDouble maxSelf(ReadonlyVec3DDouble b) {
        x = MathUtils.max(x, b.x());
        y = MathUtils.max(y, b.y());
        z = MathUtils.max(z, b.z());
        return this;
    }

    /**
     * Min self.
     *
     * @param b
     *            the b
     *
     * @return the vec3 d
     */
    public final Vec3DDouble minSelf(ReadonlyVec3DDouble b) {
        x = MathUtils.min(x, b.x());
        y = MathUtils.min(y, b.y());
        z = MathUtils.min(z, b.z());
        return this;
    }

    /**
     * Applies a uniform modulo operation to the vector, using the same base for
     * all components.
     *
     * @param base
     *            the base
     *
     * @return itself
     */
    public final Vec3DDouble modSelf(double base) {
        x %= base;
        y %= base;
        z %= base;
        return this;
    }

    /**
     * Calculates modulo operation for each vector component separately.
     *
     * @param bx
     *            the bx
     * @param by
     *            the by
     * @param bz
     *            the bz
     *
     * @return itself
     */

    public final Vec3DDouble modSelf(double bx, double by, double bz) {
        x %= bx;
        y %= by;
        z %= bz;
        return this;
    }

    /**
     * Normalizes the vector so that its magnitude = 1.
     *
     * @return itself
     */
    public final Vec3DDouble normalize() {
        double mag = Math.sqrt(x * x + y * y + z * z);
        if (mag > 0) {
            mag = 1f / mag;
            x *= mag;
            y *= mag;
            z *= mag;
        }
        return this;
    }

    /**
     * Normalizes the vector to the given length.
     *
     * @param len
     *            desired length
     * @return itself
     */
    public final Vec3DDouble normalizeTo(double len) {
        double mag = Math.sqrt(x * x + y * y + z * z);
        if (mag > 0) {
            mag = len / mag;
            x *= mag;
            y *= mag;
            z *= mag;
        }
        return this;
    }

    /**
     * Replaces the vector components with their multiplicative inverse.
     *
     * @return itself
     */
    public final Vec3DDouble reciprocal() {
        x = 1f / x;
        y = 1f / y;
        z = 1f / z;
        return this;
    }

    public final Vec3DDouble reflect(ReadonlyVec3DDouble normal) {
        return set(normal.scale(this.dot(normal) * 2).subSelf(this));
    }

    /**
     * Rotates the vector around the giving axis.
     *
     * @param axis
     *            rotation axis vector
     * @param theta
     *            rotation angle (in radians)
     *
     * @return itself
     */
    public final Vec3DDouble rotateAroundAxis(ReadonlyVec3DDouble axis, double theta) {
        final double ax = axis.x();
        final double ay = axis.y();
        final double az = axis.z();
        final double ux = ax * x;
        final double uy = ax * y;
        final double uz = ax * z;
        final double vx = ay * x;
        final double vy = ay * y;
        final double vz = ay * z;
        final double wx = az * x;
        final double wy = az * y;
        final double wz = az * z;
        final double si = Math.sin(theta);
        final double co = Math.cos(theta);
        double xx = ax * (ux + vy + wz)
                + (x * (ay * ay + az * az) - ax * (vy + wz)) * co + (-wy + vz)
                * si;
        double yy = ay * (ux + vy + wz)
                + (y * (ax * ax + az * az) - ay * (ux + wz)) * co + (wx - uz)
                * si;
        double zz = az * (ux + vy + wz)
                + (z * (ax * ax + ay * ay) - az * (ux + vy)) * co + (-vx + uy)
                * si;
        x = xx;
        y = yy;
        z = zz;
        return this;
    }

    /**
     * Rotates the vector by the given angle around the X axis.
     *
     * @param theta
     *            the theta
     *
     * @return itself
     */
    public final Vec3DDouble rotateX(double theta) {
        final double co = Math.cos(theta);
        final double si = Math.sin(theta);
        final double zz = co * z - si * y;
        y = si * z + co * y;
        z = zz;
        return this;
    }

    /**
     * Rotates the vector by the given angle around the Y axis.
     *
     * @param theta
     *            the theta
     *
     * @return itself
     */
    public final Vec3DDouble rotateY(double theta) {
        final double co = Math.cos(theta);
        final double si = Math.sin(theta);
        final double xx = co * x - si * z;
        z = si * x + co * z;
        x = xx;
        return this;
    }

    /**
     * Rotates the vector by the given angle around the Z axis.
     *
     * @param theta
     *            the theta
     *
     * @return itself
     */
    public final Vec3DDouble rotateZ(double theta) {
        final double co = Math.cos(theta);
        final double si = Math.sin(theta);
        final double xx = co * x - si * y;
        y = si * x + co * y;
        x = xx;
        return this;
    }

/*
    public Vec3DDouble roundTo(double prec) {
        x = MathUtils.roundTo(x, prec);
        y = MathUtils.roundTo(y, prec);
        z = MathUtils.roundTo(z, prec);
        return this;
    }
*/

    @Override
    public Vec3DDouble scale(double s) {
        return new Vec3DDouble(x * s, y * s, z * s);
    }

    @Override
    public Vec3DDouble scale(double a, double b, double c) {
        return new Vec3DDouble(x * a, y * b, z * c);
    }

    @Override
    public Vec3DDouble scale(ReadonlyVec3DDouble s) {
        return new Vec3DDouble(x * s.x(), y * s.y(), z * s.z());
    }

    public Vec3DDouble scale(Vec3DDouble s) {
        return new Vec3DDouble(x * s.x, y * s.y, z * s.z);
    }

    /**
     * Scales vector uniformly and overrides coordinates with result.
     *
     * @param s
     *            scale factor
     *
     * @return itself
     */
    public Vec3DDouble scaleSelf(double s) {
        x *= s;
        y *= s;
        z *= s;
        return this;
    }

    /**
     * Scales vector non-uniformly by vector {a,b,c} and overrides coordinates
     * with result.
     *
     * @param a
     *            scale factor for X coordinate
     * @param b
     *            scale factor for Y coordinate
     * @param c
     *            scale factor for Z coordinate
     *
     * @return itself
     */
    public Vec3DDouble scaleSelf(double a, double b, double c) {
        x *= a;
        y *= b;
        z *= c;
        return this;
    }

    public Vec3DDouble scaleSelf(ReadonlyVec3DDouble s) {
        x *= s.x();
        y *= s.y();
        z *= s.z();
        return this;
    }

    /**
     * Scales vector non-uniformly by vector v and overrides coordinates with
     * result.
     *
     * @param s
     *            scale vector
     *
     * @return itself
     */

    public Vec3DDouble scaleSelf(Vec3DDouble s) {
        x *= s.x;
        y *= s.y;
        z *= s.z;
        return this;
    }

    /**
     * Overrides coordinates with the given values.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     *
     * @return itself
     */
    public Vec3DDouble set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vec3DDouble set(ReadonlyVec3DDouble v) {
        x = v.x();
        y = v.y();
        z = v.z();
        return this;
    }

    /**
     * Overrides coordinates with the ones of the given vector.
     *
     * @param v
     *            vector to be copied
     *
     * @return itself
     */
    public Vec3DDouble set(Vec3DDouble v) {
        x = v.x;
        y = v.y;
        z = v.z;
        return this;
    }

    public final Vec3DDouble setComponent(Axis id, double val) {
        switch (id) {
            case X:
                x = val;
                break;
            case Y:
                y = val;
                break;
            case Z:
                z = val;
                break;
        }
        return this;
    }

    public final Vec3DDouble setComponent(int id, double val) {
        switch (id) {
            case 0:
                x = val;
                break;
            case 1:
                y = val;
                break;
            case 2:
                z = val;
                break;
        }
        return this;
    }

    public Vec3DDouble setX(double x) {
        this.x = x;
        return this;
    }

    /**
     * Overrides XY coordinates with the ones of the given 2D vector.
     *
     * @param v
     *            2D vector
     *
     * @return itself
     */
    public Vec3DDouble setXY(Vec2D v) {
        x = v.x;
        y = v.y;
        return this;
    }

    public Vec3DDouble setY(double y) {
        this.y = y;
        return this;
    }

    public Vec3DDouble setZ(double z) {
        this.z = z;
        return this;
    }

    public Vec3DDouble shuffle(int iterations) {
        double t;
        for (int i = 0; i < iterations; i++) {
            switch (MathUtils.random(3)) {
                case 0:
                    t = x;
                    x = y;
                    y = t;
                    break;
                case 1:
                    t = x;
                    x = z;
                    z = t;
                    break;
                case 2:
                    t = y;
                    y = z;
                    z = t;
                    break;
            }
        }
        return this;
    }

    /**
     * Replaces all vector components with the signum of their original values.
     * In other words if a components value was negative its new value will be
     * -1, if zero =&gt; 0, if positive =&gt; +1
     *
     * @return itself
     */
    public Vec3DDouble signum() {
        x = (x < 0 ? -1 : x == 0 ? 0 : 1);
        y = (y < 0 ? -1 : y == 0 ? 0 : 1);
        z = (z < 0 ? -1 : z == 0 ? 0 : 1);
        return this;
    }

    /**
     * Rounds the vector to the closest major axis. Assumes the vector is
     * normalized.
     *
     * @return itself
     */
    public final Vec3DDouble snapToAxis() {
        if (MathUtils.abs(x) < 0.5f) {
            x = 0;
        } else {
            x = x < 0 ? -1 : 1;
            y = z = 0;
        }
        if (MathUtils.abs(y) < 0.5f) {
            y = 0;
        } else {
            y = y < 0 ? -1 : 1;
            x = z = 0;
        }
        if (MathUtils.abs(z) < 0.5f) {
            z = 0;
        } else {
            z = z < 0 ? -1 : 1;
            x = y = 0;
        }
        return this;
    }

    @Override
    public final Vec3DDouble sub(double a, double b, double c) {
        return new Vec3DDouble(x - a, y - b, z - c);
    }

    @Override
    public final Vec3DDouble sub(ReadonlyVec3DDouble v) {
        return new Vec3DDouble(x - v.x(), y - v.y(), z - v.z());
    }

    public final Vec3DDouble sub(Vec3DDouble v) {
        return new Vec3DDouble(x - v.x, y - v.y, z - v.z);
    }

    /**
     * Subtracts vector {a,b,c} and overrides coordinates with result.
     *
     * @param a
     *            X coordinate
     * @param b
     *            Y coordinate
     * @param c
     *            Z coordinate
     *
     * @return itself
     */
    public final Vec3DDouble subSelf(double a, double b, double c) {
        x -= a;
        y -= b;
        z -= c;
        return this;
    }

    public final Vec3DDouble subSelf(ReadonlyVec3DDouble v) {
        x -= v.x();
        y -= v.y();
        z -= v.z();
        return this;
    }

    /**
     * Subtracts vector v and overrides coordinates with result.
     *
     * @param v
     *            vector to be subtracted
     *
     * @return itself
     */
    public final Vec3DDouble subSelf(Vec3DDouble v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
        return this;
    }

    @Override
    public final Vec2D to2DXY() {
        return new Vec2D((float)x, (float)y);
    }

    @Override
    public final Vec2D to2DXZ() {
        return new Vec2D((float)x, (float)z);
    }

    @Override
    public final Vec2D to2DYZ() {
        return new Vec2D((float)y, (float)z);
    }

    @Override
    public double[] toArray() {
        return new double[] { x, y, z };
    }

    @Override
    public double[] toArray4(double w) {
        return new double[] { x, y, z, w };
    }

    public final Vec3DDouble toCartesian() {
        final double a = x * Math.cos(z);
        final double xx = a * Math.cos(y);
        final double yy = x * Math.sin(z);
        final double zz = a * Math.sin(y);
        x = xx;
        y = yy;
        z = zz;
        return this;
    }

    public final Vec3DDouble toSpherical() {
        final double xx = Math.abs(x) <= MathUtils.EPS ? MathUtils.EPS : x;
        final double zz = z;

        final double radius = Math.sqrt((xx * xx) + (y * y) + (zz * zz));
        z = Math.asin(y / radius);
        y = Math.atan(zz / xx) + (xx < 0.0 ? MathUtils.PI : 0);
        x = radius;
        return this;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(48);
        sb.append("{x:").append(x).append(", y:").append(y).append(", z:")
                .append(z).append("}");
        return sb.toString();
    }

    @Override
    public final double x() {
        return x;
    }

    @Override
    public final double y() {
        return y;
    }

    @Override
    public final double z() {
        return z;
    }
}
