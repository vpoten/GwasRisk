/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.parser

import org.clados.gwasrisk.Main
import org.clados.gwasrisk.Utils
import org.clados.gwasrisk.Classify

import org.jfree.chart.*
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.DefaultXYDataset
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.xy.XYDotRenderer
import org.jfree.chart.plot.ValueMarker
import org.jfree.ui.Layer
import org.jfree.chart.renderer.category.MinMaxCategoryRenderer
import java.awt.Color
import java.awt.Stroke
import java.awt.BasicStroke

/**
 *
 * @author victor
 */
class ResultsParser {

    public static final String TOK_FOLD = '***** Performing fold'
    public static final String TOK_DATA_CUT_PVAL = '>>>>> Generating dataset for'
    public static final String TOK_DATA_CUT_PVAL2 = 'Classify thr='
    public static final String TOK_SUMM_CLASIF = '=== Classifier'
    public static final String TOK_FINAL_RES = '***** Final Results'
    public static final String TOK_FINAL_CUT_PVAL = '===== P-value cut ='
    public static final String TOK_FINAL_CLASIF = '----- Classifier'
    public static final String TOK_FINAL_CLASS = 'Class'
    
    static measureRegex = /(\w+): MAX ([\d\.,]+) MIN ([\d\.,]+) AVG ([\d\.,]+)/
    static classProbReg = /Class (\w+): Prior probability = ([\d\.,]+)/
    static fixLogRegex = /Intercept=([\d\.,-]+), Slope=([\d\.,-]+)/
    static simpLogRegInter = /([\d\.,-]+) \+\s*/
    static simpLogRegSlop = /\[wGRS\] \* ([\d\.,-]+)\s*/
    static correctPctRegex = /Correctly Classified Instances\s+\d+\s+([\d\.,-]+)\s+%/
    
    
    //reverse comparator
    static def reverseComp= [ compare:{a,b-> b<=>a} ] as Comparator
    
    static String TABLE_CSSCLASS = 'report_table'
    static String HTML_OUT = 'tables.html'
    static String TABLE_OUT = 'exported_data.txt'
    static String RESULT_FOLDER = 'result/'
    static String CHART_FOLDER = 'charts/'
    
    static int WIDTH = 800
    static int HEIGHT = 600
    
    //readable classifier names
    static def classifNames = [(Classify.CLS_SIMPLE_LOG):'LR wGRS ML', (Classify.CLS_SIMPLE_LOG_GRS):'LR GRS ML',
        (Classify.CLS_NBC):'gNBC', (Classify.CLS_FIXED_LOG):'aNBC', (Classify.CLS_SVM):'gSVM',
        (Classify.CLS_FOREST):'gForest', (Classify.CLS_C45):'gC4.5', (Classify.CLS_BOOST):'gBoost',
        (Classify.CLS_NBC_G2):'g2NBC', (Classify.CLS_FOREST_G2):'g2Forest', (Classify.CLS_SVM_G2):'g2SVM',
        (Classify.CLS_BOOST_G2):'g2Boost', (Classify.CLS_C45_G2):'g2C4.5']
    
    //readable measures names
    static def measureNames = [(Classify.areaUnderROC):'AUC (2)', (Classify.truePositiveRate):'Sensitivity',
        (Classify.trueNegativeRate):'Specificity', (Classify.precision):'Precision (2)',
        (Classify.weightedAreaUnderROC):'AUC', (Classify.weightedPrecision):'Precision',
        (Classify.pctCorrect):'Correct pct.', (ResultData.FEAT_PCT_CORR):'Correct pct. (2)' ]
    
    //classifier colors
    static def classifColor = ['LR wGRS ML':Color.GREEN, 'LR GRS ML':Color.YELLOW,
        'gNBC':Color.BLUE, 'aNBC':Color.RED, 'gSVM':Color.ORANGE, 
        'gForest':Color.CYAN, 'gC4.5':Color.MAGENTA, 'gBoost':Color.PINK,
        'g2NBC':Color.RED, 'g2SVM':Color.ORANGE, 
        'g2Forest':Color.CYAN, 'g2C4.5':Color.MAGENTA, 'g2Boost':Color.PINK]
    
