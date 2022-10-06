/**
 * ***************************************************************************
 * Web3d.org Copyright (c) 2009 Java Source
 *
 * This source is licensed under the GNU LGPL v2.1 Please read
 * http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any purpose.
 * Use it at your own risk. If there's a problem you get to fix it.
 *
 ***************************************************************************
 */
package org.xj3d.impl.core.eventmodel;

// External imports
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector3d;

import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.operation.transform.GeocentricTransform;

// Local imports
import org.xj3d.core.eventmodel.OriginManager;

/**
 * OriginManager implementation.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
class DefaultOriginManager implements OriginManager {

    /**
     * Property name determining whether the GeoOrigin nodes should be used
     * instead of the OriginManager as source of origin data
     */
    private static final String ORIGIN_MANAGER_ENABLED_PROP =
            "org.xj3d.core.eventmodel.OriginManager.enabled";
    /**
     * Property name for the elevation threshold
     */
    private static final String DEFAULT_ORIGIN_MANAGER_ELEVATION_THRESHOLD_PROP =
            "org.xj3d.impl.core.eventmodel.DefaultOriginManager.elevation_threshold";
    /**
     * Property name for the elevation threshold
     */
    private static final String DEFAULT_ORIGIN_MANAGER_DISTANCE_THRESHOLD_PROP =
            "org.xj3d.impl.core.eventmodel.DefaultOriginManager.distance_threshold";
    /**
     * The default elevation above the ellipsoid at which a new origin is
     * established
     */
    private static final double DEFAULT_ELEVATION_THRESHOLD = 50_000;
    /**
     * The default orthodromic distance from an active origin at which a new
     * origin is established
     */
    private static final double DEFAULT_DISTANCE_THRESHOLD = 50_000;
    /**
     * The actual elevation above the ellipsoid at which a new origin is
     * established
     */
    private double elevation_threshold;
    /**
     * The actual orthodromic distance from an active origin at which a new
     * origin is established
     */
    private double distance_threshold;
    /**
     * Flag indicating whether dynamic origin calculation is enabled
     */
    private boolean enabled;
    /**
     * Object containing the origin
     */
    private Vector3d origin;
    /**
     * Coordinate transformer
     */
    private GeocentricTransform gt;
    /**
     * Coordinate transformer
     */
    private GeodeticCalculator gd;
    /**
     * Scratch array to retrieve transformed ellipsoidal coordinates in [lng,
     * lat, elv] order
     */
    private double[] wgs84;
    /**
     * Scratch array for holding Earth Centered Earth Fixed cartesian
     * coordinates x, y - equatorial plane, +x intersects equator - prime
     * meridian +z = north polar axis
     */
    private double[] ecef;
    /**
     * Flag indicating that the origin object is active
     */
    private boolean originActive;

    /**
     * Constructor
     */
    DefaultOriginManager() {
        checkProperties();
        if (enabled) {
            origin = new Vector3d();
            gt = new GeocentricTransform(DefaultEllipsoid.WGS84, true);
            //gd = new GeodeticCalculator(DefaultEllipsoid.WGS84);
            wgs84 = new double[3];
            ecef = new double[3];
            originActive = false;
        }
    }

    //-------------------------------------------------------------
    // Methods defined by OriginManager
    //-------------------------------------------------------------
    /**
     * Return the coordinate to be used as the origin.
     *
     * @return The object containing the origin, or null if one is not active.
     */
    @Override
    public Vector3d getOrigin() {
        if (originActive) {
            return (origin);
        } else {
            return (null);
        }
    }

    /**
     * Notify the manager of a change in view position, return whether this
     * change has caused a recalculation of the origin.
     *
     * @param position The new view position
     * @return true if the origin has changed, false otherwise.
     */
    @Override
    public boolean updateViewPosition(Vector3f position) {

        boolean update = false;

        if (enabled) {

            ecef[0] = position.x;
            ecef[1] = position.z;
            ecef[2] = position.y;

            if (originActive) {
                ecef[0] += origin.x;
                ecef[1] += origin.z;
                ecef[2] += origin.y;
            }

            gt.inverseTransform(ecef, 0, wgs84, 0, 1);

            if (originActive) {
                if (wgs84[2] > elevation_threshold) {
                    // the elevation above the ellipsoid is greater than the trigger,
                    // change origin to earth center
                    origin.set(0, 0, 0);
//System.out.println( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> RESET" );
                    originActive = false;
                    update = true;
                } else {
                    gd.setDestinationGeographicPoint(wgs84[0], wgs84[1]);
                    double distance = gd.getOrthodromicDistance();
                    if (distance > distance_threshold) {
                        // the distance from the active origin is greater than the trigger,
                        // reset the origin to the current position at the ellipsoid surface
                        wgs84[2] = 0;
                        gt.transform(wgs84, 0, ecef, 0, 1);
                        origin.set(ecef[0], ecef[2], ecef[1]);
//System.out.println( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>TRIGGER_DISTANCE>"+ java.util.Arrays.toString(wgs84) );
                        gd = new GeodeticCalculator(DefaultEllipsoid.WGS84);
                        gd.setStartingGeographicPoint(wgs84[0], wgs84[1]);
                        update = true;
                    }
                }
            } else {
                if (wgs84[2] < elevation_threshold) {
                    // the elevation above the ellipsoid is less than the trigger,
                    // set the origin to the current position at the ellipsoid surface
                    wgs84[2] = 0;
                    gt.transform(wgs84, 0, ecef, 0, 1);
                    origin.set(ecef[0], ecef[2], ecef[1]);
//System.out.println( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>TRIGGER_ELEVATION>"+ java.util.Arrays.toString(wgs84) );
                    originActive = true;
                    gd = new GeodeticCalculator(DefaultEllipsoid.WGS84);
                    gd.setStartingGeographicPoint(wgs84[0], wgs84[1]);
                    update = true;
                }
            }
        }
        return (update);
    }

    /**
     * Return whether Geo* nodes should use this manager as their source for
     * origin data. If disabled, then Geo* nodes should use the GeoOrigin node
     * for data.
     *
     * @return true if Geo* nodes should use this manager. false if nodes should
     * the GeoOrigin node.
     */
    @Override
    public boolean getEnabled() {
        return (enabled);
    }

    //-------------------------------------------------------------
    // Local Methods
    //-------------------------------------------------------------
    /**
     * Configure working parameters from properties
     */
    private void checkProperties() {
        String[] prop = AccessController.doPrivileged(
                new PrivilegedAction<String[]>() {
            @Override
            public String[] run() {
                String[] prop = new String[3];
                prop[0] = System.getProperty(ORIGIN_MANAGER_ENABLED_PROP);
                prop[1] = System.getProperty(DEFAULT_ORIGIN_MANAGER_ELEVATION_THRESHOLD_PROP);
                prop[2] = System.getProperty(DEFAULT_ORIGIN_MANAGER_DISTANCE_THRESHOLD_PROP);
                return (prop);
            }
        });

        // enabled flag
        if (prop[0] == null) {
            enabled = false;
        } else {
            enabled = Boolean.parseBoolean(prop[0]);
        }

        // trigger elevation
        if (prop[1] == null) {
            elevation_threshold = DEFAULT_ELEVATION_THRESHOLD;
        } else {
            try {
                elevation_threshold = Double.parseDouble(prop[1]);
            } catch (NumberFormatException nfe) {
                elevation_threshold = DEFAULT_ELEVATION_THRESHOLD;
            }
        }

        // trigger distance
        if (prop[2] == null) {
            distance_threshold = DEFAULT_DISTANCE_THRESHOLD;
        } else {
            try {
                distance_threshold = Double.parseDouble(prop[2]);
            } catch (NumberFormatException nfe) {
                distance_threshold = DEFAULT_DISTANCE_THRESHOLD;
            }
        }
    }
}
