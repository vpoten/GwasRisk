/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.neural;

/**
 *
 * @author victor
 */
public class Neuron {

    protected double [] weights = null;


    /**
     * creates internal weights and initialize it randomly on interval [-1,1]
     *
     * @param ninputs
     */
    public Neuron( int ninputs ) {
        this.weights=new double [ninputs];

        for(int i=0;i<this.weights.length;i++)
            this.weights[i]=2.0*Math.random()-1.0;
    }

    /**
     * copy the values of vector, starting at initpos, into weights
     *
     * @param vector
     * @param initpos
     */
    public void setWeights( double [] vector, int initpos ){
        System.arraycopy(vector, initpos, this.weights, 0, this.weights.length);
    }

    /**
     * copy the values of internal weights into vector, starting at initpos
     *
     * @param vector
     * @param initpos
     */
    public void getWeights( double [] vector, int initpos ){
        System.arraycopy(this.weights, 0, vector, initpos, this.weights.length);
    }

    public double [] getWeights(){
        return this.weights;
    }

    public double getWeight(int i){
        return this.weights[i];
    }

    public void setWeight(int i, double val){
        this.weights[i]=val;
    }

    public int getNumWeights(){
        return this.weights.length;
    }

    /**
     * calculates the output sumatory
     *
     * @param inputs
     * @return
     */
    public double output( double [] inputs ){

        double sum=0;

        for(int i=0; i<this.weights.length; i++)
            sum += this.weights[i]*inputs[i];

        return sum;
    }


}
