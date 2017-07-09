/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk

import org.clados.gwasrisk.parser.ResultsParser as RP

/**
 *
 * @author victor
 */
class HaploArff {
    
    static def TEST_LABEL = 'Test'
    static def TRAIN_LABEL = 'TestForHaploRisk'

    static def thresholds = [0.15, 0.1, 0.05, 0.01, 1e-3, 1e-4, 1e-5, 1e-6, 1e-7]
    ///static def thresholds = [0.15] //thresholds for local debugging
    
    static def datasetRegex = /\w+_size(\d+)_thr([\w\.]+)_([a-zA-Z]+)\.arff[\.gz]*/
    
    /**
     * call example:
     * haploArffClas --input=/home/victor/Escritorio/WTCCC1/haplo_phased/arff/T1D --output=/home/victor/Escritorio/WTCCC1/haplo_phased/output --size=5 --mode=recessive
     */
    static int perform(args){
        String input = args.find{ it.startsWith(Main.OPT_INPUT_DIR) }
        String output = args.find{ it.startsWith(Main.OPT_OUTPUT_DIR) }
        String strSize = args.find{ it.startsWith(Main.OPT_SIZE) }
        String gModel = args.find{ it.startsWith(Main.OPT_MODE) }
        
        def dirs = Main.checkAndCleanDirOpts([input, output])
        
        def error = {
            System.err.println("Usages:\n"+Main.USA_HAPLOARFF_CLAS)
            return 1 //error
        }
        
        if( !dirs || !strSize || !gModel )
            return error()
            
        input = dirs[0]
        output = dirs[1]
        strSize = strSize.substring( strSize.indexOf('=')+1 ) as Integer
        gModel = gModel.substring( gModel.indexOf('=')+1 )
        
        println "Performing Haplotype arff classification."
        println "Genetic model: ${gModel}"//genetic models {'RECESSIVE','ADDITIVE','DOMINANT'}
        println "Haplotype size: ${strSize}"
        
        def measures = [:]
        def actualThr = []
        
        thresholds.each{ cutPval->
            // get the appropriate test and train set
            def testSet = getSet(input, strSize, cutPval, TEST_LABEL)
            def trainSet = getSet(input, strSize, cutPval, TRAIN_LABEL)
            
            if( testSet && trainSet ) {
                println "${RP.TOK_DATA_CUT_PVAL2}${cutPval}, datasets ${trainSet} and ${testSet}"
                measures[cutPval] = [:]
                actualThr << cutPval
                
                Classify.g2ClasNames.each{
                    Classify cls = new Classify()
                    cls.setClassifier(it, gModel)
                    cls.classifier.decomposeClass = false //modify GenoTrios behaviour

                    cls.loadTestSet(testSet)
                    cls.loadTrainSet(trainSet)

                    cls.classify()
                    System.out.println( cls.getSummary() )
                    measures[cutPval][it] = cls.getMeasures()
                    cls = null
                }
            }
        }
        
        FoldSimulation.printPerformance([measures], Classify.g2ClasNames, actualThr)
        
        return 0
    }
    
    static int performArffTrios(args){
        String input = args.find{ it.startsWith(Main.OPT_INPUT_DIR) }
        String strSize = args.find{ it.startsWith(Main.OPT_SIZE) }
        
        def dirs = Main.checkAndCleanDirOpts([input])
        
        def error = {
            System.err.println("Usages:\n"+Main.USA_HAPLOARFF_CLAS)
            return 1 //error
        }
        
        if( !dirs || !strSize )
            return error()
            
        input = dirs[0]
        strSize = strSize.substring( strSize.indexOf('=')+1 ) as Integer
        
        println "Performing Haplotype trios arff classification."
        println "Haplotype size: ${strSize}"
        
        def measures = [:]
        def actualThr = []
        
        [/*1.0*/0.05].each{ cutPval->
            // get the appropriate test and train set
            def testSet = getSet(input, strSize, cutPval, TEST_LABEL)
            def trainSet = getSet(input, strSize, cutPval, TRAIN_LABEL)
            
            if( testSet && trainSet ) {
                println "${RP.TOK_DATA_CUT_PVAL2}${cutPval}, datasets ${trainSet} and ${testSet}"
                measures[cutPval] = [:]
                actualThr << cutPval
                
                Classify.haploClasNames.each{
                    Classify cls = new Classify()
                    cls.setClassifier(it)

                    cls.loadTestSet(testSet)
                    cls.loadTrainSet(trainSet)

                    cls.classify()
                    System.out.println( cls.getSummary() )
                    measures[cutPval][it] = cls.getMeasures()
                    cls = null
                }
            }
        }
        
        FoldSimulation.printPerformance([measures], Classify.haploClasNames, actualThr)
        
        return 0
    }
    
    /**
     *
     */
    protected static getSet(String dir, String size, Double thr, String set){
        def files = new File(dir).list({d, f-> f ==~ datasetRegex } as FilenameFilter).toList()
        
        def file = files.find{ 
                def mat = (it =~ datasetRegex)[0]
                if( mat[2].toLowerCase()=='all' ){ (thr==1.0 && mat[1]==size && mat[3]==set) }
                else{ (mat[1]==size && (mat[2] as Double)==thr && mat[3]==set) }
            }
            
        
        return (file==null) ? null : dir+file
    }
    
}

