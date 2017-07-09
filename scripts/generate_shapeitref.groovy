#!/usr/bin/env groovy

import org.clados.gwasrisk.PhasingImpute
import org.clados.gwasrisk.PhasingSnpData

//gou file constants
int FAM_IDX = 0 //column index
int ID_IDX = 1 //column index 
int AFF_IDX = 5 //column index
int GENDER_IDX = 4 //column index

// modes
String MODE_CONTROL = 'control'
    
def regexPou = /\w+_[a-zA-Z]*(\d+)\w+\.pou/
def regexRs = /\w+_[a-zA-Z]*(\d+)\w+\.rs/
def regexGou = /(\w+_[a-zA-Z]*)(\d+)(\w+)\.gou/

// checks whether a gou record corresponds to a parent
def isParent = { toks ->
    return (toks[2]=='0' && toks[3]=='0')
}
// checks whether the given chrom. numbers are equal
def isEqualChr = { chr1, chr2->
    return (chr1 as Integer)==(chr2 as Integer)
}
    
//check args
if( !this.args || this.args.length!=2 ){
    println 'Generate shapeit reference hap/legend/sample'
    println 'usage: generate_shapeitref.groovy <input_dir> <output_dir>'
    return 1
}

boolean onlyParents = true 
String mode = null // modes: 'control' or null
String dir = this.args[0]
String outdir = this.args[1]

if( !dir.endsWith('/') )
    dir += '/'

if( !outdir.endsWith('/') )
    outdir += '/'

println "Input dir: ${dir}"
println "Output dir: ${outdir}"
String filePref = PhasingImpute.getFilePref(dir, regexGou)

def gouFiles = new File(dir).list({d, f-> f ==~ regexGou } as FilenameFilter).toList()
gouFiles = gouFiles.findAll{ (new File(dir+it).length()) > 0L }

def pouFiles = new File(dir).list({d, f-> f ==~ regexPou } as FilenameFilter).toList()
def rsFiles = new File(dir).list({d, f-> f ==~ regexRs } as FilenameFilter).toList()


// read subjects info
def subjInfo = []

def reader = new BufferedReader(new FileReader( new File(dir+gouFiles[0]) ))

reader.eachLine{ line->
    def toks = line.split("\\s",8)

    if( !onlyParents || (onlyParents && isParent(toks)) ){
        def subjmap = [ (FAM_IDX):toks[FAM_IDX], (ID_IDX):toks[ID_IDX], 
            (AFF_IDX):toks[AFF_IDX], (GENDER_IDX):toks[GENDER_IDX] ]

        subjInfo << subjmap
    }
}

reader.close()

// generate .sample
def fileSample = "${filePref}_${mode ?: 'all'}.sample"
def population = 'CEU'
def writer1 = new PrintWriter(outdir+fileSample)
writer1.println('sample population group sex') //write header

println "Generating sample file with ${subjInfo.size()} samples"

subjInfo.eachWithIndex{ subj, i->
    def subgrp = 'control'
    writer1.println( "${subj[FAM_IDX]}:${subj[ID_IDX]} ${population} ${subgrp} ${subj[GENDER_IDX]}" )
}

writer1.close()

// main loop
gouFiles.each{ fileGou->
    def mat = (fileGou =~ regexGou)
    def chr = mat[0][2]
    def fileRs = rsFiles.find{ isEqualChr((it =~ regexRs)[0][1],chr) }
    def file = pouFiles.find{ isEqualChr((it =~ regexPou)[0][1],chr) }

    // read snps from .rs and .pou
    def snpsData = []

    reader = new BufferedReader(new FileReader( new File(dir+file) ))
    def readerRs = new BufferedReader(new FileReader( new File(dir+fileRs) ))
    int i = 0

    reader.eachLine{ pos->
        def rsId = readerRs.readLine()
        snpsData << new PhasingSnpData( id:(i++), rsId:rsId, pos:(pos as Integer) )
    }

    reader.close()
    readerRs.close()

    // read subject genotypes
    def subjects = PhasingImpute.readGouGenotypes(dir+fileGou, onlyParents)

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
        snp.alleles = PhasingImpute.getAllelesFromSubjects(subjects, j)
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
        }

        writer.println(line.toString())

        //check for errors
        String errorMsg = 'Wrong number of haplotypes generated; snp:'+snp.rsId

        if( mode == null )
            assert line.length()==subjects.size()*2*2-1, errorMsg
        else if( mode == MODE_CONTROL ) 
            assert line.length()==subjects.size()*2-1, errorMsg
    }

    writer.close()
}
        
return 0