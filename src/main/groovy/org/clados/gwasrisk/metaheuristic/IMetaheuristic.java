/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.metaheuristic;

import java.util.Date;

/**
 * interface for metaheuristics
 * 
 * @author victor
 */
public interface IMetaheuristic {

    public void doSearch();

    public void setSolution(IMetaheuristicSolution solution);

    public IMetaheuristicSolution getSolution();
    
    public IMetaheuristicSolution getBestSolution();

    public double getLambda();
    public void setLambda( double val );

    public void setId( int id );
    public int getId();


    public Date getStartTime();
    public Date getEndTime();
    
}
