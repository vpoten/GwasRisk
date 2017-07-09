/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.metaheuristic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author victor
 */
public class DifferentialEvolution extends MetaheuristicBase {

    // DE parameters
    private double F = 0.5;
    private double CR = 0.5;
    private int NP = 100;

    // operators
    private IDEMutation mutation=null;
    private IDECrossover crossover = null;

    // population
    private ArrayList<DESolution> population=null;
    private ArrayList<DESolution> newPopulation=null;

    //bounds for parameters
    private double [] upperBounds=null;
    private double [] lowerBounds=null;

//    //objects used in FCM
//    private boolean hybridFCM=false;
//    private FCM fcmCluster = new FCM();
//    private double [][] dataSetFCM=null;
//    private int fcmPeriod = 10;
//
//    //objects used in FCM + FANS
//    private boolean fansFCM=false;
//    private FANSDouble fans=null;
//    private int fansIter=100;
//    private double K_SIG_INI=0.05;//FANS mutation std dev

    //if modify the F parameter randomly between [0.5,1]
    private boolean modifyF = false;

    private ArrayList<DEPopLoopWork> workers=null;
//    private ArrayList<DEFansWork> fansWorkers=null;
    private int TIME_SLEEP = 1000;//in milliseconds

    
    public DifferentialEvolution() {

    }

    /**
     * construct from a saved string
     *
     * @param r : buffered reader
     * @param eval :  DESolution evaluator
     */
    public DifferentialEvolution(BufferedReader r, IDESolutionEval eval)
            throws IOException {


        String l_line=r.readLine();
        this.setF( Double.parseDouble(l_line) );

        l_line=r.readLine();
        this.setCR( Double.parseDouble(l_line) );
        
        l_line=r.readLine();
        this.setNP( Integer.parseInt(l_line) );

        l_line=r.readLine();
        int dim=Integer.parseInt(l_line);

        setNewPopulation( new ArrayList<DESolution>() );
        setPopulation( new ArrayList<DESolution>() );

        //create population
        for(int i=0; i<getNP(); i++){

            DESolution sol=new DESolution( dim, eval);

            for(int j=0; j<dim; j++){
                l_line=r.readLine();
                sol.setParameter(j, Double.parseDouble(l_line));
            }

            getPopulation().add( sol );
            getNewPopulation().add( new DESolution( dim, eval) );
        }

        
        if( eval!=null ){
            //sort population by fitness
            Collections.sort(getPopulation(), solComp);

            //get the current solution
            this.setSolution( getPopulation().get(getPopulation().size()-1) );
        }

        r.close();

    }



    
    public void doSearch() {

        if( getMutation()==null || getCrossover()==null || getPopulation()==null
                || getNewPopulation()==null ){
            throw new RuntimeException("DE bad initialization.");
        }

        DESolution init=getPopulation().get(0);

        DESolution trial = new DESolution(init.size(), init.getEvaluator());
        DESolution mutant = new DESolution(init.size(), init.getEvaluator());

//        if( isHybridFCM() && dataSetFCM==null ){
//            dataSetFCM=new double [getNP()][init.size()];
//        }
        
        setStartTime( new Date() );

        while( !endCondition() ){

            if( this.getWorkers()!=null )
                runPopLoopWorkers( this, getTimeSleep());
            else
                populationLoop( trial, mutant );

            
//            if( isHybridFCM() && (getIterations()%getFcmPeriod()==0) )
//                FCMClustering();
            

            if( isModifyF() )
                setF( Math.random()*0.5 + 0.5);
            

            //sort population by fitness
            Collections.sort(getNewPopulation(), solComp);

            //swap population
            ArrayList<DESolution> swap=this.getPopulation();
            this.setPopulation( this.getNewPopulation() );
            this.setNewPopulation( swap );

            //get the current solution
            this.setSolution( getPopulation().get(getPopulation().size()-1) );

        }
        
        setEndTime( new Date() );
    }

    /**
     *
     * @return the list of workers
     */
    private ArrayList<DEPopLoopWork> getWorkers() {
        return workers;
    }

