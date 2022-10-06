/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

// External imports
import java.io.InputStream;
import java.io.IOException;
import java.util.Map;

import org.w3c.dom.Node;

// Local imports
// None

/**
 * Basic browser interface that represents the interface to the VRML browser
 * from any application.
 * <p>
 * Individual VRML browser implementors are to extend this
 * interface and provide this functionality. The individual users will not see
 * anything but this interface.
 * <p>
 * A number of the methods in this application can take strings representing URLs.
 * Relative URL strings contained in URL fields of nodes or these method
 * arguments are interpreted as follows:
 * <p>
 * Relative URLs are treated as per clause B.3.5 of the EAI Java Bindings
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.15 $
 */
public interface Browser {

    /**
     * Get the name of the browser. The name is an implementation specific
     * string representing the browser.
     *
     * @return The name of the browser or null if not supported
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    String getName()
        throws InvalidBrowserException;

    /**
     * Get the version of the browser. Returns an implementation specific
     * representation of the version number.
     *
     * @return The version of the browser or null if not supported
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    String getVersion()
        throws InvalidBrowserException;

    /**
     * Get a listing of the profiles that this browser implementation is
     * capable of supporting.
     *
     * @return The listing of all supported profiles
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    ProfileInfo[] getSupportedProfiles()
        throws InvalidBrowserException;

    /**
     * Get a specific profile.
     *
     * @param name The profile name
     * @return The specified profile
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     * @throws NotSupportedException The requested profile is not supported
     */
    ProfileInfo getProfile(String name)
        throws InvalidBrowserException, NotSupportedException;

    /**
     * Get a listing of all the components that this browser implementation is
     * capable of supporting.
     *
     * @return The listing of all supported components
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    ComponentInfo[] getSupportedComponents()
      throws InvalidBrowserException;

    /**
     * Get specific component info at the requested support level.
     *
     * @param name The component name
     * @param level The minimum component level
     * @return The specified component info
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     * @throws NotSupportedException The requested profile is not supported
     */
    ComponentInfo getComponentInfo(String name, int level)
        throws InvalidBrowserException, NotSupportedException;

    /**
     * Get the information about the current scene. If no scene has been set
     * then this will return null.
     *
     *
     * @return The current scene data
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    X3DExecutionContext getExecutionContext()
        throws InvalidBrowserException;

    /**
     * Create a new, empty scene that can be used to programmatically fill in
     * new scenes. This scene has the give profile and/or component information
     * set, and is not currently being rendered. Either argument may be null,
     * but not both arguments. If the profile is null, then the default profile
     * of "NONE" is used, and only the components are used. The scene version
     * is set to the highest supported version of the specification that this
     * browser currently implements.
     *
     * @param profile The profile to use for this scene or null for none
     * @param components The list of components to use or null
     * @return A new empty scene to work with
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws IllegalArgumentException Both arguments provided are null or
     *   zero length
     */
    X3DScene createScene(ProfileInfo profile, ComponentInfo[] components)
        throws InvalidBrowserException, IllegalArgumentException;

