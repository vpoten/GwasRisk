/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk



/**
 *
 * @author victor
 */

class HaploData {
    String id
    String chr
    double pvalue = 0.0
    def highSeqs = [] //high risk haplotypes (String or DNAEncoder)
    def lowSeqs = [] //low risk haplotypes (String or DNAEncoder)
    static final char MISS_CHAR = '?'
    
    /**
     * similarity measure
     */
    private int similarity(seq1,seq2){
        int sim = 0
        int len = seq1.length()
        for(int i=0; i<len; i++){
            if( seq1.charAt(i)==seq2.charAt(i) || seq1.charAt(i)==MISS_CHAR ||
                seq2.charAt(i)==MISS_CHAR ){
                sim++
            }
            else{
                break
            }
        }
        return sim
    }
    
    /**
     * returns 1 for risk haplotype, 0 for non risk and 9 for missingness
     */
    public byte isRiskHaplo(String seq){
        if( seq=='?' )
            return 9 //length 1 missing value
            
        if( seq.length()<=5 ){
            //check identity for shorter haplotypes
            if( highSeqs.any{ it.equals(seq) } )
                return 1
            if( lowSeqs.any{ it.equals(seq) } )
                return 0
        }
        
        int m1 = highSeqs.collect{similarity(it,seq)}.max()
        int m2 = lowSeqs.collect{similarity(it,seq)}.max()
        
        if( m1 > m2 ){
            return 1
        }
        else if( m1 == m2 ){
            return (byte)Math.round( Math.random() )
        }
            
        return 0
    }
    
}


class HaploOps {
    private Matrix rAlleles
    private def mapHaplo = [:]
    private def mapDetailFiles = [:] //.detail files
    private def selectedHaplo
    
    static final String FOLDER_HOLD = 'holdout/'
    static final String FOLDER_TRAINING = 'training/'
    static final String FOLDER_TRAINING_ALL = 'training_all/'
    static final String FOLDER_TEST = 'test/'
    static final int THREADS = 2
    
    static final String DAT_LBL = '.dat'
    static final String TES_LBL = '.tes'
    static final String PHENO_ATT_NAME = 'phenotype'
    static final String MESS_HAP_EXT = '.msshap'
    static final String MESS_CHR_EXT = '.msschr'
    
    static def regexPou = /\w+_[a-zA-Z]*(\d+)\w+\.pou/
    static def regexGou = /\w+_[a-zA-Z]*(\d+)\w+\.gou/
    static def regexRs = /\w+_[a-zA-Z]*(\d+)\w+\.rs/
        
    static def regexTrainHPed = /(\w+_)(\d+)_(\d+)(\.dat\.haped)/ //*_(chr).dat.haped
    static def regexTestHPed = /(\w+_)(\d+)_(\d+)(\.tes\.haped)/ //*_(chr).tes.haped
    
    static final transformBases = ['1':'A', '2':'C', '3':'G', '4':'T', 
                'A':'A', 'C':'C', 'G':'G', 'T':'T', '?':'?', '0':'?']
    
    static def seqRegex = /[AGTC\?]+/
    
    private int numDat = 0
    private int numTes = 0
    
    /**
     *
     */
    public def generatePatterns(input, output, cutPval, swSize, boolean use2GTree=false){
        getHaplotypes(input+FOLDER_HOLD, cutPval, swSize, use2GTree)
        def patterns = generatePatterns(output, swSize, true)
        return patterns
    }
    
    /**
     *
     */
    public def generatePatterns(input, output, String chr, int start, int end){
        getSNPs(input+FOLDER_TEST, chr, start, end)
        getAlleles(output, 1)
        def patterns = generatePatterns(output, 1, false)
        return patterns
    }
    
    /**
     * 
     * @param input : folder with FOLDER_HOLD and FOLDER_TEST subfolders inside it
     */
    public def generateTestPatterns(input, cutPval, swSize){
        getHaplotypes(input+FOLDER_HOLD, cutPval, swSize, false)
        boolean testOnly = true
        def patterns = generatePatterns(input+FOLDER_TEST, swSize, true, testOnly)
        return patterns
    }
    
