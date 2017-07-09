package org.clados.gwasrisk;

import org.clados.gwasrisk.parser.ResultsParser as RP

/**
 * Class that performs the GWAS risk prediction tasks
 *
 */
public class Main 
{
    //commands
    public static final String COMM_CREATE_PLINK = "plinkFiles"
    public static final String COMM_ASSOC_TEST = "assocTest"
    public static final String COMM_GEN_DATASET = "genDataset"
    public static final String COMM_10F_SIMUL = "10foldSimul"
    public static final String COMM_PARSE_RES = "parseResults"
    public static final String COMM_TRIOS_CLAS = "triosClas"
    public static final String COMM_HLA_CLAS = "hlaClas"
    public static final String COMM_TRIOSSNP_CLAS = "triosSnpClas"
    public static final String COMM_TRIOS10F = "trios10fold"
    public static final String COMM_TRIOSMESS_CLAS = "triosMessClas"
    public static final String COMM_IMPUTE_PHAS = "imputePhasing"
    public static final String COMM_IMPUTE_REF = "imputeRef"
    public static final String COMM_TRIOSTEST_CLAS = "triosTestClas"
    public static final String COMM_HAPLOARFF_CLAS = "haploArffClas"
    public static final String COMM_PARSE_HAPLOARFF = "parseHaploArff"
    public static final String COMM_SNPARFF_CLAS = "snpArffClas"
    public static final String COMM_PARSE_SNPARFF = "parseSnpArff"
    
    //options
    public static final String OPT_INPUT_DIR = "--input="
    public static final String OPT_OUTPUT_DIR = "--output="
    public static final String OPT_CONTROL_DIR = "--control="
    public static final String OPT_PLINK = "--plink="
    public static final String OPT_EXIST_TEST = "--exist-test" // (genDataset) dont repeat plink tests
    public static final String OPT_EXCL_KNOWN = "--excl-known" // (10foldSimul) exclude known snps
    public static final String OPT_SNPS_LIST = "--snps-list=" // (10foldSimul) use snps in file to classify
    public static final String OPT_HAPLOTYPE = "--haplotype" // (10foldSimul) haplotype classification
    public static final String OPT_SIZE = "--size="
    public static final String OPT_2GTREE = "--2gtree"
    public static final String OPT_MESS_PHASE = "--messPhase" //mess phase (triosClas)
    public static final String OPT_MESS_CHR = "--messChr" //mess chr phase (triosClas)
    public static final String OPT_IMPUTE = "--impute="
    public static final String OPT_HAPS = "--haps="
    public static final String OPT_LEGEND = "--legend="
    public static final String OPT_MAP = "--map="
    public static final String OPT_MODE = "--mode="
     
    public static def COMMANDS = [ COMM_CREATE_PLINK, COMM_ASSOC_TEST, 
        COMM_GEN_DATASET, COMM_10F_SIMUL, COMM_PARSE_RES, COMM_TRIOS_CLAS, 
        COMM_HLA_CLAS, COMM_TRIOSSNP_CLAS, COMM_TRIOS10F, COMM_TRIOSMESS_CLAS,
        COMM_IMPUTE_PHAS, COMM_IMPUTE_REF, COMM_TRIOSTEST_CLAS, 
        COMM_HAPLOARFF_CLAS, COMM_PARSE_HAPLOARFF, COMM_SNPARFF_CLAS,
        COMM_PARSE_SNPARFF ]
    
    //usages
    public static final String USA_CREATE_PLINK = 
        "${COMM_CREATE_PLINK} ${OPT_INPUT_DIR}<dir> ${OPT_OUTPUT_DIR}<dir> ${OPT_CONTROL_DIR}<dir>"
    
    public static final String USA_ASSOC_TEST = 
        "${COMM_ASSOC_TEST} ${OPT_INPUT_DIR}<dir> ${OPT_PLINK}<plink executable>"
    
    public static final String USA_GEN_DATASET = 
        "${COMM_GEN_DATASET} ${OPT_INPUT_DIR}<dir> ${OPT_OUTPUT_DIR}<dir> ${OPT_CONTROL_DIR}<dir> ${OPT_PLINK}<plink executable> [${OPT_EXIST_TEST}]"
    
