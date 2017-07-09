/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clados.gwasrisk;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author victor
 */
public class DnaEncoderTest {
    
    public DnaEncoderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
     
    @Test
    public void testMethods() {
        String cad = "AGTCATGC?AA";
        DnaEncoder inst = new DnaEncoder(cad);
        
        assertEquals(inst.charAt(0),'A');
        assertEquals(inst.charAt(2),'T');
        assertEquals(inst.charAt(3),'C');
        assertEquals(inst.charAt(8),'?');
        assertEquals(inst.charAt(9),'A');
        assertEquals(inst.charAt(10),'A');
        assertEquals(inst.length(), cad.length());
        
        assertEquals(inst.decode(), cad);
        assertTrue( inst.equals(cad) );
        assertTrue( inst.equals("AGTCATGC?AA") );
        assertFalse( inst.equals("AGTCTTGC?AA") );
        assertFalse( inst.equals("AGTCATGC?A?") );
        
        cad = "A?";
        inst = new DnaEncoder(cad);
        assertEquals(inst.charAt(0),'A');
        assertEquals(inst.charAt(1),'?');
        assertEquals(inst.length(), cad.length());
    }
    
}
