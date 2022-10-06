/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.impl.core.loading;

// External imports
import java.io.*;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.ietf.uri.*;
import org.ietf.uri.URIUtils;

// Local imports
import org.j3d.util.ErrorReporter;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.nodes.*;

import org.xj3d.core.loading.CacheDetails;
import org.xj3d.core.loading.FileCache;
import org.xj3d.core.loading.LoadDetails;
import org.xj3d.core.loading.LoadRequestHandler;

/**
 * A loader thread for a single piece of content at any given time.
 * <p>
 *
 * The content loader is used to wait on a queue of available content and
 * load the next available item in the queue.
 * <p>
 *
 * When loading, the content loader loads the complete file, it ignores any
 * reference part of the URI. This allows for better caching.
 *
 * The loader is used to
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.7 $
 */
class ContentLoadHandler extends BaseLoadHandler
    implements LoadRequestHandler {

    /** Message for an unrecognized message */
    private static final String UNKNOWN_ERROR_MSG =
        "Unknown error in content loading process";

    /** Message for errors in setContent() */
    private static final String CONTENT_ERROR_MSG =
        "Error setting external content:";

    /** Message for errors in setContent() */
    private static final String CONTENT_WARNING_MSG =
        "Problem setting external content:";

    /** Message for errors attempting to write to an invalid field index */
    private static final String INVALID_FIELD_MSG =
        "Internal error caused by attempting to send content to an invalid " +
        "field index: ";

    /** Message for no valid URLS in the load process */
    private static final String NO_URLS_MSG =
        "Cannot resolve any URLS for URL: ";

    /** The cache representation that this loader is using */
    private FileCache fileCache;

    /** A map for determining whether content is an inline */
    private Set<String> inlineSet;

    /**
     * Create a content loader that reads values from the given queue and
     * stores intermediate results in the given map.
     *
     * @param cache The file cache implementation to use for this handler
     */
    ContentLoadHandler(FileCache cache) {
        fileCache = cache;

        inlineSet = new HashSet<>(6);
        inlineSet.add("model/vrml");
        inlineSet.add("x-world/x-vrml");
        inlineSet.add("application/xml");
        inlineSet.add("model/x3d+xml");
        inlineSet.add("model/x3d+vrml");
        inlineSet.add("model/x3d+binary");
    }

    //----------------------------------------------------------
    // Methods defined by LoadRequestHandler
    //----------------------------------------------------------

    /**
     * Process this load request now.
     *
     * @param reporter The errorReporter to send all messages to
     * @param url The list of URLs to load
     * @param loadList The list of LoadDetails objects to sent the fulfilled
     *    requests to
     */
    @Override
    public void processLoadRequest(ErrorReporter reporter,
                                   String[] url,
                                   Vector<LoadDetails> loadList) {

        boolean content_found;
        Object content;
        ContentLoadDetails details;
        String mime_type;

        VRMLSingleExternalNodeType single_node;
        VRMLMultiExternalNodeType multi_node;

        // run through all the details and make sure that we have at
        // least one that needs to be loaded.
        boolean load_needed = false;

        for (LoadDetails loadList1 : loadList) {
            details = (ContentLoadDetails) loadList1;
            int state;
            if(details.fieldIndex == -1) {
                single_node = (VRMLSingleExternalNodeType)details.node;
                state = single_node.getLoadState();
            } else {
                multi_node = (VRMLMultiExternalNodeType)details.node;
                state = multi_node.getLoadState(details.fieldIndex);
            }
            load_needed = (state != VRMLExternalNodeType.LOAD_COMPLETE);
        }

        if(!load_needed) {
            return;
        }

        content_found = false;

        // Ignore this as we have not yet registered that we are
        // actually processing anything.
        terminateCurrent = false;

        for(int i = 0; i < loadList.size() && !load_needed; i++) {
            details = (ContentLoadDetails) loadList.get(i);
            if(details.fieldIndex == -1) {
                single_node = (VRMLSingleExternalNodeType)details.node;
                single_node.setLoadState(VRMLExternalNodeType.LOADING);
            } else {
                multi_node = (VRMLMultiExternalNodeType)details.node;
                multi_node.setLoadState(details.fieldIndex,
                                        VRMLExternalNodeType.LOADING);
            }
        }

        int num_urls = (url == null) ? 0 : url.length;

        for(int i = 0; !content_found && (i < num_urls); i++) {

            // check the string for a # and remove the reference
            String file_url = url[i];
            int index;
            if((index = file_url.lastIndexOf('#')) != -1) {
                file_url = file_url.substring(0, index);
            }

            // Check the cache first to see if we have something here
            CacheDetails cached_version =
                fileCache.checkForFile(file_url);

            if(cached_version != null) {
                mime_type = cached_version.getContentType();
                content = cached_version.getContent();

                content_found = true;

                for (LoadDetails loadList1 : loadList) {
                    try {
                        details = (ContentLoadDetails) loadList1;
                        if(details.fieldIndex == -1) {
                            single_node = (VRMLSingleExternalNodeType)details.node;

                            if(!single_node.checkValidContentType(mime_type))
                                continue;

                            single_node.setLoadedURI(url[i]);
                            single_node.setContent(mime_type, content);
                            single_node.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);
                        } else {
                            multi_node = (VRMLMultiExternalNodeType)details.node;

                            if(!multi_node.checkValidContentType(details.fieldIndex,
                                    mime_type))
                                continue;

                            multi_node.setLoadedURI(details.fieldIndex, url[i]);
                            multi_node.setContent(details.fieldIndex,
                                    mime_type,
                                    content);
                            multi_node.setLoadState(details.fieldIndex,
                                    VRMLExternalNodeType.LOAD_COMPLETE);
                        }
                    } catch (IllegalArgumentException | InvalidFieldException e) {

                        String errorMsg;

                        if (e instanceof IllegalArgumentException) {

                            // from the setContent method
                            errorMsg = CONTENT_ERROR_MSG;
                        } else if (e instanceof InvalidFieldException) {
                            errorMsg = INVALID_FIELD_MSG;
                        } else {
                            errorMsg = UNKNOWN_ERROR_MSG;
                        }
                        reporter.errorReport(errorMsg, e);
                    }
                }
            } else {
                content_found = loadExternal(reporter,
                        url[i],
                        file_url,
                        loadList);
            }
        }

        if(!content_found) {
            for (LoadDetails loadList1 : loadList) {
                details = (ContentLoadDetails) loadList1;
                if(details.fieldIndex == -1) {
                    single_node = (VRMLSingleExternalNodeType)details.node;
                    single_node.setLoadState(VRMLExternalNodeType.LOAD_FAILED);
                } else {
                    multi_node = (VRMLMultiExternalNodeType)details.node;
                    multi_node.setLoadState(details.fieldIndex,
                            VRMLExternalNodeType.LOAD_FAILED);
                }
            }

            if(url != null && url.length > 0)
                reporter.warningReport(NO_URLS_MSG + url[0], null);
        }

        // Cleanup so we don't hold any references longer than we need to
        currentConnection = null;
    }

    /**
     * Load the file from an external URL because we couldn't find it in
     * the cache.
     *
     * @param reporter The errorReporter to send all messages to
     */
    private boolean loadExternal(ErrorReporter reporter,
                                 String origUri,
                                 String fileUri,
                                 Vector<LoadDetails> loadList) {

//System.out.println("loadExternal: " + fileUri);

        VRMLSingleExternalNodeType single_node;
        VRMLMultiExternalNodeType multi_node;
        boolean content_found = false;

        // Always assume a fully qualified URI
        URL[] source_urls;
        String mime_type;
        Object content;

        try {
            URI uri = URIUtils.createURI(fileUri);

            if(terminateCurrent)
                return false;

            source_urls = uri.getURLList();
        } catch (IOException | ArrayIndexOutOfBoundsException ex) {
            reporter.errorReport("Failed to parse url: " + fileUri, ex);
            return false;
        }

        if(terminateCurrent || (source_urls == null)) {
            return false;
        }

        int latestCheckedIndex = -1;

        // loop through the list of candidate URLs and look for
        // something that matches. If it does, set it in the node
        // for use.
        for(URL url : source_urls)
        {
            latestCheckedIndex++; // save and report if found
            try {
                currentConnection = url.getResource();
            } catch (IOException ioe) {
                reporter.errorReport("Failed to parse url: " + url, ioe);
                continue;
            }

            if(terminateCurrent)
                break;

            try {
                if(!makeConnection(reporter)) {
                    if(terminateCurrent)
                        break;
                    else
                        continue;
                }

                if(terminateCurrent) {
                    currentConnection.close();
                    break;
                }

                mime_type = currentConnection.getContentType();
                if(terminateCurrent) {
                    currentConnection.close();
                    break;
                }

                if(mime_type == null) {
                    currentConnection.close();
                    continue;
                }

                if (inlineSet.contains(mime_type)) {
                    content_found = loadInline(currentConnection, mime_type, url, reporter, origUri, fileUri, loadList);

                    if (content_found)
                        break;
                    else
                        continue;
                }

                content = currentConnection.getContent();
                currentConnection.close();

                if(content == null)
                    continue;

                boolean match_found = false;

                for (LoadDetails loadList1 : loadList) {
                    ContentLoadDetails details = (ContentLoadDetails) loadList1;
                    if(details.fieldIndex == -1) {
                        single_node = (VRMLSingleExternalNodeType)details.node;

                        if(!single_node.checkValidContentType(mime_type))
                            continue;

                        match_found = true;
                        single_node.setLoadedURI(origUri);

                        single_node.setContent(mime_type, content);
                        single_node.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);
                    } else {
                        multi_node = (VRMLMultiExternalNodeType)details.node;

                        if(!multi_node.checkValidContentType(details.fieldIndex, mime_type))
                            continue;

                        match_found = true;
                        multi_node.setLoadedURI(details.fieldIndex, origUri);
                        multi_node.setContent(details.fieldIndex, mime_type, content);
                        multi_node.setLoadState(details.fieldIndex,
                                VRMLExternalNodeType.LOAD_COMPLETE);
                    }
                }

                if(!match_found)
                    continue;

                // TODO
                // Disable caching of a loaded scene. Possibly not the
                // best thing - particularly for proto libraries, but it
                // is the best thing for now until we can work out the
                // issues with scene caching.

                if(!(content instanceof BasicScene)) {

                    // Images are already cached as Texture Objects
                    if (mime_type.indexOf("image") != 0)
                        fileCache.cacheFile(fileUri, mime_type, content);
                }
                // Yippee! made it. Break out of the loop and exit
                // the load process.
                content_found = true;
                break;

            } catch (IOException | IllegalArgumentException | InvalidFieldException e) {

                String errorMsg;

                // ignore and move on
                if (e instanceof IOException) {

                    errorMsg = e.getMessage();

                    if (terminateCurrent)
                        break;
                } else if (e instanceof IllegalArgumentException)
                    errorMsg = CONTENT_ERROR_MSG;
                else if (e instanceof InvalidFieldException)
                    errorMsg = INVALID_FIELD_MSG;
                else
                    errorMsg = UNKNOWN_ERROR_MSG;

                if (errorMsg != null)
                    reporter.errorReport(errorMsg, e);
            }

        } // for loop

        if (content_found && (latestCheckedIndex > -1))
        {
            // Corresponding "File not found:: message in BaseLoadHandler.makeConnection()
            String msg = "Success: file found, " + source_urls[latestCheckedIndex].toExternalForm() + "\n";
            reporter.messageReport(msg);
        }

        return content_found;
    }

    /**
     * Load the file from an external URL because we couldn't find it in
     * the cache.
     *
     * @param reporter The errorReporter to send all messages to
     */
    private boolean loadInline(ResourceConnection currentConnection,
                               String mimeType,
                               URL url,
                               ErrorReporter reporter,
                               String origUri,
                               String fileUri,
                               Vector<LoadDetails> loadList) {

        VRMLSingleExternalNodeType single_node;
        VRMLMultiExternalNodeType multi_node;
        Object content;
        boolean match_found = false;

        // Inlines that have been notified
        HashSet<VRMLExternalNodeType> alreadyNotified = new HashSet<>();


        try {
            content = currentConnection.getContent();
            currentConnection.close();

            if(content == null)
                return false;

            ContentLoadDetails details = (ContentLoadDetails)loadList.get(0);
			// TODO confirm whether Inline or PROTO should be "single_node"
            if(details.fieldIndex == -1) {
                single_node = (VRMLSingleExternalNodeType)details.node;

                if(single_node.checkValidContentType(mimeType)) {

                    match_found = true;
                    single_node.setLoadedURI(origUri);

                    alreadyNotified.add(single_node);
                    single_node.setContent(mimeType, content);
                    single_node.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);
                }
            } else {
                multi_node = (VRMLMultiExternalNodeType)details.node;

                if(multi_node.checkValidContentType(details.fieldIndex, mimeType)) {

                    match_found = true;
                    multi_node.setLoadedURI(details.fieldIndex, origUri);
                    alreadyNotified.add(multi_node);
                    multi_node.setContent(details.fieldIndex, mimeType, content);
                    multi_node.setLoadState(details.fieldIndex,
                                            VRMLExternalNodeType.LOAD_COMPLETE);
                }
            }

         } catch (IOException | IllegalArgumentException | InvalidFieldException e) {

            String errorMsg;

            if (e instanceof IOException) {
                // ignore and move on
                errorMsg = e.getMessage();
            } else if (e instanceof IllegalArgumentException)

                // from the setContent method
                errorMsg = CONTENT_ERROR_MSG;
            else if (e instanceof InvalidFieldException)
                errorMsg = INVALID_FIELD_MSG;
            else
                errorMsg = UNKNOWN_ERROR_MSG;

            if (errorMsg != null)
                reporter.errorReport(errorMsg, e);
        }

        // Create new geometry for each inline.  Reusing doesn't work right now
        String mime_type;

        for(int j = 1; j < loadList.size(); j++) {
            ContentLoadDetails details = (ContentLoadDetails)loadList.get(j);

            if (alreadyNotified.contains(details.node)) {
                continue;
            }

            try {
                currentConnection = url.getResource();
            } catch (IOException ioe) {
                reporter.errorReport("Failed to parse url: " + url, ioe);
                return match_found;
            }

            if(terminateCurrent)
                break;

            try {
                if(!makeConnection(reporter)) {
                    if(terminateCurrent)
                        break;
                    else
                        return match_found;
                }

                if(terminateCurrent) {
                    currentConnection.close();
                    break;
                }

                mime_type = currentConnection.getContentType();
                if(terminateCurrent) {
                    currentConnection.close();
                    break;
                }

                if(mime_type == null) {
                    currentConnection.close();
                    return match_found;
                }

                content = currentConnection.getContent();
                currentConnection.close();

                if(content == null)
                    return match_found;


                if(details.fieldIndex == -1) {
                    single_node = (VRMLSingleExternalNodeType)details.node;

                    if(single_node.checkValidContentType(mimeType)) {

                        match_found = true;
                        single_node.setLoadedURI(origUri);

                        alreadyNotified.add(single_node);
                        single_node.setContent(mimeType, content);
                        single_node.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);
                    }
                } else {
                    multi_node = (VRMLMultiExternalNodeType)details.node;

                    if(multi_node.checkValidContentType(details.fieldIndex, mimeType)) {

                        match_found = true;
                        multi_node.setLoadedURI(details.fieldIndex, origUri);
                        alreadyNotified.add(multi_node);
                        multi_node.setContent(details.fieldIndex, mimeType, content);
                        multi_node.setLoadState(details.fieldIndex,
                                                VRMLExternalNodeType.LOAD_COMPLETE);
                    }
                }
             } catch (IOException | IllegalArgumentException | InvalidFieldException e) {

                String errorMsg;

                if (e instanceof IOException) {
                    // ignore and move on
                    errorMsg = e.getMessage();
                } else if (e instanceof IllegalArgumentException) {

                    // from the setContent method
                    errorMsg = CONTENT_ERROR_MSG;
                } else if (e instanceof InvalidFieldException) {
                    errorMsg = INVALID_FIELD_MSG;
                } else {
                    errorMsg = UNKNOWN_ERROR_MSG;
                }

                if (errorMsg != null) {
                    reporter.errorReport(errorMsg, e);
                }
            }
        }

        return match_found;
    }
}
