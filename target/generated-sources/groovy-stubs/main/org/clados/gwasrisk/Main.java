package org.clados.gwasrisk;

import org.clados.gwasrisk.parser.ResultsParser;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;
import groovy.lang.*;
import groovy.util.*;

public class Main
  extends java.lang.Object  implements
    groovy.lang.GroovyObject {
public static java.lang.String COMM_CREATE_PLINK;
public static java.lang.String COMM_ASSOC_TEST;
public static java.lang.String COMM_GEN_DATASET;
public static java.lang.String COMM_10F_SIMUL;
public static java.lang.String COMM_PARSE_RES;
public static java.lang.String COMM_TRIOS_CLAS;
public static java.lang.String COMM_HLA_CLAS;
public static java.lang.String COMM_TRIOSSNP_CLAS;
public static java.lang.String COMM_TRIOS10F;
public static java.lang.String COMM_TRIOSMESS_CLAS;
public static java.lang.String COMM_IMPUTE_PHAS;
public static java.lang.String COMM_IMPUTE_REF;
public static java.lang.String COMM_TRIOSTEST_CLAS;
public static java.lang.String COMM_HAPLOARFF_CLAS;
public static java.lang.String COMM_PARSE_HAPLOARFF;
public static java.lang.String COMM_SNPARFF_CLAS;
public static java.lang.String COMM_PARSE_SNPARFF;
public static java.lang.String OPT_INPUT_DIR;
public static java.lang.String OPT_OUTPUT_DIR;
public static java.lang.String OPT_CONTROL_DIR;
public static java.lang.String OPT_PLINK;
public static java.lang.String OPT_EXIST_TEST;
public static java.lang.String OPT_EXCL_KNOWN;
public static java.lang.String OPT_SNPS_LIST;
public static java.lang.String OPT_HAPLOTYPE;
public static java.lang.String OPT_SIZE;
public static java.lang.String OPT_2GTREE;
public static java.lang.String OPT_MESS_PHASE;
public static java.lang.String OPT_MESS_CHR;
public static java.lang.String OPT_IMPUTE;
public static java.lang.String OPT_HAPS;
public static java.lang.String OPT_LEGEND;
public static java.lang.String OPT_MAP;
public static java.lang.String OPT_MODE;
public static java.lang.Object COMMANDS;
public static java.lang.String USA_CREATE_PLINK;
public static java.lang.String USA_ASSOC_TEST;
public static java.lang.String USA_GEN_DATASET;
public static java.lang.String USA_10F_SIMUL;
public static java.lang.String USA_PARSE_RES;
public static java.lang.String USA_TRIOS_CLAS;
public static java.lang.String USA_TRIOSMESS_CLAS;
public static java.lang.String USA_HLA_CLAS;
public static java.lang.String USA_TRIOSSNP_CLAS;
public static java.lang.String USA_TRIOS10F;
public static java.lang.String USA_IMPUTE_PHAS;
public static java.lang.String USA_IMPUTE_REF;
public static java.lang.String USA_TRIOSTEST_CLAS;
public static java.lang.String USA_HAPLOARFF_CLAS;
public static java.lang.String USA_PARSE_HAPLOARFF;
public static java.lang.String USA_SNPARFF_CLAS;
public static java.lang.String affCode;
public static java.lang.String unaffCode;
public static java.lang.Object THRESHOLDS;
public static java.lang.Object THRESHOLDS_TRIOS;
public static java.lang.String filtOpts;
public static int PAT_SIZE;
public static int NFOLD;
public static double orCutoff;
public static int THREADS;
public Main
() {}
public static  java.lang.Object readExcluded(java.lang.String dir) { return null;}
public static  java.lang.Object readKnownSnps(java.lang.String dir) { return null;}
public static  java.lang.Object createPedFile(java.lang.Object gouPath, java.lang.Object contGouPath, java.lang.Object outPedPath) { return null;}
public static  java.lang.Object createMapFile(java.lang.Object input, java.lang.Object output) { return null;}
public static  int assocTest(java.lang.Object args, java.lang.String pFiltOpts) { return (int)0;}
public static  java.lang.Object genDatasetsAndClassify(java.lang.String output, java.util.Set options) { return null;}
public  groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}
public  void setMetaClass(groovy.lang.MetaClass mc) { }
public  java.lang.Object invokeMethod(java.lang.String method, java.lang.Object arguments) { return null;}
public  java.lang.Object getProperty(java.lang.String property) { return null;}
public  void setProperty(java.lang.String property, java.lang.Object value) { }
public static  java.lang.Object getRegexMap() { return null;}
public static  void setRegexMap(java.lang.Object value) { }
public static  java.lang.Object getRegexDatPed() { return null;}
public static  void setRegexDatPed(java.lang.Object value) { }
public static  java.lang.Object getRegexTesPed() { return null;}
public static  void setRegexTesPed(java.lang.Object value) { }
public static  Matrix getgMatrix() { return (Matrix)null;}
public static  void setgMatrix(Matrix value) { }
public static  void main(java.lang.String[] args) { }
public static  java.lang.Object checkAndCleanDirOpts(java.lang.Object list) { return null;}
public static  java.lang.Object readExcluded(java.lang.String dir, int prefLen) { return null;}
public static  java.lang.Object readKnownSnps(java.lang.String dir, int distance) { return null;}
public static  java.lang.Object readExcludedSnps(java.lang.String dir) { return null;}
public static  java.lang.Object createPedFile(java.lang.Object gouPath, java.lang.Object contGouPath, java.lang.Object outPedPath, java.lang.Object exclusionSet) { return null;}
public static  java.lang.Object createMapFile(java.lang.Object input, java.lang.Object output, java.lang.Object excludedSnps) { return null;}
public static  int assocTest(java.lang.Object args, java.lang.String pFiltOpts, java.lang.Object testList) { return (int)0;}
public static  java.lang.Object genDatasetsAndClassify(java.lang.String output, java.util.Set options, java.util.List snpList) { return null;}
protected  groovy.lang.MetaClass $getStaticMetaClass() { return (groovy.lang.MetaClass)null;}
}
