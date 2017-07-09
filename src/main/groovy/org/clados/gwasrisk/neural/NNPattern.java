/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.neural;

/**
 * Neural network pattern
 * 
 * @author victor
 */
public class NNPattern {
    private double [] inputs = null;
    private double[] outputs = null;


    public NNPattern( double [] in, double [] out ) {

        inputs=new double [in.length];
        outputs=new double [out.length];

        setInputs(in);
        setOutputs(out);
    }

    /**
     *
     * @param buffer vector of ninputs+noutputs lengh where from copy the values
     * @param ninputs
     * @param noutputs
     */
    public NNPattern(double[] buffer, int ninputs, int noutputs) {

        inputs=new double [ninputs];
        outputs=new double [noutputs];

        int pos=0;

        for(int i=0; i<ninputs; i++)
            inputs[i]=buffer[pos++];

        for(int i=0; i<noutputs; i++)
            outputs[i]=buffer[pos++];
    }

    /**
     * @return the inputs
     */
    public double[] getInputs() {
        return inputs;
    }

    /**
     * copy the values of inputs into internal input
     *
     * @param inputs the inputs to set
     */
    public void setInputs(double[] inputs) {
        System.arraycopy(inputs, 0, this.inputs, 0, this.inputs.length);
    }

    /**
     * @return the outputs
     */
    public double[] getOutputs() {
        return outputs;
    }

    /**
     * copy the values of outputs into internal output
     *
     * @param outputs the outputs to set
     */
    public void setOutputs(double[] outputs) {
        System.arraycopy(outputs, 0, this.outputs, 0, this.outputs.length);
    }

    /**
     * @return the number of inputs
     */
    public int getNInputs() {
        return inputs.length;
    }

    /**
     * @return the number of outputs
     */
    public int getNOutputs() {
        return outputs.length;
    }

    /**
     * 
     * @return the index of the maximun output value
     */
    public int getMaxOutput(){

        int max=0;
        double vmax=outputs[0];

        for(int j=1; j<outputs.length; j++){

            if( outputs[j]>vmax ){
                vmax=outputs[j];
                max=j;
            }
        }

        return max;
    }

}
