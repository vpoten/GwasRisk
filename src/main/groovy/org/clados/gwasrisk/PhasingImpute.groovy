/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk

/**
 * utility class
 */
class PhasingSnpData {
    int id
    String rsId
    int pos
    String alleles
    
    /**
     *
     */
    String encodeChiamoGen(String pAlleles) {
        if( pAlleles[0]==pAlleles[1] ){
            if( pAlleles[0]==alleles[0] )
                return ' 1 0 0'
            else if( pAlleles[0]==alleles[1] )
                return ' 0 0 1'
            else 
                return ' 0.33 0.33 0.33'
        }
        
        Character notMiss = ' '
        
        if( pAlleles[0]=='?' ){ 
            notMiss = pAlleles[1]
        }
        else if( pAlleles[1]=='?' ){
            notMiss = pAlleles[0]
        }
        
        if( notMiss==alleles[0] ){
            return ' 0.5 0.5 0'
        }
        else if( notMiss==alleles[1] ){
            return ' 0 0.5 0.5'
        }
        
        return ' 0 1 0'
    }
    
    /**
     *
     */
    Character encodeHaps(Character base){
        Character hap = '?'
        
        if( base==alleles[1] )
            hap = '1'
        else if( base==alleles[0] )
            hap = '0'
            
        return hap
    }
}

/**
 * Phasing and Impute genotypes using Impute software.
 * 
 * https://mathgen.stats.ox.ac.uk/impute/impute_v2.html#home
 * 
 * @author victor
 */
class PhasingImpute {
    
    static final String IMPUTE2 = 'impute2'
    
    static int numThreads = 1
    static int regionStep = 5000000
    
    // regexs
    static def regexGens = /\w+_[a-zA-Z]*(\d+)\w*\.gen[s]?/
    static def regexMap = /\w+_map_[a-zA-Z]*(\d+)\w+\.txt/
    static def regexLegend = /\w+_[a-zA-Z]*(\d+)\w*\.legend/
    static def regexHap = /\w+_[a-zA-Z]*(\d+)\w*\.hap[s]?/
    static def regexChunk = /\w+_(\d+)_chunk(\d+)\.phasing\.impute2_haps/
    static def regexImputeHaps = /\w+_(\d+)_all\.phasing\.impute2_haps/
    
    static def genotypeMap = buildGenotypes()
    
    // modes
    static final String MODE_CONTROL = 'control'
    static final String MODE_CASECONT = 'casecont'
    
    static final int FAM_IDX = 0
    static final int ID_IDX = 1
    static final int AFF_IDX = 5
    static final int GENDER_IDX = 4
    
    /**
     *
     */
    static doPhasing(args) {
        String input = args.find{ it.startsWith(Main.OPT_INPUT_DIR) }
        String output = args.find{ it.startsWith(Main.OPT_OUTPUT_DIR) }
        String impute = args.find{ it.startsWith(Main.OPT_IMPUTE) }
        String haps = args.find{ it.startsWith(Main.OPT_HAPS) }
        String legend = args.find{ it.startsWith(Main.OPT_LEGEND) }
        String map = args.find{ it.startsWith(Main.OPT_MAP) }
        
        def dirs = Main.checkAndCleanDirOpts([input,output,impute,haps,legend,map])
        
        def error = {
            System.err.println("Usages:\n${Main.USA_IMPUTE_PHAS}")
            return 1 //error
        }
        
        if( !dirs )
            return error()
            
        input = dirs[0]
        output = dirs[1]
        impute = dirs[2]
        haps = dirs[3]
        legend = dirs[4]
        map = dirs[5]
        
        String filePref = getFilePref(input, regexGens)
        
        runPhasing(output, impute, input, legend, haps, map, true)
        joinChunks(output, filePref)
        imputedHapsToGou(input, output, filePref)
        
        return 0
    }
    
    /**
     * .gou, .pou, .rs to .legend and .haps 
     */
    static doBuildReference(args) {
        String input = args.find{ it.startsWith(Main.OPT_INPUT_DIR) }
        String output = args.find{ it.startsWith(Main.OPT_OUTPUT_DIR) }
        String mode = args.find{ it.startsWith(Main.OPT_MODE) }
        
        def dirs = Main.checkAndCleanDirOpts([input,output])
        
        def error = {
            System.err.println("Usages:\n${Main.USA_IMPUTE_REF}")
            return 1 //error
        }
        
        if( !dirs )
            return error()
            
        input = dirs[0]
        output = dirs[1]
        
        if( mode )
            mode = mode.substring( mode.indexOf('=')+1 )
        
        println "Building impute2 reference files: mode=${mode}"
        
        String filePref = getFilePref(input, HaploOps.regexGou)
        
        goupouToLegend(input, output, filePref, true, mode)
        
        return 0
    }
    
