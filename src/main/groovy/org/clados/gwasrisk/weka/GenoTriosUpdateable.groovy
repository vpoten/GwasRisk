/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.weka

import weka.classifiers.Classifier
import weka.classifiers.UpdateableClassifier
import weka.core.Instance

/**
 *
 * @author victor
 */
class GenoTriosUpdateable extends GenoTrios implements UpdateableClassifier {
    
    
    /**
     *
     */
    void updateClassifier(Instance instance){
        decomposePattern(instance).each{ 
            internalClas.updateClassifier(it)
        }
    }
}

