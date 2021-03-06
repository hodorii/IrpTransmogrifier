package org.harctoolbox.analyze;

import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.Pronto;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CleanerNGTest {
    @BeforeClass
    public static void setUpClass() throws Exception {
    }
    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    private String nec_12_34_56 = "0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C";
    private ModulatedIrSequence irSequence = null;
    private ModulatedIrSequence noisy = null;
    //ModulatedIrSequence cleaned = null;
    private IrSignal irSignal = null;

    public CleanerNGTest() {
        try {
            irSignal = Pronto.parse(nec_12_34_56);
            irSequence = irSignal.toModulatedIrSequence(5);
            noisy = irSequence.addNoise(60.0);
        } catch (InvalidArgumentException ex) {
            fail();
        }
    }


    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of clean method, of class Cleaner.
     */
    @Test
    public void testClean_IrSequence() {
        System.out.println("clean");
        IrSequence verynoisy = irSequence.addNoise(60);
        IrSequence cleaned = Cleaner.clean(verynoisy);
        //Assert.assertFalse(irSequence.approximatelyEquals(verynoisy, IrCoreUtils.defaultAbsoluteTolerance, 0.1));
        Assert.assertTrue(irSequence.approximatelyEquals(cleaned, IrCoreUtils.defaultAbsoluteTolerance, 0.1));

        IrSequence reallynoisy = irSequence.addNoise(200);
        cleaned = Cleaner.clean(reallynoisy);
        Assert.assertFalse(irSequence.approximatelyEquals(reallynoisy, IrCoreUtils.defaultAbsoluteTolerance, 0.1));
        Assert.assertFalse(irSequence.approximatelyEquals(cleaned, IrCoreUtils.defaultAbsoluteTolerance, 0.1));
        Assert.assertFalse(irSequence.approximatelyEquals(cleaned));
    }

    /**
     * Test of getIndexData method, of class Cleaner.
     */
//    @Test
//    public void testGetIndexData() {
//        System.out.println("getIndexData");
//        Cleaner instance = new Cleaner(noisy, 60, 0.2);
//        int[] expResult = new int[] { 4,3,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,1,0,0,0,0,0,1,0,1,0,1,0,0,0,0,0,0,0,1,0,1,0,5,4,2,0,6,4,2,0,6,4,2,0,6,4,2,0,6};
//        int[] result = instance.getIndexData();
//        assertEquals(result, expResult);
//    }

    /**
     * Test of toTimingsString method, of class Cleaner.
     */
    @Test
    public void testToTimingsString() {
        System.out.println("toTimingsString");
        Cleaner instance = new Cleaner(noisy, 60, 0.2);
        String expResult = "ED AA AA AB AB AA AA AA AA AA AB AA AA AA AB AA AA AA AA AA AB AB AB AA AA AB AB AB AA AA AA AB AB AF EC AG EC AG EC AG EC AG";
        String result = instance.toTimingsString();
        assertEquals(result, expResult);
    }

    /**
     * Test of clean method, of class Cleaner.
     */
    @Test
    public void testClean_3args_1() {
        System.out.println("clean");
        int absoluteTolerance = 60;
        double relativeTolerance = 0.2;
        IrSequence expResult = null;
        IrSequence result = Cleaner.clean(noisy, absoluteTolerance, relativeTolerance);
        Assert.assertTrue(result.approximatelyEquals(irSequence));
    }

    /**
     * Test of clean method, of class Cleaner.
     */
    @Test
    public void testClean_3args_2() {
        System.out.println("clean");
        int absoluteTolerance = 60;
        double relativeTolerance = 0.2;
        ModulatedIrSequence expResult = null;
        ModulatedIrSequence result = Cleaner.clean(noisy, absoluteTolerance, relativeTolerance);
        Assert.assertTrue(result.approximatelyEquals(irSequence));
    }
}
