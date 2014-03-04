package com.and.wellnessconnected.cassandra;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class LibTestSuite
{
	
	
    
    /**
     * @return the suite of tests being tested
     */
	
    public static junit.framework.Test suite()
    {
        return new TestSuite( AppTest.class ); //indicate all test methods that start with 'test'
    }

    
}