    /**
     * 
     */
    protected static boolean isEqualChr(String chr1, String chr2){
        return (chr1 as Integer)==(chr2 as Integer)
    }
    
    /**
     *
     */
    protected static def buildGenotypes(){
        def bases = ['A' as Character,'T' as Character,'G' as Character,
            'C' as Character,'?' as Character]
        def map = [:]
        
        bases.each{ b1->
            map[b1] = [:]
            bases.each{ b2->
                map[b1][b2] = ("${b1}${b2}" as String)
            }
        }
        
        return map
    }
    
    /**
     *
     */
    public static String getFilePref(dir, regex){
        def files = new File(dir).list({d, f-> f ==~ regex } as FilenameFilter).toList()
        files = files.findAll{ (new File(dir+it).length()) > 0L }
        
        return files[0].substring(0, files[0].indexOf('_') )
    }
    
    
    private static boolean isParent( toks ) {
        return (toks[2]=='0' && toks[3]=='0')
    }
        
    /**
     * read genotypes from a .gou file
     */
    public static List readGouGenotypes(file, boolean onlyParents){
        // read subject snps
        def reader = Utils.createReader( new File(file) )
        def subjects = []

        reader.splitEachLine("\\s"){ toks->
            if( !onlyParents || (onlyParents && isParent(toks)) ){
                def genotype = []
                for(int i=7; i<toks.size(); i+=2){
                    genotype << genotypeMap[HaploOps.recodeBase(toks[i], 2)][HaploOps.recodeBase(toks[i+1], 2)]
                }
                subjects << genotype
            }
        }

        reader.close()
        
        return subjects
    }
    
    /**
     *
     */
    public static String getAllelesFromSubjects(subjects, snpIdx){
        def alleles = [] as Set
        for(List genotype : subjects){
            (0..1).each{ j->
                if( genotype[snpIdx][j] in ['A','T','G','C'] )
                    alleles << genotype[snpIdx][j]
            }

            if( alleles.size()==2 )
                break
        }

        return alleles.sort().sum()
    }
    
    /**
     * NOT USED
     */
    protected static gouToChiamo(dir, outdir, String filePref) {
        def regexGou = HaploOps.regexGou
        
        def files = new File(dir).list({d, f-> f ==~ regexGou } as FilenameFilter).toList()
        files = files.findAll{ (new File(dir+it).length()) > 0L }
        
        // convert .rs and .pou to .map
        Trios.pouToMap(dir, outdir)
        def mapFiles = new File(outdir).list({d, f-> f ==~ Main.regexMap } as FilenameFilter).toList()
        
        files.each{ file->
            def mat = (file =~ regexGou)
            def chr = mat[0][1]
            def mapFile = mapFiles.find{ isEqualChr(((it=~Main.regexMap )[0][1]),chr) }
            
            //rename map file
            String newMap = "${filePref}_${chr}.map"
            "mv ${outdir+mapFile} ${outdir+newMap}".execute().waitFor()
            mapFile = newMap
            
            // read snps from map
            def snpsData = []
            
            new File(outdir+mapFile).eachLine{ line->
                def toks = line.split("\\s")
                //{chr rsId 0 pos}
                snpsData << new PhasingSnpData( 
                    id:snpsData.size(), rsId:toks[1], pos:(toks[3] as Integer) )
            }
            
            // read subject genotypes
            def subjects = readGouGenotypes(dir+file, false)
            
            subjects.eachWithIndex{ genotype, i->
                assert genotype.size()==snpsData.size(), 'Different snps count in subject: '+i+' chr '+chr
            }
            
            // write chiamo genotype file
            def outfile = "${filePref}_${chr}.gens"
            def writer = new FileWriter(outdir+outfile)
            println "Generating gens file ${outdir+outfile}"
            
            snpsData.eachWithIndex{ snp, i->
                // get snp alleles
                snp.alleles = getAllelesFromSubjects(subjects, i)
                
                // write snp line
                StringBuilder str = new StringBuilder()
                str << "${snp.id} ${snp.rsId} ${snp.pos} ${snp.alleles[0]} ${snp.alleles[1]}"
                
                subjects.each{ genotype->
                    str << snp.encodeChiamoGen( genotype[i] )
                }
                
                writer.write( str.toString() )
                writer.write('\n')
            }
            
            writer.close()
        }
    }
    
