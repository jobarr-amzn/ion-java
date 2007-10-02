/*
 * Copyright (c) 2007 Amazon.com, Inc.  All rights reserved.
 */

package com.amazon.ion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ListTest
    extends SequenceTestCase
{
    @Override
    protected IonSequence makeNull()
    {
        return system().newList();
    }

    @Override
    protected String wrap(String v)
    {
        return "[" + v + "]";
    }

    //=========================================================================
    // Test cases

    public void testFactoryNullList()
    {
        IonList value = system().newList();
        assertNull(value.getContainer());
        testFreshNullSequence(value);
    }

    public void testTextNullList()
    {
        IonList value = (IonList) oneValue("null.list");
        testFreshNullSequence(value);
    }

    public void testMakeNullList()
    {
        IonList value = (IonList) oneValue("[6,9]");
        assertFalse(value.isNullValue());
        value.makeNull();
        testFreshNullSequence(value);
    }

    public void testClearNonMaterializedList()
    {
        IonList value = (IonList) oneValue("[6,9]");
        testClearContainer(value);
    }

    public void testEmptyList()
    {
        IonList value = (IonList) oneValue("[]");
        testEmptySequence(value);
    }


    public void testListParsing()
    {
        IonValue nine = oneValue("9");
        IonValue four = oneValue("4");

        IonList value = (IonList) oneValue("[9,4]");
        assertEquals(2, value.size());
        assertIonEquals(nine, value.get(0));
        assertIonEquals(four, value.get(1));

        try
        {
            value.get(2);
            fail("Expected IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException e) { }

        Iterator<IonValue> i = value.iterator();
        assertTrue(i.hasNext());
        assertIonEquals(nine, i.next());
        assertTrue(i.hasNext());
        assertIonEquals(four, i.next());
        assertFalse(i.hasNext());
    }


    public void testGetTwiceReturnsSame()
    {
        IonList value = (IonList) oneValue("[a,b]");
        IonValue elt1 = value.get(1);
        IonValue elt2 = value.get(1);
        assertSame(elt1, elt2);
    }

    public void testRemovalFromParsedWithAnnot()
    {
        IonList value = (IonList) oneValue("[a::4]");
        IonInt four = (IonInt) value.get(0);

        // Don't touch anything until we remove it!

        value.remove(four);
        assertEquals(4, four.intValue());
        checkAnnotation("a", four);
    }


    /**
     * Ensure that removal from a parsed list maintains its state.
     * It can no longer share the same backing store.
     */
    public void testRemoveFromParsedListKeepsState()
    {
        IonList value = (IonList) oneValue("[1]");
        IonInt elt = (IonInt) value.get(0);
        value.remove(elt);
        assertNull(elt.getContainer());
        assertEquals(1, elt.intValue());
    }

    /** Ensure that triple-quote concatenation works inside lists. */
    public void testConcatenation()
    {
        IonList value = (IonList) oneValue("[a, '''a''' '''b''', '''c''']");
        checkSymbol("a",  value.get(0));
        checkString("ab", value.get(1));
        checkString("c",  value.get(2));
        assertEquals(3, value.size());
    }

    public void testListIteratorRemove()
    {
        IonList value = (IonList) oneValue("[a,b,c]");
        testIteratorRemove(value);
    }

    public void testCreatingNullList()
    {
        IonList list1 = system().newList();
        IonValue list2 = reload(list1);

        // FIXME ensure list1._isPositionLoaded && _isMaterialized
        assertIonEquals(list1, list2);
    }

    public void testCreatingListFromCollection()
    {
        IonSystem system = system();
        List<IonValue> elements = null;

        IonList v = system.newList(elements);
        testFreshNullSequence(v);

        elements = new ArrayList<IonValue>();
        v = system.newList(elements);
        testEmptySequence(v);

        elements.add(system.newString("hi"));
        elements.add(system.newInt(1776));
        v = system.newList(elements);
        assertEquals(2, v.size());
        checkString("hi", v.get(0));
        checkInt(1776, v.get(1));

        try {
            v = system.newList(elements);
            fail("Expected ContainedValueException");
        }
        catch (ContainedValueException e) { }

        elements = new ArrayList<IonValue>();
        elements.add(system.newInt(1776));
        elements.add(null);
        try {
            system.newList(elements);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e) { }
    }

    public void testCreatingListFromIntArray()
    {
        IonSystem system = system();
        int[] elements = null;

        IonList v = system.newList(elements);
        testFreshNullSequence(v);

        elements = new int[0];
        v = system.newList(elements);
        testEmptySequence(v);

        elements = new int[]{ 12, 13, 14 };
        v = system.newList(elements);
        assertEquals(3, v.size());
        checkInt(12, v.get(0));
        checkInt(13, v.get(1));
        checkInt(14, v.get(2));
        elements = new int[]{ 12, 13, 14 };
    }

    public void testCreatingListFromLongArray()
    {
        IonSystem system = system();
        long[] elements = null;

        IonList v = system.newList(elements);
        testFreshNullSequence(v);

        elements = new long[0];
        v = system.newList(elements);
        testEmptySequence(v);

        elements = new long[]{ 12, 13, 14 };
        v = system.newList(elements);
        assertEquals(3, v.size());
        checkInt(12, v.get(0));
        checkInt(13, v.get(1));
        checkInt(14, v.get(2));
    }

    public void testCreatingListFromValueArray()
    {
        IonSystem system = system();
        IonValue[] elements = null;

        IonList v = system.newList(elements);
        testFreshNullSequence(v);

        elements = new IonValue[0];
        v = system.newList(elements);
        testEmptySequence(v);

        elements = new IonValue[]{ system.newInt(12), system.newString("hi") };
        v = system.newList(elements);
        assertEquals(2, v.size());
        checkInt(12, v.get(0));
        checkString("hi", v.get(1));

        try {
            v = system.newList(elements);
            fail("Expected ContainedValueException");
        }
        catch (ContainedValueException e) { }

        // varargs usage
        v = system.newList(system.newInt(12), system.newString("hi"));
        assertEquals(2, v.size());
        checkInt(12, v.get(0));
        checkString("hi", v.get(1));

        try {
            v = system.newList(system.newInt(12), null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e) { }
    }

    public void testCreatingListWithString()
    {
        IonList list1 = system().newList();
        list1.add(system().newString("Hello"));

        IonValue list2 = reload(list1);

        assertIonEquals(list1, list2);

        // Again, starting from [] instead of null.list
        list1 = system().newEmptyList();
        list1.add(system().newString("Hello"));

        list2 = reload(list1);
        assertIonEquals(list1, list2);
    }
}