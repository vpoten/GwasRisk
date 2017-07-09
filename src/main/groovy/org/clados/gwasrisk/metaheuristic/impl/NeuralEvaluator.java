/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.metaheuristic.impl;

import java.util.Collection;
import org.clados.gwasrisk.metaheuristic.DESolution;
import org.clados.gwasrisk.metaheuristic.IDESolutionEval;
import org.clados.gwasrisk.neural.NeuralNetwork;
import org.clados.gwasrisk.neural.NNPattern;

/**
 * Evaluator for neural networks
 * 
 * @author victor
 */
public class NeuralEvaluator implements IDESolutionEval {

    private NeuralNetwork network = null;
    private Collection<NNPattern> patterns = null;

    private int nevaluations=0;

    /**
     * returns the sum squared error
     *
     * @param sol
     * @return
     */
    public Double getFitness(DESolution sol) {

        double sse = 0;
        nevaluations++;

        getNetwork().setWeights( sol.getParameters() );
        getNetwork().init();
       
        //foreach pattern in training set
        for( NNPattern pat : this.getPatterns() ){
            getNetwork().evaluate( pat.getInputs() );
            sse += getNetwork().calcSSE( pat.getOutputs() );
        }

        return sse;
    }

    public int getNevaluations() {
        return nevaluations;
    }

    public boolean isBetter(double val1, double val2) {
        if(val1<val2)
            return true;

        return false;
    }

    /**
     * @return the network
     */
    protected NeuralNetwork getNetwork() {
        return network;
    }

    /**
     * @param network the network to set
     */
    public void setNetwork(NeuralNetwork network) {
        this.network = network;
    }

    /**
     * @return the patterns
     */
    public Collection<NNPattern> getPatterns() {
        return patterns;
    }

    /**
     * @param patterns the patterns to set
     */
    public void setPatterns(Collection<NNPattern> patterns) {
        this.patterns = patterns;
    }

}
