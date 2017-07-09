package org.clados.gwasrisk.weka;

import org.rosuda.REngine.REXP;
import weka.core.Instance;
import org.clados.gwasrisk.weka.jri.RUtils;
import org.clados.gwasrisk.weka.jri.JRILoader;
import weka.classifiers.Classifier;
import weka.core.Instances;
import org.rosuda.REngine.REngine;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;
import groovy.lang.*;
import groovy.util.*;

public class StepwiseGLMAic
  extends weka.classifiers.Classifier  implements
    groovy.lang.GroovyObject {
public StepwiseGLMAic
() {}
public  groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}
public  void setMetaClass(groovy.lang.MetaClass mc) { }
public  java.lang.Object invokeMethod(java.lang.String method, java.lang.Object arguments) { return null;}
public  java.lang.Object getProperty(java.lang.String property) { return null;}
public  void setProperty(java.lang.String property, java.lang.Object value) { }
public  double[] getCoefficients() { return (double[])null;}
public  void setCoefficients(double[] value) { }
public  java.lang.Object getPredictors() { return null;}
public  void setPredictors(java.lang.Object value) { }
public  java.lang.Object getCoeffIndex() { return null;}
public  void setCoeffIndex(java.lang.Object value) { }
public  void buildClassifier(weka.core.Instances data) { }
public  double[] distributionForInstance(weka.core.Instance instance) { return (double[])null;}
public  void setOptions(java.lang.String[] options) { }
public  java.lang.String toString() { return (java.lang.String)null;}
public  java.lang.String getRevision() { return (java.lang.String)null;}
protected  groovy.lang.MetaClass $getStaticMetaClass() { return (groovy.lang.MetaClass)null;}
}
