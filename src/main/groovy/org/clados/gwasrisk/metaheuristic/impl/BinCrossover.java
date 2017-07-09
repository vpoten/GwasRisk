/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.metaheuristic.impl;

import org.clados.gwasrisk.metaheuristic.DESolution;
import org.clados.gwasrisk.metaheuristic.IDECrossover;

/**
 *
 * @author victor
 */
public class BinCrossover implements IDECrossover {

    /**
     *
     * @param trial : I/O
     * @param target : target vector
     * @param mutant : mutant vector
     * @param CR : cross rate [0,1]
     */
    public void cross( DESolution trial, DESolution target, DESolution mutant, double CR) {

        int size=target.size();

        for( int i=0;i<size;i++){

            if( Math.random()<=CR ){
                trial.setParameter(i, mutant.getParameter(i));
            }
            else{
                trial.setParameter(i, target.getParameter(i));
            }
        }

        //forces at least one parameter from mutant vector to be conserved
        int j=(int) Math.floor( size*Math.random() );
        trial.setParameter( j, mutant.getParameter(j));

    }

}
