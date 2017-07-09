package org.clados.gwasrisk.weka;

import org.clados.gwasrisk.metaheuristic.impl.*;
import org.clados.gwasrisk.neural.NNPattern;
import weka.core.Instance;
import org.clados.gwasrisk.metaheuristic.DifferentialEvolution;
import org.clados.gwasrisk.Main;
import org.clados.gwasrisk.neural.NeuralNetwork;
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

public class DENeural
  extends weka.classifiers.Classifier  implements
    groovy.lang.GroovyObject {
public DENeural
() {}
public  groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}
public  void setMetaClass(groovy.lang.MetaClass mc) { }
public  java.lang.Object invokeMethod(java.lang.String method, java.lang.Object arguments) { return null;}
public  java.lang.Object getProperty(java.lang.String property) { return null;}
public  void setProperty(java.lang.String property, java.lang.Object value) { }
public  org.clados.gwasrisk.neural.NeuralNetwork getNetwork() { return (org.clados.gwasrisk.neural.NeuralNetwork)null;}
public  void setNetwork(org.clados.gwasrisk.neural.NeuralNetwork value) { }
public  int getNumHid() { return (int)0;}
public  void setNumHid(int value) { }
public  int getIterations() { return (int)0;}
public  void setIterations(int value) { }
public  int getNP() { return (int)0;}
public  void setNP(int value) { }
public  int getThreads() { return (int)0;}
public  void setThreads(int value) { }
public  void buildClassifier(weka.core.Instances data) { }
public  double[] distributionForInstance(weka.core.Instance instance) { return (double[])null;}
public  void setOptions(java.lang.String[] options) { }
public  java.lang.String toString() { return (java.lang.String)null;}
public  java.lang.String getRevision() { return (java.lang.String)null;}
protected  groovy.lang.MetaClass $getStaticMetaClass() { return (groovy.lang.MetaClass)null;}
}