    /**
     * Get the current velocity of the bound viewpoint in meters per second.
     * The velocity is defined in terms of the world values, not the local
     * coordinate system of the viewpoint.
     *
     * @return The velocity in m/s or 0.0 if not supported
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    float getCurrentSpeed()
        throws InvalidBrowserException;

    /**
     * Get the current frame rate of the browser in frames per second.
     *
     * @return The current frame rate or 0.0 if not supported
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    float getCurrentFrameRate()
        throws InvalidBrowserException;

    /**
     * Replace the current world with the given nodes. Replaces the entire
     * contents of the VRML world with the new nodes. Any node references that
     * belonged to the previous world are still valid but no longer form part of
     * the scene graph (unless it is these nodes passed to this method). The
     * URL of the world still represents the just unloaded world.
     * <p>
     * Calling this method causes a SHUTDOWN event followed by an INITIALIZED
     * event to be generated.
     *
     * @param scene The new scene to render in the browser
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    void replaceWorld(X3DScene scene)
        throws InvalidBrowserException;

    /**
     * Load the URL as the new root of the scene. Replaces all the current
     * scene graph with the new world. A non-blocking call that will change the
     * contents at some time in the future.
     * <p>
     * Generates an immediate SHUTDOWN event and then when the new contents are
     * ready to be loaded, sends an INITIALIZED event.
     *
     * @param url The list of URLs in decreasing order of preference as defined
     *   in the VRML97 specification.
     * @param parameters The list of parameters to accompany the load call as
     *   defined in the Anchor node specification of VRML97
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws InvalidURLException All of the URLs passed to this method are
     *    bogus and cannot be translated to usable values
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    void loadURL(String[] url, Map<String, Object> parameters)
        throws InvalidBrowserException, InvalidURLException;

    /**
     * Get the description of the current world.
     *
     * @return A description string or null if none set
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    String getDescription()
        throws InvalidBrowserException;

    /**
     * Set the description of the current world. If the world is operating as
     * part of a web browser then it shall attempt to set the title of the
     * window. If the browser is from a component then the result is dependent
     * on the implementation
     *
     * @param desc The description string to set.
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    void setDescription(String desc)
        throws InvalidBrowserException;

    /**
     * Parse the given string and turn this into a list of X3D nodes. Method
     * is a blocking call that won't return until all of the top level nodes
     * defined in the string have been returned.
     * <p>
     * At the point that this method returns, external files such as textures,
     * sounds and inlines may not have been loaded.
     * <p>
     * The string may contain all legal X3D syntax - either UTF8 or XML
     * encoding. The X3D header line is not required to be present in the
     * string if UTF8 syntax.
     *
     * @param x3dString The string containing VRML string syntax
     * @return The scene that was created from the string.
     * @throws InvalidX3DException If the string does not contain legal
     *   X3D/VRML syntax or no node instantiations
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    X3DScene createX3DFromString(String x3dString)
        throws InvalidBrowserException,
               InvalidX3DException;

    /**
     * Parse the contents of the stream and interpret it as X3D content. The
     * browser shall interpret the content using the initial characters of
     * the stream to determine which encoding and file format is used.
     *
     * @param fileParent the parent directory to form our base URL
     * @param is The input stream that contains the content to parse
     * @return The scene that was created from the string.
     * @throws InvalidX3DException If the string does not contain legal
     *   X3D/VRML syntax or no node instantiations
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     * @throws IOException An I/O Error occurred while reading the stream
     */
    X3DScene createX3DFromStream(String fileParent, InputStream is)
        throws InvalidBrowserException,
               InvalidX3DException,
               IOException;

    /**
     * Create and load X3D from the given URL. The call will not return until
     * the basic top-level scene has been processed or an error has occurred.
     * Inlines, textures, sound and externprotos are not guaranteed to be
     * loaded at this time. If the caller needs to know what the final URL was
     * that loaded, use the getWorldURL() call from the returned scene.
     *
     * @param url The list of URLs in decreasing order of preference as defined
     *   in the VRML97/X3D specification.
     * @return The scene that was created from the URLs.
     * @throws InvalidX3DException If the string does not contain legal
     *   X3D/VRML syntax or no node instantiations
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws InvalidURLException All of the URLs passed to this method are
     *    bogus and cannot be translated to usable values
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    X3DScene createX3DFromURL(String[] url)
        throws InvalidBrowserException,
               InvalidURLException,
               InvalidX3DException;

    /**
     * A utility request to import a W3C DOM document or document fragment and
     * convert it to an X3D scene. The method only performs a conversion
     * process and does not display the resulting scene. The scene may then be
     * used as the argument for the replaceWorld service. When the conversion
     * is made, there is no lasting connection between the DOM and the
     * generated scene. Each request shall be a one-off conversion attempt
     * (the conversion may not be successful if the DOM does not match the X3D
     * scene graph structure).
     *
     *
     * @param element The root element to convert
     * @return A scene representation corresponding to the document
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws InvalidDocumentException The document structure cannot be
     *    converted to an X3D scene for some reason
     */
    X3DScene importDocument(Node element)
        throws InvalidBrowserException,
               InvalidDocumentException,
               NotSupportedException;

