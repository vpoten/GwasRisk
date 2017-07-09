/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clados.gwasrisk.weka.jri;

import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;
import weka.core.Attribute;
import weka.core.Instances;

/**
 * Static utility methods for pushing/pulling data to/from R.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @author vpoten
 */
public class RUtils {
    
  public static String cleanse(String q) {
    // return "'" + q + "'";
      
    q = q.replace('\'','_').replace(':', '_').replace('-', '_');

    q = q.replace('-', '.').replace(' ', '.').replace("\\", "\\\\");
    q = q.replace("%", "\\%").replace("'", "\\'").replace("\n", "\\n");
    q = q.replace("\r", "\\r").replace("\"", "\\\"");
    q = q.replace("$", "_dollar_").replace("#", "_hash_").replace("(", "_op_");
    q = q.replace(")", "_cp_").replace("[", "_ob_").replace("]", "_cb_");
    q = q.replace("{", "_obr_").replace("}", "_cbr_");
    q = q.replace("!", "_exl_").replace(";", "_semiC_");
    q = q.replace("/", "_div_").replace("@", "_at_").replace("+", "_plus_");
    q = q.replace("=", "_eq_").replace("?", "_qm_");
    q = q.replace( ">", "_gr_" ).replace( "<", "_lt_" );

    return q;
  }

  /**
   * Transfer a set of instances into a R data frame in the workspace
   * 
   * @param session the REngine to use
   * @param insts the instances to transfer
   * @param frameName the name of the data frame in R
   * @param nomToNumeric convert nominal to numeric values
   * @throws RSessionException if the requesting object is not the current
   *           session holder
   * @throws REngineException if a problem occurs on the R end
   * @throws REXPMismatchException if a problem occurs on the R end
   */
  public static void instancesToDataFrame(REngine session,
    Instances insts, String frameName, boolean nomToNumeric) throws RSessionException,
    REngineException, REXPMismatchException {

    // checkSessionHolder(requester);

    // transfer data to R, one column at a time
    for (int i = 0; i < insts.numAttributes(); i++) {
      Attribute att = insts.attribute(i);

      if (att.isNumeric() || nomToNumeric) {
        double[] d = new double[insts.numInstances()];
        for (int j = 0; j < insts.numInstances(); j++) {
          if (insts.instance(j).isMissing(i)) {
            d[j] = REXPDouble.NA;
          } else {
            d[j] = att.isNominal() ? Double.parseDouble(insts.instance(j).stringValue(i)) : insts.instance(j).value(i);
          }
        }
        session.assign(cleanse("v_" + att.name()), d);
      } else if (att.isNominal()) {
        int[] d = new int[insts.numInstances()];
        String[] labels = new String[att.numValues()];
        int[] levels = new int[att.numValues()];
        for (int j = 0; j < att.numValues(); j++) {
          labels[j] = cleanse(att.value(j));
          levels[j] = j;
        }
        for (int j = 0; j < insts.numInstances(); j++) {
          if (insts.instance(j).isMissing(i)) {
            d[j] = REXPInteger.NA;
          } else {
            d[j] = (int) insts.instance(j).value(i);
          }
        }
        session.assign(cleanse("v_" + att.name()), d);
        session.assign(cleanse("v_" + att.name() + "_labels"), labels);
        session.assign(cleanse("v_" + att.name() + "_levels"), levels);
        /*
         * System.err.println("Evaluating : " + quote(att.name() + "_factor") +
         * "=factor(" + quote(att.name()) + ",labels=" + quote(att.name() +
         * "_levels") + ")");
         */

        session.parseAndEval(
          cleanse( "v_" + att.name() + "_factor" ) + "=factor(" + cleanse( "v_" + att.name() ) + ",levels=" + cleanse(
            "v_" + att.name() + "_levels" ) + ",labels=" + cleanse( "v_" + att.name() + "_labels" ) + ")" );
      } else if (att.isString()) {
        String[] d = new String[insts.numInstances()];
        for (int j = 0; j < insts.numInstances(); j++) {
          if (insts.instance(j).isMissing(i)) {
            d[j] = ""; // doesn't seem to be a missing value constant in
                       // REXPString
          } else {
            d[j] = insts.instance(j).stringValue(i);
          }
        }
        session.assign(cleanse("v_" + att.name()), d);
      }
    }

    // create the named data frame from the column objects
    // and then clean up the workspace (remove column objects)

    // first try and remove any existing data frame
    session.parseAndEval("remove(" + frameName + ")");

    // create the frame
    StringBuffer temp = new StringBuffer();
    temp.append(frameName).append("=data.frame(");
    for (int i = 0; i < insts.numAttributes(); i++) {
      Attribute att = insts.attribute(i);

      if (att.isNumeric() || att.isString() || nomToNumeric) {
        temp.append("\"").append(cleanse(att.name())).append("\"" + "=").append(cleanse("v_" + att.name()));
      } else if (att.isNominal()) {
        temp.append("\"").append(cleanse(att.name())).append("\"" + "=")
          .append(cleanse("v_" + att.name() + "_factor"));
      }

      if (i < insts.numAttributes() - 1) {
        temp.append(",");
      }
    }
    temp.append(")");
    session.parseAndEval(temp.toString());

    // clean up column objects
    temp = new StringBuffer();
    temp.append("remove(");
    for (int i = 0; i < insts.numAttributes(); i++) {
      Attribute att = insts.attribute(i);

      if (att.isNumeric() || att.isString() || nomToNumeric) {
        temp.append(cleanse("v_" + att.name()));
      } else if (att.isNominal()) {
        temp.append(cleanse("v_" + att.name() + "_factor"));
      }

      if (i < insts.numAttributes() - 1) {
        temp.append(",");
      }
    }
    temp.append(")");

    // System.err.println("Executing: " + temp.toString());
    session.parseAndEval(temp.toString());
  }
  
}
