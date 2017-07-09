/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk

import org.clados.gwasrisk.parser.ResultsParser as RP

/**
 *
 * @author victor
 */
class SnpArff {
    static def TEST_LABEL = 'Test'
    static def TRAIN_LABEL = 'Training'

    static def thresholds = [0.15, 0.1, 0.05, 0.01, 1e-3, 1e-4, 1e-5, 1e-6, 1e-7]
    ///static def thresholds = [1e-5, 1e-6, 1e-7] //thresholds for local debugging
    
    static def datasetRegex = /\w+_size(\d+)_thr([\w\.]+)_([a-zA-Z]+)\.([a-zA-Z]+)\.arff[\.gz]*/
    
    /**
     * call example:
     * snpArffClas --input=/home/victor/Escritorio/WTCCC1/pedToArff/out/T1D/
     */
    static int perform(args){
        String input = args.find{ it.startsWith(Main.OPT_INPUT_DIR) }
        String mode = args.find{ it.startsWith(Main.OPT_MODE) }
        
        def dirs = Main.checkAndCleanDirOpts([input])
        
        def error = {
            System.err.println("Usages:\n"+Main.USA_SNPARFF_CLAS)
            return 1 //error
        }
        
        if( !dirs )
            return error()
            
        input = dirs[0]
        
        if( mode ){
            mode = mode.substring( mode.indexOf('=')+1 )
        }
        
        println "Performing SNP arff classification."
        
        def measures = [:]
        def actualThr = []
        
        def mapGrsFiles = [(Classify.P_wGRS):'wgrs', (Classify.P_GRS):'grs', (Classify.P_rScore):'rs']
        def clasNames = Classify.snpsClasNames
        
        if( mode.startsWith('alle') ) {
            println "Mode allele classification selected"
            mapGrsFiles = [(Classify.P_allele):'allele']
            clasNames = Classify.alleleClasNames
        }
        
        thresholds.each{ cutPval->
            def testSet = getSet(input, '1', cutPval, TEST_LABEL, 'rs')
            def trainSet = getSet(input, '1', cutPval, TRAIN_LABEL, 'rs')
            
            if( mode.startsWith('alle') ) {
                testSet = getSet(input, '1', cutPval, TEST_LABEL, 'allele')
                trainSet = getSet(input, '1', cutPval, TRAIN_LABEL, 'allele')
            }
            
            if( testSet && trainSet ) {
                println "${RP.TOK_DATA_CUT_PVAL2}${cutPval}, datasets ${trainSet} and ${testSet}"
                measures[cutPval] = [:]
                actualThr << cutPval
                    
                clasNames.each{ name->
                    //get subname for the current classifier
                    def subname = mapGrsFiles.find{ ele-> name.startsWith(ele.key) }.value

                    // get the appropriate test and train set
                    testSet = getSet(input, '1', cutPval, TEST_LABEL, subname)
                    trainSet = getSet(input, '1', cutPval, TRAIN_LABEL, subname)

                    // get classifier by name
                    Classify cls = new Classify()
                    cls.setClassifier(name)

                    cls.loadTestSet(testSet)
                    cls.loadTrainSet(trainSet)

                    cls.classify()
                    System.out.println( cls.getSummary() )
                    measures[cutPval][name] = cls.getMeasures()
                }
            }
        }
        
        FoldSimulation.printPerformance([measures], clasNames, actualThr)
        
        return 0
    }
    
    /**
     *
     */
    protected static getSet(String dir, String size, Double thr, String set, String type){
        def files = new File(dir).list({d, f-> f ==~ datasetRegex } as FilenameFilter).toList()
        
        def file = files.find{ 
                def mat = (it =~ datasetRegex)[0]
                if( mat[2].toLowerCase()=='all' ){ return false }
                (mat[1]==size && (mat[2] as Double)==thr && mat[3]==set && mat[4]==type)
            }
            
        return (file==null) ? null : dir+file
    }
    
}
