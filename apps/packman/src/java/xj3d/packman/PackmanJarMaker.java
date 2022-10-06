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
import java.util.jar.*;
import java.io.*;

// Local imports
// None

/**
 * Generates a jar file.
 *
 * @author Ben Yarger
 * @version $Revision: 1.5 $
 */
public class PackmanJarMaker
{
	/** Keeps the reference of the jar file to be written to. */
	private static File jarFile;

	/** Keeps the output stream to the jar file. */
	private static FileOutputStream fos;

	/** Keeps the jar output stream to the jar file. */
	private static JarOutputStream jos;

	/** Keeps the path to the jar file. */
	private static String jarPath;

	/**
	 * Handles the generation of jar files.
	 */
	public PackmanJarMaker()
	{

	}

	/**
	 * Open the jar stream for output.
	 *
	 * @param jarPath The fully qualified path and file name of the jar to
	 * create.
	 */
	public boolean startJar(String jarPath)
	{
		try
		{
			Manifest manifest = new Manifest();
			Attributes attributes = manifest.getMainAttributes();
  			attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");

			jarFile = new File(jarPath);
			fos = new FileOutputStream(jarFile);
			jos = new JarOutputStream(fos, manifest);

			this.jarPath = jarPath;
		}
		catch(IOException e)
		{
			e.printStackTrace(System.err);
			return false;
		}

		return true;
	}

	/**
	 * Adds the specified file to the jar.
	 *
	 * @param fileName The fully qualified path and name of the file to add.
	 * @param jarLocation The directory path to place the file inside the jar.
	 */
	public boolean addFileToJar(String fileName, String jarLocation)
	{
		byte[] buffer = new byte[1_024];
		int bytesRead;

		jarLocation = jarLocation.replace("\\", "/");

		try
		{
			File fileToAdd = new File(fileName);

			FileInputStream fileInStream = new FileInputStream(fileToAdd);

			try
			{
				JarEntry newEntry;

				newEntry = new JarEntry(jarLocation);

				jos.putNextEntry(newEntry);

				while ((bytesRead = fileInStream.read(buffer)) != -1)
				{
					jos.write(buffer, 0, bytesRead);
				}

				System.out.println("Added " + fileToAdd.getName() + " to jar at " + jarLocation + ".");

				jos.closeEntry();
			}
			catch(IOException e)
			{
//				System.out.println("exception: "+e);
			}

			try
			{
				fileInStream.close();
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace(System.err);
			}
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace(System.err);
			return false;
		}

		return true;
	}

	/**
	 * Flushes and closes the jar stream, ending creation of the jar.
	 */
	public boolean closeJar()
	{
		try
		{
			jos.flush();
			jos.finish();
			jos.close();

			fos.close();
		}
		catch(IOException e)
		{
			e.printStackTrace(System.err);
			return false;
		}

		return true;
	}
}