    /**
     * scans an haps file to get the different base codes that appears into it.
     */
    protected static hapsCoding(dir) {
        def files = new File(dir).list({d, f-> f ==~ regexHap } as FilenameFilter).toList()
        files = files.findAll{ (new File(dir+it).length()) > 0L }
        def codes = [] as Set
        
        def reader = Utils.createReader( new File(dir+files[0]) )
        
        (0..10).each{
            String line = reader.readLine()
            
            if( line!=null ){
                (0..line.length()-1).each{ i-> 
                    if( line[i]!=' ' && line[i]!='\t' )
                        codes << line[i]
                }
            }
        }
        
        reader.close()
        
        return codes
    }
    
    /**
     * NOT USED
     */
    protected static recodeHaps(dir, legendDir, outdir) {
        def files = new File(dir).list({d, f-> f ==~ regexHap } as FilenameFilter).toList()
        files = files.findAll{ (new File(dir+it).length()) > 0L }
        
        def legendFiles = new File(legendDir).list({d, f-> f ==~ regexLegend } as FilenameFilter).toList()
         
        files.each{ file->
            def mat = (file =~ regexHap)
            def chr = mat[0][1]
            def legFile = legendFiles.find{ isEqualChr(((it=~regexLegend)[0][1]),chr) }
            
            //parse legend file
            def reader = Utils.createReader( new File(legendDir+legFile) )
            reader.readLine() //skip header
            def snps = []
            
            reader.splitEachLine("\\s"){ toks->
                snps << genotypeMap[toks[2] as Character][toks[3] as Character]
            }
            
            reader.close()
            
            //reader haps file ({1,2,3,4} coded) and recode it
            int numLines = Utils.countLines(dir+file)
            reader = Utils.createReader( new File(dir+file) )
            String outfile = "recoded_haps_chr${chr}.haps"
            def writer = new FileWriter(outdir+outfile)
            
            println "Recoding haps file ${file} to ${outfile}"
            assert (numLines==snps.size()), 'Haps file contains diff. number of snps than legend file'
            int i = 0
            def bases = ['1','2','3','4']
            
            reader.splitEachLine("\\s"){ toks->
                StringBuilder line = new StringBuilder()
                
                toks.each{
                    assert (it in bases), 'Wrong base to recode, not in [1,2,3,4]'
                    def base = HaploOps.recodeBase(it, 1)
                    line << ((snps[i][0]==base) ? '0 ' : '1 ')
                }
                
                line.deleteCharAt( line.length()-1 )
                writer.write( line.toString() )
                writer.write('\n')
                
                i++
            }
            
            reader.close()
            writer.close()
        }
    }
    
    /**
     *
     */
    protected static runPhasing(String dir, String impute, String gensDir, String legendDir, 
         String hapsDir,  String mapDir, boolean wholeChrom = false) {
         
        
        if( !impute.endsWith('/') )
            impute += '/'
            
        def files = new File(gensDir).list({d, f-> f ==~ regexGens } as FilenameFilter).toList()
        def mapFiles = new File(mapDir).list({d, f-> f ==~ regexMap } as FilenameFilter).toList()
        def hapFiles = new File(hapsDir).list({d, f-> f ==~ regexHap } as FilenameFilter).toList()
        def legendFiles = new File(legendDir).list({d, f-> f ==~ regexLegend } as FilenameFilter).toList()
        
        /**
        ./impute2
         -phase
         -m ./Example/example.chr22.map
         -h ./Example/example.chr22.1kG.haps
         -l ./Example/example.chr22.1kG.legend
         -g ./Example/example.chr22.study.gens
         -strand_g ./Example/example.chr22.study.strand
         -int 20.4e6 20.5e6
         -Ne 20000
         -o ./Example/example.chr22.phasing.impute2
         **/
        def tasks = []
        
        files.each{ file->
            def mat = (file =~ regexGens)
            def chr = mat[0][1]
            
            String mapFile = mapFiles.find{ isEqualChr(((it =~ regexMap)[0][1]),chr) }
            String hapFile = hapFiles.find{ isEqualChr(((it =~ regexHap)[0][1]),chr) }
            String legFile = legendFiles.find{ isEqualChr(((it =~ regexLegend)[0][1]),chr) }
            
            //split into regios of regionStep size
            
            // get the last snp basepair
            def proc = "tail -n 1 ${legendDir}${legFile}".execute()
            proc.waitFor()
            int lastPos = proc.text.split("\\s")[1] as Integer
            int start = 1
            int chunk = 0
            
            while( start<lastPos ){
                String command = "${impute}${IMPUTE2} -phase "

                command += "-m ${mapDir}${mapFile} "
                command += "-h ${hapsDir}${hapFile} "
                command += "-l ${legendDir}${legFile} "
                command += "-g ${gensDir+file} "
                
                if( wholeChrom )
                    command += "-int 1 ${lastPos} -allow_large_regions "
                else
                    command += "-int ${start} ${(start+regionStep < lastPos) ? start+regionStep : lastPos} "
                
                command += "-Ne ${20000} "//population number
                ///command += '-os 2 '//dont impute missing, only type 2 snps
                command += "-o ${dir}triosSet_${chr}_chunk${String.format("%03d", chunk++)}.phasing.impute2"

                tasks << command
                start += (wholeChrom) ? lastPos : regionStep+1
            }
        }
        
        Utils.runCommands( tasks, numThreads)
    }
    
