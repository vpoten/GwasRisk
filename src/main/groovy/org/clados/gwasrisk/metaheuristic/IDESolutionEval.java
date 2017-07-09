/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.metaheuristic;

/**
 *
 * @author victor
 */
public interface IDESolutionEval {
    
    public Double getFitness(DESolution sol);

    public boolean isBetter(double val1, double val2 );

    public int getNevaluations();
}
