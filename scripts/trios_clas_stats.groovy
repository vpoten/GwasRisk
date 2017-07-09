#!/usr/bin/env groovy

//constants
def THRESHOLDS = [1e-7]
int swSize = 2
String HAPLO_PREFIX = 'haplotypes_'
String subname = 'phased_pv'
String AFF_CODE = '2'
def attrRegex = /@ATTRIBUTE\s+'(\d+):(\d+[TU])'\s+\{0,1\}/
def hpedRegex = /\w+_(\d+)_(\d+)\.tes\.haped/

//measures
String M_AFF_HI_RISK = 'hiRiskAff'
String M_UNAFF_LO_RISK = 'loRiskUnaff'
String M_POS_MISS = 'posMissing'

if( !this.args || this.args.length!=2 ){
    println 'Calc. Stats.'
    println 'usage: trios_clas_stats.groovy <dir1> <dir2>'
    return 1
}

def dirs = [this.args[0], this.args[1]]

(0..1).each{ 
    if( !dirs[it].endsWith('/') )
        dirs[it] = dirs[it]+'/' 
        
    println "Dir ${it}: ${dirs[it]}"
}
   
println "sWindow size: ${swSize}" 

// closure: compose arff file name
def arffFileName = { cutPval->
    def cutStr = (cutPval as String).substring(2)//decimal part of number
    return "${HAPLO_PREFIX}${subname}${cutStr}_${swSize}.tes.arff"
}

def newMeasureMap = {
    return [(M_AFF_HI_RISK):0, (M_UNAFF_LO_RISK):0, (M_POS_MISS):(0..swSize-1).collect{0.0}]
}

// 1 - read arff files

// map where to store measures
def measures = [:]

THRESHOLDS.each{ thr->
    measures[thr] = [:]
    
    dirs.each{ dir->
        boolean patterns = false
        def attributes = []
        def attStats = [:]

        //parse arff test file
        println "Parsing ${dir+arffFileName(thr)}"
        
        (new File( dir+arffFileName(thr) )).eachLine{ line->
            if( line.startsWith('%') || line.isEmpty() ){
                // nothing to do
            }
            else if( line ==~ attrRegex ){
                def mat = (line =~ attrRegex)
                def chr = mat[0][1]
                def id = mat[0][2]
                
                attributes << "${chr}:${id}"
                attStats["${chr}:${id}"] = newMeasureMap()
            }
            else if( line.startsWith('@DATA') ){
                patterns = true
            }
            else if( patterns ){
                def toks = line.split(',')
                boolean affected = (toks[toks.size()-1]==AFF_CODE)
                
                (0..toks.size()-2).each{
                    def currMap = attStats[attributes[it]]
                    
                    if( affected && toks[it]=='1' )
                        currMap[M_AFF_HI_RISK] = (currMap[M_AFF_HI_RISK] + 1)
                    else if( !affected && toks[it]=='0' )
                        currMap[M_UNAFF_LO_RISK] = (currMap[M_UNAFF_LO_RISK] + 1)
                }
            }
        }
        
        measures[thr][dir] = attStats
    }
}

// 2 - read hped files
THRESHOLDS.each{ thr->
    dirs.each{ dir->
        //get hped files for window size == swSize
        def hpedFiles = new File(dir).list({d, f-> f ==~ hpedRegex } as FilenameFilter).toList()
        hpedFiles = hpedFiles.findAll{ ((it=~hpedRegex)[0][2] as Integer)==swSize }

        //get hped files for selected chromosomes
        def chrs = measures[thr][dir].keySet().collect{ it.substring(0, it.indexOf(':')) } as Set
        hpedFiles = hpedFiles.findAll{ (it=~hpedRegex)[0][1] in chrs }
        
        def attStats = measures[thr][dir]
        double numSubjects = 0
        
        hpedFiles.each{ file->
            def chr = (file=~hpedRegex)[0][1]
            def positions = (measures[thr][dir].keySet().findAll{ it.startsWith(chr+':') }.
                collect{ (it.substring(it.indexOf(':')+1, it.length()-1)) as Integer }) as TreeSet
            int cont = 0
            
            println "Parsing ${dir+file}"
            (new File( dir+file )).eachLine{ line->
                def toks = line.split("\\s")
                cont++
                
                positions.each{ pos->
                    int idx = pos*2
                    def ids = ['T','U'].collect{"${chr}:${pos}${it}"}
                    
                    (0..swSize-1).each{
                        attStats[ids[0]][M_POS_MISS][it] =  attStats[ids[0]][M_POS_MISS][it] + (toks[7+pos][it]=='?' ? 1 : 0)
                        attStats[ids[1]][M_POS_MISS][it] =  attStats[ids[1]][M_POS_MISS][it] + (toks[7+pos+1][it]=='?' ? 1 : 0)
                    }
                }
            }
            
            if( numSubjects==0 )
                numSubjects = cont
        }
        
        attStats.each{ k, v->
            (0..v[M_POS_MISS].size()-1).each{ v[M_POS_MISS][it] = v[M_POS_MISS][it]/numSubjects }
        }

    }
}

// Final - print results
println "\nFinal Results:"

THRESHOLDS.each{ thr->
    println "\n====== Threshold: ${thr} ======"
    
    dirs.each{ dir->
        println "\n*** Dataset ${dir}***\n"
        def attStats = measures[thr][dir]
        
        attStats.each{ k,v->
            println "\nAttribute ${k}:"
            
            [M_AFF_HI_RISK,M_UNAFF_LO_RISK].each{
                println "${it}: ${v[it]}"
            }
            
            String cad = "Position miss. perc. (%):"
            v[M_POS_MISS].eachWithIndex{val,idx-> cad += " ${idx}=${val*100}" }
            println cad
        }
    }
}

return 0