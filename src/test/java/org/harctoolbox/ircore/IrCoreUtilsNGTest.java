package org.harctoolbox.ircore;

import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IrCoreUtilsNGTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public IrCoreUtilsNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of seconds2microseconds method, of class IrCoreUtils.
     */
    @Test
    public void testSeconds2microseconds() {
        System.out.println("seconds2microseconds");
        double secs = 12.3456;
        double expResult = 12345600.0;
        double result = IrCoreUtils.seconds2microseconds(secs);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of milliseconds2microseconds method, of class IrCoreUtils.
     */
    @Test
    public void testMilliseconds2microseconds() {
        System.out.println("milliseconds2microseconds");
        double ms = 3.145926;
        double expResult = 3145.926;
        double result = IrCoreUtils.milliseconds2microseconds(ms);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of khz2Hz method, of class IrCoreUtils.
     */
    @Test
    public void testKhz2Hz() {
        System.out.println("khz2Hz");
        double khz = 38.4;
        double expResult = 38400.0;
        double result = IrCoreUtils.khz2Hz(khz);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of percent2real method, of class IrCoreUtils.
     */
    @Test
    public void testPercent2real() {
        System.out.println("percent2real");
        double percent = 42.0;
        double expResult = 0.42;
        double result = IrCoreUtils.percent2real(percent);
        assertEquals(result, expResult, 0.000001);
    }

    /**
     * Test of real2percent method, of class IrCoreUtils.
     */
    @Test
    public void testReal2percent() {
        System.out.println("real2percent");
        double x = 0.7342;
        double expResult = 73.42;
        double result = IrCoreUtils.real2percent(x);
        assertEquals(result, expResult, 0.000001);
    }

    /**
     * Test of l1Norm method, of class IrCoreUtils.
     */
    @Test
    public void testL1Norm_DoubleObjectArr() {
        System.out.println("l1NormDouble");
        Double[] sequence = new Double[] { Double.valueOf(1f), Double.valueOf(2f), Double.valueOf(3f), Double.valueOf(4f) };
        double expResult = 10.0;
        double result = IrCoreUtils.l1Norm(sequence);
        assertEquals(result, expResult, 0.0000001);
    }

    /**
     * Test of l1Norm method, of class IrCoreUtils.
     */
    @Test
    public void testL1Norm_xdoubleArr() {
        System.out.println("l1Normdouble");
        double[] sequence = new double[] { 1f, 2f, 3f, 4f };
        double expResult = 10.0;
        double result = IrCoreUtils.l1Norm(sequence);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of l1Norm method, of class IrCoreUtils.
     */
    @Test
    public void testL1Norm_3args() {
        System.out.println("l1Norm");
        double[] sequence = new double[] { 1f, 2f, 3f, 4f };
        int beg = 1;
        int length = 2;
        double expResult = 5.0;
        double result = IrCoreUtils.l1Norm(sequence, beg, length);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of approximatelyEquals method, of class IrCoreUtils.
     */
    @Test
    public void testApproximatelyEquals_4args_1() {
        System.out.println("approximatelyEquals");
        Assert.assertTrue( IrCoreUtils.approximatelyEquals(73.0, 74.0, 1.0, 0.0));
        Assert.assertFalse(IrCoreUtils.approximatelyEquals(73.0, 74.0, 0.9, 0.0));
        Assert.assertFalse(IrCoreUtils.approximatelyEquals(73.0, 74.0, 0.0, 0.01));
        Assert.assertTrue( IrCoreUtils.approximatelyEquals(103.0, 104.0, 0.0, 0.01));
    }

    /**
     * Test of approximatelyEquals method, of class IrCoreUtils.
     */
    @Test
    public void testApproximatelyEquals_4args_2() {
        System.out.println("approximatelyEquals");
        Assert.assertTrue(IrCoreUtils.approximatelyEquals(99, 100, 1, 0));
        Assert.assertFalse(IrCoreUtils.approximatelyEquals(99, 101, 1, 0));
    }

    /**
     * Test of reverse method, of class IrCoreUtils.
     */
    @Test
    public void testReverse() {
        System.out.println("reverse");
        long x = 73L;
        int width = 8;
        long expResult = 146L;
        long result = IrCoreUtils.reverse(x, width);
        assertEquals(result, expResult);
    }

    /**
     * Test of ones method, of class IrCoreUtils.
     */
    @Test
    public void testOnes_long() {
        System.out.println("ones");
        long width = 7L;
        long expResult = 127L;
        long result = IrCoreUtils.ones(width);
        assertEquals(result, expResult);
    }

    /**
     * Test of ones method, of class IrCoreUtils.
     */
    @Test
    public void testOnes_int() {
        System.out.println("ones");
        int width = 9;
        int expResult = 511;
        int result = IrCoreUtils.ones(width);
        assertEquals(result, expResult);
    }

    /**
     * Test of maskTo method, of class IrCoreUtils.
     */
    @Test
    public void testMaskTo_long_long() {
        System.out.println("maskTo");
        long data = 73L;
        long width = 4L;
        long expResult = 9L;
        long result = IrCoreUtils.maskTo(data, width);
        assertEquals(result, expResult);
    }

    /**
     * Test of maskTo method, of class IrCoreUtils.
     */
    @Test
    public void testMaskTo_int_int() {
        System.out.println("maskTo");
        int data = 777;
        int width = 8;
        long expResult = 9L;
        long result = IrCoreUtils.maskTo(data, width);
        assertEquals(result, expResult);
    }

    /**
     * Test of microseconds2milliseconds method, of class IrCoreUtils.
     */
    @Test
    public void testMicroseconds2milliseconds() {
        System.out.println("microseconds2milliseconds");
        double us = 1234;
        double expResult = 1.234;
        double result = IrCoreUtils.microseconds2milliseconds(us);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of hz2khz method, of class IrCoreUtils.
     */
    @Test
    public void testHz2khz() {
        System.out.println("hz2khz");
        double frequency = 45678;
        double expResult = 45.678;
        double result = IrCoreUtils.hz2khz(frequency);
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of us2Periods method, of class IrCoreUtils.
     */
    @Test
    public void testUs2Periods() {
        System.out.println("us2Periods");
        double us = 1050.0;
        double frequency = 40000;
        double expResult = 42;
        double result = IrCoreUtils.us2Periods(us, frequency);
        assertEquals(result, expResult, 0.0);
    }
//
//    /**
//     * Test of l1Norm method, of class IrCoreUtils.
//     */
//    @Test
//    public void testL1Norm_Iterable() {
//        System.out.println("l1Norm");
//        Iterable<Double> sequence = null;
//        double expResult = 0.0;
//        double result = IrCoreUtils.l1Norm(sequence);
//        assertEquals(result, expResult, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of approximatelyEquals method, of class IrCoreUtils.
//     */
//    @Test
//    public void testApproximatelyEquals_double_double() {
//        System.out.println("approximatelyEquals");
//        double x = 0.0;
//        double y = 0.0;
//        boolean expResult = false;
//        boolean result = IrCoreUtils.approximatelyEquals(x, y);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