    public static final String USA_10F_SIMUL = 
        "${COMM_10F_SIMUL} ${OPT_INPUT_DIR}<dir> ${OPT_OUTPUT_DIR}<dir> ${OPT_CONTROL_DIR}<dir> ${OPT_PLINK}<plink executable> "+
        "[${OPT_EXCL_KNOWN}] [${OPT_SNPS_LIST}] [${OPT_HAPLOTYPE}]"
    
    
      
    public static final String USA_PARSE_RES =
        "${COMM_PARSE_RES} ${OPT_INPUT_DIR}<dir>"
    
    public static final String USA_TRIOS_CLAS =
        "${COMM_TRIOS_CLAS} ${OPT_INPUT_DIR}<dir> ${OPT_OUTPUT_DIR}<dir> ${OPT_SIZE}<size> [${OPT_2GTREE}]"
    
    public static final String USA_TRIOSMESS_CLAS =
        "${COMM_TRIOSMESS_CLAS} ${OPT_OUTPUT_DIR}<dir> ${OPT_SIZE}<size> [ ${OPT_MESS_PHASE} | ${OPT_MESS_CHR} ]"
    
    public static final String USA_HLA_CLAS =
        "${COMM_HLA_CLAS} ${OPT_INPUT_DIR}<dir> ${OPT_OUTPUT_DIR}<dir>"
    
    public static final String USA_TRIOSSNP_CLAS =
        "${COMM_TRIOSSNP_CLAS} ${OPT_INPUT_DIR}<dir> ${OPT_OUTPUT_DIR}<dir> ${OPT_PLINK}<plink executable>"
    
    public static final String USA_TRIOS10F = 
        "${COMM_TRIOS10F} ${OPT_INPUT_DIR}<dir> ${OPT_OUTPUT_DIR}<dir> ${OPT_PLINK}<plink executable>"
    
    public static final String USA_IMPUTE_PHAS = "${COMM_IMPUTE_PHAS} ${OPT_INPUT_DIR}<dir> ${OPT_OUTPUT_DIR}<dir>" +
        " ${OPT_IMPUTE}<impute dir> ${OPT_HAPS}<dir> ${OPT_LEGEND}<dir> ${OPT_MAP}<dir> "
    
    public static final String USA_IMPUTE_REF = "${COMM_IMPUTE_REF} ${OPT_INPUT_DIR}<dir> ${OPT_OUTPUT_DIR}<dir> ${OPT_MODE}mode"
    
    public static final String USA_TRIOSTEST_CLAS = "${COMM_TRIOSTEST_CLAS} ${OPT_INPUT_DIR}<dir> ${OPT_OUTPUT_DIR}<dir> ${OPT_SIZE}<size>"
    
    public static final String USA_HAPLOARFF_CLAS = "${COMM_HAPLOARFF_CLAS} ${OPT_INPUT_DIR}<dir> "+
        "${OPT_OUTPUT_DIR}<dir> ${OPT_SIZE}<size> ${OPT_MODE}<genetic model>"
    
    public static final String USA_PARSE_HAPLOARFF = "${COMM_PARSE_HAPLOARFF} ${OPT_INPUT_DIR}<dir> "
    
    public static final String USA_SNPARFF_CLAS = "${COMM_SNPARFF_CLAS} ${OPT_INPUT_DIR}<dir> "
     
    //regular expresions for files
    static def regexMap = /\w+_(\d+)\.map/ //*_(chr).map
    static def regexDatPed = /(\w+_)(\d+)(\.dat\.ped)/ //*_(chr).dat.ped
    static def regexTesPed = /(\w+_)(\d+)(\.tes\.ped)/ //*_(chr).dat.ped
    
    public static final String affCode = '2'
    public static final String unaffCode = '1'
    
