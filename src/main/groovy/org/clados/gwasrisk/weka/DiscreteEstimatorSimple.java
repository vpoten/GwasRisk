/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clados.gwasrisk.weka;

import weka.estimators.Estimator;

/**
 * a very simple discrete estimator, for two symbols {0,1}
 * @author victor
 */
public class DiscreteEstimatorSimple extends Estimator {

    double counts0 = 1;
    double counts1 = 1;
    double sumCounts = 2;
    
    @Override
    public void addValue(double data, double weight) {
        if( data==0.0 )
            counts0 += weight;
        else
            counts1 += weight;
        
        sumCounts += weight;
    }
    
    @Override
    public double getProbability(double data) {
        if( data==0.0 )
            return counts0 / sumCounts;
        
        return counts1 / sumCounts;
    }

    public String getRevision() {
        return "1.0";
    }
    
}
