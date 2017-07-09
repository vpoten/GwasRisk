#!/usr/bin/env groovy

def CHR_LIST = (1..22).collect{it as String}
def PLINK = '/home/clados/victor/plink-1.07-x86_64/plink'
def PLINK_FILTER = ''

def regexPed = /\w*_(\d+)\.bed/


//get and check args
if( !this.args || this.args.length!=2 ){
    println 'Convert plink format BED to PED'
    println 'usage: ped_to_tped.groovy <input_dir> <out_dir>'
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
    def outFile = file.replace('.bed','')
    def command = 
"${PLINK} --noweb --bfile ${input+file.replace('.bed','')} --recode ${PLINK_FILTER} --out ${output+outFile}"
    
    println "exec: ${command}"
    if( command.execute().waitFor()!=0 ){
        println "Error converting .bed to .ped"
    }
    
    //remove useless output
    "rm -f ${output+outFile}.nosex".execute().waitFor()
}


println "End time: ${new Date()}"

return 0