    /**
     *
     */
    private def getHaplotypes(String input, double cutPval, int swSize, boolean use2GTree){
        def regexDet = (use2GTree) ? /\w+_[a-zA-Z]*(\d+)\w+_SWOfSize(\d+)And\w+TDT2GTree_\w+\.detail/ :
                            /\w+_[a-zA-Z]*(\d+)\w+_SWOfSize(\d+)And\w+TDT2G_\w+\.detail/
        
        def detFiles = new File(input).list({d, f-> f ==~ regexDet } as FilenameFilter).toList()
        
        detFiles = detFiles.findAll{ ((it=~regexDet)[0][2] as Integer)==swSize }
        
        detFiles.each{ file->
            def mat = (file =~ regexDet)
            def chr = mat[0][1]
            mapHaplo[chr] = [] as TreeSet
            mapDetailFiles[chr] = input+file //store .detail file
            
            /*
            //first pass: read pvalues
            def pvalues
            def reader
            
            if( cutPval<1.0 ){
                //if exists a p-value threshold
                pvalues = [1.0]
                reader = Utils.createReader(new File(input+file))
                
                reader.eachLine{ line ->
                    def toks = line.split("\\s",3)
                    if( toks[0]!='no' )
                        pvalues << (toks[1] as Double)
                    else
                        pvalues << 1.0
                }
                pvalues << 1.0
                reader.close()
            }
            
            //second pass
            reader = Utils.createReader(new File(input+file))
            int count = 1
            reader.splitEachLine("\\s"){ toks ->
                if( toks[0]!='no' ){
                    if( cutPval==1.0 ){
                        // get every valid haplotype
                        mapHaplo[chr] << (toks[0] as Integer)
                    }
                    else if( pvalues[count]<cutPval && [-1,1].every{pvalues[count]<pvalues[count+it]} ){
                        // get local minimum only
                        mapHaplo[chr] << (toks[0] as Integer)
                    }
                }
                
                count++ //inc. count
            }
            
            reader.close()
            pvalues = null
            */
           
            def reader = Utils.createReader(new File(input+file))
            
            reader.splitEachLine("\\s"){ toks ->
                if( toks[0]!='no' ){
                    if( cutPval==1.0 ){
                        // get every valid haplotype
                        mapHaplo[chr] << (toks[0] as Integer)
                    }
                    else if( (toks[1] as Double)<cutPval ){
                        mapHaplo[chr] << (toks[0] as Integer)
                    }
                }
            }
            
            reader.close()
        }
        
        println "${mapHaplo.values().sum{it.size()}} haplotypes selected"
    }
    
    /**
     * parse haplotype high and low risk sequences of the given chromosome
     */
    private def parseHaploHiLoSeqs(chr, swSize){
        ///println "Parsing HiLo risk seqs. in .detail file for chr ${chr}"
        def map = [:] as TreeMap
        
        if( !mapDetailFiles[chr] )
            return map
            
        def reader = Utils.createReader(new File(mapDetailFiles[chr]))
        
        reader.splitEachLine("\\s"){ toks ->
            if( toks[0]!='no' && mapHaplo[chr].contains(toks[0] as Integer) ){
                map[toks[0] as Integer] = parseHaploToks(toks, chr, swSize, true)
            }
        }
        
        reader.close()
        
        return map
    }
    
    /**
     * parses a line of haplotype data (.detail file)
     */
    private HaploData parseHaploToks(toks, chr, int swSize, boolean getSeqs){
        def haploData = new HaploData(id:toks[0], chr:chr, pvalue:(toks[1] as Double))

        if( getSeqs ){
            //get low and high risk sequences for this haplotype
            int i = 2 // TODO treat -X in high and low risk sequences
            while( toks[i]!='G1:' ){ i++ }
            i++
            while( !toks[i].contains(':') ){ haploData.highSeqs << toks[i++] }
            while( toks[i]!='G2:' ){ i++ }
            i++
            while( !toks[i].contains(':') ){ haploData.lowSeqs << toks[i++] }
        }

        //check seqs
        ///assert haploData.highSeqs.every{ it ==~ seqRegex }, 'wrong G1 seq'
        ///assert haploData.lowSeqs.every{ it ==~ seqRegex }, 'wrong G2 seq'
        
        return haploData
    }
    
    /**
     * gets the start row in rAlleles matrix. set could contain '.test' or '.train'
     */
    int getStartRow(String set){
        if( set.contains(TES_LBL) ){
            return numDat+1
        }
        return 0
    }
    
    /**
     * builds a pattern ID inserting ',' between the given values
     */
    static String patternId(familyId, indivId){
        return "${familyId},${indivId}"
    }
    
