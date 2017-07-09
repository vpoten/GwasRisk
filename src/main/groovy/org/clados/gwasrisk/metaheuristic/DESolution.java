/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.metaheuristic;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Differential Evolution solution (vector of double parameters)
 * 
 * @author victor
 */
public class DESolution implements IMetaheuristicSolution  {

    private double [] parameters=null;
    private IDESolutionEval evaluator=null;
    private Double fitness=null;


    /**
     * creates a new random solution (values between [-1,1])
     *
     * @param size
     * @param eval
     */
    public DESolution( int size, IDESolutionEval eval ) {
        parameters=new double [size];
        setEvaluator(eval);

        for(int i=0;i<parameters.length;i++)
            parameters[i]=2.0*Math.random()-1.0;

    }

    public DESolution( int size, IDESolutionEval eval, double low, double up ) {
        parameters=new double [size];
        setEvaluator(eval);

        for(int i=0;i<parameters.length;i++)
            parameters[i]=low+Math.random()*(up-low);

    }

    public DESolution( int size, IDESolutionEval eval,
            double [] lowers, double [] uppers ) {
        parameters=new double [size];
        setEvaluator(eval);

        for(int i=0;i<parameters.length;i++)
            parameters[i]=lowers[i]+Math.random()*(uppers[i]-lowers[i]);

    }

    /**
     * reset the DESolution an re-initializes it
     * @param lowers
     * @param uppers
     */
    public void reset( double [] lowers, double [] uppers ){
        fitness=null;

        for(int i=0;i<parameters.length;i++)
            parameters[i]=lowers[i]+Math.random()*(uppers[i]-lowers[i]);
    }

    public void reset(){
        fitness=null;

        for(int i=0;i<parameters.length;i++)
            parameters[i]=Math.random();
    }

    
    public double getParameter( int idx ){
        return parameters[idx];
    }

    public void setParameter( int idx, double val ){
        fitness=null;
        parameters[idx]=val;
    }

    public int size(){
        return parameters.length;
    }

    public void copy( DESolution other ){
        fitness=other.getFitness();
        System.arraycopy( other.parameters, 0, parameters, 0, size() );
    }


    public Double getFitness() {

        if( fitness==null ){
            fitness=getEvaluator().getFitness(this);
        }
        return fitness;
    }

    public Double recalcFitness() {

        fitness=getEvaluator().getFitness(this);
        return fitness;
    }

    public boolean isBetter(IMetaheuristicSolution other) {

        if( !(other instanceof DESolution) ){
            throw new ClassCastException("bad DESolution.");
        }

        return getEvaluator().isBetter(getFitness(), other.getFitness());
    }

    /**
     * @return the evaluator
     */
    public IDESolutionEval getEvaluator() {
        return evaluator;
    }

    /**
     * @param evaluator the evaluator to set
     */
    protected void setEvaluator(IDESolutionEval evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * invalidate the current fitness 
     */
    public void invalidate(){
        fitness=null;
    }

    /**
     * copy the parameters into vector
     * 
     * @param vector where to copy the parameters
     */
    public void getParameters( double [] vector ){
        System.arraycopy( this.parameters, 0, vector, 0, size() );
    }

    /**
     * returns the internal array of parameters
     * 
     * @return
     */
    public double [] getParameters(){
        return this.parameters;
    }

    /**
     * set the parameters with the values of vector
     *
     * @param vector
     */
    public void setParameters( double [] vector ){
        fitness=null;
        System.arraycopy( vector, 0, this.parameters, 0, size() );
    }

    /**
     * 
     * @param out
     * @throws java.io.IOException
     */
    public void toFile( BufferedWriter out ) throws IOException{
        for(int j=0; j<this.size(); j++){
            out.write( this.getParameter(j)+"\n" );
        }
    }

    /**
     * appends values to string
     *
     * @param cad
     */
    public void toSaveString( String cad ){
        for(int j=0; j<this.size(); j++){
            cad+=this.getParameter(j)+"\n";
        }
    }
    
}