    //classifier stroke
    static def classifStroke = [ 'additive':(new BasicStroke(1.0f)),
        'dominant':(new BasicStroke(1.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10.0f, [30.0f,5.0f] as float[], 0.0f)),
        'recessive':(new BasicStroke(1.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10.0f, [10.0f,5.0f,2.0f,5.0f] as float[], 0.0f)) ]
    
    //html table style
    static def TABLE_STYLE_SHEET = """
<style type="text/css">
img {
    border: 1px solid #ccc;
}
.${TABLE_CSSCLASS} {
    border: 1px solid #ccc;
    width: 80%
}
.${TABLE_CSSCLASS} tr {
    border: 0;
}
.${TABLE_CSSCLASS} td, th {
    font: 12px verdana, arial, helvetica, sans-serif;
    line-height: 13px;
    padding: 5px 6px;
    text-align: left;
    vertical-align: top;
}
.${TABLE_CSSCLASS} th, caption {
    background: #fff;
    color: #666;
    font-size: 12px;
    font-weight: bold;
    line-height: 18px;
    padding: 2px 6px;
}
.${TABLE_CSSCLASS} caption {
    padding-top: 15px;
}
</style>
"""
    
    /**
     * helper method
     */
    static private def rowValue(row, measure, resData) {
        //row is a map with keys = {F_CUTPVAL,'1','2'}
        if( measure in [Classify.areaUnderROC, Classify.precision] ){
            // calculate weighted measure using probabilities
            if( resData.classProb['1'] && resData.classProb['2'] )
                return resData.classProb['1']*row['1'] + resData.classProb['2']*row['2']
            else
                return row['2']
        }
        else{
            if( measure in [Classify.pctCorrect,ResultData.FEAT_PCT_CORR] )
                return row['2']*0.01
            else
                return row['2']
        }
    }
    
