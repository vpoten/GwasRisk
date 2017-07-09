/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk

import org.clados.gwasrisk.parser.ResultsParser as RP

/**
 * Use SNPs of HLA region.
 * chr6, from 30736061 to 33163225
 * 
 * @author victor
 */
class HLAClassif {
    static Matrix gMatrix = new Matrix(0,0) //global matrix, used in every iteration
    
    static final int LOW_LIMIT = 30736061
    static final int HIGH_LIMIT = 33163225
    
    
    /**
     *
     */
    static int perform(args){
        String input = args.find{ it.startsWith(Main.OPT_INPUT_DIR) }
        String output = args.find{ it.startsWith(Main.OPT_OUTPUT_DIR) }
        
        def dirs = Main.checkAndCleanDirOpts([input, output])
        
        def error = {
            System.err.println("Usages:\n"+Main.USA_HLA_CLAS)
            return 1 //error
        }
        
        if( !dirs )
            return error()
            
        input = dirs[0]
        output = dirs[1]
        
        println "Performing HLA classification."
        
        HaploOps.genPhaseFiles(input+HaploOps.FOLDER_TRAINING_ALL, input+HaploOps.FOLDER_TEST, output, 1, ['6'])
        def thresholds = [1.0]
        def measures = [:]
        def prefix = 'hla_'
        
        def mapGrsFiles = [(Classify.P_allele):'allele_pv', (Classify.P_phased):'phased_pv', 
            (Classify.P_rScore):'rs_pv', (Classify.P_G2):'phased_pv']
        
        thresholds.each{ cutPval->
            println "\n${RP.TOK_DATA_CUT_PVAL} ${cutPval} p-value cut (${new Date()})"
            
            measures[cutPval] = [:]
            def cutStr = (cutPval as String).substring(2)//decimal part of number

            def haploOps = new HaploOps(rAlleles:gMatrix)
            def patterns = haploOps.generatePatterns(input, output, '6', LOW_LIMIT, HIGH_LIMIT)
            
            mapGrsFiles.each{ type, subname->
                patterns.each{ suff, list-> //generate .arff files for tes and dat sets
                    haploOps.genArff( list, "${prefix}${cutPval}", "${output}${prefix}${subname}${cutStr}${suff}.arff", type)
                }
            }
            
            Classify.hlaClasNames.each{
                // get classifier by name and load the appropriate test and train set
                Classify cls = new Classify()
                cls.setClassifier(it)
                
                //get subname for the current classifier
                def subname = mapGrsFiles.find{ ele-> it.startsWith(ele.key) }.value
                
                cls.loadTestSet( "${output}${prefix}${subname}${cutStr}.tes.arff" )
                cls.loadTrainSet( "${output}${prefix}${subname}${cutStr}.dat.arff" )

                cls.classify()
                System.out.println( cls.getSummary() )
                measures[cutPval][it] = cls.getMeasures()
                cls = null
            }
        }
        
        FoldSimulation.printPerformance([measures], Classify.hlaClasNames, [1.0])
        
        return 0
    }
    
    
}

