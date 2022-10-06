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

package org.web3d.util;

// Standard imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the implementation of the IntHashSet class.  Pretty basic stuff.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class IntHashSetTest extends TestCase {

    public IntHashSetTest(String name)
    {
        super(name);
    }

    /**
     * Tests the toArray() method using a passed in array.
     * The result has to be sorted before comparing with the expected result.
     */
    public void testToArray()
    {
        IntHashSet<Integer> hs=new IntHashSet<>();
        assertTrue("Add 1",hs.add(1));
        assertTrue("Add 2",hs.add(2));
        assertTrue("Add 1 Again",!hs.add(1));
        assertEquals("Size check",hs.size(),2);
        int[] arr = new int[2];
        hs.toArray(arr);
        java.util.Arrays.sort(arr);
        assertTrue(java.util.Arrays.equals(arr,new int[]{1,2}));
    }

    /**
     * Tests the toArray() method requiring a new array to be created.
     * The result has to be sorted before comparing with the expected result.
     */
    public void testToArrayTooSmall()
    {
        IntHashSet<Integer> hs=new IntHashSet<>();
        assertTrue("Add 1",hs.add(2_701));
        assertTrue("Add 2",hs.add(353));
        assertEquals("Size check",hs.size(),2);
        int[] arr = hs.toArray(new int[0]);
        java.util.Arrays.sort(arr);
        assertTrue(java.util.Arrays.equals(arr,new int[]{353, 2_701}));
    }

    /**
     * A simple stress test.  Adds 10000 objects and removes them all again,
     * making sure they are all added and removed correctly and the final size
     * is 0.
     */
    public void testLotsOfAddingAndRemoving()
    {
        int max=10_000;
        IntHashSet<Integer> hs=new IntHashSet<>();
        for(int i=0;i<max;i++)
        {
            assertTrue("Adding "+i,hs.add(i));
        }
        assertEquals(max,hs.size());
        assertTrue("Not empty",!hs.isEmpty());

        for(int i=0;i<max;i++)
        {
            assertTrue(hs.contains(i));
        }

        assertFalse(hs.contains(max));

        for(int i=0;i<max;i++)
        {
            assertTrue("Removing "+i,hs.remove(i));
        }
        assertEquals(0,hs.size());
        assertTrue(hs.isEmpty());
        assertEquals("[]",hs.toString());
    }

    /**
     * Test that dups don't work.
     */
    public void testDups() {
        IntHashSet<Integer> hs = new IntHashSet<>();

        int val = Integer.MAX_VALUE;
        hs.add(val);
        assertFalse(hs.add(val));

        assertEquals(1,hs.size());
        hs.remove(val);
        assertEquals(0, hs.size());
        assertFalse(hs.contains(val));
    }

    /**
     * A simple range stress test.  Spot check corners of int range.
     */
    public void testRange() {
        IntHashSet<Integer> hs = new IntHashSet<>();

        int val = Integer.MAX_VALUE;
        hs.add(val);
        assertTrue(hs.contains(val));
        hs.remove(val);
        assertTrue(hs.isEmpty());

        hs = new IntHashSet<>();

        val = Integer.MIN_VALUE;
        hs.add(val);
        assertTrue(hs.contains(val));
        hs.remove(val);
        assertTrue(hs.isEmpty());

        val = 0;
        hs.add(val);
        assertTrue(hs.contains(val));
        hs.remove(val);
        assertTrue(hs.isEmpty());

    }

    /**
     * Tests the clear() method, confirming by trying to add and remove objects
     * that have been cleared.
     */
    public void testClear()
    {
        IntHashSet<Integer> hs1=new IntHashSet<>();
        hs1.add(1);
        hs1.add(2);
        hs1.add(3);
        hs1.clear();
        assertTrue(hs1.isEmpty());
        assertTrue(!hs1.contains(1));
        assertTrue(!hs1.remove(2));
        assertTrue(hs1.add(3));
    }

    /**
     * Ensure that two objects are equal and have the same hashCode() when they
     * contain the same elements (even if added in a different order) and
     * that they aren't equal when an element is removed.  Note that hashCodes
     * don't have to be different for object that aren't equal.
     */
    public void testEqualsAndHashCode()
    {
        IntHashSet<Integer> hs1=new IntHashSet<>();
        hs1.add(1);
        hs1.add(2);
        hs1.add(3);

        IntHashSet<Integer> hs2=new IntHashSet<>();
        hs2.add(3);
        hs2.add(1);
        hs2.add(2);

        assertEquals(hs1.hashCode(),hs2.hashCode());
        assertTrue(hs1.equals(hs2));

        hs1.remove(2);

        assertTrue(!hs1.equals(hs2));
    }


    /**
     * Tests the addAll method with a List containing duplicates.
     * The duplicates are discarded.
     */
    public void testAddAllCollection()
    {
        int[] l = new int[] {1,2,1,3};

        IntHashSet<Integer> hs=new IntHashSet<>();
        assertTrue(hs.addAll(l));
        assertEquals(3,hs.size());
        assertTrue(!hs.addAll(l));
    }

    /**
     * Tests the removeAll method with a List containing duplicates.
     * The duplicates are ignored.
     */
    public void testRemoveAllCollection()
    {
        int[] l = new int[] {1,2,1,3};
        IntHashSet<Integer> hs=new IntHashSet<>();
        hs.add(1);
        hs.add(4);
        assertTrue(hs.removeAll(l));
        assertEquals(1,hs.size());
        assertEquals("[4]",hs.toString());

        assertTrue(!hs.removeAll(l));
    }

    /**
     * Compares two Object arrays
     */
    private void assertEquals(Object[] a1,Object[] a2)
    {
        assertEquals(a1==null,a2==null);
        if (a1==null)
          return;
        assertEquals(a1.length,a2.length);
        for(int i=0;i<a1.length;i++)
          assertEquals(a1[i],a2[i]);
    }

    public static Test suite()
    {
        return new TestSuite(IntHashSetTest.class);
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
}