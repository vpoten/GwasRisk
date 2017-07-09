/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.neural;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Implements feedforward neural network
 * 
 * @author victor
 */
public class NeuralNetwork {
    private int nInputs = 0;
    private int nOutputs = 0;
    private int nHidden = 0;
    private ArrayList<Neuron> hiddenLayer = new ArrayList<Neuron>();
    private ArrayList<Neuron> outputLayer = new ArrayList<Neuron>();

    protected double [] outsHidden = null;
    protected double [] outputs = null;

    
    public NeuralNetwork() {
    }


    /**
     * construct from a saved string
     * 
     * @param nnSaved string generated previously with toSaveString method
     * @throws java.io.IOException
     */
    public NeuralNetwork( String nnSaved )
            throws IOException{

        BufferedReader r = new BufferedReader(new StringReader( nnSaved ));

        String l_line=r.readLine();
        this.setNInputs( Integer.parseInt(l_line) );

        l_line=r.readLine();
        this.setNOutputs( Integer.parseInt(l_line) );

        l_line=r.readLine();
        this.setNHidden( Integer.parseInt(l_line) );

        //read hidden layer weights
        double [] weights=new double [this.getNInputs()];

        for(int i=0; i<getNHidden(); i++){
            Neuron neu=new Neuron( weights.length );

            for(int j=0; j<weights.length; j++){
                l_line=r.readLine();
                weights[j]=Double.parseDouble(l_line);
            }

            neu.setWeights(weights, 0);
            this.getHiddenLayer().add( neu );
        }

        //read output layer weights
        weights=new double [this.getNHidden()];
        
        for(int i=0; i<getNOutputs(); i++){
            Neuron neu=new Neuron( weights.length );

            for(int j=0; j<weights.length; j++){
                l_line=r.readLine();
                weights[j]=Double.parseDouble(l_line);
            }

            neu.setWeights(weights, 0);
            this.getOutputLayer().add( neu );
        }


        r.close();

    }

    /**
     *
     * @param nin number of inputs
     * @param nout number of outputs
     * @param nhid number of neurons in hidden layer
     */
    public NeuralNetwork(int nin, int nout, int nhid) {

        this.setNInputs(nin);
        this.setNOutputs(nout);
        this.setNHidden(nhid);

        for(int i=0; i<getNHidden(); i++){
            this.getHiddenLayer().add( new Neuron(getNInputs()) );
        }
        
        outsHidden=new double [getNHidden()];

        for(int i=0; i<getNOutputs(); i++){
            this.getOutputLayer().add( new Neuron(this.getNHidden()) );
        }

        outputs=new double [getNOutputs()];

    }


    /**
     * @return the nInputs
     */
    public int getNInputs() {
        return nInputs;
    }

    public int getRealNInputs() {
        return getNInputs();
    }

    /**
     * @return the nOutputs
     */
    public int getNOutputs() {
        return nOutputs;
    }

    /**
     * @return the nHidden
     */
    public int getNHidden() {
        return nHidden;
    }

    /**
     * @param nInputs the nInputs to set
     */
    protected void setNInputs(int nInputs) {
        this.nInputs = nInputs;
    }

    /**
     * @param nOutputs the nOutputs to set
     */
    protected void setNOutputs(int nOutputs) {
        this.nOutputs = nOutputs;
    }

    /**
     * @param nHidden the nHidden to set
     */
    protected void setNHidden(int nHidden) {
        this.nHidden = nHidden;
    }

    /**
     * @return the hiddenLayer
     */
    protected ArrayList<Neuron> getHiddenLayer() {
        return hiddenLayer;
    }

    /**
     * @return the outputLayer
     */
    protected ArrayList<Neuron> getOutputLayer() {
        return outputLayer;
    }

    /**
     * evaluate the network over the inputs
     *
     * @param inputs
     */
    public void evaluate( double [] inputs ){

        for(int i=0; i<getNHidden(); i++){
            outsHidden[i]=this.getHiddenLayer().get(i).output(inputs);
            outsHidden[i]=transferFunction(outsHidden[i]);
        }

        for(int i=0; i<getNOutputs(); i++){
            outputs[i]=this.getOutputLayer().get(i).output(outsHidden);
            outputs[i]=transferFunction(outputs[i]);
        }
    }


    protected double transferFunction( double x ){
        return sigmoid(x);
    }

    protected double sigmoid(double x){
        return 1.0/(1+Math.exp(-x));
    }

    protected double identity(double x){
        return x;
    }
    

    /**
     * cpoy the output into vector
     *
     * @param vector
     */
    public void getOutput( double [] vector ){
        System.arraycopy(outputs, 0, vector, 0, outputs.length);
    }

    protected void getHiddenOutput( double [] vector ){
        System.arraycopy(outsHidden, 0, vector, 0, outsHidden.length);
    }

    /**
     * sets the weights of all neurons in the net (hidden+outputs)
     * 
     * @param vector of length : #hidden*#inputs + #hidden*#outputs
     */
    public void setWeights( double [] vector ){

        int hweights = getNInputs()*getNHidden();

        for(int i=0; i<getNHidden(); i++){
            this.getHiddenLayer().get(i).setWeights(vector, getNInputs()*i );
        }

        for(int i=0; i<getNOutputs(); i++){
            this.getOutputLayer().get(i).setWeights(vector, hweights+getNHidden()*i );
        }
    }

    /**
     * gets the weights of all neurons in the net (hidden+outputs)
     *
     * @param vector of length : #hidden*#inputs + #hidden*#outputs
     */
    public void getWeights( double [] vector ){

        int hweights = getNInputs()*getNHidden();

        for(int i=0; i<getNHidden(); i++){
            this.getHiddenLayer().get(i).getWeights(vector, getNInputs()*i );
        }

        for(int i=0; i<getNOutputs(); i++){
            this.getOutputLayer().get(i).getWeights(vector, hweights+getNHidden()*i );
        }
    }

    /**
     * calculates the sum of squared error, compared with the current output
     *
     * @param outExpect the expected output
     * @return
     */
    public double calcSSE( double [] outExpect ){

        double sse=0;

        for(int j=0; j<outputs.length; j++){
            double temp = outputs[j]-outExpect[j];
            sse += temp*temp;
        }

        return sse;
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
    
    /**
     * reset the network
     */
    public void init(){
        for(int j=0; j<outputs.length; j++)
            outputs[j]=0.0;
    }

    
    public String toSaveString(){

        String cad="";

        cad+=this.getNInputs()+"\n";
        cad+=this.getNOutputs()+"\n";
        cad+=this.getNHidden()+"\n";

        double [] weights=null;

        for(Neuron neu : this.getHiddenLayer() ){
            weights=neu.getWeights();

            for(int j=0; j<weights.length; j++)
                cad+=weights[j]+"\n";
        }

        for(Neuron neu : this.getOutputLayer() ){
            weights=neu.getWeights();

            for(int j=0; j<weights.length; j++)
                cad+=weights[j]+"\n";
        }

        return cad;
    }
}
