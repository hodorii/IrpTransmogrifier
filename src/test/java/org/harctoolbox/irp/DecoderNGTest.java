package org.harctoolbox.irp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSignal;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class DecoderNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private final Decoder decoder;

    public DecoderNGTest() throws IOException, SAXException, IrpSyntaxException {
        IrpDatabase irp = new IrpDatabase("src/main/config/IrpProtocols.xml");
        irp.expand();
        decoder = new Decoder(irp);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of decode method, of class Decoder.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Test
    public void testDecode() throws InvalidArgumentException {
        System.out.println("decode");
        IrSignal irSignal = new IrSignal("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C");
        Map<String, Decoder.Decode> result = decoder.decode(irSignal, false);
        assertEquals(result.size(), 1);
        assertEquals(result.get("NEC1").toString(), "NEC1: {D=12,F=56,S=34}");
    }

    /**
     * Test of decode method, of class Decoder.
     * Takes long time (around 10s).
     */
    @Test(enabled = true)
    public void testDecode_List_String() {
        System.out.println("decode");
        List<String> protocols = new ArrayList<>(0);
        String irpDatabasePath = "src/main/config/IrpProtocols.xml";
        try {
            Decoder.decode(protocols, irpDatabasePath);
        } catch (IOException | SAXException | IrpException ex) {
            fail();
        }
    }

    /**
     * Test of decode method, of class Decoder.
     */
    @Test
    public void testDecode_IrSignal_boolean() {
        try {
            System.out.println("decode");
            IrSignal irSignal = new IrSignal("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 06A4 015B 0057 0016 0E6C");
            Map<String, Decoder.Decode> result = decoder.decode(irSignal, false);
            assertEquals(result.size(), 1);
            result = decoder.decode(irSignal, true);
            assertEquals(result.size(), 2);
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(DecoderNGTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}