    //pvalue thresholds
    public static final THRESHOLDS = [0.8, 0.5, 0.1, 0.05, 0.01, 0.001, 0.0001, 0.00001]
    //public static final THRESHOLDS = [0.00001]
    // public static final THRESHOLDS = [0.5, 0.1, 0.05, 0.01, 0.001, 0.0001, 0.00001]
    ///public static final THRESHOLDS_TRIOS = 
     //   [0.8, 0.6, 0.4, 0.2, 0.15, 0.1, 0.05, 0.01, 0.001, 0.0001, 1e-5, 1e-6, 1e-7]
    public static final THRESHOLDS_TRIOS = [0.8]
    ///public static final THRESHOLDS_TRIOS = [1e-5, 1e-6, 1e-7]
        
    public static final String filtOpts =  '--hwe-all --maf 0.01 --hwe 0.05' //plink filtering options
    public static final int PAT_SIZE = 3500
    public static final int NFOLD = 10
    public static final double orCutoff = 0.0
    public static final int THREADS = 5
    
    static Matrix gMatrix = new Matrix(0,0) //global matrix, used in every iteration
    
    /**
     *
     */
    public static void main( String[] args )
    {
        if( args.length==0 || !(args[0] in COMMANDS) ) {
            // first check args
            if( args.length>0 )
                System.err.println( args[0]+" operation, not valid.");

            System.err.println( "Valid operations : "+COMMANDS )
            System.exit(1)
        }

        println "Start time: ${new Date()}\n"
        int res = 0
        
        if( args[0] == COMM_CREATE_PLINK ){
            res = createPLINK(args as List)
        }
        else if( args[0] == COMM_ASSOC_TEST ){
            res = assocTest(args as List,'')
        }
        else if( args[0] == COMM_GEN_DATASET ){
            res = genDatasets(args as List)
        }
        else if( args[0] == COMM_10F_SIMUL) {
            res = FoldSimulation.perform(args as List, NFOLD)
        }
        else if( args[0] == COMM_PARSE_RES ){
            res = RP.doParse(args as List)
        }
        else if( args[0] == COMM_TRIOS_CLAS ){
            res = Trios.perform(args as List)
        }
        else if( args[0] == COMM_HLA_CLAS ){
            res = HLAClassif.perform(args as List)
        }
        else if( args[0] == COMM_TRIOSSNP_CLAS ){
            res = Trios.performSnp(args as List)
        }
        else if( args[0] == COMM_TRIOS10F ){
            res = Trios.performSnp(args as List, true)
        }
        else if( args[0] == COMM_TRIOSMESS_CLAS ){
            res = Trios.performMessOrIndepClass(args as List, false)
        }
        else if( args[0] == COMM_IMPUTE_PHAS ){
            res = PhasingImpute.doPhasing(args as List)
        }
        else if( args[0] == COMM_IMPUTE_REF ){
            res = PhasingImpute.doBuildReference(args as List)
        }
        else if( args[0] == COMM_TRIOSTEST_CLAS ){
            res = Trios.performMessOrIndepClass(args as List, true)
        }
        else if( args[0] == COMM_HAPLOARFF_CLAS ){
            res = HaploArff.perform(args as List)
        }
        else if( args[0] == COMM_PARSE_HAPLOARFF ){
            res = RP.doParseHaploArff(args as List)
        }
        else if( args[0] == COMM_SNPARFF_CLAS ){
            res = SnpArff.perform(args as List)
        }
        else if( args[0] == COMM_PARSE_SNPARFF ){
            res = RP.doParseSnpArff(args as List)
        }
        
        println "End time: ${new Date()}\n"
        
        if(res)
            System.exit(res)
    }
    
    
    /**
     * checks that the given dirs exists
     * 
     * @param list : a list of string options
     * @return a list of dir names or null if error
     */
    public static checkAndCleanDirOpts(list){
        def dirs = list.collect{ //extract dir names from options
                if(it){
                    String dir = it.substring( it.indexOf('=')+1 )
                    //add '/' to the dir path
                    dir.endsWith(File.separator) ? dir : dir+File.separator
                }
                else{
                    ''
                }
            }
        
        for( dir in dirs ){ //check if the directories exists
            if( !dir || !(new File(dir).exists()) ){
                System.err.println( "directory option missing or not exists.");
                return null
            }
        }
        
        return dirs
    }
    
