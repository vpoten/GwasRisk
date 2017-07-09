/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk

import weka.core.converters.ArffLoader
import weka.core.Instances
import weka.core.Instance
import weka.classifiers.Evaluation
import weka.classifiers.Classifier
import weka.classifiers.functions.SimpleLogistic
import weka.classifiers.bayes.NaiveBayesUpdateable
import weka.classifiers.functions.Logistic
import weka.classifiers.functions.LibSVM
import weka.classifiers.meta.AdaBoostM1
import weka.classifiers.trees.RandomForest
import weka.classifiers.trees.J48
import org.clados.gwasrisk.Utils
import org.clados.gwasrisk.weka.*
import org.clados.gwasrisk.parser.ResultsParser as RP

/**
 *
 * @author victor
 */
class Classify {
	
    ArffLoader trainSet
    ArffLoader testSet
    Evaluation evaluation
    Classifier classifier
    String clsName
    
    double alpha0 = 0.0
    double alpha1 = 1.0
    
    //performance measures constants
    public static final String areaUnderROC = 'areaUnderROC'
    public static final String truePositiveRate = 'truePositiveRate'
    public static final String trueNegativeRate = 'trueNegativeRate'
    public static final String falsePositiveRate = 'falsePositiveRate'
    public static final String falseNegativeRate = 'falseNegativeRate'
    public static final String precision = 'precision'
    public static final String pctCorrect = 'pctCorrect'
    public static final String weightedAreaUnderROC = 'weightedAreaUnderROC'
    public static final String weightedTruePositiveRate = 'weightedTruePositiveRate'
    public static final String weightedTrueNegativeRate = 'weightedTrueNegativeRate'
    public static final String weightedPrecision = 'weightedPrecision'
    
    // list of measures
    public static final MEASURES = [areaUnderROC, truePositiveRate, trueNegativeRate,
        falsePositiveRate, falseNegativeRate, precision, pctCorrect, weightedAreaUnderROC, 
        weightedTruePositiveRate, weightedTrueNegativeRate, weightedPrecision
    ]
    
    //classifier prefixes
    public static final String P_wGRS = 'wGRS:' //prefix for wGRS classifiers
    public static final String P_GRS = 'GRS:' //prefix for GRS classifiers
    public static final String P_eWGRS = 'eWGRS:' //prefix for ewGRS classifiers
    public static final String P_rScore = 'rScore:'//prefix for risk score classifiers
    public static final String P_allele = 'allele:'
    public static final String P_phased = 'phased:'
    public static final String P_G2 = 'g2:'
    
    public static final String NBC_class = NaiveBayesUpdateable.class.name///NaiveBayesLogProbUpdateable.class.name
    //classifier names
    public static final String CLS_SIMPLE_LOG = P_wGRS+SimpleLogistic.class.name
    public static final String CLS_SIMPLE_LOG_GRS = P_GRS+SimpleLogistic.class.name
    public static final String CLS_NBC = P_rScore+NBC_class
    public static final String CLS_FIXED_LOG = P_eWGRS+FixedLogistic.class.name
    public static final String CLS_SVM = P_rScore+LibSVM.class.name
    public static final String CLS_SVM_Sigmoid = P_rScore+LibSVM.class.name+'Sigmoid'
    public static final String CLS_BOOST = P_rScore+AdaBoostM1.class.name
    public static final String CLS_FOREST = P_rScore+RandomForest.class.name
    public static final String CLS_MULTI_LOG = P_rScore+Logistic.class.name
    public static final String CLS_C45 = P_rScore+J48.class.name
    public static final String CLS_BOOST_FOREST = P_rScore+AdaBoostM1.class.name+RandomForest.class.name
    public static final String CLS_BOOST_SVM = P_rScore+AdaBoostM1.class.name+LibSVM.class.name
    public static final String CLS_GLM_AIC = P_rScore+StepwiseGLMAic.class.name
  
    //list of classifiers
    static public final classifiersNames = [/*CLS_SIMPLE_LOG, CLS_SIMPLE_LOG_GRS,
        CLS_NBC, CLS_FIXED_LOG,*/ CLS_SVM, CLS_SVM_Sigmoid, CLS_FOREST, CLS_C45, CLS_BOOST_FOREST, CLS_BOOST_SVM /*, CLS_BOOST, CLS_MULTI_LOG*/]
    