    /**
     * encode phenotype using plink standard
     */
    static String encodePheno(gouCode){
        if( gouCode == '2' )
            return Main.affCode
            
        return Main.unaffCode
    }
    
    
    /**
     *
     */
    protected def countInstances(dir){
        def trainList = new File(dir).list({d, f-> f ==~ regexTrainHPed } as FilenameFilter)
        numDat = (trainList) ? Utils.countLines( dir + trainList[0] ) : 0
        
        def testList = new File(dir).list({d, f-> f ==~ regexTestHPed } as FilenameFilter)
        numTes = (testList) ? Utils.countLines( dir + testList[0] ) : 0
    }
    
    /**
     *
     */
    protected def Map generatePatterns(String input, int hapLen, boolean parseHiLoSeqs, 
        boolean testOnly = false) {
        
        countInstances(input)
        Map patterns = [ (TES_LBL):new GwasPattern [numTes] ]
            
        if( !testOnly )
            patterns[DAT_LBL] = new GwasPattern [numDat]
        
        int numHaplo = mapHaplo.values().sum{ it.size() }
        selectedHaplo = new ArrayList(numHaplo)
        
        // create matrix array the first time that is used
        if( rAlleles.columnDimension < numHaplo*2 )
            rAlleles.recreate(numDat+numTes+1, numHaplo*2 )
            
        rAlleles.set(0 as Byte)// clean matrix
        
        def doGenerate = { set, regex->
            def filter = {d, f-> f ==~ regex } as FilenameFilter
            
            // get hped files (order is important for rAlleles)
            def files = new File(input).list(filter).toList()
            files = files.findAll{ (it=~regex)[0][3]==(hapLen as String) }.sort()
            int colStart = 0
            
            println "Generating patterns for ${set} set; processing ${files.size()} files"
            
            files.each{ file->
                def mat = (file =~ regex)
                def chr = mat[0][2]
                def haploChr = mapHaplo[chr]
                
                if( haploChr && parseHiLoSeqs ){
                    haploChr = parseHaploHiLoSeqs(chr, hapLen)
                    assert (haploChr.size()==mapHaplo[chr].size()), 'haplotype maps sizes are not equal'
                }
                
                def patArr = patterns[set] //get pattern set (train or test)
                
                int start = getStartRow(set)
                def reader = Utils.createReader( new File(input+file) )
                int i=0
                
                reader.eachLine{ line ->
                    def tokens = line.split("\\s",8)
                    GwasPattern pattern = null
                    
                    if( patArr[i]==null ){//if the pattern not exists yet
                        patArr[i] = new GwasPattern( id:patternId(tokens[0], tokens[1]), 
                            phenotype:encodePheno(tokens[5]) )
                    }
                    
                    pattern = patArr[i]
                    
                    
                    String genotype = tokens[7]//remainder of the line
                    int step = hapLen*2+2
                    
                    haploChr?.eachWithIndex{ idx, haploData, j->
                        int base = idx*step
                        byte x1 = haploData.isRiskHaplo(genotype.substring(base,base+hapLen))
                        byte x2 = haploData.isRiskHaplo(genotype.substring(base+hapLen+1,base+step-1))

                        if( set==DAT_LBL || (testOnly && set==TES_LBL) ){
                            // do for training set only (or testOnly mode)
                            if( i==0 ) //keep snpData
                                selectedHaplo << haploData
                        }

                        rAlleles.set( i+start, colStart+j*2, x1)
                        rAlleles.set( i+start, colStart+j*2+1, x2)
                    }
                    
                    i++//increment pattern counter
                }
                
                reader.close()
                colStart += ((haploChr) ? haploChr.size()*2 : 0)
                haploChr?.clear()
                haploChr = null
                System.gc()
                try{ Thread.sleep(15*1000) }catch(e){}
            }
        }
        
        ///doGenerate('.dat', regexTrainHPed)
        ///doGenerate('.tes', regexTestHPed)
        if( testOnly )
            doGenerate(TES_LBL, regexTestHPed)
        else
            Utils.runClosures([{doGenerate(DAT_LBL, regexTrainHPed)}, {doGenerate(TES_LBL, regexTestHPed)}], THREADS)
        
        return patterns
    }
    
    
    
    /**
     * 0,1,2,3,4 to ?,A,C,G,T
     * 
     * @param swSize : size of sliding window; if swSize==1 returns -X as X (instead of ?)
     */
    static char recodeBase(base, int swSize){
        if( base.startsWith('-') ){
            if( swSize>1 )
                return '?'
            else
                base = base.substring(1)
        }
            
        return transformBases[base]
    }
    
