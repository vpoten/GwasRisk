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
class FoldSimulation {
    
    static String hapOpts = '--hap-window 2 --mhf 0.1'
    
     static  boolean trainingTest=false
     
    static String id='TOK'

    static int perform(args, int nfold){
        
     
        if (nfold==1) 
        {
            trainingTest=true;
            nfold=2;
        }
        String input = args.find{ it.startsWith(Main.OPT_INPUT_DIR) }
        String output = args.find{ it.startsWith(Main.OPT_OUTPUT_DIR) }
        String control = args.find{ it.startsWith(Main.OPT_CONTROL_DIR) }
        String plink = args.find{ it.startsWith(Main.OPT_PLINK) }
        String exclKnown = args.find{ it.startsWith(Main.OPT_EXCL_KNOWN) }
        String snpFile = args.find{ it.startsWith(Main.OPT_SNPS_LIST) }
        String haplotype = args.find{ it.startsWith(Main.OPT_HAPLOTYPE) }
        
        
        def dirs = Main.checkAndCleanDirOpts([input, output, control])
        
        boolean QC=input.contains("QC");
        
         
        
        boolean tped=input.contains("tplink");
        
        def error = {
            if (trainingTest==false) System.err.println("Usages:\n"+Main.USA_10F_SIMUL)
                else System.err.println("Usages:\n"+Main.USA_TRAININGTEST_CLASS)
            return 1 //error
        }
        
        if(!dirs || !plink)
            return error()
            
        input = dirs[0]
        output = dirs[1]
        control = dirs[2]
        
        //print some option settings
        println "OR cutoff = ${Main.orCutoff}"
        
        if( haplotype )
            println "Performing haplotype classification."
        
        def excludedSnps = [] as Set
        def snpList = null
        
        if (!QC)
        if( snpFile ){
            snpFile = snpFile.substring( snpFile.indexOf('=')+1 )
            println "Reading SNPs list from: ${snpFile}"
            snpList = readSnps(snpFile)
        }
        else{
            //load excluded snps
            excludedSnps = Main.readExcludedSnps(control) as TreeSet

            if(exclKnown){// if exclude known snps
                println "Excluding known SNPs"
                def knownSnps = Main.readKnownSnps(input, 1000000)
                if( knownSnps!=null )
                    excludedSnps += knownSnps
                else
                    println "Known SNPs file not found in ${input}"
            }
        }
        
        //read excluded patterns
        def exclusionSet=[] as TreeSet
        if (!QC) // QC has to be performed 
        exclusionSet = (Main.readExcluded(input) + Main.readExcluded(control)) as TreeSet
        
            
        //generate case and controls ped and map files
        if (tped)
        Utils.runClosures( [{createPedFileFromTped(input, output, Main.affCode, exclusionSet, excludedSnps, plink)},
            {createPedFileFromTped(control, output, Main.unaffCode, exclusionSet, excludedSnps, plink)}], 2 )
        else
        if (!QC) // QC has to be performed
        {
                  def testList = ["--assoc --model"]
                     Main.assocTest([Main.OPT_INPUT_DIR+output, plink], '', testList)
                      Utils.runClosures( [{performQC(input, output, Main.affCode, exclusionSet, excludedSnps, plink)},
            {performQC(control, output, Main.unaffCode, exclusionSet, excludedSnps, plink)}], 2 )
      
        }
           
        def measures = []//list of performance measures
        
        //repeat nfold times
        for(i in 1..nfold)
        if (trainingTest==false || i==1) // only once for training/test approach
        {
            if (trainingTest==false)
            println "\n${RP.TOK_FOLD} ${i} *****\n"
            else println "\n'***** Performing Training/test computations *****\n"
                      
            //create .tes.ped and .dat.ped files
            generateTrainAndTest(output, nfold)
            
            def options = [] as Set
            
            if( haplotype ){
                Main.assocTest([Main.OPT_INPUT_DIR+output, plink], '', 
                    ["--hap-logistic ${Main.filtOpts} ${hapOpts}"])
                
                //phase ped files (for highest p-value cutoff)
                GrsOps.genPhaseFiles(output, plink, Main.THRESHOLDS[0])
                
                //add haplotype option
                options << haplotype
            }
            else if( snpList ) // a snp list is provided instead of using p values for cut-offs
            {
                Main.assocTest([Main.OPT_INPUT_DIR+output, plink], '', ['--assoc'])
            }
            else{
                if (!QC) // QC has to be performed
                // performs association, HWE, missing and trend tests
                Main.assocTest([Main.OPT_INPUT_DIR+output, plink], Main.filtOpts)
                else 
                {
                     def testList = ["--assoc ", "--model "]
                     Main.assocTest([Main.OPT_INPUT_DIR+output, plink], '', testList)
                }
            }
            
            // generate arff files and classify; add measures to list
            measures << Main.genDatasetsAndClassify(output, options, snpList)
        }
        
        printPerformance(measures, Classify.classifiersNames, snpList ? [1.0] : Main.THRESHOLDS)
        
        return 0
    }
    
