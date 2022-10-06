/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.packman;

// External imports
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.parser.DefaultFieldParserFactory;

/**
 * Performs URL restructuring for generation of jar file. The filter converts
 * URL paths to a specific jar structure for the jar created. The input file
 * specified during execution will be in the root of the jar and all subsequent
 * files will be in /extras/.
 * <p>
 *
 * @author Ben Yarger
 * @version $Revision: 1.7 $
 */
public class URLFilter extends AbstractFilter
{
	/** Keeps track of url field encounters. True when in url field. */
	private boolean urlFound;

	/** Url type lookup */
	private Map<String, String> urlTypeLookup;

	/** Shared access with urlQueue. */
	private URLQueue urlQueue;

	/** The path to the root file of the scene. */
	private static String rootPath;

	    /** The logging identifier of this app */
    private static final String LOG_NAME = "URLFilter";

    /**
     * Create an instance of the filter.
     */
    public URLFilter()
    {
		urlFound = false;

		urlTypeLookup = new HashMap();
		urlTypeLookup.put("url", "url");
		urlTypeLookup.put("leftUrl", "leftUrl");
		urlTypeLookup.put("rightUrl", "rightUrl");
		urlTypeLookup.put("frontUrl", "frontUrl");
		urlTypeLookup.put("backUrl", "backUrl");
		urlTypeLookup.put("topUrl", "topUrl");
		urlTypeLookup.put("bottomUrl", "bottomUrl");

		urlQueue = new URLQueue();
    }

	/**
	 * Notification of a field declaration. This notification is only called
	 * if it is a standard node. If the node is a script or PROTO declaration
	 * then the {@link ScriptHandler} or {@link ProtoHandler} methods are
	 * used.
	 *
	 * @param name The name of the field declared
	 * @throws SAVException This call is taken at the wrong time in the
	 *   structure of the document
	 * @throws VRMLException The content provided is invalid for this
	 *   part of the document or can't be parsed
	 */
	public void startField(String name) throws SAVException, VRMLException
	{
		if(urlTypeLookup.containsKey(name))
		{
			urlFound = true;
		}

		if(contentHandler != null)
			contentHandler.startField(name);
	}


	/**
	 * Notification of the end of a field declaration. This is called only at
	 * the end of an MFNode declaration. All other fields are terminated by
	 * either {@link #useDecl(String)} or {@link #fieldValue(String)}.
	 *
	 * @throws SAVException This call is taken at the wrong time in the
	 *   structure of the document
	 * @throws VRMLException The content provided is invalid for this
	 *   part of the document or can't be parsed
	 */
	public void endField() throws SAVException, VRMLException
	{
		if(urlFound)
		{
			urlFound = false;
		}

		if(contentHandler != null)
			contentHandler.endField();
    }

	//---------------------------------------------------------------
	// Local methods
	//---------------------------------------------------------------

