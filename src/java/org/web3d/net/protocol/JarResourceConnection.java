/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.net.protocol;

// Standard imports
import java.io.*;
import java.util.jar.*;
import java.util.HashMap;
import java.util.Map;

import java.security.cert.Certificate;
import java.net.MalformedURLException;

import org.ietf.uri.ResourceConnection;
import org.ietf.uri.URI;
import org.ietf.uri.URL;

// Application specific imports
import org.j3d.util.HashSet;

/**
 * Representation of a JAR resource that performs caching of the JAR file
 * on the client machine to prevent refetch issues.
 *  <p>
 *
 * Presents a standardised interface to JAR files, regardless of their
 * location. The methods provided here model those in the
 * <code>java.net</code> version with one difference. The
 * <code>getJarFile</code> method is private rather than public. If you need
 * access to the entire JAR file, then that should be by making the appropriate
 * URL with no entry name and accessing through the <code>getContent</code>
 * method. For this, the content type shall be <code>x-java/jar</code>
 *  <p>
 *
 * This is cached by writing the file to disk.
 *
 * @author    Justin Couch
 * @version $Revision: 1.5 $
 */
class JarResourceConnection extends ResourceConnection {

    /** Buffer size for when we download bytes to disk */
    private static final int BUFFER_SIZE = 2048;

    /**
     * The content type that is returned to represent a JAR file -
     * <code>x-java/jar</code>
     */
    public static final String JAR_CONTENT_TYPE = "x-java/jar";

    /** The file extension normally used on JAR files.: <code>.jar</code> */
    public static final String JAR_FILE_EXTENSION = "jar";

    /** The input stream to the file. Not created until requested */
    private InputStream inputStream = null;

    /** The content type of the file */
    private String contentType = null;

    /** The URI to the JAR file itself */
    private URI jarFileURI;

    /**
     * The name of the entry to fetch. If the connection is only to a JAR file
     * with no entry attributes, this will remain null
     */
    private String jarEntryName;

    /** Hash of the JAR location, which we use to load the original file. */
    private String jarLocationHash;

    /** The file that we are making the queries on */
    private JarFile jarFile;

    /**
     * The entry that we fetched. If there was no entry specified in the URL
     * then this will be null.
     */
    private JarEntry jarEntry;

    /** A mutex map to stop multiple threads from downloading the same jar */
    private static Map<String, Object> loadMutexMap;

    /** The files which are currently loading */
    private static HashSet<String> loadingSet;

    /**
     * Static initializer to make sure that we have the jar content type
     * registered for when we need it.
     */
    static {
        addContentTypeToDefaultMap(JAR_CONTENT_TYPE, JAR_FILE_EXTENSION);
        loadMutexMap = new HashMap<>();
        loadingSet = new HashSet<>();
    }

    /**
     * Create an instance of this connection to the jar file at the nominated
     * location. This location itself may be another URL. If there is no JAR
     * entry to fetch then the name should be <code>null</code>.
     *
     * @param jarLocation The URI to establish the connection to
     * @param jarEntry The name of the entry to fetch
     * @exception MalformedURLException We stuffed up something in the filename
     */
    JarResourceConnection(URI jarLocation, String jarEntryPath)
        throws MalformedURLException {

        super(new URL("jar:" + jarLocation.toExternalForm() + "!/" + jarEntryPath));

        jarFileURI = jarLocation;
        jarLocationHash = Integer.toString(jarLocation.hashCode());

        // protection against zero length strings causing weirdness
        if((jarEntryPath != null) && (jarEntryPath.length() != 0))
            jarEntryName = jarEntryPath;
    }

    /**
     * Get the input stream for this. Throws an UnknownServiceExeception if
     * there is no stream available.
     *
     * @return The stream
     */
    @Override
    public InputStream getInputStream()
        throws IOException {

        if(!connected)
            connect();

        if(inputStream == null) {
            if(jarFile == null)
                downloadFile();

            // if we have an entry, get the stream to that, otherwise, just
            // return the stream that we've got to the JAR file
            if(jarEntryName != null) {
                if(jarEntry == null)
                    getJarEntry();

                inputStream = jarFile.getInputStream(jarEntry);
            } else {
                inputStream = new FileInputStream(jarFile.getName());
            }
        }

        return inputStream;
    }

    /**
     * Get the content type of the resource that this stream points to.
     * Returns a standard MIME type string. If the content type is not known then
     * <code>unknown/unknown</code> is returned (the default implementation).
     *
     * @return The content type of this resource
     */
    @Override
    public String getContentType() {

        if(contentType == null) {
            if(jarEntryName != null)
                contentType = findContentType(jarEntryName);
            else
                contentType = JAR_CONTENT_TYPE;
        }

        return contentType;
    }

    @Override
    public void connect()
        throws IOException {

        if(connected)
            return;

        downloadFile();

        connected = true;
    }

