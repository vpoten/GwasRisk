/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clados.gwasrisk.weka;

import java.util.Enumeration;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Utils;

/**
 *
 * @author victor
 */
public class NaiveBayesLogProb extends NaiveBayes {
    
    @Override
    public double [] distributionForInstance(Instance instance) 
    throws Exception {
        if (m_UseDiscretization) {
          m_Disc.input(instance);
          instance = m_Disc.output();
        }
        double [] probs = new double[m_NumClasses];
        for (int j = 0; j < m_NumClasses; j++) {
          probs[j] = Math.log( m_ClassDistribution.getProbability(j) );
        }
        
        Enumeration enumAtts = instance.enumerateAttributes();
        int attIndex = 0;
        while (enumAtts.hasMoreElements()) {
          Attribute attribute = (Attribute) enumAtts.nextElement();
          if (!instance.isMissing(attribute)) {
            double temp, max = 0;
            for (int j = 0; j < m_NumClasses; j++) {
              temp = Math.log( m_Distributions[attIndex][j].
                                              getProbability(instance.value(attribute))
                    );
              
              probs[j] += temp;
            }
          }
          attIndex++;
        }
        
        //covert logs to probabilities and normalize
        probs = Utils.logs2probs(probs);
        
        return probs;
    }
    
}
