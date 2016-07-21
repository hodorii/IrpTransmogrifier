/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.harctoolbox.irp;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class NumberWithDecimalsNGTest {

    private final NumberWithDecimals pi;
    private final NumberWithDecimals e;
    private final NumberWithDecimals answer;
    private final NumberWithDecimals sheldon;

    public NumberWithDecimalsNGTest() throws IrpSyntaxException {
        pi = new NumberWithDecimals("3.1415926");
        e = new NumberWithDecimals(2.71);
        answer = new NumberWithDecimals(42);
        sheldon = new NumberWithDecimals(73);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of parseNumberWithDecimals method, of class NumberWithDecimals.
     * @throws java.lang.Exception
     */
    @Test
    public void testParse() throws Exception {
        System.out.println("parseNumberWithDecimals");
        assertEquals(NumberWithDecimals.parse("73"), 73f, 0.0000001);
        assertEquals(NumberWithDecimals.parse("73.42"), 73.42, 0.0000001);
        assertEquals(NumberWithDecimals.parse(".73"), .73, 0.0000001);
    }

    /**
     * Test of toFloat method, of class NumberWithDecimals.
     */
    @Test
    public void testToFloat_0args() {
        System.out.println("toFloat");
        assertEquals(pi.toFloat(), 3.1415926);
    }

    /**
     * Test of toString method, of class NumberWithDecimals.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        assertEquals(sheldon.toString(), "73");
    }

    /**
     * Test of toIrpString method, of class NumberWithDecimals.
     */
    @Test
    public void testToIrpString() {
        System.out.println("toIrpString");
        assertEquals(sheldon.toIrpString(), "73");
        assertEquals(pi.toIrpString(), "3.1415926");
    }
}