    /**
     * Return the list of attributes for the JAR entry. If the resource points
     * to the JAR file only, then this method returns <code>null</code>.
     *
     * @return The list of attributes
     * @exception IOException There was an error reading the file
     */
    public Attributes getAttributes()
        throws IOException {

        if(jarEntryName == null)
            return null;

        if(jarEntry == null)
            getJarEntry();

        Attributes ret_vals = null;

        if(jarEntry != null)
            ret_vals = jarEntry.getAttributes();

        return ret_vals;
    }

    /**
     * Get the certificates that describe this entry in the JAR file. If the
     * resource points to a Jar file, then this method returns <code>null</code>.
     *
     * @return The list of certificates
     * @exception IOException There was an error reading the file
     */
    public Certificate[] getCertificates()
        throws IOException {

        if(jarEntryName == null)
            return null;

        if(jarEntry == null)
            getJarEntry();

        Certificate[] ret_vals = null;

        if(jarEntry != null)
            ret_vals = jarEntry.getCertificates();

        return ret_vals;
    }

    /**
     * Get the name of the entry that is being fetched from this connection. If
     * the connection points to the file as a whole then this returns
     * <code>null</code>
     *
     * @return The name of the entry, if any
     */
    public String getEntryName() {
        return jarEntryName;
    }

    /**
     * Fetch the JAR entry as nominated by the resource. If the resource does not
     * name an entry, then it returns <code>null</code>
     *
     * @return A reference to the JAREntry or </code>null</code>
     * @exception IOException There was an error reading the file
     */
    public JarEntry getJarEntry()
        throws IOException {

        if(jarEntryName == null)
            return null;

        if(jarEntry == null) {
            if(jarFile == null)
                downloadFile();

            jarEntry = jarFile.getJarEntry(jarEntryName);

            if(jarEntry == null)
                throw new FileNotFoundException(jarEntryName +
                    " entry not found in the JAR file");
        }

        return jarEntry;
    }

    /**
     * Get the URL to the actual JAR file. Note that <code>jar:</code> URLs are
     * not supposed to have anything except a URL as the location, we have
     * allowed the code to use URNs as well.
     */
    public URI getJarFileURI() {
        return jarFileURI;
    }

    /**
     * Get the main attributes of the Jar File. These are the attributes that
     * are contained in the manifest entry.
     *
     * @return a reference to the attributes of the manifest
     * @exception IOException There was an error reading the file
     */
    public Attributes getMainAttributes()
        throws IOException {

        Manifest man = getManifest();

        Attributes ret_val = null;

        if(man != null)    // always possible if the JAR file doesn't have one
            ret_val = man.getMainAttributes();

        return ret_val;
    }

    /**
     * Get the manifest from the JAR file.
     *
     * @return A reference to the manifest.
     * @exception IOException There was an error reading the file
     */
    public Manifest getManifest()
        throws IOException {

        if(jarFile == null)
            downloadFile();

        return jarFile.getManifest();
    }

    /**
     * Take the location URI and download it to local disk in the temp
     * directory. Current implementation is inefficient for local JAR files
     * because it will still copy it to the new location.
     */
    private void downloadFile()
        throws IOException {

        String tmp_dir = System.getProperty("java.io.tmpdir");

        File output_file = new File(tmp_dir, jarLocationHash + ".jar");
        output_file.deleteOnExit();

        Object mutex = loadMutexMap.get(jarLocationHash);

        if(mutex == null) {
            mutex = new Object();

            loadMutexMap.put(jarLocationHash, mutex);
        }

        synchronized(mutex) {
            if(loadingSet.contains(jarLocationHash)) {

                try {
                    mutex.wait();
                } catch(InterruptedException ie) {
                }
            }

            // does this file already exist? If so, no need to download.

            // Not existing. Download now from end point
            ResourceConnection resc = jarFileURI.getResource();

            if(output_file.exists()) {
                if (output_file.lastModified() > resc.getLastModified()) {
                    if(jarFile == null)
                        jarFile = new JarFile(output_file);

                    return;
                }
            }

            loadingSet.add(jarLocationHash);

            try {
                resc.connect();
                InputStream is = resc.getInputStream();
                BufferedOutputStream bos;
                try (BufferedInputStream bis = (is instanceof BufferedInputStream) ?
                        (BufferedInputStream)is :
                        new BufferedInputStream(is)) {
                    FileOutputStream fos = new FileOutputStream(output_file);
                    bos = new BufferedOutputStream(fos);
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytes_read;
                    int total = 0;
                    while((bytes_read = bis.read(buffer, 0, BUFFER_SIZE)) != -1)
                        bos.write(buffer, 0, bytes_read);
                }
                bos.close();

                jarFile = new JarFile(output_file);
            } finally {
                loadingSet.remove(jarLocationHash);
                loadMutexMap.remove(jarLocationHash);
                mutex.notifyAll();
            }
        }
    }
}