    /**
     *
     */
    static int doParse(args){
        String input = args.find{ it.startsWith(Main.OPT_INPUT_DIR) }
        def dirs = Main.checkAndCleanDirOpts([input])
        
        def error = {
            System.err.println("Usages:\n"+Main.USA_PARSE_RES)
            return 1 //error
        }
        
        if(!dirs)
            return error()
            
        input = dirs[0]
        def regex = /(.+)\.result["\.gz"]*/
        def data = parseDir(input, regex, 1) // data is a Map with key=disease
        
        // Build and write tables and charts
        Utils.createDir(input+RESULT_FOLDER)
        Utils.createDir(input+RESULT_FOLDER+CHART_FOLDER)
        
        //file where to write html output
        def fileWriter = new PrintWriter(input+RESULT_FOLDER+HTML_OUT)
        fileWriter.println("<html>\n<head>\n${TABLE_STYLE_SHEET}</head>\n<body><br/>")
        
        
        //1. Build a table per classifier and measure (rows:cutPvals, cols:diseases)
        def measures = [ Classify.weightedAreaUnderROC, Classify.areaUnderROC, 
            Classify.truePositiveRate, Classify.trueNegativeRate, Classify.weightedPrecision, 
            Classify.precision, Classify.pctCorrect ].findAll{ data[data.firstKey()].existMeasure(it) }
        
        if( !(Classify.pctCorrect in measures) ){
            //calculates pct. of correct classified manually
            data.each{ it.value.calcPctCorrect() }
            measures << ResultData.FEAT_PCT_CORR
        }
        
        def tableList = []
        
        def classifiers =  data[data.firstKey()].getClassifiers() as TreeSet
        def cutPvals = data[data.firstKey()].getCutoffVals()
        
        classifiers.each{ classifier->
            measures.each{ measure->
                //create table object
                TableWriter table = new TableWriter( tableClass:TABLE_CSSCLASS, rowHeader:true, 
                    caption:"${classifNames[classifier]} - ${measureNames[measure]}" )

                //build rows, first colum of each one
                table.thead << ['Threshold']
                cutPvals.each{ table.tbody << [it] }

                data.each{ disease, resData->
                    table.thead[0] << disease//add disease to header
                    def rows = resData.query(ResultData.QUERY_MEASURE_TABLE, [classifier,measure])

                    rows.each{ row-> //row is a map with keys = {F_CUTPVAL,'1','2'}
                        def val = rowValue(row,measure,resData)
                        //add value to row
                        table.tbody.find{ it[0]==row[ResultData.F_CUTPVAL] } << String.format('%.4f',val)
                    }
                }

                tableList << table
            }
        }
        
        TableWriter.write(fileWriter, tableList)
        fileWriter.println("<br/>")
        
        //2. Build and write charts
        
        
        //chart 1: disease - measure (X=threshols,Y=measure values)
        data.each{ disease, resData->
            measures.each{ measure->
                // create Dataset
                def dataset = new DefaultCategoryDataset()

                classifiers.each{ classifier->
                    def rows = resData.query(ResultData.QUERY_MEASURE_TABLE, [classifier,measure])
                    rows.each{ row-> //row is a map with keys = {F_CUTPVAL,'1','2'}
                        def val = rowValue(row,measure,resData)
                        // add value to Dataset
                        dataset.addValue(val, classifNames[classifier], row[ResultData.F_CUTPVAL])
                    }
                }

                // render chart
                def imgFile = "${disease}_${measureNames[measure]}.png"

                exportLineChart(dataset, "${disease} - ${measureNames[measure]}", 
                    'Threshold', measureNames[measure], input+RESULT_FOLDER+CHART_FOLDER+imgFile )

                //write image tag to result file
                fileWriter.println("<img src=\"${CHART_FOLDER+imgFile}\" alt=\"${disease} - ${measureNames[measure]}\" /><br/><br/>") 
            }
        }
        
        fileWriter.println("<br/>")
        
        //chart 2: disease - threshold (X=a0_NBC,Y=a0_ML)
        
        def toDoubleArray = { list ->
            def array = new double [list.size()][list[0].size()]
            list.eachWithIndex{ l2, i->
                l2.eachWithIndex{ ele, j->
                    array[i][j] = ele
                }
            }
            return array
        }
        
        data.each{ disease, resData->
            // create Dataset
            def dataset = new DefaultXYDataset()
                
            cutPvals.each{ cutPval->
                def rows = resData.query(ResultData.QUERY_A0, [cutPval])
                def datarows = [[],[]]
                
                rows.each{ row-> //row is a map with keys = {F_A0_NBC,F_A0_ML'}
                    if( row[ResultData.F_A0_NBC]!=null && row[ResultData.F_A0_ML]!=null ){
                        datarows[0] << row[ResultData.F_A0_NBC]
                        datarows[1] << row[ResultData.F_A0_ML]
                    }
                }
                // add value to Dataset
                dataset.addSeries( cutPval, toDoubleArray(datarows))
            }
            
            // render chart
            def imgFile = "${disease}_a0.png"

            exportScatterPlot(dataset, "${disease} - a0_NBC vs a0_ML", 
                'a0_NBC', 'a0_ML', input+RESULT_FOLDER+CHART_FOLDER+imgFile )

            //write image tag to result file
            fileWriter.println("<img src=\"${CHART_FOLDER+imgFile}\" alt=\"${disease} - a0_NBC vs a0_ML\" /><br/><br/>")
        }
        
        //chart 3: disease - threshold (X=thresholds,Y=a1_ML)
        data.each{ disease, resData->
            // create Dataset
            def dataset = new DefaultCategoryDataset()
                
            cutPvals.each{ cutPval->
                def rows = resData.query(ResultData.QUERY_A1, [cutPval])
                rows.eachWithIndex{ row, i-> //row is a map with keys = {F_A1_ML'}
                     dataset.addValue(row[ResultData.F_A1_ML], i, cutPval)
                }
            }
            
            // render chart
            def imgFile = "${disease}_a1.png"

            exportBarChart(dataset, "${disease} - a1_ML", 
                'Threshold', 'a1_ML', input+RESULT_FOLDER+CHART_FOLDER+imgFile )

            //write image tag to result file
            fileWriter.println("<img src=\"${CHART_FOLDER+imgFile}\" alt=\"${disease} - a1_ML\" /><br/><br/>")
        }
        
        
        //end result writing
        fileWriter.println('</body></html>')
        fileWriter.close()
        
        return 0
    }
    
