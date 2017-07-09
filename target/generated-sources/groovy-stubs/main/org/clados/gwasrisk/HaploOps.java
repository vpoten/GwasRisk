package org.clados.gwasrisk;

import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;
import groovy.lang.*;
import groovy.util.*;

public class HaploOps
  extends java.lang.Object  implements
    groovy.lang.GroovyObject {
public HaploOps
() {}
public  java.lang.Object generatePatterns(java.lang.Object input, java.lang.Object output, java.lang.Object cutPval, java.lang.Object swSize) { return null;}
protected  java.util.Map generatePatterns(java.lang.String input, int hapLen, boolean parseHiLoSeqs) { return (java.util.Map)null;}
public static  java.lang.Object genPhaseFiles(java.lang.Object train, java.lang.Object test, java.lang.Object output, int swSize) { return null;}
public static  boolean messPhase(java.lang.Object arffFile, java.lang.Object type, double messProb) { return (boolean)false;}
public  groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}
public  void setMetaClass(groovy.lang.MetaClass mc) { }
public  java.lang.Object invokeMethod(java.lang.String method, java.lang.Object arguments) { return null;}
public  java.lang.Object getProperty(java.lang.String property) { return null;}
public  void setProperty(java.lang.String property, java.lang.Object value) { }
public static  java.lang.String getFOLDER_HOLD() { return (java.lang.String)null;}
public static  java.lang.String getFOLDER_TRAINING() { return (java.lang.String)null;}
public static  java.lang.String getFOLDER_TRAINING_ALL() { return (java.lang.String)null;}
public static  java.lang.String getFOLDER_TEST() { return (java.lang.String)null;}
public static  int getTHREADS() { return (int)0;}
public static  java.lang.String getDAT_LBL() { return (java.lang.String)null;}
public static  java.lang.String getTES_LBL() { return (java.lang.String)null;}
public static  java.lang.String getPHENO_ATT_NAME() { return (java.lang.String)null;}
public static  java.lang.String getMESS_HAP_EXT() { return (java.lang.String)null;}
public static  java.lang.String getMESS_CHR_EXT() { return (java.lang.String)null;}
public static  java.lang.Object getRegexPou() { return null;}
public static  void setRegexPou(java.lang.Object value) { }
public static  java.lang.Object getRegexGou() { return null;}
public static  void setRegexGou(java.lang.Object value) { }
public static  java.lang.Object getRegexRs() { return null;}
public static  void setRegexRs(java.lang.Object value) { }
public static  java.lang.Object getRegexTrainHPed() { return null;}
public static  void setRegexTrainHPed(java.lang.Object value) { }
public static  java.lang.Object getRegexTestHPed() { return null;}
public static  void setRegexTestHPed(java.lang.Object value) { }
public static  java.lang.Object getTransformBases() { return null;}
public static  java.lang.Object getSeqRegex() { return null;}
public static  void setSeqRegex(java.lang.Object value) { }
public  java.lang.Object generatePatterns(java.lang.Object input, java.lang.Object output, java.lang.Object cutPval, java.lang.Object swSize, boolean use2GTree) { return null;}
public  java.lang.Object generatePatterns(java.lang.Object input, java.lang.Object output, java.lang.String chr, int start, int end) { return null;}
public  java.lang.Object generateTestPatterns(java.lang.Object input, java.lang.Object cutPval, java.lang.Object swSize) { return null;}
public  int getStartRow(java.lang.String set) { return (int)0;}
public static  java.lang.String patternId(java.lang.Object familyId, java.lang.Object indivId) { return (java.lang.String)null;}
public static  java.lang.String encodePheno(java.lang.Object gouCode) { return (java.lang.String)null;}
protected  java.lang.Object countInstances(java.lang.Object dir) { return null;}
protected  java.util.Map generatePatterns(java.lang.String input, int hapLen, boolean parseHiLoSeqs, boolean testOnly) { return (java.util.Map)null;}
public static  char recodeBase(java.lang.Object base, int swSize) { return (char)0;}
public static  java.lang.Object genPhaseFiles(java.lang.Object train, java.lang.Object test, java.lang.Object output, int swSize, java.lang.Object chrsToUse) { return null;}
public  java.lang.Object genArff(java.lang.Object patternsArr, java.lang.Object relation, java.lang.Object arffFile, java.lang.Object type) { return null;}
public static  boolean messPhase(java.lang.Object arffFile, java.lang.Object type, double messProb, boolean messHaplo) { return (boolean)false;}
protected  groovy.lang.MetaClass $getStaticMetaClass() { return (groovy.lang.MetaClass)null;}
}