    static public final snpsClasNames = [CLS_SIMPLE_LOG, CLS_SIMPLE_LOG_GRS,
        CLS_NBC, CLS_SVM, CLS_FOREST, CLS_C45, CLS_BOOST]
    
    //classifier names
    public static final String CLS_DENEUR_GT = P_phased+DENeural.class.name
    public static final String CLS_NBC_GT = P_phased+NBC_class
    public static final String CLS_FOREST_GT = P_phased+RandomForest.class.name
    public static final String CLS_SVM_GT = P_phased+LibSVM.class.name
    public static final String CLS_BOOST_GT = P_phased+AdaBoostM1.class.name
    public static final String CLS_C45_GT = P_phased+J48.class.name
    
    public static final String CLS_NBC_G2 = P_G2+NBC_class
    public static final String CLS_FOREST_G2 = P_G2+RandomForest.class.name
    public static final String CLS_SVM_G2 = P_G2+LibSVM.class.name
    public static final String CLS_BOOST_G2 = P_G2+AdaBoostM1.class.name
    public static final String CLS_C45_G2 = P_G2+J48.class.name
    
    public static final String CLS_NBC_ALE = P_allele+NBC_class
    public static final String CLS_FOREST_ALE = P_allele+RandomForest.class.name
    public static final String CLS_SVM_ALE = P_allele+LibSVM.class.name
    public static final String CLS_BOOST_ALE = P_allele+AdaBoostM1.class.name
    public static final String CLS_C45_ALE = P_allele+J48.class.name
    
    
    //classifiers for HLA region
    static public final hlaClasNames = [ CLS_NBC_G2, CLS_FOREST_G2, CLS_SVM_G2, /*CLS_BOOST_G2,*/
        CLS_C45_G2, CLS_NBC_GT, CLS_FOREST_GT, CLS_SVM_GT, /*CLS_BOOST_GT,*/ CLS_C45_GT, 
        CLS_NBC_ALE, CLS_FOREST_ALE, CLS_SVM_ALE, /*CLS_BOOST_ALE,*/ CLS_C45_ALE, 
        CLS_NBC, CLS_FOREST, CLS_SVM, /*CLS_BOOST,*/ CLS_C45]
    
    
    //classifiers for haplotypes
    ///static public final haploClasNames = [CLS_NBC_GT, CLS_NBC_ALE, CLS_NBC_G2, CLS_NBC]
    static public final haploClasNames = [CLS_NBC_G2]
    
    //classifiers for haplotypes: G2 classifier
    static public final g2ClasNames = [CLS_NBC_G2, CLS_FOREST_G2, CLS_SVM_G2, CLS_BOOST_G2,
        CLS_C45_G2]
    
    //classifiers for alleles
    static public final alleleClasNames = [CLS_NBC_ALE, CLS_FOREST_ALE, CLS_SVM_ALE, CLS_BOOST_ALE,
        CLS_C45_ALE]
    
    
    /**
     *
     */
    public void setClassifier(String pclsName, subtype=null){
        classifier = getByName(pclsName, subtype)
        clsName = pclsName
    }
    
    /**
     * get summary for current classifier and evaluation
     */
    String getSummary(){
        //print some statistics
        StringBuilder strb = new StringBuilder();
        //classifier info
        strb.append( "\n${RP.TOK_SUMM_CLASIF} ${clsName} ===\n" )
        strb.append( classifier.toString()+'\n' )
        
        //confusion matrix
        strb.append( evaluation.toMatrixString() )
        
        //auc values, TPR, FPR, ...
        strb.append( evaluation.toClassDetailsString() )
        
        //summary stats
        strb.append( evaluation.toSummaryString(false) )
        return strb.toString()
    }
    
