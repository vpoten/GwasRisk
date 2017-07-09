/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.weka

import org.clados.gwasrisk.Main
import weka.classifiers.Classifier
import weka.core.Instances
import weka.core.Instance
import weka.core.Utils
import weka.core.Attribute
import weka.core.FastVector

/**
 * Genotype classifier using haplotypes
 * 
 * @author victor
 */
class GenoTrios extends Classifier {
	
    protected Classifier internalClas = null
    protected Instances dataset = null
    protected def decompPats
    protected def classCodes = ['?':Instance.missingValue()]
    protected GeneticModel gModel = GeneticModel.RECESSIVE
    protected boolean decomposeClass = true //decomposePattern behaviour
    
    // genetic model constants
    enum GeneticModel { RECESSIVE, ADDITIVE, DOMINANT }
    static final String GMODEL_OPT = 'genemodel'
    static final def GMODEL_MAP = ['RECESSIVE':GeneticModel.RECESSIVE,
       'ADDITIVE':GeneticModel.ADDITIVE, 'DOMINANT':GeneticModel.DOMINANT]
    
    private def copyValues = { att->
        FastVector vector = new FastVector(att.numValues())
        (0..att.numValues()-1).each{
            vector.addElement( att.value(it) )
        }
        return vector
    }
    
    
    void buildClassifier(Instances data){
        //build new attribute info
        int numAtt = (int)( (data.numAttributes()-1)/2 ) + 1
        FastVector vector = new FastVector(numAtt)
        
        for(int i=0; i<data.numAttributes()-1; i+=2){
            def oldAtt = data.attribute(i)
            def name = oldAtt.name()
            name = name.substring(0, name.length()-1)
            vector.addElement(  new Attribute(name, copyValues(oldAtt) ) )
        }
        
        def classAtt = data.attribute(data.numAttributes()-1)
        vector.addElement(  new Attribute(classAtt.name(), copyValues(classAtt) ) )
        
        
        //create new dataset
        dataset = new Instances('', vector, data.numInstances()*2 )
        dataset.setClassIndex( dataset.numAttributes()-1 )
        
        //initialize decomposed patterns
        decompPats = [new double [dataset.numAttributes()],
            new double[dataset.numAttributes()] ]
        
        //initialize class codes
        [Main.affCode, Main.unaffCode].each{ code->
            classCodes[code] = [0,1].find{ classAtt.value(it)==code } as Double
        }
        
        //add instances to dataset
        for(int i=0; i<data.numInstances(); i++){
            decomposePattern(data.instance(i), true).each{ ins->
                dataset.add(ins)
            }
        }
        
        internalClas.buildClassifier( dataset )
    }
    
    
    double [] distributionForInstance(Instance instance){
        def probs = [0.0, 0.0] as double []
        def distribs = decomposePattern(instance).collect{ internalClas.distributionForInstance(it) }
        double leftProb = distribs[0][1]
        double rightProb = distribs[1][1]
        
        if( gModel==GeneticModel.RECESSIVE ){
            probs[1] = leftProb*rightProb
        }
        else if( gModel==GeneticModel.ADDITIVE ){
            double leftBasicMeasure = Math.log(leftProb)-Math.log(1-leftProb)
            double rightBasicMeasure = Math.log(rightProb)-Math.log(1-rightProb)
            probs[1] = 1.0/(1.0 + Math.exp(-leftBasicMeasure-rightBasicMeasure))
        }
        else{ //GeneticModel.DOMINANT
            probs[1] = leftProb*rightProb + (1-leftProb)*rightProb + leftProb*(1-rightProb)
        }
        
        probs[0] = 1.0 - probs[1]
        return probs
    }
    
    
    void setOptions(String[] options){
        String optionString = Utils.getOption('classifier', options)
	if (optionString) {
	    internalClas = Class.forName(optionString).newInstance()
	}
        else{
            throw new RuntimeException('Classifier class not present')
        }
        
        optionString = Utils.getOption(GMODEL_OPT, options)
        
        if(optionString){
            gModel = GMODEL_MAP[optionString.toUpperCase()]
            
            if( gModel==null ){
                gModel = GeneticModel.RECESSIVE
            }
        }
        
        internalClas.setOptions(options)
    }
    
    
    /**
     * Returns a description of the model
     * 
     * @return the description of the model
     */
    public String toString(){
        String gmodel = GMODEL_MAP.find{ it.value==gModel }.key
        return "GenoTrios classifier [Genetic_Model=${gmodel},decomp_class=${decomposeClass}]:\n"+internalClas.toString()
    }
    
    
    public String getRevision(){
        return "1.0"
    }
    
    
    /**
     *
     */
    protected def decomposePattern(Instance instance, boolean newPatt = false){
        def patts = decompPats
        
        if(newPatt){
            patts = [new double[dataset.numAttributes()],
                new double [dataset.numAttributes()] ]
        }
        
        for(int i=0; i<instance.numAttributes()-1; i+=2){
            int idx = (int)(i/2)
            patts[0][idx] = instance.value(i)
            patts[1][idx] = instance.value(i+1)
        }
        
        int clsIdx = dataset.classIndex()
        def clsVal = instance.stringValue(instance.classIndex())

        
        if( decomposeClass ){
            /*** old
            // decompose class: left_pattern=affected, right_pattern=phenotype (valid for trios)
            patts[0][clsIdx] = classCodes[Main.affCode]
            patts[1][clsIdx] = (clsVal==Main.affCode) ? classCodes[Main.affCode] : classCodes[Main.unaffCode]
            ***/
           if(clsVal==Main.affCode){ //child coded as affected<->transmitted
              patts[0][clsIdx] = classCodes[Main.affCode]
              patts[1][clsIdx] = classCodes[Main.affCode]
           }
           else{ //parent
              patts[0][clsIdx] = classCodes[Main.affCode] //affected<->transmitted
              patts[1][clsIdx] = classCodes[Main.unaffCode] //unaffected<->untransmitted
           }
        }
        else{
            // not decompose class: left_pattern=right_pattern=phenotype
            patts[0][clsIdx] = classCodes[clsVal]
            patts[1][clsIdx] = classCodes[clsVal]
        }
        
        def instances = patts.collect{ new Instance(1.0, it) }
        instances.each{ it.dataset = dataset }
        
        return instances
    }
    
}

