/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2008
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
import java.util.Stack;

// Local imports
// None

/**
 * Keeps track of the URL's discovered during parsing of x3d, x3dv and x3db
 * files.
 * <p>
 *
 * @author Ben Yarger
 * @version $Revision: 1.4 $
 */
public class URLQueue
{
	/** Keeps the found root and generated jar paths from parsing X3D URL's. */
	private static Stack<PackmanDataPath> uriQueueStack;

	/** The path to the file.. */
	private static String urlPath;

	/** The path inside the jar file. */
	private static String jarUrlPath;

	/**
	 * Empty constructor. Must initialize once to setup data objects.
	 */
	public URLQueue()
	{

	}

	/**
	 * Initializes data objects. Must be called once before using. If called
	 * more than once, it will reset the data objects.
	 */
	public void initialize()
	{
		uriQueueStack = new Stack<>();
		urlPath = null;
		jarUrlPath = null;
	}

	/**
	 * Initializes data objects. Must be called once before using. If called
	 * more than once, it will reset the data objects.
	 *
	 * @param urlPath Initial value to set the urlPath to. This is the value
	 * that is held as the last path popped off the stack.
	 */
	public void initialize(String urlPath, String jarUrlPath)
	{
		uriQueueStack = new Stack<>();
		this.urlPath = urlPath;
		this.jarUrlPath = jarUrlPath;
	}

	/**
	 * Push a data set onto the stack.
	 *
	 * @param rootPath The url to preserve on the stack.
	 * @param jarPath The path inside the jar file.
	 */
	public void push(String rootPath, String jarPath)
	{
		PackmanDataPath dataPath = new PackmanDataPath(rootPath, jarPath);

		uriQueueStack.push(dataPath);
	}

	/**
	 * Pop the top data path set off the stack.
	 *
	 * @return boolean Returns true if successful or false if the stack is
	 * empty.
	 */
	public boolean pop()
	{
		if(uriQueueStack.empty())
		{
			return false;
		}

		PackmanDataPath dataPath = uriQueueStack.pop();
		urlPath = dataPath.getFilePath();
		jarUrlPath = dataPath.getInternalJarPath();

		return true;
	}

	/**
	 * Gets the last url path popped off the top of the stack.
	 *
	 * @return String The last url popped off the stack.
	 */
	public String getUrlPath()
	{
		return urlPath;
	}

	/**
	 * Gets the last internal jar path popped off the top of the stack.
	 *
	 * @return String The last internal jar path popped off the stack.
	 */
	public String getJarUrlPath()
	{
		return jarUrlPath;
	}

	/**
	 * Gets the status of the stack.
	 *
	 * @return boolean Returns true if empty, otherwise false.
	 */
	public boolean isEmpty()
	{
		return uriQueueStack.empty();
	}
}
