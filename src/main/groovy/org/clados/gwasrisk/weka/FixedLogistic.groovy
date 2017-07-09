/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.weka

import weka.classifiers.Classifier
import weka.core.Instances
import weka.core.Instance
import weka.core.Utils
import weka.core.Attribute

/**
 * options -I <intercept> -S <slope>
 * 
 * @author victor
 */
class FixedLogistic extends Classifier {
    
    double intercept = 0
    double slope = 1
    private Attribute attribute
    
    void buildClassifier(Instances data){
        // attribute 0 is wGRS (attribute 1 is the class)
        attribute = data.attribute(0)
        return
    }
    
    
    double [] distributionForInstance(Instance instance){
        double z = intercept + slope*instance.value(attribute.index())
        double prob = 1.0/(1.0+Math.exp(-z))
        return [1-prob, prob] as double []
    }
    
    
    void setOptions(String[] options){
        String optionString = Utils.getOption('I', options)
	if (optionString) {
	    intercept = optionString as Double
	}
        
        optionString = Utils.getOption('S', options)
	if (optionString) {
	    slope = optionString as Double
	}
    }
    
    /**
     * Returns a description of the model
     * 
     * @return the description of the model
     */
    public String toString(){
        return "\nIntercept=${intercept}, Slope=${slope}\n"
    }
    
    public String getRevision(){
        return "1.0"
    }
    
}

