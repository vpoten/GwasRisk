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
class Trios {
    static Matrix gMatrix = new Matrix(0,0) //global matrix, used in every iteration
    
    static final String HAPLO_PREFIX = 'haplotypes_'
	
    /**
     *
     */
    static int perform(args){
        String input = args.find{ it.startsWith(Main.OPT_INPUT_DIR) }
        String output = args.find{ it.startsWith(Main.OPT_OUTPUT_DIR) }
        String strSize = args.find{ it.startsWith(Main.OPT_SIZE) }
        def _2gtree = (args.find{ it.startsWith(Main.OPT_2GTREE) }) ? true : false
        
        def dirs = Main.checkAndCleanDirOpts([input, output])
        
        def error = {
            System.err.println("Usages:\n"+Main.USA_TRIOS_CLAS)
            return 1 //error
        }
        
        if( !dirs || !strSize )
            return error()
            
        input = dirs[0]
        output = dirs[1]
        int swSize = strSize.substring( strSize.indexOf('=')+1 ) as Integer
        def prefix = HAPLO_PREFIX
        
        def thresholds = Main.THRESHOLDS_TRIOS
        def measures = [:]
        
        println "Performing trios haplotype classification."
        println "sWindow size = ${swSize}"
        
        if( _2gtree )
            println "Using 2GTree"
        
        HaploOps.genPhaseFiles(input+HaploOps.FOLDER_TRAINING, input+HaploOps.FOLDER_TEST, output, swSize)
        
        ///def mapGrsFiles = [(Classify.P_allele):'allele_pv', (Classify.P_phased):'phased_pv', 
        ///    (Classify.P_rScore):'rs_pv', (Classify.P_G2):'phased_pv']
        def mapGrsFiles = [(Classify.P_G2):'phased_pv']
        
        // generate datasets for each cut value
        thresholds.each{ cutPval->
            println "\n${RP.TOK_DATA_CUT_PVAL} ${cutPval} p-value cut (${new Date()})"
            
            measures[cutPval] = [:]
            def cutStr = (cutPval as String).substring(2)//decimal part of number

            def haploOps = new HaploOps(rAlleles:gMatrix)
            def patterns = haploOps.generatePatterns(input, output, cutPval, swSize, _2gtree)

            mapGrsFiles.each{ type, subname->
                patterns.each{ suff, list-> //generate .arff files for tes and dat sets
                    haploOps.genArff( list, "${prefix}${cutPval}", "${output}${prefix}${subname}${cutStr}_${swSize}${suff}.arff", type)
                }
            }
            
            Classify.haploClasNames.each{
                // get classifier by name and load the appropriate test and train set
                Classify cls = new Classify()
                cls.setClassifier(it)
                
                //get subname for the current classifier
                def subname = mapGrsFiles.find{ ele-> it.startsWith(ele.key) }.value
                
                cls.loadTestSet( "${output}${prefix}${subname}${cutStr}_${swSize}.tes.arff" )
                cls.loadTrainSet( "${output}${prefix}${subname}${cutStr}_${swSize}.dat.arff" )

                cls.classify()
                System.out.println( cls.getSummary() )
                measures[cutPval][it] = cls.getMeasures()
                cls = null
            }
        }
        
        FoldSimulation.printPerformance([measures], Classify.haploClasNames, thresholds)
        
        return 0
    }
     