    /**
     * set the number of working threads for DE loop (one for evaluator)
     * and DE FANS local search if setted
     * @pre : the population must be created before call this method
     *
     * @param evals the list of evaluators used in each thread
     * @param time_sleep in milliseconds, time to sleep waiting fot threads
     */
    public void setWorkers( List<IDESolutionEval> evals, int time_sleep ){

        workers=new ArrayList<DEPopLoopWork>();

        this.setTimeSleep(time_sleep);

        int num=evals.size();

        for(int i=0; i<num; i++){
            workers.add( new DEPopLoopWork( i, num, this, evals.get(i)) );
        }

//        if( isFansFCM() ){
//
//            int dim=getPopulation().get(0).size();
//
//            fansWorkers=new ArrayList<DEFansWork>();
//
//            for(int i=0; i<num; i++){
//                FANSDouble fansd = new FANSDouble(dim, evals.get(i));
//
//                int maxTrials=5;
//                FANSNeighborBase neigFuzzMang =
//                        new FANSNeighborBase( 0.15, maxTrials);
//                neigFuzzMang.setLambda( 0.9 );
//
//                fansd.setEvaluator(neigFuzzMang);
//                fansd.setNeighManager(neigFuzzMang);
//
//                SecStructFANSOper operMang = new SecStructFANSOper(fansd, K_SIG_INI);
//
//                fansd.setConditions(operMang);
//                fansd.setOperator(operMang);
//                fansd.setOperManager(operMang);
//
//                fansWorkers.add( new DEFansWork( i, num, fansd, evals.get(i), getFansIter()) );
//            }
//        }
    }

    /**
     * DE population main loop
     * 
     * @param trial solution to reuse
     * @param mutant solution to reuse
     */
    private void populationLoop( DESolution trial, DESolution mutant ){

        for(int i=0;i<getNP();i++){

            DESolution target = getPopulation().get(i);

            //mutation operation
            this.getMutation().mutate( mutant, i, getPopulation(), getF() );

            //correct mutant vector (ensure inside bounds)
            restrictParameters(mutant);

            //crossover operation
            this.getCrossover().cross( trial, target, mutant, getCR() );

            //selection operation
            if( trial.isBetter(target) ){
                this.getNewPopulation().get(i).copy(trial);
            }
            else{
                this.getNewPopulation().get(i).copy(target);
            }
        }
    }

    public double getLambda() {
        return 1.0;//nothing to do
    }


    public void setLambda(double val) {
        return;//nothing to do
    }

    /**
     * @return the F
     */
    public double getF() {
        return F;
    }

    /**
     * @param F the F to set
     */
    public void setF(double F) {
        this.F = F;
    }

    /**
     * @return the CR
     */
    public double getCR() {
        return CR;
    }

    /**
     * @param CR the CR to set
     */
    public void setCR(double CR) {
        this.CR = CR;
    }

    /**
     * @return the NP
     */
    public int getNP() {
        return NP;
    }

    /**
     * @param NP the NP to set
     */
    public void setNP(int NP) {
        this.NP = NP;
    }

//    /**
//     * do the one step FCM cluster operation (improve exploitation)
//     */
//    private void FCMClustering() {
//
//        //choose the number of clusters between 2 and sqrt(NP)
//        int c = (int)Math.floor(Math.random()*(Math.floor(Math.sqrt(getNP())-2)))+2;
//
//        fcmCluster.createClusters(c, dataSetFCM[0].length);
//
//        //create the dataSet for FCM
//        for( int i=0; i<getNP(); i++ ){
//            getNewPopulation().get(i).getParameters( dataSetFCM[i] );
//        }
//        
//        //gets c different indices
//        int [] indices=new int [c];
//        getDifferent( getNP(), indices);
//
//        //set it as initial centers
//        for(int i=0; i<c; i++){
//            fcmCluster.setCluster( i, dataSetFCM[indices[i]] );
//        }
//
//        //do one step FCM clustering
//        fcmCluster.cluster(dataSetFCM, 0.0, 1);
//
//        ArrayList<DESolution> list=new ArrayList<DESolution>();
//
//        //gets the new centroids as solutions
//        double [] vector = new double [dataSetFCM[0].length];
//
//        for( int i=0; i<c; i++){
//
//            DESolution newSol=new DESolution(dataSetFCM[0].length,
//                    getNewPopulation().get(0).getEvaluator() );
//
//            fcmCluster.getCluster(i, vector);
//            newSol.setParameters(vector);
//            newSol.getFitness();
//            list.add( newSol );
//        }
//
//
//        if( this.isFansFCM() && this.getWorkers()==null ){
//            //if hybrid local search
//            for(int i=0; i<c; i++){
//                fans.reset();
//                fans.setSolution( list.get(i) );
//                fans.setIterLimit( fans.getIterLimit()+getFansIter() );
//                fans.doSearch();
//                list.get(i).copy( (DESolution) fans.getBestSolution());
//            }
//        }
//        else if( this.isFansFCM() ){
//            //if hybrid local search in multiple threads
//            runFansWorkers( this, list, getTimeSleep());
//        }
//
//        //gets c individuals randomly for replacement
//        getDifferent( getNP(), indices);
//
//        for(int i=0; i<c; i++){
//            list.add( getNewPopulation().get(indices[i]) );
//        }
//
//
//        //sort list by fitness
//        Collections.sort( list, solComp);
//
//        for(int i=0; i<c; i++)
//            list.remove(0);//remove the worst of the list
//        
//
//        //update the population with the best solutions
//        for(int i=0; i<c; i++)
//            getNewPopulation().get(indices[i]).copy( list.get(i) );
//        
//
//    }

    
    /**
     * put parameters inside bounds
     *
     * @param mutant
     */
    public void restrictParameters(DESolution mutant) {

        if( getUpperBounds()==null || getLowerBounds()==null )
            return;

        int size=mutant.size();

        for(int i=0;i<size;i++ ){

            if( mutant.getParameter(i) > this.getUpperBounds()[i] ){
                mutant.setParameter(i, this.getUpperBounds()[i]);
            }
            else if( mutant.getParameter(i) < this.getLowerBounds()[i] ){
                mutant.setParameter(i, this.getLowerBounds()[i]);
            }
        }
    }