    /**
     * read excluded patterns list
     */
    public static readExcluded(String dir, int prefLen = 0){
        def regex = /exclusion-list-.+\.txt/
        def files = new File(dir).list({d, f-> f ==~ regex } as FilenameFilter).toList()
        def file = files.find{ !it.contains('snps') }
        def excluded = []
        
        new File(dir+file).eachLine{line->
            if( !line.startsWith('#') ){
                excluded << line.split("\\s")[2].substring(prefLen)
            }
        }
        
        return excluded
    }
    
    /**
     * Read known snps and those snps near them.
     * Uses known_snps_XXX.list file
     */
    public static readKnownSnps(String dir, int distance = 0){
        def regex = /known_snps_\w+\.list/
        def files = new File(dir).list({d, f-> f ==~ regex } as FilenameFilter).toList()
        
        if(!files)
            return null
            
        def excluded = [] as TreeSet
        def coordinates = [:]
        
        //store excluded coordinates for each cromosome
        new File(dir+files[0]).splitEachLine("\\s"){toks->
            // fields: chrom, coord, rs_id, aff_id
            if( coordinates[(toks[0])]==null )
                coordinates[(toks[0])] = []
                
            excluded << toks[3]
            coordinates[(toks[0])] << (toks[1] as Integer)
        }
        
        if( distance==0 )
            return excluded //return only listed known snps
        
        def mapFiles = new File(dir).list({d, f-> f ==~ regexMap } as FilenameFilter).toList()
        mapFiles.each{ mapFile->
            def mat = (mapFile =~ regexMap)
            def chr = mat[0][1]
            // add snps in range +/- distance
            
            new File(dir+mapFile).eachLine{ line->
                def toks = line.split("\\s")
                //fields: chrom, snp_id, Genetic distance, Base-pair position
                int pos = toks[3] as Integer
                
                if( coordinates[chr].any{(pos >= it-distance) && (pos <= it+distance)} )
                    excluded << toks[1]
            }
        }
        
        return excluded
    }
    
    /**
     * read excluded snps list
     */
    public static readExcludedSnps(String dir){
        def regex = /exclusion-list-snps-.+\.txt/
        def files = new File(dir).list({d, f-> f ==~ regex } as FilenameFilter).toList()
        def excluded = []
        
        new File(dir+files[0]).eachLine{line->
            if( !line.startsWith('#') && !line.startsWith('CHR') ){
                excluded << line.split("\\s")[1]
            }
        }
        
        return excluded
    }
            
