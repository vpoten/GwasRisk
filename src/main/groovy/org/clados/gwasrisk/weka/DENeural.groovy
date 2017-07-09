/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.weka

import org.clados.gwasrisk.Main
import org.clados.gwasrisk.neural.NeuralNetwork
import org.clados.gwasrisk.neural.NNPattern
import org.clados.gwasrisk.metaheuristic.DifferentialEvolution
import org.clados.gwasrisk.metaheuristic.impl.*
import weka.classifiers.Classifier
import weka.core.Instances
import weka.core.Instance
import weka.core.Utils
import weka.core.Attribute

/**
 *
 * @author victor
 */
class DENeural extends Classifier {
    NeuralNetwork network
    int numHid = 2
    int iterations = 1000
    int NP = 50
    int threads = 1
    
    private def instanceToNNPat = { ins->
        def input = new double [ins.numAttributes()-1]
        def output = new double [ins.classAttribute().numValues() ?: 1]
        
        (0..input.length-1).each{ input[it] = ins.value(it) }
        
        (0..output.length-1).each{ output[it] = 0.1 }
        
        output[(int)ins.classValue()] = 0.9
        
        return new NNPattern( input, output )
    }
        
    void buildClassifier(Instances data){
        int nin = data.numAttributes()-1
        int nout = data.classAttribute().numValues() ?: 1
        
        network = new NeuralNetwork(nin, nout, numHid)
        
        //prepare DE
        double CR=0.9;
        double F=0.8;
        int dim = numHid*network.getNInputs() + numHid*nout;
        
        def patterns = []
        
        (0..data.numInstances()-1).each{ 
            patterns << instanceToNNPat( data.instance(it) ) 
        }

        DifferentialEvolution devol = new DifferentialEvolution();

        devol.setCR( CR );
        devol.setF( F );
        devol.setNP( NP );
        devol.setCrossover( new BinCrossover() );
        devol.setMutation( new RandMutation() );

        NeuralEvaluator evaluator= new NeuralEvaluator();
        evaluator.setNetwork(network);
        evaluator.setPatterns(patterns);

        devol.createRandomPopulation( dim, evaluator, -0.05, 0.05);
        
        devol.setIterLimit( iterations );
        
        if( threads>1 ){
            def workers = []
            
            (1..threads).each{
                def eval = new NeuralEvaluator()
                eval.setNetwork( new NeuralNetwork(nin, nout, numHid) )
                eval.setPatterns(patterns)
                workers << eval
            }
            
            devol.setWorkers( workers, 1000)
        }
        
        devol.doSearch();

        double [] vector = new double [dim];
        devol.getBestSolution().getParameters(vector);

        network.setWeights(vector);
        network.init();
    }
    
    
    double [] distributionForInstance(Instance instance){
        def pat = instanceToNNPat(instance)
        network.evaluate( pat.getInputs() )
        def vector = new double [instance.classAttribute().numValues() ?: 1]
        network.getOutput( vector )
        
        //normalize output
        def probs = vector as List
        double norm = probs.sum()
        (0..probs.size()-1).each{ probs[it] /= norm }
        return probs as double []
    }
    
    
    void setOptions(String[] options){
        String optionString = Utils.getOption('N', options)
	if (optionString) {
	    numHid = optionString as Integer
	}
        
        optionString = Utils.getOption('I', options)
	if (optionString) {
	    iterations = optionString as Integer
	}
        
        optionString = Utils.getOption('P', options)
	if (optionString) {
	    threads = optionString as Integer
	}
    }
    
    /**
     * Returns a description of the model
     * 
     * @return the description of the model
     */
    public String toString(){
        def strbuild = new StringBuilder()
        strbuild << "#iterations: ${iterations}\n"
        strbuild << "#inputs: ${network.getNInputs()}\n"
        strbuild << "#outputs: ${network.getNOutputs()}\n"
        strbuild << "#hidden_neurons: ${network.getNHidden()}\n"
        return strbuild.toString()
    }
    
    public String getRevision(){
        return "1.0"
    }
}