    /**
     *  @param size : size of returned indexes set (< limit)
     *  @param limit : max index to include in set
     *  @return a Set of randomly generated integers between 0 and limit-1
     */
    static Set randomIndexSet( int size, int limit) {
        def set = [] as TreeSet
        while( set.size()<size ){
            set << (int)Math.floor(Math.random()*limit)
        }
        return set
    }
    
    /**
     * create .tes.ped and .dat.ped files
     * 
     */
    static generateTrainAndTest(output, int nfold){
                      
        def regexPed = /\w+_(\d+)\.ped/ //*_(chr).ped
        def filter = {d, f-> f ==~ regexPed } as FilenameFilter
        def pedFiles = new File(output).list(filter).toList()
        
        def chrs = pedFiles.collect{ (it=~regexPed)[0][1] } as Set
        def testIndex = null
        
        chrs.each{ chr->
            //get pair of case/control files for current chr
            def pair = pedFiles.findAll{ ((it=~regexPed)[0][1])==chr }.sort()
            //calculate the number of lines
            def nlines = pair.collect{ Utils.countLines(output+it) }
            
            if( !testIndex ) //generate indexes of test set selected lines randomly
                testIndex = nlines.collect{randomIndexSet((int)(it/nfold), it)} 
            
            String basename = "tempSet_${chr}"
            def writerTes = new BufferedWriter(new FileWriter(output+basename+'.tes.ped'))
            def writerDat = new BufferedWriter(new FileWriter(output+basename+'.dat.ped'))
            
            println "Generating train and test set using ${pair[0]} and ${pair[1]}"
            
            (0..1).each
            {
                    new File(output+pair[it]).eachLine{ line, num->
                    if( testIndex[it].contains(num-1) )
                        writerTes.writeLine(line)
                    else
                        writerDat.writeLine(line)
                }
            }
            
            writerTes.close()
            writerDat.close()
        }
        
    }
    
    /**
     *
     */
    static printPerformance(listM, listClassif, thresholds = null){
        println "\n${RP.TOK_FINAL_RES} *****\n"
        
        def format = { val-> String.format("%.4f",val) }
        def avg = { list-> list.sum()/(double)list.size() }
        
        if( !thresholds )
            thresholds = [1.0]// we dont need a p-value cutoff
            
        //for each pvalue thresholds
        thresholds.each{ cutPval->
            println "\n${RP.TOK_FINAL_CUT_PVAL} ${cutPval} =====\n"
            
            //for each classifier
            listClassif.each{ clsName->
                println "\n${RP.TOK_FINAL_CLASIF} ${clsName} -----\n"
                
                //for each class idx
                (0..1).each{ classIdx->
                    println "\n${RP.TOK_FINAL_CLASS} ${classIdx+1}:\n"
                    
                    Classify.MEASURES.each{ measure-> //print performance measures
                        def results = listM.collect{ it[cutPval][clsName][classIdx][measure] }
                        println "${measure}: MAX ${format(results.max())} MIN ${format(results.min())} AVG ${format(avg(results))}"
                    }
                }
            }
        }
    }
    
