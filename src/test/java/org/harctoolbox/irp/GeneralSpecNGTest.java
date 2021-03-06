package org.harctoolbox.irp;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GeneralSpecNGTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    public GeneralSpecNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of toString method, of class GeneralSpec.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.IrpSemanticException
     */
    @Test
    public void testToString() throws IrpSyntaxException, IrpSemanticException {
        try {
            System.out.println("toString");
            GeneralSpec instance = new GeneralSpec("{42%, 10p,msb,40k}");
            Assert.assertEquals(instance.toString(), "Frequency = 40000.0Hz, unit = 250.0us, msb, Duty cycle = 42%.");
            assertEquals(new GeneralSpec("{ }").toString(), "Frequency = 38000.0Hz, unit = 1.0us, lsb, Duty cycle: -.");
            assertEquals(new GeneralSpec("{38.4k,564}").toString(), "Frequency = 38400.0Hz, unit = 564.0us, lsb, Duty cycle: -.");
            assertEquals(new GeneralSpec("{564,38.4k}").toString(), "Frequency = 38400.0Hz, unit = 564.0us, lsb, Duty cycle: -.");
            assertEquals(new GeneralSpec("{22p,40k}").toString(), "Frequency = 40000.0Hz, unit = 550.0us, lsb, Duty cycle: -.");
            assertEquals(new GeneralSpec("{msb, 889u}").toString(), "Frequency = 38000.0Hz, unit = 889.0us, msb, Duty cycle: -.");
            assertEquals(new GeneralSpec("{42%, 10p,msb,40k}").toString(), "Frequency = 40000.0Hz, unit = 250.0us, msb, Duty cycle = 42%.");
            assertEquals(new GeneralSpec("{msb ,40k , 33.33333% ,10p }").toString(), "Frequency = 40000.0Hz, unit = 250.0us, msb, Duty cycle = 33%.");
            assertEquals(new GeneralSpec("{msb, 123u, 100k, 10p, 1000k}").toString(), "Frequency = 1000000.0Hz, unit = 10.0us, msb, Duty cycle: -.");
        } catch (ArithmeticException ex) {
            fail();
        }
    }

    /**
     * Test of getBitDirection method, of class GeneralSpec.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.IrpSemanticException
     */
    @Test
    public void testGetBitDirection() throws IrpSyntaxException, IrpSemanticException {
        try {
            System.out.println("getBitDirection");
            GeneralSpec instance = new GeneralSpec("{lsb, msb ,40k , 33.33333% ,10p }");
            BitDirection result = instance.getBitDirection();
            Assert.assertEquals(result, BitDirection.msb);
            instance = new GeneralSpec("{lsb ,40k , 33.33333% ,10p }");
            result = instance.getBitDirection();
            Assert.assertEquals(result, BitDirection.lsb);
            instance = new GeneralSpec("{40k , 33.33333% ,10p }");
            result = instance.getBitDirection();
            Assert.assertEquals(result, BitDirection.lsb);
        } catch (ArithmeticException ex) {
            Logger.getLogger(GeneralSpecNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getFrequency method, of class GeneralSpec.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.IrpSemanticException
     */
    @Test
    public void testGetFrequency() throws IrpSyntaxException, IrpSemanticException {
        try {
            System.out.println("getFrequency");
            GeneralSpec instance = new GeneralSpec("{msb, 12.3k, 33.33333% ,10p }");
            double result = instance.getFrequency();
            Assert.assertEquals(result, 12300f, 0.0001);
            Assert.assertEquals(new GeneralSpec("{msb, 33.33333% ,10p }").getFrequency(), ModulatedIrSequence.defaultFrequency, 0.0001);
        } catch (ArithmeticException ex) {
            Logger.getLogger(GeneralSpecNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getUnit method, of class GeneralSpec.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     * @throws org.harctoolbox.irp.IrpSemanticException
     */
    @Test
    public void testGetUnit() throws IrpSyntaxException, IrpSemanticException {
        try {
            System.out.println("getUnit");
            GeneralSpec instance = new GeneralSpec("{123u, msb ,40k , 33.33333% ,10p }"); // Do not remove the silly formatting!!
            double expResult = 250f;
            double result = instance.getUnit();
            Assert.assertEquals(result, expResult, 0.0001);
        } catch (ArithmeticException ex) {
            Logger.getLogger(GeneralSpecNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getDutyCycle method, of class GeneralSpec.
     */
    @Test
    public void testGetDutyCycle() {
        try {
            System.out.println("getDutyCycle");
            GeneralSpec instance = new GeneralSpec("{123u, msb ,40k , 73% ,10p }"); // Do not remove the silly formatting!!
            double expResult = 0.73f;
            double result = instance.getDutyCycle();
            Assert.assertEquals(result, expResult, 0.0001);
        } catch (ArithmeticException | IrpSemanticException ex) {
            fail();
        }
    }

//    /**
//     * Test of evaluatePrint method, of class GeneralSpec.
//     * @throws java.lang.Exception
//     */
//    @Test
//    public void testEvaluatePrint() throws Exception {
//        System.out.println("evaluatePrint");
//        GeneralSpec instance = new GeneralSpec("{123u, msb ,40k , 73% ,10p }"); // Do not remove the silly formatting!!
//        String str = "";
//        instance.evaluatePrint(str);
//    }

    /**
     * Test of toIrpString method, of class GeneralSpec.
     */
    @Test
    public void testToIrpString() {
        try {
            System.out.println("toIrpString");
            GeneralSpec instance = new GeneralSpec("{123u, msb ,40k , 73% ,10p }"); // Do not remove the silly formatting!!
            String expResult = "{40.0k,250,msb,73%}";
            String result = instance.toIrpString();
            assertEquals(result, expResult);
        } catch (IrpSemanticException | ArithmeticException ex) {
            fail();
        }
    }

//    /**
//     * Test of toElement method, of class GeneralSpec.
//     */
//    @Test
//    public void testToElement() {
//        System.out.println("toElement");
//        Document document = null;
//        GeneralSpec instance = new GeneralSpec();
//        Element expResult = null;
//        Element result = instance.toElement(document);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
