/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.parser

import org.clados.gwasrisk.Classify

/**
 *
 * @author victor
 */
class ResultData {
    //available queries
    
    // return list of map with keys = {F_CUTPVAL,'1','2'}
    public static final String QUERY_MEASURE_TABLE = 'measureTable{classifier}{measure}{value?}'
    //return list of map with keys = {F_A0_NBC,F_A0_ML}
    public static final String QUERY_A0 = 'pointA0{cutoff}'
    //return list of map with keys = {F_A1_ML}
    public static final String QUERY_A1 = 'pointA1{cutoff}'
     
    //fields
    public static final String F_CUTPVAL = 'cutPval'
    public static final String F_A0_NBC = 'a0_NBC'
    public static final String F_A0_ML = 'a0_ML'
    public static final String F_A1_ML = 'a1_ML'
    
    
    public static final String FEAT_INTERCEPT = 'Intercept'
    public static final String FEAT_SLOPE = 'Slope'
    public static final String FEAT_PCT_CORR = 'RD.PctCorrect'
	
    //list of maps: [cutPval][classifier][measure] -> value
    def foldData
    
    //map [cutPval][classifier][classNum][measure]['AVG'/'MAX'/'MIN'] -> value
    def totalData
    
    //map [classNum] -> value
    def classProb
    
    /**
     *
     */
    def getClassifiers(){
        def set = [] as Set
        totalData.each{k,v-> set += v.keySet() }
        return set
    }
    
    /**
     *
     */
    def getCutoffVals(){
        return totalData.keySet()
    }
    
    /**
     *
     */
    boolean existMeasure(measure){
        return totalData[totalData.firstKey()].any{ clss->
            clss.value.any{ clsNum->
                clsNum.value.any{ measures->
                    measures.key==measure
                }
            }
        }
    }
    
    /**
     *
     */
    def calcPctCorrect(){
        def classifiers = getClassifiers()
        def cutoffs = getCutoffVals()
        
        cutoffs.each{ cutoff->
            classifiers.each{ classif->
                def values = foldData.collect{ it[cutoff][classif][FEAT_PCT_CORR] }
                def max = values.max()
                def min = values.min()
                def avg = values.sum()/((double)values.size())
                
                ['1','2'].each{ clsNum->
                    totalData[cutoff][classif][clsNum][FEAT_PCT_CORR] = [:]
                    totalData[cutoff][classif][clsNum][FEAT_PCT_CORR]['MAX'] = max
                    totalData[cutoff][classif][clsNum][FEAT_PCT_CORR]['MIN'] = min
                    totalData[cutoff][classif][clsNum][FEAT_PCT_CORR]['AVG'] = avg
                }
            }
        }
    }
    
    /**
     * returns a list of rows (maps).
     */
    def query(queryName, params){
        def reportRows = null

        if( queryName==QUERY_MEASURE_TABLE ){
            def classifier = params[0]
            def measure = params[1]
            def value = params[2] ?: 'AVG'
            reportRows = []
            
            totalData.each{ cutPval, clss->
                def map = [(F_CUTPVAL):cutPval]
                
                clss[classifier].each{ classNum, measures->
                    map[classNum] = (measures[measure]) ? measures[measure][value] : null
                }
                
                reportRows << map
            }
        }//end QUERY_MEASURE_TABLE
        else if( queryName==QUERY_A0 ){
            def cutoff = params[0]
            reportRows = []
            
            foldData.each{ cutOffs->
                def map = [:]
                map[F_A0_ML] = cutOffs[cutoff][Classify.CLS_SIMPLE_LOG][FEAT_INTERCEPT]
                map[F_A0_NBC] = cutOffs[cutoff][Classify.CLS_FIXED_LOG][FEAT_INTERCEPT]
                reportRows << map
            }
        }
        else if( queryName==QUERY_A1 ){
            def cutoff = params[0]
            reportRows = []
            
            foldData.each{ cutOffs->
                def map = [:]
                map[F_A1_ML] = cutOffs[cutoff][Classify.CLS_SIMPLE_LOG][FEAT_SLOPE]
                reportRows << map
            }
        }
            
        return reportRows
    }
    
}

