/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clados.gwasrisk.weka;

import org.clados.gwasrisk.Classify;
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
public class StepwiseGLMAicTest {
    
    public StepwiseGLMAicTest() {
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

//    @Test
//    public void test() {
//        String dir = "/home/victor/Escritorio/WTCCC1/best100_clasif/orig/";
//        String testSet = dir+"T1D_size1_best100_Test.rs.arff.gz";
//        String trainSet = dir+"T1D_size1_best100_Training.rs.arff.gz";
//                
//        // get classifier by name
//        Classify cls = new Classify();
//        cls.setClassifier( Classify.CLS_GLM_AIC );
//
//        cls.loadTestSet(testSet);
//        cls.loadTrainSet(trainSet);
//
//        cls.classify();
//        System.out.println( cls.getSummary() );
//                
//        assertNull(null);
//    }
    
//    @Test
//    public void testCV() {
//        String dir = "/home/victor/Escritorio/WTCCC1/best100_clasif/orig/";
//        
//        String trainSet = dir+"RA_size1_best100_all.rs.arff";
//                
//        // get classifier by name
//        Classify cls = new Classify();
//        cls.setClassifier( Classify.CLS_GLM_AIC );
//
//        cls.loadTrainSet(trainSet);
//
//        StringBuffer output = new StringBuffer();
//        cls.classifyCV(10, output);
//        System.out.println( output.toString() );
//        
//        
//        trainSet = dir+"T1D_size1_best100_all.rs.arff";
//                
//        // get classifier by name
//        cls = new Classify();
//        cls.setClassifier( Classify.CLS_GLM_AIC );
//
//        cls.loadTrainSet(trainSet);
//
//        output = new StringBuffer();
//        cls.classifyCV(10, output);
//        System.out.println( output.toString() );
//                
//        assertNull(null);
//    }
    
}
