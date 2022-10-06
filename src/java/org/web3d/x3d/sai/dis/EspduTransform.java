/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai.dis;

import org.web3d.x3d.sai.X3DGroupingNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DSensorNode;

/**
 * Defines the requirements of an X3D EspduTransform node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface EspduTransform extends X3DGroupingNode, X3DSensorNode {

    /**
     * Return the center value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getCenter(float[] val);

    /**
     * Set the center field.
     *
     * @param val The float[] to set.
     */
    void setCenter(float[] val);

    /**
     * Return the rotation value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getRotation(float[] val);

    /**
     * Set the rotation field.
     *
     * @param val The float[] to set.
     */
    void setRotation(float[] val);

    /**
     * Return the scale value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getScale(float[] val);

    /**
     * Set the scale field.
     *
     * @param val The float[] to set.
     */
    void setScale(float[] val);

    /**
     * Return the scaleOrientation value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getScaleOrientation(float[] val);

    /**
     * Set the scaleOrientation field.
     *
     * @param val The float[] to set.
     */
    void setScaleOrientation(float[] val);

    /**
     * Return the translation value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getTranslation(float[] val);

    /**
     * Set the translation field.
     *
     * @param val The float[] to set.
     */
    void setTranslation(float[] val);

    /**
     * Return the marking String value.
     *
     * @return The marking String value.
     */
    String getMarking();

    /**
     * Set the marking field.
     *
     * @param val The String to set.
     */
    void setMarking(String val);

    /**
     * Return the siteID int value.
     *
     * @return The siteID int value.
     */
    int getSiteID();

    /**
     * Set the siteID field.
     *
     * @param val The int to set.
     */
    void setSiteID(int val);

    /**
     * Return the applicationID int value.
     *
     * @return The applicationID int value.
     */
    int getApplicationID();

    /**
     * Set the applicationID field.
     *
     * @param val The int to set.
     */
    void setApplicationID(int val);

    /**
     * Return the entityID int value.
     *
     * @return The entityID int value.
     */
    int getEntityID();

    /**
     * Set the entityID field.
     *
     * @param val The int to set.
     */
    void setEntityID(int val);

    /**
     * Return the readInterval double value.
     *
     * @return The readInterval double value.
     */
    double getReadInterval();

    /**
     * Set the readInterval field.
     *
     * @param val The double to set.
     */
    void setReadInterval(double val);

    /**
     * Return the writeInterval double value.
     *
     * @return The writeInterval double value.
     */
    double getWriteInterval();

    /**
     * Set the writeInterval field.
     *
     * @param val The double to set.
     */
    void setWriteInterval(double val);

    /**
     * Return the networkMode String value.
     *
     * @return The networkMode String value.
     */
    String getNetworkMode();

    /**
     * Set the networkMode field.
     *
     * @param val The String to set.
     */
    void setNetworkMode(String val);

    /**
     * Return the address String value.
     *
     * @return The address String value.
     */
    String getAddress();

    /**
     * Set the address field.
     *
     * @param val The String to set.
     */
    void setAddress(String val);

    /**
     * Return the port int value.
     *
     * @return The port int value.
     */
    int getPort();

    /**
     * Set the port field.
     *
     * @param val The int to set.
     */
    void setPort(int val);

    /**
     * Return the articulationParameterCount int value.
     *
     * @return The articulationParameterCount int value.
     */
    int getArticulationParameterCount();

    /**
     * Set the articulationParameterCount field.
     *
     * @param val The int to set.
     */
    void setArticulationParameterCount(int val);

    /**
     * Return the number of MFFloat items in the articulationParameterArray
     * field.
     *
     * @return the number of MFFloat items in the articulationParameterArray
     * field.
     */
    int getNumArticulationParameterArray();

    /**
     * Return the articulationParameterArray value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getArticulationParameterArray(float[] val);

    /**
     * Set the articulationParameterArray field.
     *
     * @param val The float[] to set.
     */
    void setArticulationParameterArray(float[] val);

    /**
     * Return the articulationParameterValue0 float value.
     *
     * @return The articulationParameterValue0 float value.
     */
    float getArticulationParameterValue0();

    /**
     * Return the articulationParameterValue1 float value.
     *
     * @return The articulationParameterValue1 float value.
     */
    float getArticulationParameterValue1();

    /**
     * Return the articulationParameterValue2 float value.
     *
     * @return The articulationParameterValue2 float value.
     */
    float getArticulationParameterValue2();

    /**
     * Return the articulationParameterValue3 float value.
     *
     * @return The articulationParameterValue3 float value.
     */
    float getArticulationParameterValue3();

    /**
     * Return the articulationParameterValue4 float value.
     *
     * @return The articulationParameterValue4 float value.
     */
    float getArticulationParameterValue4();

    /**
     * Return the articulationParameterValue5 float value.
     *
     * @return The articulationParameterValue5 float value.
     */
    float getArticulationParameterValue5();

    /**
     * Return the articulationParameterValue6 float value.
     *
     * @return The articulationParameterValue6 float value.
     */
    float getArticulationParameterValue6();

    /**
     * Return the articulationParameterValue7 float value.
     *
     * @return The articulationParameterValue7 float value.
     */
    float getArticulationParameterValue7();

    /**
     * Set the articulationParameterValue0 field.
     *
     * @param val The float to set.
     */
    void setArticulationParameterValue0(float val);

    /**
     * Set the articulationParameterValue1 field.
     *
     * @param val The float to set.
     */
    void setArticulationParameterValue1(float val);

    /**
     * Set the articulationParameterValue2 field.
     *
     * @param val The float to set.
     */
    void setArticulationParameterValue2(float val);

    /**
     * Set the articulationParameterValue3 field.
     *
     * @param val The float to set.
     */
    void setArticulationParameterValue3(float val);

    /**
     * Set the articulationParameterValue4 field.
     *
     * @param val The float to set.
     */
    void setArticulationParameterValue4(float val);

    /**
     * Set the articulationParameterValue5 field.
     *
     * @param val The float to set.
     */
    void setArticulationParameterValue5(float val);

    /**
     * Set the articulationParameterValue6 field.
     *
     * @param val The float to set.
     */
    void setArticulationParameterValue6(float val);

    /**
     * Set the articulationParameterValue7 field.
     *
     * @param val The float to set.
     */
    void setArticulationParameterValue7(float val);

    /**
     * Return the timestamp double value.
     *
     * @return The timestamp double value.
     */
    double getTimestamp();

    /**
     * Return the detonationResult int value.
     *
     * @return The detonationResult int value.
     */
    int getDetonationResult();

    /**
     * Set the detonationResult field.
     *
     * @param val The int to set.
     */
    void setDetonationResult(int val);

    /**
     * Return the detonationLocation value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getDetonationLocation(float[] val);

    /**
     * Set the detonationLocation field.
     *
     * @param val The float[] to set.
     */
    void setDetonationLocation(float[] val);

    /**
     * Return the detonationRelativeLocation value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getDetonationRelativeLocation(float[] val);

    /**
     * Set the detonationRelativeLocation field.
     *
     * @param val The float[] to set.
     */
    void setDetonationRelativeLocation(float[] val);

    /**
     * Return the isDetonated boolean value.
     *
     * @return The isDetonated boolean value.
     */
    boolean getIsDetonated();

    /**
     * Return the detonateTime double value.
     *
     * @return The detonateTime double value.
     */
    double getDetonateTime();

    /**
     * Return the eventApplicationID int value.
     *
     * @return The eventApplicationID int value.
     */
    int getEventApplicationID();

    /**
     * Set the eventApplicationID field.
     *
     * @param val The int to set.
     */
    void setEventApplicationID(int val);

    /**
     * Return the eventEntityID int value.
     *
     * @return The eventEntityID int value.
     */
    int getEventEntityID();

    /**
     * Set the eventEntityID field.
     *
     * @param val The int to set.
     */
    void setEventEntityID(int val);

    /**
     * Return the eventSiteID int value.
     *
     * @return The eventSiteID int value.
     */
    int getEventSiteID();

    /**
     * Set the eventSiteID field.
     *
     * @param val The int to set.
     */
    void setEventSiteID(int val);

    /**
     * Return the fired1 boolean value.
     *
     * @return The fired1 boolean value.
     */
    boolean getFired1();

    /**
     * Set the fired1 field.
     *
     * @param val The boolean to set.
     */
    void setFired1(boolean val);

    /**
     * Return the fired2 boolean value.
     *
     * @return The fired2 boolean value.
     */
    boolean getFired2();

    /**
     * Set the fired2 field.
     *
     * @param val The boolean to set.
     */
    void setFired2(boolean val);

    /**
     * Return the fireMissionIndex int value.
     *
     * @return The fireMissionIndex int value.
     */
    int getFireMissionIndex();

    /**
     * Set the fireMissionIndex field.
     *
     * @param val The int to set.
     */
    void setFireMissionIndex(int val);

    /**
     * Return the firingRange float value.
     *
     * @return The firingRange float value.
     */
    float getFiringRange();

    /**
     * Set the firingRange field.
     *
     * @param val The float to set.
     */
    void setFiringRange(float val);

    /**
     * Return the firingRate int value.
     *
     * @return The firingRate int value.
     */
    int getFiringRate();

    /**
     * Set the firingRate field.
     *
     * @param val The int to set.
     */
    void setFiringRate(int val);

    /**
     * Return the munitionApplicationID int value.
     *
     * @return The munitionApplicationID int value.
     */
    int getMunitionApplicationID();

    /**
     * Set the munitionApplicationID field.
     *
     * @param val The int to set.
     */
    void setMunitionApplicationID(int val);

    /**
     * Return the munitionEndPoint value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getMunitionEndPoint(float[] val);

    /**
     * Set the munitionEndPoint field.
     *
     * @param val The float[] to set.
     */
    void setMunitionEndPoint(float[] val);

    /**
     * Return the munitionEntityID int value.
     *
     * @return The munitionEntityID int value.
     */
    int getMunitionEntityID();

    /**
     * Set the munitionEntityID field.
     *
     * @param val The int to set.
     */
    void setMunitionEntityID(int val);

    /**
     * Return the munitionSiteID int value.
     *
     * @return The munitionSiteID int value.
     */
    int getMunitionSiteID();

    /**
     * Set the munitionSiteID field.
     *
     * @param val The int to set.
     */
    void setMunitionSiteID(int val);

    /**
     * Return the munitionStartPoint value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getMunitionStartPoint(float[] val);

    /**
     * Set the munitionStartPoint field.
     *
     * @param val The float[] to set.
     */
    void setMunitionStartPoint(float[] val);

    /**
     * Return the firedTime double value.
     *
     * @return The firedTime double value.
     */
    double getFiredTime();

    /**
     * Set the firedTime field.
     *
     * @param val The double to set.
     */
    void setFiredTime(double val);

    /**
     * Return the number of MFString items in the geoSystem field.
     *
     * @return the number of MFString items in the geoSystem field.
     */
    int getNumGeoSystem();

    /**
     * Return the geoSystem value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getGeoSystem(String[] val);

    /**
     * Set the geoSystem field.
     *
     * @param val The String[] to set.
     */
    void setGeoSystem(String[] val);

    /**
     * Return the geoOrigin X3DNode value.
     *
     * @return The geoOrigin X3DNode value.
     */
    X3DNode getGeoOrigin();

    /**
     * Set the geoOrigin field.
     *
     * @param val The X3DNode to set.
     */
    void setGeoOrigin(X3DNode val);

    /**
     * Return the entityCategory int value.
     *
     * @return The entityCategory int value.
     */
    int getEntityCategory();

    /**
     * Set the entityCategory field.
     *
     * @param val The int to set.
     */
    void setEntityCategory(int val);

    /**
     * Return the entityDomain int value.
     *
     * @return The entityDomain int value.
     */
    int getEntityDomain();

    /**
     * Set the entityDomain field.
     *
     * @param val The int to set.
     */
    void setEntityDomain(int val);

    /**
     * Return the entityExtra int value.
     *
     * @return The entityExtra int value.
     */
    int getEntityExtra();

    /**
     * Set the entityExtra field.
     *
     * @param val The int to set.
     */
    void setEntityExtra(int val);

    /**
     * Return the entityKind int value.
     *
     * @return The entityKind int value.
     */
    int getEntityKind();

    /**
     * Set the entityKind field.
     *
     * @param val The int to set.
     */
    void setEntityKind(int val);

    /**
     * Return the entitySpecific int value.
     *
     * @return The entitySpecific int value.
     */
    int getEntitySpecific();

    /**
     * Set the entitySpecific field.
     *
     * @param val The int to set.
     */
    void setEntitySpecific(int val);

    /**
     * Return the entityCountry int value.
     *
     * @return The entityCountry int value.
     */
    int getEntityCountry();

    /**
     * Set the entityCountry field.
     *
     * @param val The int to set.
     */
    void setEntityCountry(int val);

    /**
     * Return the entitySubcategory int value.
     *
     * @return The entitySubcategory int value.
     */
    int getEntitySubcategory();

    /**
     * Set the entitySubcategory field.
     *
     * @param val The int to set.
     */
    void setEntitySubcategory(int val);

    /**
     * Return the appearance int value.
     *
     * @return The appearance int value.
     */
    int getAppearance();

    /**
     * Set the appearance field.
     *
     * @param val The int to set.
     */
    void setAppearance(int val);

    /**
     * Return the linearVelocity value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getLinearVelocity(float[] val);

    /**
     * Set the linearVelocity field.
     *
     * @param val The float[] to set.
     */
    void setLinearVelocity(float[] val);

    /**
     * Return the linearAcceleration value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getLinearAcceleration(float[] val);

    /**
     * Set the linearAcceleration field.
     *
     * @param val The float[] to set.
     */
    void setLinearAcceleration(float[] val);

    /**
     * Return the forceID int value.
     *
     * @return The forceID int value.
     */
    int getForceID();

    /**
     * Set the forceID field.
     *
     * @param val The int to set.
     */
    void setForceID(int val);

    /**
     * Return the number of MFString items in the xmppParams field.
     *
     * @return the number of MFString items in the xmppParams field.
     */
    int getNumXmppParams();

    /**
     * Return the xmppParams value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getXmppParams(String[] val);

    /**
     * Set the xmppParams field.
     *
     * @param val The String[] to set.
     */
    void setXmppParams(String[] val);
}