    /**
     * returns a list of maps with several clasification measures, list[i] = map
     * for class i. The performance measures belongs to the current evaluation
     */
    def getMeasures(){
        def list = []
        
        (0..1).each{
            def map = [:]
            map[(areaUnderROC)] = evaluation.areaUnderROC(it)
            map[(truePositiveRate)] = evaluation.truePositiveRate(it)
            map[(trueNegativeRate)] = evaluation.trueNegativeRate(it)
            map[(falsePositiveRate)] = evaluation.falsePositiveRate(it)
            map[(falseNegativeRate)] = evaluation.falseNegativeRate(it)
            map[(precision)] = evaluation.precision(it)
            
            //measures that don't depend of the class
            map[(pctCorrect)] = evaluation.pctCorrect()
            map[(weightedAreaUnderROC)] = evaluation.weightedAreaUnderROC()
            map[(weightedTruePositiveRate)] = evaluation.weightedTruePositiveRate()
            map[(weightedTrueNegativeRate)] = evaluation.weightedTrueNegativeRate()
            map[(weightedPrecision)] = evaluation.weightedPrecision()
            
            list << map
        }
        
        return list
    }
    
    
    def loadTrainSet(arffFile){
        trainSet = loadDataset(new File(arffFile))
    }
    
    def loadTestSet(arffFile){
        testSet = loadDataset(new File(arffFile))
    }
    
    
    /**
     * get a new classifier by name
     */
    private Classifier getByName(clsName, subtype=null){
        if( clsName in [CLS_SIMPLE_LOG, CLS_SIMPLE_LOG_GRS] )
            return simpleLogistic()
        else if( clsName in [CLS_NBC, CLS_NBC_GT, CLS_NBC_ALE] )
            return naiveBayes()
        else if( clsName in [CLS_FIXED_LOG] )
            return fixedLogistic(alpha0, alpha1)
        else if( clsName in [CLS_SVM, CLS_SVM_Sigmoid, CLS_SVM_GT, CLS_SVM_ALE] )
            return libSVM()
            else if( clsName in [CLS_SVM_Sigmoid] )
            return libSVMSigmoid()
        else if( clsName in [CLS_MULTI_LOG] )
            return multiLogistic()
        else if( clsName in [CLS_FOREST] )
            return randomForest()
        else if( clsName in [CLS_FOREST_GT, CLS_FOREST_ALE] )
            return randomForestGT()
        else if( clsName in [CLS_BOOST, CLS_BOOST_GT, CLS_BOOST_ALE] )
            return adaBoost()
        else if( clsName in [CLS_BOOST_FOREST] )
            return adaBoostForest()
        else if( clsName in [CLS_BOOST_SVM] )
            return adaBoostSVM()
        else if( clsName in [CLS_C45, CLS_C45_GT, CLS_C45_ALE] )
            return c45Tree()
        else if( clsName in [CLS_DENEUR_GT] )
            return deNeural()
        else if( clsName in [CLS_NBC_G2, CLS_FOREST_G2, CLS_SVM_G2, CLS_BOOST_G2, CLS_C45_G2] )
            return genoTrios(clsName, subtype)
        else if ( clsName in [CLS_GLM_AIC])
            return glmAic()
        
        return null
    }
    
    private static Classifier glmAic(){
        def cls = new StepwiseGLMAic()
        return cls
    }
    
    private static Classifier simpleLogistic(){
        def cls = new SimpleLogistic()
        cls.setOptions(['-I', '0', '-M', '500', '-H', '50', '-W', '0.0'] as String[])
        return cls
    }
    
    private static Classifier naiveBayes(){
        def cls = new NaiveBayesUpdateable()
        ///def cls = new NaiveBayesLogProbUpdateable()
        cls.setDisplayModelInOldFormat(true)
        return cls
    }
    
    private static Classifier fixedLogistic(double inter, double slope){
        def cls =  new FixedLogistic()
        cls.setOptions(['-I', inter as String, '-S', slope as String] as String[])
        return cls
    }
    
    private static Classifier libSVM(){
        def cls = new LibSVM()
        //-K 2=RBF 3=Sigmoid
        cls.setOptions(['-K', '2'] as String[])
        return cls
    }
    
     private static Classifier libSVMSigmoid(){
        def cls = new LibSVM()
        //-K 2=RBF 3=Sigmoid
        cls.setOptions(['-K', '3'] as String[])
        return cls
    }
    
    private static Classifier multiLogistic(){
        return new Logistic()
    }
    
    private static Classifier randomForest(){
        def cls = new RandomForest()
        cls.setOptions(['-I', '20','-K','2','-depth','6'] as String[])
        return cls
    }
    