    /**
     *
     */
    protected static joinChunks(String dir, String filePref) {
        def files = new File(dir).list({d, f-> f ==~ regexChunk } as FilenameFilter).toList()
        
        def chroms = files.collect{ (it =~ regexChunk)[0][1] } as Set
        
        chroms.each{ chr->
            def chrFiles = files.findAll{ isEqualChr(((it =~ regexChunk)[0][1]),chr) }.sort()
            
            String outFile = "${dir}${filePref}_${chr}_all.phasing.impute2_haps"
            
            println "Joining ${chrFiles.size()} chunk files into ${outFile}"
            
            // do concat of chunk files (sorted list)
            def cat = 'cat '
            cat += chrFiles.sum{ dir+it+' ' }
            
            def proc = cat.execute()
            proc.consumeProcessOutputStream( new FileOutputStream(outFile) )
            proc.waitFor()
                
            //clean up chunk files
            //chrFiles.each{ "rm -f ${dir+it}".execute().waitFor() }
        }
    }
    
    /**
     * converts impute2 _haps output files to .gou
     */
    protected static imputedHapsToGou(String dir, String outdir, String filePref) {
        def goufiles = new File(dir).list({d, f-> f ==~ HaploOps.regexGou } as FilenameFilter).toList()
        goufiles = goufiles.findAll{ (new File(dir+it).length()) > 0L }
        
        def subjects = []
        
        //get subjects from a .gou file
        new File(dir+goufiles[0]).eachLine{ line->
            def toks = line.split("\\s",8)
            subjects << 
                ("${toks[0]} ${toks[1]} ${toks[2]} ${toks[3]} ${toks[4]} ${toks[5]} ${toks[6]}" as String)
        }
        
        def files = new File(outdir).list({d, f-> f ==~ regexImputeHaps } as FilenameFilter).toList()
        
        files.each{ file ->
            def mat = (file =~ regexImputeHaps)
            def chr = mat[0][1]
            
            //read genotypes
            def genotypes = subjects.collect{ new StringBuilder() }
            def reader = Utils.createReader( new File(outdir+file) )
            
            reader.splitEachLine("\\s"){ toks->
                def bases = ['0':toks[3], '1':toks[4], '?':'?']
                int j = 0
                for(int i=5; i<toks.size(); i+=2){
                    genotypes[j++] << " ${bases[toks[i]]} ${bases[toks[i+1]]}" 
                }
                
                assert (j==subjects.size()), 'Wrong subject count in _haps for '+toks[1]
            }
            
            reader.close()
            
            //write gou
            def outfile = "${filePref}_${chr}_phased.gou"
            println "Generating GOU file ${outfile} from imputed haps in ${file}"
            def writer = new FileWriter(outdir+outfile)
            
            subjects.eachWithIndex{ head, i->
                writer.write(head)
                writer.write(genotypes[i].toString())
                writer.write('\n')
            }
            
            writer.close()
        }
    }
    
