/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.weka

import org.clados.gwasrisk.weka.jri.JRILoader
import org.clados.gwasrisk.weka.jri.RUtils
import org.rosuda.REngine.REXP
import org.rosuda.REngine.REngine

import weka.classifiers.Classifier
import weka.core.Instances
import weka.core.Instance

/**
 *
 * @author victor
 */
class StepwiseGLMAic  extends Classifier {
    
    double [] coefficients = null
    def predictors
    def coeffIndex = null
        
    void buildClassifier(Instances data){
        
        REngine rengine = JRILoader.createEngine()
        
        String frameName = 'trainSet'
        RUtils.instancesToDataFrame(rengine, data, frameName, true)
        
        String className = data.attribute(data.numAttributes()-1).name()
        
        REXP res = rengine.parseAndEval('library(MASS)') //load library
        if (res.isNull()) {
            System.err.println("Cannot load library MASS")
            System.exit(1)
        }
            
        //transform {1,2} to {0,1}
        res = rengine.parseAndEval('newCol <- '+frameName+'$'+className+'-1');
        res = rengine.parseAndEval(frameName+'$'+className+' <- newCol');
        
        //full model
        res = rengine.parseAndEval("train.glm <- glm(${className} ~ ., family = binomial, data = ${frameName})")
        
        //empty model
        res = rengine.parseAndEval("train.glm2 <- glm(${className} ~ 1, family = binomial, data = ${frameName})")
        
        // stepwise AIC
        String expr = "model <- stepAIC(train.glm2, scope=list(lower=formula(train.glm2),upper=formula(train.glm)), direction=\"forward\", trace = FALSE)"
        res = rengine.parseAndEval(expr)
        
        res = rengine.parseAndEval('model$coefficients')
        
        coefficients = res.asDoubles()
        predictors = res._attr().asNativeJavaObject().names.collect{it.substring(1)}
        
        def attMapIdx = [:] as TreeMap
        for(int i=0; i<data.numAttributes(); i++) {
            String attName = data.attribute(i).name()
            attName = attName.replace('-','_').replace(':','_')
            attMapIdx[attName] = i
        }
        
        coeffIndex = (1..predictors.size()-1).collect{ attMapIdx[predictors[it]] }
        
        rengine = null
    }
    
    double [] distributionForInstance(Instance instance){
        double z = coefficients[0]
        
        coeffIndex.eachWithIndex{ var, i->
            z += coefficients[i+1]*(instance.stringValue(var) as Double)
        }
        
        double prob = 1.0/(1.0+Math.exp(-z))
        return [1-prob, prob] as double []
    }
    
    void setOptions(String[] options){
        // TODO
    }
    
    /**
     * Returns a description of the model
     * 
     * @return the description of the model
     */
    public String toString(){
        StringBuilder strb = new StringBuilder()
        strb << "Stepwise GLM AIC:\n"
        strb << "#predictors=${predictors.size()-1}\n\n"
        strb << "Intercept=${coefficients[0]}\n"
        (1..predictors.size()-1).each{
            strb << "${predictors[it]}=${coefficients[it]}\n"
        }
        
        return strb.toString()
    }
    
    
    public String getRevision(){
        return "1.0"
    }
    
}