    /**
     *
     */
    static int performMessOrIndepClass(args, boolean indepClas){
        String output = args.find{ it.startsWith(Main.OPT_OUTPUT_DIR) }
        String input = args.find{ it.startsWith(Main.OPT_INPUT_DIR) }
        def messPhase = (args.find{ it.startsWith(Main.OPT_MESS_PHASE) }) ? true : false
        def messChr = (args.find{ it.startsWith(Main.OPT_MESS_CHR) }) ? true : false
        String strSize = args.find{ it.startsWith(Main.OPT_SIZE) }
        
        def dirs = [output]
        if( indepClas ){ dirs << input }
        
        dirs = Main.checkAndCleanDirOpts(dirs)
        
        def error = {
            if( indepClas )
                System.err.println("Usages:\n"+Main.USA_TRIOSTEST_CLAS)
            else
                System.err.println("Usages:\n"+Main.USA_TRIOSMESS_CLAS)
            return 1 //error
        }
        
        if( !dirs || !strSize )
            return error()
            
        output = dirs[0]
        int swSize = strSize.substring( strSize.indexOf('=')+1 ) as Integer
        
        
        if( indepClas ){ 
            input = dirs[1] 
            println "Performing trios classif. for an independent test set."
            HaploOps.genPhaseFiles(null, input+HaploOps.FOLDER_TEST, input+HaploOps.FOLDER_TEST, swSize)
        }
        
        def thresholds = Main.THRESHOLDS_TRIOS
        def measures = [:]
        def mapGrsFiles = [(Classify.P_G2):'phased_pv']
        def prefix = HAPLO_PREFIX
        double messProb = 0.5
        
        // generate datasets for each cut value
        thresholds.each{ cutPval->
            println "\n${RP.TOK_DATA_CUT_PVAL} ${cutPval} p-value cut (${new Date()})"
            
            measures[cutPval] = [:]
            def cutStr = (cutPval as String).substring(2)//decimal part of number
           
            def haploOps = null
            def patterns = null
            
            if( indepClas ){
                // generate test set for indepClas
                haploOps = new HaploOps(rAlleles:gMatrix)
                patterns = haploOps.generateTestPatterns(input, cutPval, swSize)
            }
                
            mapGrsFiles.each{ type, subname->
                if( indepClas ){
                    patterns.each{ suff, list-> //generate .arff files for tes set (in input dir)
                        haploOps.genArff( list, "${prefix}${cutPval}", 
                            "${input+HaploOps.FOLDER_TEST}${prefix}${subname}${cutStr}_${swSize}${suff}.arff", type)
                    }
                }
                else {
                    // call messPhase
                    [HaploOps.TES_LBL].each{ suff->
                        String arffFile = "${output}${prefix}${subname}${cutStr}_${swSize}${suff}.arff"
                        HaploOps.messPhase(arffFile, type, messProb, !messChr)
                    }
                }
            }
            
            
            [Classify.CLS_NBC_G2].each{
                // get classifier by name and load the appropriate test and train set
                Classify cls = new Classify()
                cls.setClassifier(it)
                
                //get subname for the current classifier
                def subname = mapGrsFiles.find{ ele-> it.startsWith(ele.key) }.value
                
                if( indepClas ) {
                    cls.loadTestSet( "${input+HaploOps.FOLDER_TEST}${prefix}${subname}${cutStr}_${swSize}.tes.arff" )
                }
                else {
                    String messSuff = (!messChr) ? HaploOps.MESS_HAP_EXT : HaploOps.MESS_CHR_EXT
                    cls.loadTestSet( "${output}${prefix}${subname}${cutStr}_${swSize}.tes.arff${messSuff}" )
                }
                
                cls.loadTrainSet( "${output}${prefix}${subname}${cutStr}_${swSize}.dat.arff" )

                cls.classify()
                System.out.println( cls.getSummary() )
                measures[cutPval][it] = cls.getMeasures()
                cls = null
            }
        }
        
        FoldSimulation.printPerformance([measures], [Classify.CLS_NBC_G2], thresholds)
        
        return 0
    }
    