    /**
     *
     */
    static goupouToLegend(String dir, String outdir, String filePref, boolean onlyParents = false, 
        String mode = null) {
        
        def regexPou = HaploOps.regexPou
        def regexRs = HaploOps.regexRs
        def regexGou = HaploOps.regexGou
        
        def gouFiles = new File(dir).list({d, f-> f ==~ regexGou } as FilenameFilter).toList()
        gouFiles = gouFiles.findAll{ (new File(dir+it).length()) > 0L }
        
        def pouFiles = new File(dir).list({d, f-> f ==~ regexPou } as FilenameFilter).toList()
        def rsFiles = new File(dir).list({d, f-> f ==~ regexRs } as FilenameFilter).toList()
        
        def casesSet = null
        
        // read subjects info
        def subjInfo = []
        
        def reader = Utils.createReader( new File(dir+gouFiles[0]) )
    
        reader.eachLine{ line->
            def toks = line.split("\\s",8)
            
            if( !onlyParents || (onlyParents && isParent(toks)) ){
                def subjmap = [ (FAM_IDX):toks[FAM_IDX], (ID_IDX):toks[ID_IDX], 
                    (AFF_IDX):toks[AFF_IDX], (GENDER_IDX):toks[GENDER_IDX] ]

                subjInfo << subjmap
            }
        }

        reader.close()
        
        if( mode == MODE_CASECONT ){
            //generate the random set of cases to include
            casesSet = FoldSimulation.randomIndexSet( (int)(subjInfo.size()/2), subjInfo.size() )
        }

        // generate .sample
        def fileSample = "${filePref}_${mode}.sample"
        def population = 'CEU'
        def writer1 = new PrintWriter(outdir+fileSample)
        writer1.println('sample population group sex') //write header
        
        println "Generating sample file with ${subjInfo.size()} samples"
        
        subjInfo.eachWithIndex{ subj, i->
            def subgrp = 'control'
            
            if( mode == MODE_CASECONT && (i in casesSet) )
                subgrp = 'case'
                
            writer1.println( "${subj[FAM_IDX]}:${subj[ID_IDX]} ${population} ${subgrp} ${subj[GENDER_IDX]}" )
        }
        
        writer1.close()
        
        // main loop
        pouFiles.each{ file->
            def mat = (file =~ regexPou)
            def chr = mat[0][1]
            def fileRs = rsFiles.find{ isEqualChr((it =~ regexRs)[0][1],chr) }
            def fileGou = gouFiles.find{ isEqualChr((it =~ regexGou)[0][1],chr) }
            
            // read snps from .rs and .pou
            def snpsData = []
            
            reader = Utils.createReader( new File(dir+file) )
            def readerRs = Utils.createReader( new File(dir+fileRs) )
            int i = 0
            
            reader.eachLine{ pos->
                def rsId = readerRs.readLine()
                snpsData << new PhasingSnpData( id:(i++), rsId:rsId, pos:(pos as Integer) )
            }
            
            reader.close()
            readerRs.close()
            
            // read subject genotypes
            def subjects = readGouGenotypes(dir+fileGou, onlyParents)
            
            subjects.eachWithIndex{ genotype, j->
                assert genotype.size()==snpsData.size(), 'Different snps count in subject: '+j+' chr '+chr
            }
            
            println "info: ${subjects.size()} genotypes of size ${snpsData.size()} in chr ${chr}"
            
            // write .legend
            def fileLeg = "${filePref}_${chr}.legend"
            println "Generating legend ${outdir+fileLeg} using ${file} and ${fileRs}"
            
            def writer = new PrintWriter(outdir+fileLeg)
            writer.println("rsID position a0 a1")//legend header
            
            snpsData.eachWithIndex{ snp, j->
                // get snp alleles
                snp.alleles = getAllelesFromSubjects(subjects, j)
                writer.println("${snp.rsId} ${snp.pos} ${snp.alleles[0]} ${snp.alleles[1]}")
            }
            
            writer.close()
            
            // write .hap
            def fileHap = "${filePref}_${chr}.hap"
            println "Generating hap ${outdir+fileHap} using ${fileGou}"
            
            writer = new PrintWriter(outdir+fileHap)
            
            
            snpsData.eachWithIndex{ snp, j->
                def line = new StringBuilder()
                
                subjects.eachWithIndex{ genotype, k ->
                    if( mode == null ) 
                        line << ((k==0) ? '' : ' ') + "${snp.encodeHaps(genotype[j][0] as Character)} ${snp.encodeHaps(genotype[j][1] as Character)}"
                    else if( mode == MODE_CONTROL ) 
                        line << ((k==0) ? '' : ' ') + snp.encodeHaps(genotype[j][1] as Character)
                    else if( mode == MODE_CASECONT )
                        line << ((k==0) ? '' : ' ') + 
                            ((k in casesSet) ? snp.encodeHaps(genotype[j][0] as Character) :  snp.encodeHaps(genotype[j][1] as Character))
                }
                
                writer.println(line.toString())
                
                //check for errors
                String errorMsg = 'Wrong number of haplotypes generated; snp:'+snp.rsId
                
                if( mode == null )
                    assert line.length()==subjects.size()*2*2-1, errorMsg
                else if( mode == MODE_CONTROL ||mode == MODE_CASECONT ) 
                    assert line.length()==subjects.size()*2-1, errorMsg
            }
            
            writer.close()
        }
    }
    
    
}