    /**
     * 
     * @param train : training folder or null
     * @param test : test folder
     */
    static def genPhaseFiles(train, test, output, int swSize, chrsToUse=[] ){
        
        def doPhase = { set, dir->
            def files = new File(dir).list({d, f-> f ==~ regexGou } as FilenameFilter).toList()
            
            if( chrsToUse )
                files = files.findAll{ (it=~regexGou)[0][1] in chrsToUse }
            
            files.each{ file->
                def mat = (file =~ regexGou)
                def chr = mat[0][1]
                
                def reader = Utils.createReader( new File(dir+file) )

                String outname = "${output}tempSet_${chr}_${swSize}${set}.haped"

                if( !(new File(outname)).exists() ){
                    def writer = new BufferedWriter( new FileWriter(outname) )

                    println "Generating sliding windows for ${file} in ${outname}"

                    reader.splitEachLine("\\s"){ toks->
                        //parent<->unaffected, children<->affected
                        String affCode = (toks[2]=='0' && toks[3]=='0') ? Main.unaffCode : Main.affCode
                        
                        if( affCode==Main.unaffCode || set==TES_LBL ) {//dont write children patterns in training set
                            def line = new StringBuilder()

                            line << 
            "${toks[0]} ${toks[1]} ${toks[2]} ${toks[3]} ${toks[4]} ${affCode} ${toks[6]}"

                            int headLen = line.length() 

                            for(int i=7; i<(toks.size()-(swSize-1)*2); i+=2){
                                def trans = new StringBuilder()
                                def untrans = new StringBuilder()

                                (0..(swSize-1)).each{ j->
                                    trans << recodeBase( toks[i + 2*j], swSize )
                                    untrans << recodeBase( toks[(i+1) + 2*j], swSize )
                                }
                                line << " ${trans.toString()} ${untrans.toString()}"
                            }

                            //check length
                            int expLen = (toks.size()-7-(swSize-1)*2)*(swSize+1)
                            assert (line.length()-headLen)==expLen, "Haplotype string length error, expected=${expLen}, found=${line.length()-headLen}"

                            writer.writeLine( line.toString() )
                        }
                    }
                    writer.close()
                    reader.close()
                }
                else{
                    println "Sliding windows file ${outname} already exists"
                }
            }
        }
        
        if( !train )
            doPhase(TES_LBL,test)
        else
            Utils.runClosures([{doPhase(DAT_LBL,train)}, {doPhase(TES_LBL,test)}], THREADS)
    }
    
    
    /**
     *
     */
    def genArff( patternsArr, relation, arffFile, type){
        def writer = new BufferedWriter(new FileWriter(arffFile))
        
        writer.writeLine("@RELATION ${relation}")
        writer.writeLine('')
        
        if( type in [Classify.P_phased, Classify.P_G2] ){
            selectedHaplo.each{ 
                writer.writeLine("@ATTRIBUTE '${it.chr}:${it.id}T' {0,1}")
                writer.writeLine("@ATTRIBUTE '${it.chr}:${it.id}U' {0,1}") 
            }
        }
        else if( type==Classify.P_rScore ){
            selectedHaplo.each{ 
                writer.writeLine("@ATTRIBUTE '${it.chr}:${it.id}' {0,1,2}")
            }
        }
        else{
            selectedHaplo.each{ 
                writer.writeLine("@ATTRIBUTE '${it.chr}:${it.id}' {0,1}")
            }
        }
        
        writer.writeLine("@ATTRIBUTE ${PHENO_ATT_NAME} {1,2}")
        writer.writeLine('')
        writer.writeLine('@DATA')
        
        int start = getStartRow(arffFile)
        int cols = selectedHaplo.size()
        
        def attValue = { val -> ( val>2 ) ? '?' : val }
        
        (0..patternsArr.length-1).each{ i-> 
            def pat = patternsArr[i]
            writer.writeLine("%${pat.id}")
            def scores = new StringBuilder()
            
            if( type in [Classify.P_phased, Classify.P_G2] ){
                (0..(cols-1)).each{ 
                    scores << "${attValue(rAlleles.get(i+start, it*2))},${attValue(rAlleles.get(i+start, it*2+1))},"
                }
                scores << pat.phenotype
            }
            else if( type==Classify.P_rScore ){
                (0..(cols-1)).each{ 
                    scores << "${attValue(rAlleles.get(i+start, it*2) + rAlleles.get(i+start, it*2+1))},"
                }
                scores << pat.phenotype
            }
            else{
                (0..(cols-1)).each{ 
                    scores << "${attValue(rAlleles.get(i+start, it*2))},"
                }
                scores << Main.affCode + "\n"
                (0..(cols-1)).each{ 
                    scores << "${attValue(rAlleles.get(i+start, it*2+1))},"
                }
                scores << pat.phenotype
            }
            
            writer.writeLine( scores.toString() )
        }
                  
        writer.close()
    }
    
