/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

import vrml.eai.Browser;
import vrml.eai.Node;
import vrml.eai.field.*;


/**
 *   A test to try out createVrmlFromString, and try constructing a scene
 *   graph from components using eventIn's.
 *   <P>
 *   The sequence of operations is
 *   1.  setBrowserFactory, getBrowserComponent, getBrowser, etc.
 *   2.  createVrmlFromString
 *   3.  replaceWorld with the newly created nodes
 *   4.  Expect to see a sphere on the display
 *   <P>
 */

public class CreateFromString {
  public static void main(String[] args) {
    Browser browser=TestFactory.getBrowser();

    browser.addBrowserListener(new GenericBrowserListener());
    /* Test one */
    Node nodes[]=browser.createVrmlFromString(
      "DEF Root Transform {}"
    );
    System.out.println("Number of nodes from create:"+nodes.length);
    System.out.println("Replacing world...");
    EAIBrowserInitWaiter waiter=new EAIBrowserInitWaiter();
    browser.addBrowserListener(waiter);
    browser.replaceWorld(nodes);
    try {
        waiter.waitForInit();
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    System.out.println("World replaced.");

    /* Test two */
    Node nullResult[]=browser.createVrmlFromString("");
    if (nullResult==null)
      System.out.println("Returned a null array for no nodes.");
    else if (nullResult.length!=0)
      System.out.println("Returned nodes for the empty string.");
    else
      System.out.println("Returned the correct result for empty string.");
    try {
      if (browser.getNode("Root")!=null)
        System.out.println("Incorrectly added to namespace.");
      System.out.println("Failed to throw invalid node exception.");
    } catch (vrml.eai.InvalidNodeException ine) {
      System.out.println("Correctly threw invalid node exception.");
    }
    /* Test three */
    Node levelOne[]=browser.createVrmlFromString(
      "Viewpoint {}\n"+
      "Group {}\n"
    );
    Node levelTwo[]=browser.createVrmlFromString(
      "Transform {\n"+
      "  children [\n"+
      "    Shape {\n"+
      "      appearance Appearance {\n"+
      "        material Material {\n"+
      "          emissiveColor 1 0 0\n"+
      "        }\n"+
      "      }\n"+
      "    geometry Sphere {radius 1}\n"+
      "    }\n"+
      "  ]\n"+
      "}"+
      "Shape {\n"+
      "  appearance Appearance {\n"+
      "    material Material {\n"+
      "      emissiveColor 0 0 1\n"+
      "    }\n"+
      "  }\n"+
      "  geometry Sphere {radius 1}\n"+
      "}"
    );
    ((EventInMFNode)nodes[0].getEventIn("set_children")).setValue(levelOne);
    ((EventInMFNode)levelOne[1].getEventIn("set_children")).setValue(levelTwo);
    ((EventInSFVec3f)levelTwo[0].getEventIn("translation")).setValue(new float[]{2.0f,0.0f,0.0f});
  }
}
