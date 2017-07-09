package org.clados.gwasrisk.weka;

import weka.core.Instance;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Utils;
import weka.core.Instances;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;
import groovy.lang.*;
import groovy.util.*;

public class FixedLogistic
  extends weka.classifiers.Classifier  implements
    groovy.lang.GroovyObject {
public FixedLogistic
() {}
public  groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}
public  void setMetaClass(groovy.lang.MetaClass mc) { }
public  java.lang.Object invokeMethod(java.lang.String method, java.lang.Object arguments) { return null;}
public  java.lang.Object getProperty(java.lang.String property) { return null;}
public  void setProperty(java.lang.String property, java.lang.Object value) { }
public  double getIntercept() { return (double)0;}
public  void setIntercept(double value) { }
public  double getSlope() { return (double)0;}
public  void setSlope(double value) { }
public  void buildClassifier(weka.core.Instances data) { }
public  double[] distributionForInstance(weka.core.Instance instance) { return (double[])null;}
public  void setOptions(java.lang.String[] options) { }
public  java.lang.String toString() { return (java.lang.String)null;}
public  java.lang.String getRevision() { return (java.lang.String)null;}
protected  groovy.lang.MetaClass $getStaticMetaClass() { return (groovy.lang.MetaClass)null;}
}
