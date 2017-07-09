package org.clados.gwasrisk;

import org.clados.gwasrisk.weka.DiscreteEstimatorSimple;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;
import groovy.lang.*;
import groovy.util.*;

public class GrsOps
  extends java.lang.Object  implements
    groovy.lang.GroovyObject {
public GrsOps
() {}
protected  java.util.Map generatePatterns(java.lang.String input, boolean haplotype) { return (java.util.Map)null;}
protected  java.util.Map generatePatterns(java.lang.String input) { return (java.util.Map)null;}
public  groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}
public  void setMetaClass(groovy.lang.MetaClass mc) { }
public  java.lang.Object invokeMethod(java.lang.String method, java.lang.Object arguments) { return null;}
public  java.lang.Object getProperty(java.lang.String property) { return null;}
public  void setProperty(java.lang.String property, java.lang.Object value) { }
public  double getAlpha0() { return (double)0;}
public  void setAlpha0(double value) { }
public static  java.lang.Object getRegexDatHPed() { return null;}
public static  void setRegexDatHPed(java.lang.Object value) { }
public static  java.lang.Object getRegexTesHPed() { return null;}
public static  void setRegexTesHPed(java.lang.Object value) { }
public  java.lang.Object generatePatterns(java.lang.String input, double cutPval) { return null;}
public  java.lang.Object generatePatternsUnfilter(java.lang.String input, java.util.List snpList) { return null;}
public  java.lang.Object generateHapPatterns(java.lang.String input, double cutPval) { return null;}
protected  java.lang.Object countInstances(java.lang.Object dir) { return null;}
public static  java.lang.Object genPhaseFiles(java.lang.String input, java.lang.String plink, double cutPval) { return null;}
protected  int getNumSnps() { return (int)0;}
protected  java.util.Map generatePatterns(java.lang.String input, boolean haplotype, int hapLen) { return (java.util.Map)null;}
public  java.lang.Object genArff(java.util.Collection patterns, java.lang.Object relation, java.lang.Object arffFile, java.lang.String valGrs) { return null;}
public  java.lang.Object genArffRiskScores(java.util.Collection patterns, java.lang.Object relation, java.lang.Object arffFile) { return null;}
protected  groovy.lang.MetaClass $getStaticMetaClass() { return (groovy.lang.MetaClass)null;}
}
