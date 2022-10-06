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
 *****************************************************************************/

package org.xj3d.ui.construct;

// External imports
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.security.AccessController;
import java.security.PrivilegedAction;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.InvalidConfigurationException;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

import org.web3d.vrml.nodes.FrameStateManager;

import org.web3d.vrml.scripting.ScriptEngine;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ViewpointManager;

import org.xj3d.core.loading.WorldLoaderManager;

/**
 * Utility methods for loading classes.
 * 
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class ClassLoadUtilities {
    
    /** The logging identifier of this class */
    private static final String LOG_NAME = "ClassLoadUtilities";
    
    /** The error reporting mechanism */
    protected ErrorReporter errorReporter;
    
    /** 
     * Constructor 
     */
    public ClassLoadUtilities( ) {
        this( null );
    }
    
    /** 
     * Constructor 
     * 
     * @param reporter The error reporter
     */
    public ClassLoadUtilities( ErrorReporter reporter ) {
        errorReporter = 
            ( reporter == null ) ? new DefaultErrorReporter( ) : reporter;
    }
    
    /**
     * Load a class instance constructed from the default constructor.
     *
     * @param classname The fully qualified name of the class to load
     * @param required Is this class required for operation, If true - an exception
     * will be thrown if the class fails to load for any reason. If false - an
     * error report will be generated if the class fails to load, but execution 
     * will continue and a null will be returned.
     * @return The class instance.
     * @exception InvalidConfigurationException If the required flag is true and the
     * class cannot be loaded.
     */
    public Object loadClass( final String classname, final boolean required ) {
        Object obj = AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Class<?> cls = Class.forName( classname );
                return( cls.getDeclaredConstructor().newInstance( ) );
            } catch( Error err ) {
                if ( required ) {
                    throw new InvalidConfigurationException(
                            LOG_NAME +": Missing required Class:" + classname );
                } else {
                    //Exception ewrapper = new Exception( err.getMessage( ), err );
                    errorReporter.warningReport(
                            LOG_NAME +": Cannot load Class: " + classname, null );
                    //LOG_NAME +": Cannot load Class: " + classname, ewrapper );
                }
            } catch( ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e ) {
                if ( required ) {
                    throw new InvalidConfigurationException(
                            LOG_NAME +": Missing required Class:" + classname );
                } else {
                    errorReporter.warningReport(
                            LOG_NAME +": Cannot load Class: " + classname, e );
                }
            }
            
            return null;
        });
        //errorReporter.messageReport( LOG_NAME +": Class loaded: " + classname );
        return( obj );
    }
    
    /**
     * Load a class instance constructed with the argument parameters.
     *
     * @param classname The fully qualified name of the class to load.
     * @param constParams The array of parameters to pass to the Class's constructor.
     * @param paramTypes The array of class objects, matching the constParams argument
     * that are used to select the appropriate Class constructor.
     * @param required Is this class required for operation. If true - an exception
     * will be thrown if the class fails to load for any reason. If false - an
     * error report will be generated if the class fails to load, but execution 
     * will continue and a null will be returned.
     * @return The class instance.
     */
    public Object loadClass( 
        final String classname, 
        final Object[] constParams, 
        final Class[] paramTypes, 
        final boolean required ) {
        
        Object obj = AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Class<?> cls = Class.forName( classname );
                Constructor<?> constructor = cls.getConstructor( paramTypes );
                return( constructor.newInstance( constParams ) );
            } catch( Error err ) {
                if ( required ) {
                    throw new InvalidConfigurationException(
                            LOG_NAME +": Missing required Class:" + classname );
                } else {
                    Exception ewrapper = new Exception( err.getMessage( ), err );
                    errorReporter.warningReport(
                            LOG_NAME +": Cannot load class: " + classname, ewrapper );
                }
            }  catch( ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
                if ( required ) {
                    throw new InvalidConfigurationException(
                            LOG_NAME +": Missing required Class:" + classname );
                } else {
                    errorReporter.warningReport(
                            LOG_NAME +": Cannot load Class: " + classname, e );
                }
            }
            return( null );
        });
        //errorReporter.messageReport( LOG_NAME +": Class loaded: " + classname );
        return( obj );
    }
    
    /**
     * Load a script engine.
     *
     * @param classname The fully qualified name of the script engine class to load
     * @param core The browser core
     * @param viewpointManager The 
     * @param routeManager The route manager
     * @param stateManager The frame state manager
     * @param worldLoader The manager of the world loaders
     * @param required Is this class required for operation. If true - an exception
     * will be thrown if the class fails to load for any reason. If false - an
     * error report will be generated if the class fails to load, but execution 
     * will continue and a null will be returned.
     * @return The script engine instance.
     * @exception InvalidConfigurationException If the required flag is true and the
     * class cannot be loaded.
     */
    protected ScriptEngine loadScriptEngine(  
        final String classname,
        final BrowserCore core, 
        final ViewpointManager viewpointManager, 
        final RouteManager routeManager, 
        final FrameStateManager stateManager, 
        final WorldLoaderManager worldLoader,
        final boolean required ) {
        
        ScriptEngine scriptEngine = (ScriptEngine)AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            Class<?> scriptClass;
            Object[] paramTypes;
            Object[] constParams1 = new Object[] {
                core,
                routeManager,
                stateManager,
                worldLoader };
            Object[] constParams2 = new Object[] {
                core,
                viewpointManager,
                routeManager,
                stateManager,
                worldLoader };
            ScriptEngine scriptEngine1 = null;
            boolean found = false;
            try {
                scriptClass = Class.forName( classname );
                Constructor<?>[] consts = scriptClass.getConstructors();
                for (Constructor<?> const1 : consts) {
                    paramTypes = const1.getParameterTypes();
                    if (paramTypes.length == constParams1.length) {
                        scriptEngine1 = (ScriptEngine) const1.newInstance(constParams1);
                        found = true;
                        break;
                    } else if (paramTypes.length == constParams2.length) {
                        scriptEngine1 = (ScriptEngine) const1.newInstance(constParams2);
                        found = true;
                        break;
                    }
                }
                if ( !found ) {
                    if ( required ) {
                        throw new InvalidConfigurationException( 
                                LOG_NAME +": Missing required ScriptEngine:" + classname );
                    } else {
                        errorReporter.warningReport(
                                LOG_NAME +": Cannot load ScriptEngine: " + classname, null );
                    }
                }
            } catch( ClassNotFoundException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InvalidConfigurationException e ) {
                if ( required ) {
                    throw new InvalidConfigurationException(
                            LOG_NAME +": Missing required ScriptEngine:" + classname );
                } else {
                    errorReporter.warningReport(
                            LOG_NAME +": Cannot load ScriptEngine: " + classname, e );
                }
            }
            return scriptEngine1;
        });
        //errorReporter.messageReport( LOG_NAME +": ScriptEngine loaded: " + classname );
        return( scriptEngine );
    }
}