    /**
     * Print the message to the browser console without wrapping a new line
     * onto it.
     *
     * @param msg The object to be printed
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void print(Object msg)
        throws InvalidBrowserException;

    /**
     * Print the message to the browser console and append a new line
     * onto it.
     *
     * @param msg The object to be printed
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void println(Object msg)
        throws InvalidBrowserException;

    /**
     * Bind the next viewpoint in the list. The definition of "next" is not
     * specified, and may be browser dependent. If only one viewpoint is
     * declared, this method does nothing.
     *
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void nextViewpoint()
        throws InvalidBrowserException;

    /**
     * Bind the next viewpoint in the list. The definition of "next" is not
     * specified, and may be browser dependent. If only one viewpoint is
     * declared, this method does nothing.
     *
     * @param layer index of the next viewpoint
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void nextViewpoint(int layer)
        throws InvalidBrowserException;

    /**
     * Bind the previous viewpoint in the list. The definition of "previous" is
     * not specified, and may be browser dependent. If only one viewpoint is
     * declared, this method does nothing.
     *
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void previousViewpoint()
        throws InvalidBrowserException;

    /**
     * Bind the previous viewpoint in the list. The definition of "previous" is
     * not specified, and may be browser dependent. If only one viewpoint is
     * declared, this method does nothing.
     *
     * @param layer index of the previous viewpoint
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void previousViewpoint(int layer)
        throws InvalidBrowserException;

    /**
     * Bind the first viewpoint in the list. This is the first viewpoint
     * declared in the user's file. ie The viewpoint that would be bound by
     * default on loading.
     *
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void firstViewpoint()
        throws InvalidBrowserException;

    /**
     * Bind the first viewpoint in the list. This is the first viewpoint
     * declared in the user's file. ie The viewpoint that would be bound by
     * default on loading.
     *
     * @param layer index of the first viewpoint
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void firstViewpoint(int layer)
        throws InvalidBrowserException;

    /**
     * Bind the last viewpoint in the list.
     *
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void lastViewpoint()
        throws InvalidBrowserException;

    /**
     * Bind the last viewpoint in the list.
     *
     * @param layer index of the last viewpoint
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void lastViewpoint(int layer)
        throws InvalidBrowserException;

    /**
     * Get the collection of rendering properties that the browser provides.
     * Rendering properties are key/value pairs, as defined in table 9.2 of
     * ISO/IEC 19775-1. Keys are instances of Strings, while the value is
     * dependent on the property. If the property is not defined in the
     * returned map, treat it as not being supported by the browser.
     *
     * @return A read-only map of the list of properties defined by the browser
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     * @throws InvalidOperationTimingException This was not called during the
     *    correct timing during a script (may be called at any time from
     *    external)
     */
    Map<String, Object> getRenderingProperties()
        throws InvalidBrowserException, InvalidOperationTimingException;

    /**
     * Get the collection of browser properties that the browser provides.
     * Rendering properties are key/value pairs, as defined in table 9.2 of
     * ISO/IEC 19775-1. Keys are instances of Strings, while the value is
     * dependent on the property. If the property is not defined in the
     * returned map, treat it as not being supported by the browser.
     *
     * @return A read-only map of the list of properties defined by the browser
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     * @throws InvalidOperationTimingException This was not called during the
     *    correct timing during a script (may be called at any time from
     *    external)
     */
    Map<String, Object> getBrowserProperties()
        throws InvalidBrowserException, InvalidOperationTimingException;

}
