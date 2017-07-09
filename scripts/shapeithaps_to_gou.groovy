#!/usr/bin/env groovy

//constants
int LEG_ID = 0 // column index
int LEG_POS = 1 // column index
int LEG_A0 = 2 // column index
int LEG_A1 = 3 // column index

// regex
def regexLegend = /\w+_[a-zA-Z]*(\d+)\w*\.legend/
def regexHaps = /(\w+_[a-zA-Z]*)(\d+)(\w*)\.haps/
def regexSample = /.+\.sample/

//check args
if( !this.args || this.args.length!=3 ){
    println 'Converts shapeit output files to gou.'
    println 'usage: shapeithaps_to_gou.groovy <shapeit_output_dir> <reference_dir> <output_dir>'
    return 1
}

String dir = this.args[0]
String refdir = this.args[1]
String outdir = this.args[2]

if( !dir.endsWith('/') ){ dir += '/' }
if( !refdir.endsWith('/') ){ refdir += '/' }
if( !outdir.endsWith('/') ){ outdir += '/' }

println "Input dir: ${dir}"
println "Reference dir: ${refdir}"
println "Output dir: ${outdir}"

// 1 - Read reference snps (.legend)
def refSnps = [:] as TreeMap
def legendFiles = new File(refdir).list({d, f-> f ==~ regexLegend } as FilenameFilter).toList()

legendFiles.each{ file->
    def mat = (file =~ regexLegend)
    def chr = mat[0][1]
    refSnps[chr] = []
    
    def reader = new BufferedReader(new FileReader( new File(refdir+file) ))
    reader.readLine() //skip header
    
    reader.splitEachLine("\\s"){ toks-> 
        // legend: id position a0 a1
        refSnps[chr] << [ (LEG_ID):toks[LEG_ID], (LEG_POS):(toks[LEG_POS] as Integer), 
            (LEG_A0):toks[LEG_A0], (LEG_A1):toks[LEG_A1] ]
    }
    
    reader.close()
    println "${refSnps[chr].size()} snps found in .legend for chr ${chr}"
}

// 2 - read sample file
def samplefiles = new File(dir).list({d, f-> f ==~ regexSample } as FilenameFilter).toList()
samplefiles = samplefiles.findAll{ (new File(dir+it).length()) > 0L }

def subjects = []

//get subjects from .sample file
def reader1 = new BufferedReader(new FileReader(new File(dir+samplefiles[0])))
reader1.readLine() //skip header
reader1.readLine()

reader1.splitEachLine("\\s"){ toks->
    // .sample columns: ID_1 ID_2 missing sex phenotype, Change it if different format
    subjects << 
        ("0 ${toks[0]} 0 0 ${toks[3] ?: 0} ${toks[4] ?: 0} ${toks[1]}" as String)
}

reader1.close()
println "${subjects.size()} subjects found in .sample"

// 3 - read haps file and convert it to gou
def files = new File(dir).list({d, f-> f ==~ regexHaps } as FilenameFilter).toList()
files = files.findAll{ (new File(dir+it).length()) > 0L }

files.each{ file ->
    def mat = (file =~ regexHaps)
    def pref = mat[0][1]
    def chr = mat[0][2]
    def suff = mat[0][3]
    
    //read genotypes from haps file
    def genotypes = subjects.collect{ new StringBuilder() }
    def reader = new BufferedReader(new FileReader( new File(dir+file) ))
    int contRef = 0
    int contMiss = 0
    
    def chrSnpsIds = refSnps[chr].collect{ it[LEG_ID] } as TreeSet
    
    def writeMissingSnp = {
        // fill missing snp with missing data
        println "Missing SNP ${refSnps[chr][contRef][LEG_ID]} in chr ${chr}"
        contMiss++
        (0..subjects.size()-1).each{ genotypes[it] << " ? ?" }
        chrSnpsIds.remove( refSnps[chr][contRef++][LEG_ID] )
    }

    reader.splitEachLine("\\s"){ toks->    
        // haps: Chr SNPID SNPPos allele1 allele2
        assert (toks[1] in chrSnpsIds), 'SNP '+toks[1]+' not in .legend SNP set for chr '+chr
        
        while( toks[1] != refSnps[chr][contRef][LEG_ID] ) {
            writeMissingSnp()
        }
        
        def bases = ['0':toks[3], '1':toks[4], '?':'?']
        int j = 0
        for(int i=5; i<toks.size(); i+=2){
            genotypes[j++] << " ${bases[toks[i]]} ${bases[toks[i+1]]}" 
        }

        assert (j==subjects.size()), 'Wrong subject count in _haps for '+toks[1]
        contRef++
        chrSnpsIds.remove(toks[1])
    }
    
    while( !chrSnpsIds.isEmpty() ){
        writeMissingSnp()
    }
    
    assert (contRef==refSnps[chr].size()), 'Wrong snp count for chr '+chr+' '+contRef+'/'+refSnps[chr].size()

    reader.close()
    println "${contMiss} missing SNPs in chr ${chr}"

    //write gou
    def outfile = "${pref}${chr}${suff}.gou"
    println "Generating GOU file ${outfile} from haps in ${file}"
    def writer = new FileWriter(outdir+outfile)

    subjects.eachWithIndex{ head, i->
        writer.write(head)
        writer.write(genotypes[i].toString())
        writer.write('\n')
    }

    writer.close()
}

return 0