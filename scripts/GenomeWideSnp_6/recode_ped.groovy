#!/usr/bin/env groovy

def CHR_LIST = (1..22).collect{it as String}
def PLINK = '/home/clados/victor/plink-1.07-x86_64/plink'
def PLINK_FILTER = '--hwe-all --maf 0.05 --hwe 0.0001 --geno 0.05 --me 0.05 0.1 --missing-genotype ?'

def regexPed = /\w*_(\d+)\.ped/


//get and check args
if( !this.args || this.args.length!=2 ){
    println 'Recode plink PED files'
    println 'usage: recode_ped.groovy <input_dir> <output_dir>'
    return 1
}

String input = this.args[0]
String output = this.args[1]

if( !input.endsWith('/') ){ input+='/'}
if( !output.endsWith('/') ){ output+='/'}

println "Input dir: ${input}"
println "Output dir: ${output}"

println "Start time: ${new Date()}"

def pedFiles = new File(input).list({d, f-> f ==~ regexPed } as FilenameFilter).toList()


CHR_LIST.each{ chr->
    def file = pedFiles.find{ (it=~regexPed)[0][1]==chr }
    println "\nchr ${chr}: ${file}"
    
    def command = 
"${PLINK} --noweb --ped ${input+file} --map ${input+file.replace('.ped','.map')} --recode ${PLINK_FILTER} --out ${output+file.replace('.ped','')}"
    
    println "exec: ${command}"
    if( command.execute().waitFor()!=0 ){
        println "Error recoding .ped file"
    }
    
    //remove useless output
    "rm -f ${output+file}.nosex".execute().waitFor()
}


println "End time: ${new Date()}"

return 0