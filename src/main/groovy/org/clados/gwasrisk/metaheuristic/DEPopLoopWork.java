/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.metaheuristic;

/**
 * class that run in a thread some iterations of the DE loop
 * 
 * @author victor
 */
public class DEPopLoopWork implements Runnable {

    private int period=0;
    private int index=-1;

    private DESolution trial=null;
    private DESolution mutant=null;

    private DifferentialEvolution devol=null;
    private IDESolutionEval evaluator=null;

    
    public DEPopLoopWork( int index, int period, DifferentialEvolution de,
            IDESolutionEval eval ) {

        this.period=period;
        this.index=index;
        this.devol=de;
        this.evaluator=eval;

        DESolution init=de.getPopulation().get(0);

        this.trial = new DESolution(init.size(), eval);
        this.mutant = new DESolution(init.size(), eval);
    }

    /**
     * run main DE loop
     */
    public void run() {

        for( int i=this.index; i<devol.getNP(); i+=this.period ){
            
            DESolution target = devol.getPopulation().get(i);
            target.setEvaluator(this.evaluator);

            //mutation operation
            devol.getMutation().mutate( mutant, i, devol.getPopulation(), devol.getF() );

            //correct mutant vector (ensure inside bounds)
            devol.restrictParameters(mutant);

            //crossover operation
            devol.getCrossover().cross( trial, target, mutant, devol.getCR() );

            //selection operation
            if( trial.isBetter(target) )
                devol.getNewPopulation().get(i).copy(trial);
            else
                devol.getNewPopulation().get(i).copy(target);
            
        }
    }

}
