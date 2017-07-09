package org.clados.gwasrisk.weka;

import weka.core.Instance;
import org.clados.gwasrisk.Main;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Utils;
import weka.core.Instances;
import weka.core.FastVector;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;
import groovy.lang.*;
import groovy.util.*;

public class GenoTrios
  extends weka.classifiers.Classifier  implements
    groovy.lang.GroovyObject {
protected weka.classifiers.Classifier internalClas;
protected weka.core.Instances dataset;
protected java.lang.Object decompPats;
protected java.lang.Object classCodes;
protected org.clados.gwasrisk.weka.GenoTrios.GeneticModel gModel;
protected boolean decomposeClass;
public GenoTrios
() {}
protected  java.lang.Object decomposePattern(weka.core.Instance instance) { return null;}
public  groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}
public  void setMetaClass(groovy.lang.MetaClass mc) { }
public  java.lang.Object invokeMethod(java.lang.String method, java.lang.Object arguments) { return null;}
public  java.lang.Object getProperty(java.lang.String property) { return null;}
public  void setProperty(java.lang.String property, java.lang.Object value) { }
public static  java.lang.String getGMODEL_OPT() { return (java.lang.String)null;}
public static  java.lang.Object getGMODEL_MAP() { return null;}
public  void buildClassifier(weka.core.Instances data) { }
public  double[] distributionForInstance(weka.core.Instance instance) { return (double[])null;}
public  void setOptions(java.lang.String[] options) { }
public  java.lang.String toString() { return (java.lang.String)null;}
public  java.lang.String getRevision() { return (java.lang.String)null;}
protected  java.lang.Object decomposePattern(weka.core.Instance instance, boolean newPatt) { return null;}
protected  groovy.lang.MetaClass $getStaticMetaClass() { return (groovy.lang.MetaClass)null;}
public enum GeneticModel
  implements
    groovy.lang.GroovyObject {
RECESSIVE, ADDITIVE, DOMINANT;
public static org.clados.gwasrisk.weka.GenoTrios.GeneticModel MIN_VALUE;
public static org.clados.gwasrisk.weka.GenoTrios.GeneticModel MAX_VALUE;
public  groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}
public  void setMetaClass(groovy.lang.MetaClass mc) { }
public  java.lang.Object invokeMethod(java.lang.String method, java.lang.Object arguments) { return null;}
public  java.lang.Object getProperty(java.lang.String property) { return null;}
public  void setProperty(java.lang.String property, java.lang.Object value) { }
public  org.clados.gwasrisk.weka.GenoTrios.GeneticModel next() { return (org.clados.gwasrisk.weka.GenoTrios.GeneticModel)null;}
public  org.clados.gwasrisk.weka.GenoTrios.GeneticModel previous() { return (org.clados.gwasrisk.weka.GenoTrios.GeneticModel)null;}
public static  org.clados.gwasrisk.weka.GenoTrios.GeneticModel $INIT(java.lang.Object[] para) { return (org.clados.gwasrisk.weka.GenoTrios.GeneticModel)null;}
protected  groovy.lang.MetaClass $getStaticMetaClass() { return (groovy.lang.MetaClass)null;}
}
}
