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
// None

// Internal imports
// None

/**
 * Holds the path to the file to add to the jar and the path inside the jar
 * to add the file to.
 * <p>
 *
 * @author Ben Yarger
 * @version $Revision: 1.2 $
 */
public class PackmanDataPath
{
	/** The path inside the jar. */
	private String internalJarPath;

	/** The path of the file. */
	private String filePath;

	/**
	 * Creates a file path set.
	 *
	 * @param filePath The path to the file.
	 * @param internalJarPath The path to put the file to inside the jar.
	 */
	public PackmanDataPath(String filePath, String internalJarPath)
	{
		this.filePath = filePath;
		this.internalJarPath = internalJarPath;
	}

	/**
	 * Returns the file path.
	 *
	 * @return String File path.
	 */
	public String getFilePath()
	{
		return filePath;
	}

	/**
	 * Returns the path inside the jar.
	 *
	 * @return String Internal jar path.
	 */
	public String getInternalJarPath()
	{
		return internalJarPath;
	}
}
