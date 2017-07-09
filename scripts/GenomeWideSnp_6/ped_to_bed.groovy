#!/usr/bin/env groovy

def CHR_LIST = (1..22).collect{it as String}
def PLINK = 'plink'
def PLINK_FILTER = '--hwe-all --maf 0.05 --hwe 0.0001 --geno 0.05 --me 0.05 0.1 --missing-genotype ?'

def regexPed = /\w*_(\d+)\.ped/


//get and check args
if( !this.args || this.args.length!=1 ){
    println 'Convert plink format PED to BED'
    println 'usage: ped_to_tped.groovy <input_dir>'
    return 1
}

String input = this.args[0]

if( !input.endsWith('/') ){ input+='/'}

println "Input dir: ${input}"

println "Start time: ${new Date()}"

def pedFiles = new File(input).list({d, f-> f ==~ regexPed } as FilenameFilter).toList()


CHR_LIST.each{ chr->
    def file = pedFiles.find{ (it=~regexPed)[0][1]==chr }
    println "\nchr ${chr}: ${file}"
    def outFile = file.replace('.ped','')
    def command = 
"${PLINK} --noweb --ped ${input+file} --map ${input+file.replace('.ped','.map')} --make-bed ${PLINK_FILTER} --out ${input+outFile}"
    
    println "exec: ${command}"
    if( command.execute().waitFor()!=0 ){
        println "Error converting .ped to .bed"
    }
    
    //remove useless output
    "rm -f ${input+outFile}.nosex".execute().waitFor()
}


println "End time: ${new Date()}"

return 0