    /**
     *
     */
    static int doParseSnpArff(args){
        String input = args.find{ it.startsWith(Main.OPT_INPUT_DIR) }
        def dirs = Main.checkAndCleanDirOpts([input])
        
        def error = {
            System.err.println("Usages:\n"+Main.USA_PARSE_RES)
            return 1 //error
        }
        
        if(!dirs)
            return error()
            
        input = dirs[0]
        def regex = /(.+)\.result["\.gz"]*/
        def data = parseDir(input, regex, 1, true) // data is a Map with key=disease
        
        // Build and write tables and charts
        Utils.createDir(input+RESULT_FOLDER)
        Utils.createDir(input+RESULT_FOLDER+CHART_FOLDER)
        
        //file where to write html output
        def fileWriter = new PrintWriter(input+RESULT_FOLDER+HTML_OUT)
        fileWriter.println("<html>\n<head>\n${TABLE_STYLE_SHEET}</head>\n<body><br/>")
        
        
        //1. Build a table per classifier and measure (rows:cutPvals, cols:diseases)
        def measures = [ Classify.weightedAreaUnderROC, Classify.truePositiveRate, 
            Classify.trueNegativeRate, Classify.weightedPrecision, 
            Classify.pctCorrect ].findAll{ data[data.firstKey()].existMeasure(it) }
        
        if( !(Classify.pctCorrect in measures) ){
            //calculates pct. of correct classified manually
            data.each{ it.value.calcPctCorrect() }
            measures << ResultData.FEAT_PCT_CORR
        }
        
        def tableList = []
        
        def classifiers =  data[data.firstKey()].getClassifiers() as TreeSet
        def cutPvals = [] as Set
        data.each{ k,v-> cutPvals.addAll( v.getCutoffVals() ) }
        cutPvals = cutPvals.sort().reverse()
        
        classifiers.each{ classifier->
            measures.each{ measure->
                //create table object
                TableWriter table = new TableWriter( tableClass:TABLE_CSSCLASS, rowHeader:true, 
                    caption:"${classifNames[classifier]} - ${measureNames[measure]}" )

                //build rows, first colum of each one
                table.thead << ['Threshold']
                cutPvals.each{ table.tbody << [it] }

                data.each{ disease, resData->
                    table.thead[0] << disease//add disease to header
                    def rows = resData.query(ResultData.QUERY_MEASURE_TABLE, [classifier,measure])

                    cutPvals.each{ pval->
                        def row = rows.find{ it[ResultData.F_CUTPVAL]==pval }
                        def val = (row) ? String.format('%.4f',rowValue(row,measure,resData)) : 'NA'
                        //add value to row
                        table.tbody.find{ it[0]==pval } << val
                    }
                }

                tableList << table
            }
        }
        
        TableWriter.write(fileWriter, tableList)
        fileWriter.println("<br/>")
        
        //2. Build and write charts
        
        
        //chart 1: disease - measure (X=threshols,Y=measure values)
        data.each{ disease, resData->
            measures.each{ measure->
                // create Dataset
                def dataset = new DefaultCategoryDataset()

                classifiers.each{ classifier->
                    def rows = resData.query(ResultData.QUERY_MEASURE_TABLE, [classifier,measure])
                    rows.each{ row-> //row is a map with keys = {F_CUTPVAL,'1','2'}
                        def val = rowValue(row,measure,resData)
                        // add value to Dataset
                        dataset.addValue(val, classifNames[classifier], row[ResultData.F_CUTPVAL])
                    }
                }

                // render chart
                def imgFile = "${disease}_${measureNames[measure]}.png"

                exportLineChart(dataset, "${disease} - ${measureNames[measure]}", 
                    'Threshold', measureNames[measure], input+RESULT_FOLDER+CHART_FOLDER+imgFile )

                //write image tag to result file
                fileWriter.println("<img src=\"${CHART_FOLDER+imgFile}\" alt=\"${disease} - ${measureNames[measure]}\" /><br/><br/>") 
            }
        }
        
        fileWriter.println("<br/>")
        
        //end result writing
        fileWriter.println('</body></html>')
        fileWriter.close()
        
        // Export table file
        fileWriter = new PrintWriter(input+RESULT_FOLDER+TABLE_OUT)
        fileWriter.println("disease\tclassifier\tmeasure\tthreshold\tvalue") //print header
        
        data.each{ disease, resData->
            classifiers.each{ classifier->
                measures.each{ measure->
                    def rows = resData.query(ResultData.QUERY_MEASURE_TABLE, [classifier,measure])

                    rows.each{ row-> //row is a map with keys = {F_CUTPVAL,'1','2'}
                        def val = rowValue(row,measure,resData)
                        // print value row to file:
                        // (disease, classifier, measure, thr, value)
                        fileWriter.println("${disease}\t${classifNames[classifier]}\t${measure}\t${row[ResultData.F_CUTPVAL]}\t${val}")
                    }
                }
            }
        }
        
        fileWriter.close()
        // end export
        
        return 0
    }
    
    
    /**
     * call example:
     * parseHaploArff --input=/home/victor/Escritorio/WTCCC1/haploArffResults
     */
    static int doParseHaploArff(args) {
        String input = args.find{ it.startsWith(Main.OPT_INPUT_DIR) }
        def dirs = Main.checkAndCleanDirOpts([input])
        
        def error = {
            System.err.println("Usages:\n"+Main.USA_PARSE_HAPLOARFF)
            return 1 //error
        }
        
        if(!dirs)
            return error()
            
        input = dirs[0]
        
        def regex = /(\w+)_(\d+)_(\w+)\.result["\.gz"]*/ //<disease>_<size>_<gmodel>.result
        def data = parseDir(input, regex, 3, true)
        
        def diseases = [] as TreeSet
        def wSizes = [] as TreeSet
        def gModels = [] as TreeSet
        
        data.keySet().each{ key->
            def toks = key.split(',')
            diseases << toks[0]
            wSizes << (toks[1] as Integer)
            gModels << toks[2]
        }
        
        // Build and write tables and charts
        Utils.createDir(input+RESULT_FOLDER)
        Utils.createDir(input+RESULT_FOLDER+CHART_FOLDER)
        
        //file where to write html output
        def fileWriter = new PrintWriter(input+RESULT_FOLDER+HTML_OUT)
        fileWriter.println("<html>\n<head>\n${TABLE_STYLE_SHEET}</head>\n<body><br/>")
        
        
        //1. Build a table per classifier and measure (rows:cutPvals, cols:diseases)
        def measures = [ Classify.weightedAreaUnderROC, Classify.truePositiveRate, 
            Classify.trueNegativeRate, Classify.weightedPrecision, 
            Classify.pctCorrect ].findAll{ data[data.firstKey()].existMeasure(it) }
        
        if( !(Classify.pctCorrect in measures) ){
            //calculates pct. of correct classified manually
            data.each{ it.value.calcPctCorrect() }
            measures << ResultData.FEAT_PCT_CORR
        }
        
        def classifiers =  data[data.firstKey()].getClassifiers() as TreeSet
        def cutPvals = [] as Set
        data.each{ k,v-> cutPvals.addAll( v.getCutoffVals() ) }
        cutPvals = cutPvals.sort().reverse()
        
        def tableList = []
        
        wSizes.each{ wSize->
            gModels.each{ gModel->
                classifiers.each{ classifier->
                    measures.each{ measure->
                        //create table object
                        TableWriter table = new TableWriter( tableClass:TABLE_CSSCLASS, rowHeader:true, 
                            caption:"Size ${wSize}, Genetic model ${gModel}, ${classifNames[classifier]} - ${measureNames[measure]}" )

                        //build rows, first colum of each one
                        table.thead << ['Threshold']
                        cutPvals.each{ table.tbody << [it] }
                        
                        diseases.each{ disease->
                            def resData = data["${disease},${wSize},${gModel}"]
                            table.thead[0] << disease//add disease to header
                            def rows = resData.query(ResultData.QUERY_MEASURE_TABLE, [classifier,measure])

                            cutPvals.each{ pval->
                                def row = rows.find{ it[ResultData.F_CUTPVAL]==pval }
                                def val = (row) ? String.format('%.4f',rowValue(row,measure,resData)) : 'NA'
                                //add value to row
                                table.tbody.find{ it[0]==pval } << val
                            }
                        }

                        tableList << table
                    }
                }
            }
        }
        
        TableWriter.write(fileWriter, tableList)
        fileWriter.println("<br/>")
        
        //2. Build and write charts
        
        //chart 1: disease - measure (X=threshols,Y=measure values)
        wSizes.each{ wSize->
            diseases.each{ disease->
                measures.each{ measure->
                    // create Dataset
                    def dataset = new DefaultCategoryDataset()

                    gModels.each{ gModel->
                        def resData = data["${disease},${wSize},${gModel}"]
                        
                        classifiers.each{ classifier->
                            def rows = resData.query(ResultData.QUERY_MEASURE_TABLE, [classifier,measure])
                            rows.each{ row-> //row is a map with keys = {F_CUTPVAL,'1','2'}
                                def val = rowValue(row,measure,resData)
                                // add value to Dataset
                                dataset.addValue(val, classifNames[classifier]+'_'+gModel, row[ResultData.F_CUTPVAL])
                            }
                        }
                    }

                    // render chart
                    def imgFile = "${wSize}_${disease}_${measureNames[measure]}.png"
                    
                    exportLineChart(dataset, "Size ${wSize}, ${disease} - ${measureNames[measure]}", 
                        'Threshold', measureNames[measure], input+RESULT_FOLDER+CHART_FOLDER+imgFile )

                    //write image tag to result file
                    fileWriter.println("<img src=\"${CHART_FOLDER+imgFile}\" alt=\"Size ${wSize}, ${disease} - ${measureNames[measure]}\" /><br/><br/>") 
                }
                }
        }
        
        fileWriter.println("<br/>")
        
         
        //end result writing
        fileWriter.println('</body></html>')
        fileWriter.close()
        
        // Export table file
        fileWriter = new PrintWriter(input+RESULT_FOLDER+TABLE_OUT)
        fileWriter.println("disease\tgmodel\tsize\tclassifier\tmeasure\tthreshold\tvalue") //print header
        
        diseases.each{ disease->
            gModels.each{ gModel->
                wSizes.each{ wSize->
                    def resData = data["${disease},${wSize},${gModel}"]
                    
                    classifiers.each{ classifier->
                        measures.each{ measure->
                            def rows = resData.query(ResultData.QUERY_MEASURE_TABLE, [classifier,measure])
                            
                            rows.each{ row-> //row is a map with keys = {F_CUTPVAL,'1','2'}
                                def val = rowValue(row,measure,resData)
                                // print value row to file:
                                // (disease, gmodel, size, classifier, measure, thr, value)
                                fileWriter.println("${disease}\t${gModel}\t${wSize}\t${classifNames[classifier]}\t${measure}\t${row[ResultData.F_CUTPVAL]}\t${val}")
                            }
                        }
                    }
                }
            }
        }
        
        fileWriter.close()
        // end export
        
        return 0
    }
    