    /**
     * @return the mutation
     */
    public IDEMutation getMutation() {
        return mutation;
    }

    /**
     * @param mutation the mutation to set
     */
    public void setMutation(IDEMutation mutation) {
        this.mutation = mutation;
    }

    /**
     * @return the crossover
     */
    public IDECrossover getCrossover() {
        return crossover;
    }

    /**
     * @param crossover the crossover to set
     */
    public void setCrossover(IDECrossover crossover) {
        this.crossover = crossover;
    }

    /**
     * @return the population
     */
    public ArrayList<DESolution> getPopulation() {
        return population;
    }

    /**
     * @param population the population to set
     */
    protected void setPopulation(ArrayList<DESolution> population) {
        this.population = population;
    }

    /**
     * @return the newPopulation
     */
    protected ArrayList<DESolution> getNewPopulation() {
        return newPopulation;
    }

    /**
     * @param newPopulation the newPopulation to set
     */
    protected void setNewPopulation(ArrayList<DESolution> newPopulation) {
        this.newPopulation = newPopulation;
    }

    /**
     * @return the lowerBounds
     */
    protected double[] getLowerBounds() {
        return lowerBounds;
    }

    /**
     * @param lowerBounds and upperBounds
     */
    public void setBounds(double[] lowerBounds, double[] upperBounds) {
        this.lowerBounds = lowerBounds;
        this.upperBounds = upperBounds;
    }

    /**
     * @return the upperBounds
     */
    protected double[] getUpperBounds() {
        return upperBounds;
    }

    /**
     * creates a random population
     * 
     * @param size
     * @param eval
     */
    public void createRandomPopulation( int size, IDESolutionEval eval ){

        setNewPopulation( new ArrayList<DESolution>() );
        setPopulation( new ArrayList<DESolution>() );

        for(int i=0;i<getNP();i++){

            if( getUpperBounds()==null ){
                getPopulation().add( new DESolution( size, eval) );
                getNewPopulation().add( new DESolution( size, eval) );
            }
            else{
                getPopulation().add( new DESolution( size, eval,
                        getLowerBounds(), getUpperBounds() ) );
                getNewPopulation().add( new DESolution( size, eval,
                        getLowerBounds(), getUpperBounds() ) );
            }
        }

    }

    /**
     *
     * @param size
     * @param eval
     * @param low : low value
     * @param up : up value
     */
    public void createRandomPopulation( int size, IDESolutionEval eval, double low, double up ){

        setNewPopulation( new ArrayList<DESolution>() );
        setPopulation( new ArrayList<DESolution>() );

        for(int i=0;i<getNP();i++){
            getPopulation().add( new DESolution( size, eval, low, up) );
            getNewPopulation().add( new DESolution( size, eval, low, up) );
        }
    }
    
    /**
     * invalidate the solutions
     * 
     */
    public void invalidate(){

        for(int i=0;i<getNP();i++){
            getPopulation().get(i).invalidate();
            getNewPopulation().get(i).invalidate();
        }

        this.setSolution(null);
        this.setBestSolution(null);
        
    }

    /**
     * reset the solutions
     *
     */
    @Override
    public void reset() {

        if( getPopulation()!=null ){
            for(int i=0;i<getNP();i++){
                if( getUpperBounds()==null ){
                    getPopulation().get(i).reset();
                    getNewPopulation().get(i).reset();
                }
                else{
                    getPopulation().get(i).reset(getLowerBounds(), getUpperBounds());
                    getNewPopulation().get(i).reset(getLowerBounds(), getUpperBounds());
                }
            }
        }

        this.setSolution(null);
        this.setBestSolution(null);
    }

//    /**
//     * @return the hybridFCM
//     */
//    public boolean isHybridFCM() {
//        return hybridFCM;
//    }

//    /**
//     * @param hybridFCM the hybridFCM to set
//     */
//    public void setHybridFCM(boolean hybridFCM) {
//        this.hybridFCM = hybridFCM;
//    }