	/**
	 * Takes a url and if it is a valid url spits it right back. If it is a
	 * relative path then process it to create the jar path and src path
	 * for reading from and add both to the URLQueue. Finally, return the
	 * modified url path.
	 *
	 * @return String The modified url path.
	 */
	public String processURL(String urlValue)
	{
		String convertedUrlName = urlValue;

		URL url = null;

		convertedUrlName = convertedUrlName.replace("\"", "");
		convertedUrlName = convertedUrlName.replace("'", "");

		try
		{
			url = new URL(convertedUrlName);
		}
		catch(MalformedURLException mue)
		{
//			System.out.println("Relative path found. Processing as such. " + urlName);
		}

		if(url == null || url.getProtocol() == null)
		{
			String urlName = convertedUrlName;

			int convertType = Packman.getConversionType();

			if(convertType != Packman.CONVERT_TYPE_NONE)
			{
				int extensionIndex = convertedUrlName.lastIndexOf(".");

				if(extensionIndex != -1)
				{
					String extension = convertedUrlName.substring(extensionIndex+1);

					if(extension.equals("x3d") || extension.equals("x3dv") || extension.equals("x3db"))
					{
						if(convertType == Packman.CONVERT_TYPE_X3D)
						{
							convertedUrlName = convertedUrlName.substring(0, extensionIndex) + ".x3d";
						}
						else if(convertType == Packman.CONVERT_TYPE_X3DV)
						{
							convertedUrlName = convertedUrlName.substring(0, extensionIndex) + ".x3dv";
						}
						else if(convertType == Packman.CONVERT_TYPE_X3DB)
						{
							convertedUrlName = convertedUrlName.substring(0, extensionIndex) + ".x3db";
						}
						else
						{
							// Should be an error!
							System.out.println("Unable to determine conversion type");
						}
					}
				}
				else
				{
					// Should be an error!
					System.out.println("Unable to determine extension.");
				}
			}

			File urlToFile = new File(urlName);
			File urlToParentPath = new File(urlQueue.getUrlPath());

			urlName = urlToFile.getPath();
			String urlParentPath = urlToParentPath.getPath();
			String urlPath = urlParentPath.replace(rootPath, "");
			int adjustmentIndex = urlPath.lastIndexOf(File.separator);
			if(adjustmentIndex != -1)
				urlPath = urlPath.substring(0, adjustmentIndex);
			adjustmentIndex = urlPath.indexOf(File.separator);
			if(adjustmentIndex != -1)
				urlPath = urlPath.substring(adjustmentIndex+1);

			int slashIndex = urlParentPath.lastIndexOf(File.separator);
			urlParentPath = urlParentPath.substring(0, slashIndex);

			int upDirIndex = 0;

			while(upDirIndex != -1)
			{
				upDirIndex = urlName.indexOf("..");

				if(upDirIndex != -1)
				{
					slashIndex = urlParentPath.lastIndexOf(File.separator);
					urlParentPath = urlParentPath.substring(0, slashIndex);
					slashIndex = urlPath.indexOf(File.separator);
					urlPath = urlPath.substring(0, slashIndex);

					urlName = urlName.substring(upDirIndex+1);
				}
			}

			if(urlPath.equals("") || urlPath.equals(" "))
			{
				urlQueue.push((urlParentPath+File.separator+urlName), convertedUrlName);
			}
			else
			{
				urlQueue.push((urlParentPath+File.separator+urlName), (urlPath+File.separator+convertedUrlName));
			}
		}

		return convertedUrlName;
	}


	//---------------------------------------------------------------
	// Methods defined by StringContentHandler
    //---------------------------------------------------------------


    /**
     * The value of a normal field. This is a string that represents the entire
     * value of the field. MFStrings will have to be parsed. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     * <p>
     * If this field is an SFNode with a USE declaration you will have the
     * {@link #useDecl(String)} method called rather than this method.
     *
     * @param value The value of this field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void fieldValue(String value) throws SAVException, VRMLException
    {
		if(urlFound)
		{
			int index  = 0;
			String urlValue = value;
			String updatedURL = "";

			do
			{
				index = urlValue.indexOf(" ");

				if (index == -1)
				{
					updatedURL += "\""+processURL(urlValue)+"\"";
				}
				else
				{
					String urlName = urlValue.substring(0, index);
					updatedURL += "\""+processURL(urlName) + "\" ";
					urlValue = urlValue.substring(index+1);
				}
			}while(index != -1);

			urlFound = false;

			updatedURL = updatedURL.replace("\\", "/");

			if(contentHandler instanceof StringContentHandler)
			{
				contentHandler.fieldValue(updatedURL);
			}
		}
		else
		{
			if(contentHandler instanceof StringContentHandler)
			{
				contentHandler.fieldValue(value);
			}
		}
    }




    /**
	 * Set the argument parameters to control the filter operation
	 *
	 * @param arg The array of argument parameters.
	 */
	public void setArguments(String[] arg)
	{
		int argIndex = -1;
		rootPath = "";

		for (int i = 0; i < arg.length; i++)
		{
			String argument = arg[i];
			if (argument.startsWith("-"))
			{
				try
				{
					if (argument.equals("-rootpath"))
					{
						rootPath = arg[i+1];
						i = i+1;
					}
				}
				catch (Exception e)
				{
					throw new IllegalArgumentException(
						LOG_NAME + ": Error parsing filter arguments");
				}
			}
		}

		URL url = null;

		try
		{
			url = new URL(rootPath);
			rootPath = "";
		}
		catch(MalformedURLException mue)
		{
			File urlRootPath = new File(rootPath);
			rootPath = urlRootPath.getParent();
		}
    }
}