    /**
     * 
     * @param gouPath : cases file path or null
     * @param contGouPath : controls file path or null
     * @param outPedPath : output file path
     */
    public static createPedFile(gouPath, contGouPath, outPedPath, exclusionSet = [] as Set){
        
        def transAndWrite = { line, writer, pheno ->
            // writes the line result of removing the 7º field and changing the 6º field by pheno
            def toks = line.split("\\s",8)
            if( !exclusionSet.contains(toks[1]) )
                writer.writeLine("${toks[0]} ${toks[1]} ${toks[2]} ${toks[3]} ${toks[4]} ${pheno} ${toks[7]}")
        }

        def writer = new BufferedWriter(new FileWriter(outPedPath))

        if( gouPath && contGouPath )
            println "Generating ${outPedPath} using ${gouPath} and ${contGouPath}"
        else
            println "Generating ${outPedPath} using ${gouPath ?: contGouPath}"
        

        if(gouPath){
            //process input file (cases)
            def reader = Utils.createReader(new File(gouPath))
            reader.eachLine{ line ->
                transAndWrite(line, writer, affCode)
            }
            reader.close()
        }

        if(contGouPath){
            //process control file
            def reader = Utils.createReader(new File(contGouPath))
            reader.eachLine{ line ->
                transAndWrite(line, writer, unaffCode)
            }
            reader.close()
        }

        
        writer.close()
    }
    
            
    /**
     *
     */
    public static createMapFile(input, output, excludedSnps = [] as Set){
        println "Copying ${input} to ${output}"
        ///cp ${input} ${output}".execute().waitFor()

        def writer = new BufferedWriter(new FileWriter(output))
        new File(input).eachLine{ line->
            def toks = line.split("\\s")

            if( excludedSnps!=null && excludedSnps.contains(toks[1]) )//if excluded then set '-' before snp coordinate
                writer.writeLine("${toks[0]}\t${toks[1]}\t${toks[2]}\t-${toks[3]}")
            else
                writer.writeLine(line)
        }

        writer.close()
    }    
         
            
    /**
     * creates PED and MAP files for PLINK, using *.tes.gou and *.dat.gou from
     * input folder. Generates *.dat.ped and *.tes.ped in output folder,
     * also copies *.map files to output folder
     * 
     * example: plinkFiles --input=/home/victor/Escritorio/WTCCC1/CAD --output=/home/victor/Escritorio/WTCCC1/out_CAD --control=/home/victor/Escritorio/WTCCC1/58C
     */
     private static int createPLINK(args){
        String input = args.find{ it.startsWith(OPT_INPUT_DIR) }
        String output = args.find{ it.startsWith(OPT_OUTPUT_DIR) }
        String control = args.find{ it.startsWith(OPT_CONTROL_DIR) }
        
        def dirs = checkAndCleanDirOpts([input, output, control])
        
        if(!dirs){
            System.err.println("Usages:\n"+USA_CREATE_PLINK)
            return 1 //error
        }
            
        input = dirs[0]
        output = dirs[1]
        control = dirs[2]
        
        
        def regexDat = /\w+_(\d+)(\.dat\.gou)/ //*_(chr).dat.gou
        def regexTes = /\w+_(\d+)(\.tes\.gou)/ //*_(chr).tes.gou
        
        //read excluded patterns
        def exclusionSet = (readExcluded(input,5) + readExcluded(control,5)) as TreeSet
        
        [regexDat, regexTes].each{ regex->
            def filter = {d, f-> f ==~ regex } as FilenameFilter
            
            //get control/inputs
            def controls = new File(control).list(filter).toList()
            def inputs = new File(input).list(filter).toList()
            
            inputs.each{ gouName->
                def mat = (gouName =~ regex)
                def chr = mat[0][1]
                def suff = mat[0][2]
                def contName = controls.find{ it.contains('_'+chr+suff) }
                //file where to write
                def pedName = gouName.replace('.gou','.ped')
                
                createPedFile(input+gouName, control+contName, output+pedName, exclusionSet)
            }
        }

        //load excluded snps
        def excludedSnps = readExcludedSnps(control) as TreeSet
        
        // copy and transform *.map from input to output dir
        def mapFiles = new File(input).list({d, f-> f ==~ regexMap } as FilenameFilter).toList()
        mapFiles.each{ map->
            createMapFile(input+map, output+map, excludedSnps)
        }
                
        return 0 //ok
    }
 
    /**
     * builds a plink command call
     */
    private static String plinkTest( plink, test, ped, map, output){
        return "${plink} --noweb --ped ${ped} --map ${map} ${test} --out ${output}"
    }
    
    /**
     * performs association, trend and SNP missing rate tests on training set
     * 
     * example: assocTest --input=/home/victor/Escritorio/WTCCC1/out_CAD --plink=p-link
     * 
     * @param args
     * @param pFiltOpts: plink filtering options
     * @param testList : optional test list that overrides standard tests
     */
    public static int assocTest(args, String pFiltOpts, testList=null){
        String input = args.find{ it.startsWith(OPT_INPUT_DIR) }
        String plink = args.find{ it.startsWith(OPT_PLINK) }
        
        def dirs = checkAndCleanDirOpts([input])
        
        if(!dirs || !plink){
            System.err.println("Usages:\n"+USA_ASSOC_TEST)
            return 1 //error
        }
        
        input = dirs[0]
        plink = plink.substring( plink.indexOf('=')+1 )
        
        if( !testList )
            testList = ["--assoc ${pFiltOpts}", "--model ${pFiltOpts}", "--test-missing ${pFiltOpts}"]
            
        //get control/inputs
        def pedDatFiles = new File(input).list({d, f-> f ==~ regexDatPed } as FilenameFilter).toList().sort()
        def mapFiles = new File(input).list({d, f-> f ==~ regexMap } as FilenameFilter).toList()
        def tasks = []
        
        testList.each{ test->
            pedDatFiles.each{ pedName->
                def mat = (pedName =~ regexDatPed)
                def chr = mat[0][2]
                def prefix = mat[0][1]
                def mapName = mapFiles.find{ it.contains('_'+chr+'.map') }
                def outName = prefix+chr+'.dat'
            
                tasks << plinkTest(plink, test, input+pedName, input+mapName, input+outName)
            }
        }
        
        Utils.runCommands( tasks, (pedDatFiles.size()==1) ? 1 : THREADS)
        
        return 0 //ok
    }