    /**
     * 
     * @param input dir
     * @param outpur dir
     * @param phenotype to set
     */
    private static createPedFileFromTped(input, output, phenoCode, exclusionSet, excludedSnps, plink){
        
        def transAndWrite = { line, writer, pheno ->
            // writes the line result of changing the 6º field by pheno
            def toks = line.split("\\s",7)
            if( exclusionSet!=null && !exclusionSet.contains(toks[1]) )
                writer.writeLine("${toks[0]} ${toks[1]} ${toks[2]} ${toks[3]} ${toks[4]} ${pheno} ${toks[6]}")
        }
        
        plink = plink.substring( plink.indexOf('=')+1 )
        def tpedFiles = new File(input).list({d, f-> f ==~ /\w+_(\d+)\.tped/ } as FilenameFilter).toList()
        def tfam = new File(input).list({d, f-> f ==~ /\w+\.tfam/ } as FilenameFilter).toList()[0]
        
        tpedFiles.each{ tped->
            def outTmp = output+tped+'.tmp'
            def outPed = output+tped.replace('.tped','.ped')
            def outMap = output+tped.replace('.tped','.map')
            
            //run plink --recode to generate tmp .ped and .map file
            String command = "${plink} --noweb --tped ${input+tped} --tfam ${input+tfam} --recode --out ${outTmp}"
            
            if( command.execute().waitFor() !=0 ){
                System.err.println("Error while performing plink --recode for ${tped}")
                System.exit(1)
            }
            
            //generate .ped file with phenotype
            def writer = new BufferedWriter(new FileWriter(outPed))

            println "Generating ${outPed} using ${outTmp+'.ped'}"
            
            def reader = Utils.createReader( new File(outTmp+'.ped') )
            reader.eachLine{ line ->
                transAndWrite(line, writer, phenoCode)
            }
            reader.close()

            writer.close()
            
            //create modified map file
            Main.createMapFile(outTmp+'.map', outMap, excludedSnps)

            // remove tmp .ped and .map files
            "rm -f ${outTmp}.ped ${outTmp}.map ${outTmp}.log".execute().waitFor()
        }
    }
        
    /**
     * 
     * @param input dir
     * @param outpur dir
     * @param phenotype to set
     */
    private static performQC (input, output, phenoCode, exclusionSet, excludedSnps, plink){
        
          
        def tasks = []
      def pedFiles = new File(input).list({d, f-> f ==~ regexDatPed } as FilenameFilter).toList()
        def mapFiles = new File(input).list({d, f-> f ==~ regexMap } as FilenameFilter).toList()[0]
        def String test="--test-missing ${Main.pFiltOpts} --recode"
        pedFiles.each{ ped->
            def outTmp = output+ped+'.tmp'
            def outPed = output+ped.replace('.ped','QC.ped')
            def outMap = output+ped.replace('.ped','QC.map')
             tasks << plinkTest(plink, test, input+pedName, input+mapName, input+outName)
            //run plink --recode to generate tmp .ped and .map file
        }
        
         Utils.runCommands( tasks, (pedFiles.size()==1) ? 1 : Main.THREADS)
              
                
           

            // remove tmp .ped and .map files
          //  "rm -f ${outTmp}.ped ${outTmp}.map ${outTmp}.log".execute().waitFor()
        }
    
    
    
    /**
     * Reads SNPs in known snps format:
     * fields: chrom, coord, rs_id, aff_id
     * Set 0 to unknown fields
     * 
     * @return a list of aff_ids
     */
    private static readSnps(String file){
        def snps = []
        
        new File(file).splitEachLine("\\s"){toks->
            // fields: chrom, coord, rs_id, aff_id
            if( toks[3]!='0' )
                snps << toks[3]
        }
        
        return snps
    }
    
    
}

