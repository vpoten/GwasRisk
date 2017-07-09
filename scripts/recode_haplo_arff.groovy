#!/usr/bin/env groovy

import java.util.zip.GZIPInputStream

def REL_FIELD = '@RELATION'
def ATT_FIELD = '@ATTRIBUTE'
def DAT_FIELD = '@DATA'
def HAPLO_CODES = ['1','2']

//get and check args
if( !this.args || this.args.length!=1 ){
    println 'Recode separated haplotype arff to genotype arff'
    println 'usage: recode_haplo_arff.groovy <input_arff>'
    return 1
}

String input = this.args[0]

println "Input arff: ${input}"

def file = new File(input)

def reader = (file.name.endsWith('.gz')) ? 
        new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)))) : file.newReader()
        
def outFile = input.substring(0, input.indexOf('.arff'))+'.geno.arff'
println "Output arff: ${outFile}"

def writer = new File(outFile).newWriter()

boolean inData = false
int numAtts = 0
def attributes = []

reader.eachLine{ line->
    def upline = line.toUpperCase()
    
    if( line.isEmpty() ){
        writer.println('')
    }
    else if( line.startsWith('%') ){
        writer.println(line)
    }
    else if( upline.startsWith(REL_FIELD) ){
        writer.println(line+'.geno')
    }
    else if( upline.startsWith(ATT_FIELD) ){
        def toks = line.split("\\s")
        def name = toks[1].startsWith("'") ? toks[1].substring(1,toks[1].length()-1) : toks[1]
        attributes << name
    }
    else if( upline.startsWith(DAT_FIELD) ){
        numAtts = (attributes.size()-1)/2
        
        (0..numAtts-1).each{
            def at1 = attributes[it*2]
            def at2 = attributes[it*2+1]
            def name = new StringBuilder()
            int i=0
            while(at1[i]==at2[i]){
                name << at1[i++]
            }
            
            writer.println("${ATT_FIELD} '${name}' {0,1,2}") 
        }
        
        //write class attribute
        writer.println("${ATT_FIELD} '${attributes.last()}' {1,2}")
        
        writer.println('\n'+DAT_FIELD)
        inData = true
    }
    else if( inData ){
        for(int i=0; i<numAtts; i++){
            int idx = i*4
            def left = line[idx]
            def right = line[idx+2]
            def geno = '1'
            
            if( left==right ){
                geno = (left==HAPLO_CODES[0]) ? '0' : '2'
            }
            
            writer.print(geno+',')//write genotype
        }
        
        //write class attribute
        writer.println(line[numAtts*4])
    }
}

writer.close()
reader.close()

return 0