    /**
     *
     */
    static boolean messPhase(arffFile, type, double messProb, boolean messHaplo = true){
        
        if( !(type in [Classify.P_phased, Classify.P_G2]) ){
            println "messPhase: wrong type of file"
            return false
        }
        // generate different file
        String newFile = arffFile + (messHaplo ? MESS_HAP_EXT : MESS_CHR_EXT)
        
        def reader = Utils.createReader(new File(arffFile))
        def writer = new BufferedWriter(new FileWriter(newFile))
        
        String line = null
        boolean patternsZone = false
        int attIdx = 0
        String currChr = ''
        def chrIdxStart = [] //start index of chromosomes
        
        while( (line = reader.readLine())!=null ){
            if( line.isEmpty() ){
                writer.writeLine('')
            }
            else if( line.startsWith('%') ){
                writer.writeLine(line)
            }
            else if( line.startsWith('@ATTRIBUTE') ){
                //read attribute
                def toks = line.split("\\s")
                writer.writeLine(line)
                
                if( toks[1]!=PHENO_ATT_NAME ){
                    String chr = toks[1].substring(1, toks[1].indexOf(':') )
                    
                    if( chr!=currChr ){
                        currChr = chr
                        chrIdxStart << attIdx
                    }
                }
                
                attIdx += 2
            }
            else if( patternsZone ){
                int lim = line.length()-1
                boolean messChr = false
                
                for( int i=0; i<lim; i+=4 ){
                    if( messHaplo ){
                        if( Math.random()<messProb )
                            writer.write("${line[i+2]},${line[i]},")
                        else
                            writer.write("${line[i]},${line[i+2]},")
                    }
                    else{
                        if( chrIdxStart.any{it==i} )
                            messChr = (Math.random()<messProb)
                        
                        if( messChr )
                            writer.write("${line[i+2]},${line[i]},")
                        else
                            writer.write("${line[i]},${line[i+2]},")
                    }
                }
                
                //write phenotype
                writer.writeLine( line[line.length()-1] )
            }
            else if( line.startsWith('@DATA') ){
                // patterns begin
                patternsZone = true
                writer.writeLine(line)
            }
            else if( line.startsWith('@RELATION') ){
                writer.writeLine(line)
            }
        }
        
        reader.close()
        writer.close()
        
        return true
    }
    
    /**
     *
     */
    private getSNPs(String dir, String chr, int start, int end){
        def files = new File(dir).list({d, f-> f ==~ regexPou } as FilenameFilter).toList()
        def file = files.find{ (it=~regexPou)[0][1]==chr }
        mapHaplo[chr] = [:] as TreeMap
        
        def reader = Utils.createReader( new File(dir+file) )
        int i=0
        reader.eachLine{ line->
            int coord = line as Integer
            
            if( coord>=start && coord<=end )
                mapHaplo[chr][i] = new HaploData(id:line, chr:chr)
            
            i++
        }
        
        reader.close()
    }
    
    /**
     * Calculates high and low risk sequences (use it for hapLen=1)
     */
    private getAlleles(String input, int hapLen){
        def regex = regexTrainHPed
            
        // get hped files 
        def files = new File(input).list({d, f-> f ==~ regex } as FilenameFilter).toList()

        files.each{ file->
            def mat = (file =~ regex)
            def chr = mat[0][2]

            def reader = Utils.createReader( new File(input+file) )

            reader.eachLine{ line ->
                def tokens = line.split("\\s",8)
                
                String genotype = tokens[7]//remainder of the line
                int step = hapLen*2+2

                mapHaplo[chr].each{ idx, haploData->
                    int base = idx*step
                    def alleles = [ genotype.substring(base, base+hapLen),
                            genotype.substring(base+hapLen+1, base+step-1) ].sort()
                    
                    if( alleles[0]!='?' && alleles[0]!=alleles[1] ){
                        haploData.highSeqs << alleles[0]
                        haploData.lowSeqs << alleles[1]
                    }
                }
            }

            reader.close()
        }
        
        //test per SNP alleles count
        mapHaplo.each{ chr, map->
            map.each{ idx, haploData->
                assert haploData.highSeqs.size()==haploData.lowSeqs.size(), 'Different #risk alleles'
                assert haploData.highSeqs.size()==1, '#risk alleles <> 1'
            }
        }
    }
 
    
}

