#!/usr/bin/env groovy

//constants
def FAM_IDX = 0 //column index
def ID_IDX = 1 //column index
def AFF_IDX = 5 //column index
def regexGou = /(\w+_[a-zA-Z]*)(\d+)(\w+)\.gou/
def caseCode = '2'

//check args
if( !this.args || this.args.length!=2 ){
    println 'Selects one case and one control of each family.'
    println 'usage: select_gou_casecont <input_dir> <output_dir>'
    return 1
}

String dir = this.args[0]
String outdir = this.args[1]

if( !dir.endsWith('/') )
    dir += '/'

if( !outdir.endsWith('/') )
    outdir += '/'

println "Input dir: ${dir}"
println "Output dir: ${outdir}"

// get .gou files
def files = new File(dir).list({d, f-> f ==~ regexGou } as FilenameFilter).toList()
files = files.findAll{ (new File(dir+it).length()) > 0L }

// read subjects
def families = [:] as TreeMap
def reader = new BufferedReader(new FileReader( new File(dir+files[0]) ))
    
reader.eachLine{ line->
    def toks = line.split("\\s",8)
    def subjmap = [(FAM_IDX):toks[FAM_IDX], (ID_IDX):toks[ID_IDX], (AFF_IDX):toks[AFF_IDX]]
    
    if( families[subjmap[FAM_IDX]]==null )
        families[subjmap[FAM_IDX]] = []
        
    families[subjmap[FAM_IDX]] << subjmap
}

reader.close()

println "${families.size()} families readed"
int ncases = 0
int ncontrols = 0

def selected = [] as TreeSet
def signature2 = { fid, sid-> return "${fid}:${sid}" }
def signature = { subj-> return signature2(subj[FAM_IDX], subj[ID_IDX]) }


// select subjects
families.each{ famId, subjs->
    def cases = subjs.findAll{ it[AFF_IDX]==caseCode }
    def controls = subjs.findAll{ it[AFF_IDX]!=caseCode }
    ncases += (cases ? cases.size() : 0)
    ncontrols += (controls ? controls.size() : 0)
    
    if( cases && controls){
        selected << signature( cases[(int)(Math.random()*cases.size())] )
        selected << signature( controls[(int)(Math.random()*controls.size())] )
    }
}

println "${ncases} cases and ${ncontrols} controls readed"
println "${selected.size()} subjects selected"

// write new .gou files
files.each{ file->
    def mat = (file =~ regexGou)
    def pref = mat[0][1]
    def chr = mat[0][2]
    def suff = mat[0][3]

    reader = new BufferedReader(new FileReader( new File(dir+file) ))
    def outFile = "${pref}${chr}${suff}_select.gou"
    def writer = new PrintWriter(outdir+outFile)
    
    println "Generating ${outFile} from ${file}"

    reader.eachLine{ line->
        def toks = line.split("\\s",8)
        def key = signature2(toks[FAM_IDX],toks[ID_IDX])
        
        if( key in selected )
            writer.println(line)
    }

    reader.close()
    writer.close()
}
        
return 0