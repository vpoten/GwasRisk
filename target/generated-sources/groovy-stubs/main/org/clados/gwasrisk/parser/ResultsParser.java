package org.clados.gwasrisk.parser;

import org.jfree.chart.*;
import org.jfree.ui.Layer;
import org.clados.gwasrisk.Main;
import java.awt.BasicStroke;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.renderer.category.MinMaxCategoryRenderer;
import java.awt.Stroke;
import org.jfree.chart.plot.ValueMarker;
import org.clados.gwasrisk.Utils;
import org.jfree.data.xy.DefaultXYDataset;
import org.clados.gwasrisk.Classify;
import java.awt.Color;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;
import groovy.lang.*;
import groovy.util.*;

public class ResultsParser
  extends java.lang.Object  implements
    groovy.lang.GroovyObject {
public static java.lang.String TOK_FOLD;
public static java.lang.String TOK_DATA_CUT_PVAL;
public static java.lang.String TOK_DATA_CUT_PVAL2;
public static java.lang.String TOK_SUMM_CLASIF;
public static java.lang.String TOK_FINAL_RES;
public static java.lang.String TOK_FINAL_CUT_PVAL;
public static java.lang.String TOK_FINAL_CLASIF;
public static java.lang.String TOK_FINAL_CLASS;
public ResultsParser
() {}
public  groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}
public  void setMetaClass(groovy.lang.MetaClass mc) { }
public  java.lang.Object invokeMethod(java.lang.String method, java.lang.Object arguments) { return null;}
public  java.lang.Object getProperty(java.lang.String property) { return null;}
public  void setProperty(java.lang.String property, java.lang.Object value) { }
public static  java.lang.Object getMeasureRegex() { return null;}
public static  void setMeasureRegex(java.lang.Object value) { }
public static  java.lang.Object getClassProbReg() { return null;}
public static  void setClassProbReg(java.lang.Object value) { }
public static  java.lang.Object getFixLogRegex() { return null;}
public static  void setFixLogRegex(java.lang.Object value) { }
public static  java.lang.Object getSimpLogRegInter() { return null;}
public static  void setSimpLogRegInter(java.lang.Object value) { }
public static  java.lang.Object getSimpLogRegSlop() { return null;}
public static  void setSimpLogRegSlop(java.lang.Object value) { }
public static  java.lang.Object getCorrectPctRegex() { return null;}
public static  void setCorrectPctRegex(java.lang.Object value) { }
public static  java.lang.Object getReverseComp() { return null;}
public static  void setReverseComp(java.lang.Object value) { }
public static  java.lang.String getTABLE_CSSCLASS() { return (java.lang.String)null;}
public static  void setTABLE_CSSCLASS(java.lang.String value) { }
public static  java.lang.String getHTML_OUT() { return (java.lang.String)null;}
public static  void setHTML_OUT(java.lang.String value) { }
public static  java.lang.String getTABLE_OUT() { return (java.lang.String)null;}
public static  void setTABLE_OUT(java.lang.String value) { }
public static  java.lang.String getRESULT_FOLDER() { return (java.lang.String)null;}
public static  void setRESULT_FOLDER(java.lang.String value) { }
public static  java.lang.String getCHART_FOLDER() { return (java.lang.String)null;}
public static  void setCHART_FOLDER(java.lang.String value) { }
public static  int getWIDTH() { return (int)0;}
public static  void setWIDTH(int value) { }
public static  int getHEIGHT() { return (int)0;}
public static  void setHEIGHT(int value) { }
public static  java.lang.Object getClassifNames() { return null;}
public static  void setClassifNames(java.lang.Object value) { }
public static  java.lang.Object getMeasureNames() { return null;}
public static  void setMeasureNames(java.lang.Object value) { }
public static  java.lang.Object getClassifColor() { return null;}
public static  void setClassifColor(java.lang.Object value) { }
public static  java.lang.Object getClassifStroke() { return null;}
public static  void setClassifStroke(java.lang.Object value) { }
public static  java.lang.Object getTABLE_STYLE_SHEET() { return null;}
public static  void setTABLE_STYLE_SHEET(java.lang.Object value) { }
public static  int doParse(java.lang.Object args) { return (int)0;}
public static  int doParseSnpArff(java.lang.Object args) { return (int)0;}
public static  int doParseHaploArff(java.lang.Object args) { return (int)0;}
protected  groovy.lang.MetaClass $getStaticMetaClass() { return (groovy.lang.MetaClass)null;}
}