    /**
     * get indices.length different indices between 0 and size-1
     *
     * @param size
     * @param indices I/O
     */
    private void getDifferent(int size, int [] indices) {

        for(int i=0;i<indices.length;i++){

            boolean repeat=false;

            do{
                repeat=false;
                indices[i]=(int) Math.floor( size*Math.random() );

                for(int j=0;j<i;j++){
                    if(indices[j]==indices[i]){
                        repeat=true;
                        break;
                    }
                }

            }while( repeat );

        }
    }

//    /**
//     * @return the fcmPeriod
//     */
//    public int getFcmPeriod() {
//        return fcmPeriod;
//    }

//    /**
//     * @param fcmPeriod the fcmPeriod to set
//     */
//    public void setFcmPeriod(int fcmPeriod) {
//        this.fcmPeriod = fcmPeriod;
//    }

    /**
     * @return the modifyF
     */
    public boolean isModifyF() {
        return modifyF;
    }

    /**
     * if true modify the F parameter randomly between [0.5,1]
     *
     * @param modifyF the modifyF to set
     */
    public void setModifyF(boolean modifyF) {
        this.modifyF = modifyF;
    }

    
    /**
     *
     * @param de
     * @param sleeptime : sleeptime between termination checks in milliseconds
     */
    private static void runPopLoopWorkers( DifferentialEvolution de, int sleeptime ){

        ExecutorService es =  Executors.newFixedThreadPool( de.getWorkers().size() );

        for( DEPopLoopWork worker : de.getWorkers() ){
            es.execute( worker );
        }

        es.shutdown();

        //wait all threads to finish
        while( !es.isTerminated() ){

            try {
                Thread.sleep(sleeptime);
            } catch (InterruptedException ex) {
                Logger.getLogger(DifferentialEvolution.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

//    /**
//     *
//     * @param de
//     * @param list : list of solutions to optimize
//     * @param sleeptime : sleeptime between termination checks in milliseconds
//     */
//    private static void runFansWorkers(DifferentialEvolution de, ArrayList<DESolution> list, int sleeptime) {
//        ExecutorService es =  Executors.newFixedThreadPool( de.getFansWorkers().size() );
//
//        for( DEFansWork worker : de.getFansWorkers() ){
//            worker.setListSol(list);
//            es.execute( worker );
//        }
//
//        es.shutdown();
//
//        //wait all threads to finish
//        while( !es.isTerminated() ){
//
//            try {
//                Thread.sleep(sleeptime);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(DifferentialEvolution.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }

    /**
     * @return the TIME_SLEEP
     */
    protected int getTimeSleep() {
        return TIME_SLEEP;
    }

    /**
     * @param TIME_SLEEP the TIME_SLEEP to set
     */
    protected void setTimeSleep(int TIME_SLEEP) {
        this.TIME_SLEEP = TIME_SLEEP;
    }

    
    public String toSaveString(){

        String cad="";

        cad+=this.getF()+"\n";//F
        cad+=this.getCR()+"\n";//CR
        cad+=this.getNP()+"\n";//population size
        cad+=this.getPopulation().get(0).size()+"\n";//vector length

        for( DESolution sol : this.getPopulation() ){
            sol.toSaveString(cad);
        }

        return cad;
    }


    public void toFile( String filename ) throws IOException{

        BufferedWriter out = new BufferedWriter(new FileWriter(filename));

        out.write( this.getF()+"\n" );//F
        out.write( this.getCR()+"\n" );//CR
        out.write( this.getNP()+"\n" );//population size
        out.write( this.getPopulation().get(0).size()+"\n" );//vector length

        for( DESolution sol : this.getPopulation() ){
            sol.toFile(out);
        }

        out.close();
    }

//    /**
//     * @return the fansFCM
//     */
//    public boolean isFansFCM() {
//        return fansFCM;
//    }

//    /**
//     * @param fansFCM the fansFCM to set
//     */
//    public void setFansFCM(boolean fansFCM, FANSDouble fans, double sigma_ini) {
//        this.fansFCM = fansFCM;
//        this.fans=fans;
//        this.K_SIG_INI=sigma_ini;
//    }

//    /**
//     * @return the fansIter
//     */
//    public int getFansIter() {
//        return fansIter;
//    }

//    /**
//     * @param fansIter the fansIter to set
//     */
//    public void setFansIter(int fansIter) {
//        this.fansIter = fansIter;
//    }

//    /**
//     * @return the fansWorkers
//     */
//    private ArrayList<DEFansWork> getFansWorkers() {
//        return fansWorkers;
//    }


    
}
