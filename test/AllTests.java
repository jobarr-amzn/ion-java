/*
 * Copyright (c) 2007 Amazon.com, Inc.  All rights reserved.
 */

import com.amazon.ion.BadIonTests;
import com.amazon.ion.BlobTest;
import com.amazon.ion.BoolTest;
import com.amazon.ion.ClobTest;
import com.amazon.ion.DatagramTest;
import com.amazon.ion.DecimalTest;
import com.amazon.ion.EquivsTests;
import com.amazon.ion.FloatTest;
import com.amazon.ion.GoodIonTests;
import com.amazon.ion.IntTest;
import com.amazon.ion.ListTest;
import com.amazon.ion.NullTest;
import com.amazon.ion.RoundTripTests;
import com.amazon.ion.SexpTest;
import com.amazon.ion.StringTest;
import com.amazon.ion.StructTest;
import com.amazon.ion.SymbolTest;
import com.amazon.ion.TimestampTest;
import com.amazon.ion.impl.ByteBufferTest;
import com.amazon.ion.impl.ReaderTest;
import com.amazon.ion.impl.SymbolTableTest;
import com.amazon.ion.system.SimpleCatalogTest;
import com.amazon.ion.util.LoaderTest;
import com.amazon.ion.util.PrinterTest;
import com.amazon.ion.util.TextTest;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Runs all tests for the Ion project.
 */
public class AllTests
{
    public static Test suite()
    {
        TestSuite suite =
            new TestSuite("AllTests for Ion");

        //$JUnit-BEGIN$

        // Low-level facilities.
        suite.addTestSuite(ByteBufferTest.class);
        suite.addTestSuite(TextTest.class);


        // General framework tests
        suite.addTestSuite(SimpleCatalogTest.class);

        // Type-based DOM tests
        suite.addTestSuite(BlobTest.class);
        suite.addTestSuite(BoolTest.class);
        suite.addTestSuite(ClobTest.class);
        suite.addTestSuite(DecimalTest.class);
        suite.addTestSuite(FloatTest.class);
        suite.addTestSuite(IntTest.class);
        suite.addTestSuite(ListTest.class);
        suite.addTestSuite(NullTest.class);
        suite.addTestSuite(SexpTest.class);
        suite.addTestSuite(StringTest.class);
        suite.addTestSuite(StructTest.class);
        suite.addTestSuite(SymbolTest.class);
        suite.addTestSuite(TimestampTest.class);

        // Utility tests
        suite.addTestSuite(LoaderTest.class);
        suite.addTestSuite(ReaderTest.class);
        suite.addTestSuite(PrinterTest.class);

        suite.addTestSuite(SymbolTableTest.class);
        suite.addTestSuite(DatagramTest.class);

        // General processing test suite
        suite.addTest(new GoodIonTests());
        suite.addTest(new BadIonTests());
        suite.addTest(new EquivsTests());
        suite.addTest(new RoundTripTests());

        //$JUnit-END$

        return suite;
    }
}