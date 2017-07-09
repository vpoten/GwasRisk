/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.metaheuristic;

import java.util.Date;

/**
 *
 * @author victor
 */
abstract public class MetaheuristicBase implements IMetaheuristic {

    protected IMetaheuristicSolution solution=null;
    protected IMetaheuristicSolution bestSolution=null;

    private Date startTime=null;
    private Date endTime=null;

    //parameters for end condition
    private int iterLimit=0;//limit iterations
    private int timeLimit=0;//time limit (seconds)
    private int iterations=0;

    //fields for cooperative strategy
    private int id=0;


    protected SolutionComparator solComp=new SolutionComparator();

    
    
    public void setId(int id) {
        this.id=id;
    }

    public int getId() {
        return this.id;
    }

    /**
     * @return the startTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @return the endTime
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @return the bestSolution
     */
    public IMetaheuristicSolution getBestSolution() {
        return bestSolution;
    }

    /**
     * @param bestSolution the bestSolution to set
     */
    protected void setBestSolution(IMetaheuristicSolution bestSolution) {
        this.bestSolution = bestSolution;
    }

    /**
     * @return the current solution
     */
    public IMetaheuristicSolution getSolution() {
        return solution;
    }

    /**
     * @param solution the current solution to set
     */
    public void setSolution(IMetaheuristicSolution solution) {
        this.solution = solution;

        if(solution==null)
            return;

        if( this.getBestSolution()==null ){
            this.setBestSolution(this.getSolution());
        }
        else if( this.getSolution().isBetter( this.getBestSolution() ) ){
            this.setBestSolution(this.getSolution());
        }
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * @return the iterLimit
     */
    public int getIterLimit() {
        return iterLimit;
    }

    /**
     * @param iterLimit the iterLimit to set
     */
    public void setIterLimit(int iterLimit) {
        this.iterLimit = iterLimit;
    }

    /**
     * @return the timeLimit
     */
    public int getTimeLimit() {
        return timeLimit;
    }

    /**
     * @param timeLimit the timeLimit to set
     */
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    /**
     * checks end condition and increment the iteration counter
     *
     * @return
     */
    public boolean endCondition() {

        iterations++;

        if( getTimeLimit()>0 ){
            Date currTime=new Date();

            if( (currTime.getTime()-getStartTime().getTime())/1000.0 > getTimeLimit() )
                return true;
        }

        if( getIterLimit()>0 ){
            if( getIterations() > getIterLimit() )
                return true;
        }

        return false;
    }

    public int getIterations() {
        return iterations;
    }

    
    public void reset(){
        this.setSolution(null);
        this.setBestSolution(null);
    }
}
