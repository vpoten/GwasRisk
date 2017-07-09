/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clados.gwasrisk.weka.jri;

import java.io.File;
import org.rosuda.REngine.JRI.JRIEngine;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;

/**
 *
 * @author victor
 */
public class JRILoader {
    
    static REngine engine = null;
    
    static private void load() throws REngineException {
        
        String rLibsUser = System.getenv("R_LIBS_USER");
        
        if (rLibsUser == null || rLibsUser.length() == 0) {
            System.err.println("R_LIBS_USER is undefined. Cannot proceed with R native library loading");
            System.exit(1);
        }
        
        
        System.err.println("Trying R_LIBS_USER (" + rLibsUser
              + ") under Linux");
        rLibsUser = rLibsUser + File.separator + "rJava";
        
        File rJavaF = new File(rLibsUser);
        if (rJavaF.exists()) {
          System.err
            .println("Found rJava installed in " + rJavaF.getPath());
          String libraryLocation = rJavaF.getPath() + File.separator + "jri"
            + File.separator + "libjri.so";
          
          JRINativeLoader.loadLibrary(libraryLocation);
        }
        else{
            System.err.println("rJava library not found in R_LIBS_USER");
            System.exit(1);
        }
        
        engine = JRIEngine.createEngine();
        
    }
    
    static REngine createEngine() throws REngineException {
        if( engine==null ){
            load();
        }
        
        return engine;
    }
    
}