    /**
     * helper method used by parse
     */
    private static getClassifierName(val) {
        def name = Classify.classifiersNames.find{ it==val }
        
        if( name==null ){
            name = Classify.g2ClasNames.find{ it==val }
        }
        if( name==null ){
            name = Classify.snpsClasNames.find{ it==val }
        }
        
        return name
    }
    
    /**
     *
     * @return a ResultData instance
     */
    private static parse(file, boolean oneFold=false){
        
        def extractValue = { prefix, str->
            //closure that extracts the token next to the prefix inside the given string
            def toks = str.substring(prefix.length()).split("\\s") as List
            def tok = toks.find{ !it.isEmpty() }
            
            if( tok.endsWith(',') || tok.endsWith('.') ){
                return tok.substring(0, tok.length()-1)
            }
            
            return tok
        }//
        
        def toDouble = { str-> str.replace(',','.') as Double }
        
        int fold = 0
        double cutPval = 0.0
        String classifier
        String classNum
        boolean finalSumm = false//true if the final summary is being processed
        def foldList = []//list of measures
        def measures = (oneFold) ? new TreeMap(reverseComp) : null
        def totalMeasures = null
        def probabilities = [:]
        
        def reader = Utils.createReader(new File(file))
        
        reader.eachLine{ line->
            if( line.isEmpty() ){
                // nothing to do
            } 
            else if( finalSumm ){
                if( line.startsWith(TOK_FINAL_CUT_PVAL) ){
                    cutPval = extractValue(TOK_FINAL_CUT_PVAL, line) as Double
                    totalMeasures[cutPval] = [:]
                }
                else if( line.startsWith(TOK_FINAL_CLASIF) ){
                    def val = extractValue(TOK_FINAL_CLASIF, line)
                    classifier = getClassifierName(val)
                    totalMeasures[cutPval][classifier] = [:]
                }
                else if( line.startsWith(TOK_FINAL_CLASS) ){
                    classNum = extractValue(TOK_FINAL_CLASS, line)
                    if( ['.',':',',',';'].any{ classNum.endsWith(it) } )
                        classNum = classNum.substring(0, classNum.length()-1)

                    totalMeasures[cutPval][classifier][classNum] = [:]
                }
                else if( line==~measureRegex ){
                    def mat = (line=~measureRegex)
                    totalMeasures[cutPval][classifier][classNum][mat[0][1]] = [:]
                    totalMeasures[cutPval][classifier][classNum][mat[0][1]]['MAX'] = toDouble(mat[0][2])
                    totalMeasures[cutPval][classifier][classNum][mat[0][1]]['MIN'] = toDouble(mat[0][3])
                    totalMeasures[cutPval][classifier][classNum][mat[0][1]]['AVG'] = toDouble(mat[0][4])
                }
            }
            else{
                if( line.startsWith(TOK_FINAL_RES) ){
                    finalSumm = true
                    totalMeasures = new TreeMap(reverseComp)
                }
                else if( line.startsWith(TOK_FOLD) ){
                    fold = extractValue(TOK_FOLD, line) as Integer
                    measures = new TreeMap(reverseComp)
                    foldList << measures
                }
                else if( line.startsWith(TOK_DATA_CUT_PVAL) ){
                    cutPval = extractValue(TOK_DATA_CUT_PVAL, line) as Double
                    measures[cutPval] = [:]
                }
                else if( line.startsWith(TOK_DATA_CUT_PVAL2) ){
                    cutPval = extractValue(TOK_DATA_CUT_PVAL2, line) as Double
                    measures[cutPval] = [:]
                }
                else if( line.startsWith(TOK_SUMM_CLASIF) ){
                    def val = extractValue(TOK_SUMM_CLASIF, line)
                    classifier = getClassifierName(val)
                    measures[cutPval][classifier] = [:]
                }
                else if( line==~classProbReg ){
                    def mat = (line=~classProbReg)
                    if( !probabilities[mat[0][1]] )
                        probabilities[mat[0][1]] = mat[0][2] as Double
                }
                else if( line==~correctPctRegex ){
                    def mat = (line=~correctPctRegex)
                    measures[cutPval][classifier][ResultData.FEAT_PCT_CORR] = toDouble(mat[0][1])
                }
                else if( classifier==Classify.CLS_SIMPLE_LOG ){
                    if( line==~simpLogRegInter ){
                        def mat = (line=~simpLogRegInter)
                        measures[cutPval][classifier][ResultData.FEAT_INTERCEPT] = mat[0][1] as Double
                    }
                    else if( line==~simpLogRegSlop ){
                        def mat = (line=~simpLogRegSlop)
                        measures[cutPval][classifier][ResultData.FEAT_SLOPE] = mat[0][1] as Double
                    }
                }
                else if( classifier==Classify.CLS_FIXED_LOG && line==~fixLogRegex ){
                    def mat = (line=~fixLogRegex)
                    measures[cutPval][classifier][ResultData.FEAT_INTERCEPT] = mat[0][1] as Double
                    measures[cutPval][classifier][ResultData.FEAT_SLOPE] = mat[0][2] as Double
                }
            }
        }
        
        reader.close()
        
        //remove measures with incomplete data
        def incomplete = [] as Set
        totalMeasures.each{pval, clasif->
            if(!clasif){ incomplete << pval }
            else{
                clasif.each{ name, clss->
                    if( !clss ){ incomplete << pval }
                    else{
                        clss.each{ clname, meas->
                            if( !meas ){ incomplete << pval }
                        }
                    }
                }
            }
        }
        
        incomplete.each{ totalMeasures.remove(it) }
        
        return new ResultData( foldData:((oneFold) ? [measures] : foldList),
            totalData:totalMeasures, classProb:probabilities )
    }
    
