/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.metaheuristic.impl;

import org.clados.gwasrisk.metaheuristic.DESolution;
import java.util.List;
import org.clados.gwasrisk.metaheuristic.IDEMutation;

/**
 *
 * @author victor
 */
public class RandMutation implements IDEMutation {

    private int numDifVectors=1;
    private int [] indices=new int [3];

    /**
     * @return the numDifVectors
     */
    public int getNumDifVectors() {
        return numDifVectors;
    }

    /**
     * @param numDifVectors the numDifVectors to set
     */
    public void setNumDifVectors(int numDifVectors) {
        this.numDifVectors = numDifVectors;
        indices=new int [getNumDifVectors()*2+1];
    }

    /**
     *
     * @param mutant : I/O mutated vector
     * @param idx : index of target vector
     * @param population
     * @param F
     */
    public void mutate( DESolution mutant, int idx, List<DESolution> population, double F) {

        int size=mutant.size();

        //get numDifVectors*2+1 different indices
        getDifferent( idx, population.size());

        if( size==7 && getNumDifVectors()==1 ){

            double val=0.0;

            val = population.get(indices[0]).getParameter(0) -
                            population.get(indices[1]).getParameter(0);
            mutant.setParameter( 0,
                        F*val + population.get(indices[2]).getParameter(0) );

            val = population.get(indices[0]).getParameter(1) -
                            population.get(indices[1]).getParameter(1);
            mutant.setParameter( 1,
                        F*val + population.get(indices[2]).getParameter(1) );

            val = population.get(indices[0]).getParameter(2) -
                            population.get(indices[1]).getParameter(2);
            mutant.setParameter( 2,
                        F*val + population.get(indices[2]).getParameter(2) );

            val = population.get(indices[0]).getParameter(3) -
                            population.get(indices[1]).getParameter(3);
            mutant.setParameter( 3,
                        F*val + population.get(indices[2]).getParameter(3) );

            val = population.get(indices[0]).getParameter(4) -
                            population.get(indices[1]).getParameter(4);
            mutant.setParameter( 4,
                        F*val + population.get(indices[2]).getParameter(4) );

            val = population.get(indices[0]).getParameter(5) -
                            population.get(indices[1]).getParameter(5);
            mutant.setParameter( 5,
                        F*val + population.get(indices[2]).getParameter(5) );

            val = population.get(indices[0]).getParameter(6) -
                            population.get(indices[1]).getParameter(6);
            mutant.setParameter( 6,
                        F*val + population.get(indices[2]).getParameter(6) );

        }
        else if( getNumDifVectors()==1 ){

            for(int i=0; i<size; i++){

                double val = population.get(indices[0]).getParameter(i) -
                            population.get(indices[1]).getParameter(i);

                mutant.setParameter( i,
                        F*val + population.get(indices[2]).getParameter(i));
            }

        }
        else{
            //general case

            for(int i=0;i<size;i++){
                double val=0.0;

                for(int j=0;j<getNumDifVectors();j++){
                    val += population.get(indices[2*j]).getParameter(i) -
                            population.get(indices[2*j+1]).getParameter(i);
                }

                mutant.setParameter( i,
                        F*val + population.get(indices[indices.length-1]).getParameter(i));
            }
        }
        
    }

    /**
     * get numDifVectors*2+1 different indices between 0 and size-1
     *
     * @param initval : init constraint value
     * @param size
     */
    private void getDifferent(int initval, int size) {

        for(int i=0;i<indices.length;i++){

            boolean repeat=false;

            do{
                repeat=false;
                indices[i]=(int) Math.floor( size*Math.random() );

                if( indices[i]==initval ){
                    repeat=true;
                    continue;
                }

                for(int j=0;j<i;j++){
                    if(indices[j]==indices[i]){
                        repeat=true;
                        break;
                    }
                }

            }while( repeat );

        }
    }

}