    /**
     *
     */
    static int performSnp(args, boolean is10Fold = false){
        String input = args.find{ it.startsWith(Main.OPT_INPUT_DIR) }
        String output = args.find{ it.startsWith(Main.OPT_OUTPUT_DIR) }
        String plink = args.find{ it.startsWith(Main.OPT_PLINK) }
        String haplotype = args.find{ it.startsWith(Main.OPT_HAPLOTYPE) }
        
        def dirs = Main.checkAndCleanDirOpts([input, output])
        
        def error = {
            System.err.println("Usages:\n"+Main.USA_TRIOSSNP_CLAS)
            return 1 //error
        }
        
        if( !dirs || !plink )
            return error()
            
        input = dirs[0]
        output = dirs[1]
        
        def measures = []
        
        //print some option settings
        println "Performing trios SNP classification."
        println "OR cutoff = ${Main.orCutoff}"
        
        if( haplotype )
            println "Performing haplotype classification."
        
        if( is10Fold ){
            int nfold = 10
            println "Performing 10 fold classification."
            
            String allSuff = '_all.ped'
            // create PED and MAP files using .gou,.pou and .rs
            pouToMap(input, output)
            gouToPed(input, output, allSuff)
            
            // separate cases and controls
            dividePed(output, allSuff)
            
            //repeat nfold times
            for(i in 1..nfold){
                println "\n${RP.TOK_FOLD} ${i} *****\n"

                //create .tes.ped and .dat.ped files
                FoldSimulation.generateTrainAndTest(output, nfold)
                
                def options = [] as Set
            
                if( haplotype ){
                    Main.assocTest([Main.OPT_INPUT_DIR+output, plink], '', 
                        ["--hap-logistic ${Main.filtOpts} ${FoldSimulation.hapOpts}"])

                    //phase ped files (for highest p-value cutoff)
                    GrsOps.genPhaseFiles(output, plink, Main.THRESHOLDS[0])

                    //add haplotype option
                    options << haplotype
                }
                else{
                    // performs association, missing and trend tests
                    Main.assocTest([Main.OPT_INPUT_DIR+output, plink], Main.filtOpts)
                }

                // generate arff files and classify; add measures to list
                measures << Main.genDatasetsAndClassify(output, options, null)
            }
        
        }
        else{
            // create PED and MAP files using .gou,.pou and .rs
            pouToMap(input+HaploOps.FOLDER_TEST, output)

            [ ('.tes.ped'):(input+HaploOps.FOLDER_TEST), 
                ('.dat.ped'):(input+HaploOps.FOLDER_TRAINING) ].each{ suff, dir->
                 gouToPed(dir, output, suff)
            }

            println "\n${RP.TOK_FOLD} 1 *****\n" //for parsing compatibility

            // performs association, missing and trend tests
            Main.assocTest([Main.OPT_INPUT_DIR+output, plink], Main.filtOpts)

            // generate arff files and classify; add measures to list
            def options = [] as Set
            measures << Main.genDatasetsAndClassify(output, options)
        }
        
        FoldSimulation.printPerformance(measures, Classify.classifiersNames, Main.THRESHOLDS)
        
        return 0
    }
    
    
    /**
     *
     */
    static pouToMap(String dir, String outdir){
        def regexPou = HaploOps.regexPou
        def regexRs = HaploOps.regexRs
        
        def files = new File(dir).list({d, f-> f ==~ regexPou } as FilenameFilter).toList()
        def rsFiles = new File(dir).list({d, f-> f ==~ regexRs } as FilenameFilter).toList()
        
        files.each{ file->
            def mat = (file =~ regexPou)
            def chr = mat[0][1]
            def fileRs = rsFiles.find{ (it =~ regexRs)[0][1]==chr }
            def fileMap = "triosSet_${chr}.map"
            
            println "Generating MAP ${outdir+fileMap} using ${dir+file} and ${dir+fileRs}"
            
            def reader = Utils.createReader( new File(dir+file) )
            def readerRs = Utils.createReader( new File(dir+fileRs) )
            def writer = new PrintWriter(outdir+fileMap)
            
            reader.eachLine{ pos->
                def rsId = readerRs.readLine()
                writer.println("${chr} ${rsId} 0 ${pos}")
            }
            
            writer.close()
            reader.close()
            readerRs.close()
        }
        
    }
    
    
    /**
     *
     */
    static gouToPed(String dir, String outdir, String suff){
        def regexGou = HaploOps.regexGou
        
        def files = new File(dir).list({d, f-> f ==~ regexGou } as FilenameFilter).toList()
        files = files.findAll{ (new File(dir+it).length()) > 0L }
        
        files.each{ file->
            def mat = (file =~ regexGou)
            def chr = mat[0][1]
            def filePed = "triosSet_${chr}${suff}"
            
            if( !(new File(outdir+filePed)).exists() ){
                println "Generating PED ${outdir+filePed} using ${dir+file}"

                def reader = Utils.createReader( new File(dir+file) )
                def writer = new PrintWriter(outdir+filePed)

                reader.splitEachLine("\\s"){ toks->
                    def line = new StringBuilder()
                    //remove the 7º field and change the 6º field by pheno
                    line <<  
            "${toks[0]} ${toks[1]} ${toks[2]} ${toks[3]} ${toks[4]} ${HaploOps.encodePheno(toks[5])}"

                    for(int i=7; i<toks.size(); i+=2){
                        def base1 = HaploOps.recodeBase(toks[i], 1)
                        def base2 = HaploOps.recodeBase(toks[i+1], 1)
                        
                        if( base1=='?' || base2=='?' ){
                            line << ' 0 0'
                        }
                        else{
                            line << ' ';line << base1
                            line << ' ';line << base2
                        }
                    }

                    writer.println( line.toString() )
                }

                writer.close()
                reader.close()
            }
            else{
                println "PED ${outdir+filePed} already exists"
            }
        }
    }
    
    /**
     * divide a group of ped files between cases and controls
     */
    static dividePed(String dir, inSuff){
        def files = new File(dir).list().toList().findAll{ it.endsWith(inSuff) }
        def regexPed = /(\w+)_(\d+)\w+\.ped/ //*_(chr)*.ped
        
        files.each{ file->
            def mat = (file=~regexPed)
            def pref = mat[0][1]
            def chr = mat[0][2]
            def reader = Utils.createReader( new File(dir+file) )
            
            def casesWrite = new FileWriter(dir+pref+'_case_'+chr+'.ped')
            def controlWrite = new FileWriter(dir+pref+'_control_'+chr+'.ped')
            println "Dividing ${file}"

            reader.eachLine{ line->
                def toks = line.split("\\s", 7)
                def writer = (toks[5]==Main.affCode) ? casesWrite : controlWrite 
                
                writer.write("${toks[0]} ${toks[1]} ${toks[2]} ${toks[3]} ${toks[4]} ${toks[5]} ")
                writer.write(toks[6])
                writer.write('\n')
            }
            
            reader.close()
            casesWrite.close()
            controlWrite.close()
            
            "rm -f ${dir+file}".execute().waitFor() //clean all subjects file
        }
    }
    
}

