package org.clados.gwasrisk;

import org.clados.gwasrisk.weka.*;
import weka.core.Instance;
import weka.classifiers.trees.J48;
import weka.classifiers.Classifier;
import org.clados.gwasrisk.parser.ResultsParser;
import org.clados.gwasrisk.Utils;
import weka.core.Instances;
import weka.classifiers.functions.Logistic;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.Evaluation;
import weka.core.converters.ArffLoader;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.trees.RandomForest;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;
import groovy.lang.*;
import groovy.util.*;

public class Classify
  extends java.lang.Object  implements
    groovy.lang.GroovyObject {
public static java.lang.String areaUnderROC;
public static java.lang.String truePositiveRate;
public static java.lang.String trueNegativeRate;
public static java.lang.String falsePositiveRate;
public static java.lang.String falseNegativeRate;
public static java.lang.String precision;
public static java.lang.String pctCorrect;
public static java.lang.String weightedAreaUnderROC;
public static java.lang.String weightedTruePositiveRate;
public static java.lang.String weightedTrueNegativeRate;
public static java.lang.String weightedPrecision;
public static java.lang.Object MEASURES;
public static java.lang.String P_wGRS;
public static java.lang.String P_GRS;
public static java.lang.String P_eWGRS;
public static java.lang.String P_rScore;
public static java.lang.String P_allele;
public static java.lang.String P_phased;
public static java.lang.String P_G2;
public static java.lang.String NBC_class;
public static java.lang.String CLS_SIMPLE_LOG;
public static java.lang.String CLS_SIMPLE_LOG_GRS;
public static java.lang.String CLS_NBC;
public static java.lang.String CLS_FIXED_LOG;
public static java.lang.String CLS_SVM;
public static java.lang.String CLS_SVM_Sigmoid;
public static java.lang.String CLS_BOOST;
public static java.lang.String CLS_FOREST;
public static java.lang.String CLS_MULTI_LOG;
public static java.lang.String CLS_C45;
public static java.lang.String CLS_BOOST_FOREST;
public static java.lang.String CLS_BOOST_SVM;
public static java.lang.String CLS_GLM_AIC;
public static java.lang.Object classifiersNames;
public static java.lang.Object snpsClasNames;
public static java.lang.String CLS_DENEUR_GT;
public static java.lang.String CLS_NBC_GT;
public static java.lang.String CLS_FOREST_GT;
public static java.lang.String CLS_SVM_GT;
public static java.lang.String CLS_BOOST_GT;
public static java.lang.String CLS_C45_GT;
public static java.lang.String CLS_NBC_G2;
public static java.lang.String CLS_FOREST_G2;
public static java.lang.String CLS_SVM_G2;
public static java.lang.String CLS_BOOST_G2;
public static java.lang.String CLS_C45_G2;
public static java.lang.String CLS_NBC_ALE;
public static java.lang.String CLS_FOREST_ALE;
public static java.lang.String CLS_SVM_ALE;
public static java.lang.String CLS_BOOST_ALE;
public static java.lang.String CLS_C45_ALE;
public static java.lang.Object hlaClasNames;
public static java.lang.Object haploClasNames;
public static java.lang.Object g2ClasNames;
public static java.lang.Object alleleClasNames;
public Classify
() {}
public  void setClassifier(java.lang.String pclsName) { }
public  groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}
public  void setMetaClass(groovy.lang.MetaClass mc) { }
public  java.lang.Object invokeMethod(java.lang.String method, java.lang.Object arguments) { return null;}
public  java.lang.Object getProperty(java.lang.String property) { return null;}
public  void setProperty(java.lang.String property, java.lang.Object value) { }
public  weka.core.converters.ArffLoader getTrainSet() { return (weka.core.converters.ArffLoader)null;}
public  void setTrainSet(weka.core.converters.ArffLoader value) { }
public  weka.core.converters.ArffLoader getTestSet() { return (weka.core.converters.ArffLoader)null;}
public  void setTestSet(weka.core.converters.ArffLoader value) { }
public  weka.classifiers.Evaluation getEvaluation() { return (weka.classifiers.Evaluation)null;}
public  void setEvaluation(weka.classifiers.Evaluation value) { }
public  weka.classifiers.Classifier getClassifier() { return (weka.classifiers.Classifier)null;}
public  void setClassifier(weka.classifiers.Classifier value) { }
public  java.lang.String getClsName() { return (java.lang.String)null;}
public  void setClsName(java.lang.String value) { }
public  double getAlpha0() { return (double)0;}
public  void setAlpha0(double value) { }
public  double getAlpha1() { return (double)0;}
public  void setAlpha1(double value) { }
public  void setClassifier(java.lang.String pclsName, java.lang.Object subtype) { }
public  java.lang.String getSummary() { return (java.lang.String)null;}
public  java.lang.Object getMeasures() { return null;}
public  java.lang.Object loadTrainSet(java.lang.Object arffFile) { return null;}
public  java.lang.Object loadTestSet(java.lang.Object arffFile) { return null;}
public  double[] classify() { return (double[])null;}
public  void classifyCV(int nfolds, java.lang.StringBuffer output) { }
protected  groovy.lang.MetaClass $getStaticMetaClass() { return (groovy.lang.MetaClass)null;}
}
