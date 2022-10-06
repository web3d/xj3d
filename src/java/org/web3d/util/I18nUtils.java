package org.web3d.util;

import org.j3d.util.I18nManager;

import java.text.Format;
import java.text.MessageFormat;
import java.util.Locale;

/**
 * Internationalization utilities
 *
 * @author Alan Hudson
 */
public class I18nUtils {

    public static final String EXT_MSG = "EXTMSG:";
    public static final String CRIT_MSG = "CRITMSG:";

    /**
     * Set the manager to use the given application's set of localization
     * settings.
     *
     * @param appName A name string describing the end user application
     * @param resourceFiles If not null, use this as the resource bundle
     *    base name to be fetched, rather than the default file name
     */
    public static void setApplication(String appName, String[] resourceFiles) {
        I18nManagerMultiResource intl_mgr = I18nManagerMultiResource.getManager();
        intl_mgr.setApplication(appName, resourceFiles);
    }

    /**
     * Print the message.
     *
     * @param msgName The name of the message
     * @param msgMarker The marker or label to add to the beginning of the message
     * @param msgArgs Additional arguments to the message
     */
    public static void printMsg(String msgName, String msgMarker, String[] msgArgs) {
        I18nManagerMultiResource intl_multi_mgr = I18nManagerMultiResource.getManager();
        String msg;
        Locale locale;

        try {
            msg = intl_multi_mgr.getString(msgName);
            locale = intl_multi_mgr.getFoundLocale();
        } catch (Exception e) {
            System.err.println("Resource for I18nManagerMultiResource not set. Falling back to I18nManager.");

            I18nManager intl_mgr = I18nManager.getManager();
            msg = intl_mgr.getString(msgName);
            locale = intl_mgr.getFoundLocale();
        }


        if (msgArgs != null && msgArgs.length > 0) {
            Format[] fmts = { null };
            MessageFormat msg_fmt = new MessageFormat(msg, locale);
            msg_fmt.setFormats(fmts);
            msg = msg_fmt.format(msgArgs);
        }

        System.err.println(msgMarker + msg);
    }

    /**
     * Get the message corresponding to the message name.
     *
     * @param msgName The name of the message
     * @param msgArgs Additional arguments to the message
     * @return The message
     */
    public static String getMsg(String msgName, String[] msgArgs) {
    	I18nManagerMultiResource intl_mgr = I18nManagerMultiResource.getManager();
    	String msg = intl_mgr.getString(msgName);

        if (msgArgs != null && msgArgs.length > 0) {
            Format[] fmts = { null };
            MessageFormat msg_fmt = new MessageFormat(msg, intl_mgr.getFoundLocale());
            msg_fmt.setFormats(fmts);
            msg = msg_fmt.format(msgArgs);
        }

        return msg;
    }
}