    private static Classifier randomForestGT(){
//        def cls = new RandomForest()
//        cls.setOptions(['-I', '500'] as String[])
//        return cls
        return randomForest()
    }
    
    private static Classifier adaBoost(){
        def cls = new AdaBoostM1()
        cls.setOptions(['-I', '2500','-W', 'weka.classifiers.trees.DecisionStump'] as String[])
        return cls
    }
    
        private static Classifier adaBoostForest(){
        def cls = new AdaBoostM1()
        cls.setOptions(['-I', '2500','-W', 'weka.classifiers.trees.RandomForest'] as String[])
        return cls
    }
    
          private static Classifier adaBoostSVM(){
        def cls = new AdaBoostM1()
        cls.setOptions(['-I', '2500','-W', 'weka.classifiers.functions.LibSVM'] as String[])
        return cls
    }
    
    private static Classifier c45Tree(){
        return new J48()
    }
    
    private static Classifier deNeural(){
        def cls = new DENeural()
        cls.setOptions(['-I', '10000','-N', '70','-P','3'] as String[])
        return cls
    }
    
    private static Classifier genoTrios(clsName, String gModel=null){
        def cls = new GenoTrios()
        def options = null
        
        if( clsName==CLS_NBC_G2 ){
            cls = new GenoTriosUpdateable()
            options = ['-classifier',NBC_class]
        }
        else if( clsName==CLS_FOREST_G2 ){
            //options = ['-classifier','weka.classifiers.trees.RandomForest','-I', '500']
            options = ['-classifier','weka.classifiers.trees.RandomForest','-I', '20','-K','2','-depth','6']
        }
        else if( clsName==CLS_SVM_G2 ){
            options = ['-classifier','weka.classifiers.functions.LibSVM','-K', '2']
        }
        else if( clsName==CLS_BOOST_G2 ){
            options = ['-classifier','weka.classifiers.meta.AdaBoostM1','-I', '2500',
                '-W', 'weka.classifiers.trees.DecisionStump']
        }
        else if( clsName==CLS_C45_G2 ){
            options = ['-classifier','weka.classifiers.trees.J48']
        }
        
        if( gModel ){
            options << ('-'+GenoTrios.GMODEL_OPT)
            options << gModel
        }
        
        cls.setOptions(options as String[])
        return cls
    }
    
    /**
     * returns true if the classifier implements the Updateable interface
     */
    private boolean isUpdateable(){
        return (classifier.class.interfaces as List)?.any{
                it.name==weka.classifiers.UpdateableClassifier.class.name
            }
    }
    
    /**
     * classify the test set using the given classifier and the training set for
     * model building
     */
    public double[] classify(){
        Instances trainData = null
        
        if( isUpdateable() )
            trainData = trainSet.getStructure()
        else
            trainData = trainSet.getDataSet()
            
        trainData.setClassIndex(trainData.numAttributes()-1)
        classifier.buildClassifier(trainData);
        
        if( isUpdateable() ){
            Instance current;
            while ((current = trainSet.getNextInstance(trainData)) != null)
                classifier.updateClassifier(current);
        }
        
        Instances testData = testSet.getDataSet()
        testData.setClassIndex(testData.numAttributes()-1)
   
        // evaluate classifier
        evaluation = new Evaluation(trainData)
        return evaluation.evaluateModel( classifier, testData )
    }
    
    /**
     * classify the training set using CV
     */
    public void classifyCV(int nfolds, StringBuffer output){
        Instances trainData = null
        trainData = trainSet.getDataSet()
            
        trainData.setClassIndex(trainData.numAttributes()-1)
        
        // evaluate classifier
        evaluation = new Evaluation(trainData)
        
        evaluation.crossValidateModel(classifier, trainData, nfolds, new java.util.Random(1))
        output << evaluation.toSummaryString()
        output << '\n'
        output << evaluation.toMatrixString()
        output << '\n'
        output << evaluation.toClassDetailsString()
    } 
    
    /**
     *
     */
    private def loadDataset(arffFile){
        ArffLoader loader = new ArffLoader()
        loader.setSource( Utils.createInputStream(arffFile) )
        return loader
    }
    
}

