/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clados.gwasrisk.weka.jri;

/**
 * Exception for R-related problems.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 10426 $
 */
public class RSessionException extends Exception {
  public RSessionException(String message) {
    super(message);
  }
}