    /**
     *
     */
    private static int genDatasets(args){
        
        def error = {//error closure
            System.err.println("Usages:\n"+USA_GEN_DATASET)
            return 1 
        }
        
        String output = args.find{ it.startsWith(OPT_OUTPUT_DIR) }
        output = checkAndCleanDirOpts([output])[0]
        String plink = args.find{ it.startsWith(OPT_PLINK) }
        String existTest = args.find{ it.startsWith(OPT_EXIST_TEST) }
        
        if( !existTest ){
            // create ped files
            if( createPLINK(args) != 0 )
                return error()

            // performs association and trend tests
            if( assocTest([OPT_INPUT_DIR+output, plink],filtOpts) != 0 )
                return error()
        }
            
        genDatasetsAndClassify(output, [] as Set)
        
        return 0 //ok
    }
    
    /**
     *
     */
    public static genDatasetsAndClassify(String output, Set options, List snpList = null)
{
        //get file name parts
        def pedName = new File(output).list().toList().find{ it==~regexDatPed }
        def mat = (pedName =~ regexDatPed)
        def prefix = mat[0][1]
        def measures = [:]
        def thresholds = THRESHOLDS
        
        def mapGrsFiles = [(Classify.P_wGRS):'wgrs_pv', (Classify.P_GRS):'grs_pv', 
                (Classify.P_eWGRS):'ewgrs_pv', (Classify.P_rScore):'rs_pv']
        
        if( snpList )
            thresholds = [1.0]// we dont need a p-value cutoff using snps list 
            
        // generate datasets for each cut value
        thresholds.each{ cutPval->
            println "\n${RP.TOK_DATA_CUT_PVAL} ${cutPval} p-value cut (${new Date()})"
            measures[cutPval] = [:]
            def cutStr = (cutPval as String).substring(2)//decimal part of number
            
            def grsOps = new GrsOps(rAlleles:gMatrix)
            def patterns = null
            
            if( options.contains(OPT_HAPLOTYPE) )
                patterns = grsOps.generateHapPatterns(output, cutPval)
            else if( snpList )
                patterns = grsOps.generatePatternsUnfilter(output, snpList)
            else
                patterns = grsOps.generatePatterns(output, cutPval)
                
            if( patterns!=null ){
                mapGrsFiles.each{ valGrs, subname->
                    patterns.each{ suff, list-> //generate .arff files for test and dat sets
                        grsOps.genArff( list, "${prefix}${cutPval}", "${output}${prefix}${subname}${cutStr}${suff}.arff", valGrs)
                    }
                }

                double alpha0 = grsOps.alpha0
                grsOps = null
                patterns = null

                Classify.classifiersNames.each{
                    // get classifier by name and load the appropriate test and train set
                    Classify cls = new Classify(alpha0:alpha0)
                    cls.setClassifier(it)

                    //get subname for the current classifier
                    def subname = mapGrsFiles.find{ ele-> it.startsWith(ele.key) }.value

                    cls.loadTestSet( "${output}${prefix}${subname}${cutStr}.tes.arff" )
                    cls.loadTrainSet( "${output}${prefix}${subname}${cutStr}.dat.arff" )
                    /// train and test with the same set
                    ///cls.loadTrainSet( "${output}${prefix}${subname}${cutStr}.tes.arff" )

                    cls.classify()
                    System.out.println( cls.getSummary() )
                    measures[cutPval][it] = cls.getMeasures()
                    cls = null
                }
            }
        }
        
        return measures
    }

}