    /**
     *  @return a map of ResultData instances (key = file prefix)
     */
    private static parseDir(dir, regex, int numkeys, boolean oneFold=false) {
        def files = new File(dir).list({d, f-> f ==~ regex } as FilenameFilter).toList()
        def data = [:] as TreeMap
        
        files.each{ file->
            def match = (file =~ regex)[0]
            println "Parsing ${file}"
            def fileData = parse(dir+file, oneFold)
            
            def key = (1..numkeys).sum{"${match[it]},"}
            key = key.substring(0, key.length()-1)
            data[key] = fileData
        }
        
        return data
    }
    
    
    /**
     * writes chart to disk (PNG)
     */
    private static exportLineChart(dataset, title, categoryAxisLabel, valueAxisLabel, String outName){
        JFreeChart jfreechart = ChartFactory.createLineChart(
            title, categoryAxisLabel, valueAxisLabel, 
            dataset, PlotOrientation.VERTICAL, true, true, false)
        
        def categoryplot = jfreechart.getPlot()
        def numberaxis = categoryplot.getRangeAxis()
        def lineandshaperenderer = categoryplot.getRenderer()
        double min = 1.0
        
        (0..(dataset.rowCount-1)).each{ i->
            //set shape visible
            lineandshaperenderer.setSeriesShapesVisible(i, true)
            
            //set series color
            def color = classifColor.find{dataset.getRowKey(i).startsWith(it.key)}.value
            lineandshaperenderer.setSeriesPaint(i, color)
            
            //set series stroke
            def stroke = classifStroke.find{dataset.getRowKey(i).endsWith(it.key)}?.value
            if( stroke ){
                lineandshaperenderer.setSeriesStroke(i, stroke)
            }
            
            (0..(dataset.columnCount-1)).each{ j->
                //calculates min value
                if( dataset.getValue((int)i,(int)j)<min )
                    min = dataset.getValue((int)i,(int)j)
            }
        }
        
        numberaxis.setRange( ((min>0.5) ? 0.5 : min-0.05) , 1.0)
        
        ChartUtilities.saveChartAsPNG( new File(outName), jfreechart, WIDTH, HEIGHT)
    }
    
