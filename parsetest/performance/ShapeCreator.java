/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

/**
 * Peformance testing creator for a large number of Shape nodes.i 
 * Creates large files for performance testing.
 * Simple creator for now.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class ShapeCreator {
    public static void main(String[] args) {
        System.out.println("#X3D V3.0 utf8\n\nPROFILE Immersive\n");

        int num_shapes = 5000;

        if(args.length != 0) 
            num_shapes = Integer.parseInt(args[0]);

        System.out.println("Shape { geometry DEF BOX Box {} }");
        
        for(int i=0; i < num_shapes - 1; i++) {
            printBox();
        }
    }

    public static void printBox() {
        System.out.println("Shape { geometry USE BOX }");
    }

    public static void printSphere() {
        System.out.println("Shape { geometry Sphere {} }");
    }
}
