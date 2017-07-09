/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.metaheuristic;

/**
 *
 * @author victor
 */
public interface IMetaheuristicSolution {
    
    Double getFitness();

    boolean isBetter( IMetaheuristicSolution other );
}