    /**
     * writes chart to disk (PNG)
     */
    private static exportScatterPlot(xydataset, title, xAxisLabel, yAxisLabel, String outName){
        JFreeChart jfreechart = ChartFactory.createScatterPlot(
            title, xAxisLabel, yAxisLabel, 
            xydataset, PlotOrientation.VERTICAL, true, true, false)
        
        def xyplot = jfreechart.getPlot()
        xyplot.setDomainCrosshairVisible(true)
        xyplot.setDomainCrosshairLockedOnData(true)
        xyplot.setRangeCrosshairVisible(true)
        xyplot.setRangeCrosshairLockedOnData(true)
        xyplot.setDomainZeroBaselineVisible(true)
        xyplot.setRangeZeroBaselineVisible(true)
        XYDotRenderer xydotrenderer = new XYDotRenderer()
        xydotrenderer.setDotWidth(2)
        xydotrenderer.setDotHeight(2)
        xyplot.setRenderer(xydotrenderer)
        def numberaxis = xyplot.getDomainAxis()
        numberaxis.setAutoRangeIncludesZero(false)
        
        ChartUtilities.saveChartAsPNG( new File(outName), jfreechart, WIDTH, HEIGHT)
    }
    
    /**
     * writes chart to disk (PNG)
     */
    private static exportBarChart(dataset, title, categoryAxisLabel, valueAxisLabel, String outName){
        JFreeChart jfreechart = ChartFactory.createBarChart(
            title, categoryAxisLabel, valueAxisLabel, 
            dataset, PlotOrientation.VERTICAL, false, false, false)
        
        def categoryplot = jfreechart.getPlot()
        categoryplot.addRangeMarker( new ValueMarker(1.0), Layer.BACKGROUND )
        def numberaxis = categoryplot.getRangeAxis()
        numberaxis.setUpperBound(1.0)
        def minmaxcategoryrenderer = new MinMaxCategoryRenderer()
        minmaxcategoryrenderer.setDrawLines(false)
        categoryplot.setRenderer(minmaxcategoryrenderer)
        
        ChartUtilities.saveChartAsPNG( new File(outName), jfreechart, WIDTH, HEIGHT)
    }
    
}

