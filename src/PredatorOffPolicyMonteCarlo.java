
import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Lyltje
 */
public class PredatorOffPolicyMonteCarlo implements Agent {
    
    private double tau;
    private int nrRuns, currentNrRuns;
    private StateRepresentation QValuesBehavior, QValuesEstimation, numerator, denominator;    
    private ArrayList <SARcombi> SARcombis;
    private Position myPos;
    private Position startPos;
    private Position preyPos;
    private int state;
    private StateRepresentation.Action currentAction;
    private ArrayList<ArrayList<ArrayList<Double>>> rewards;
    private double discountFactor;
    boolean print;
    boolean useBehaviorPolicy, softMax;
    
    public PredatorOffPolicyMonteCarlo (double tau, int nrRuns, double init, Position startPos, Position startPosPrey, double discount){
        print = false;
        useBehaviorPolicy = true;
        softMax = true;
        this.nrRuns = nrRuns;
        discountFactor = discount;
        this.tau = tau;
        QValuesBehavior = new StateRepresentation(init);
        QValuesEstimation= new StateRepresentation(init);
        numerator= new StateRepresentation(0);
        denominator= new StateRepresentation(0);
        SARcombis = new ArrayList<SARcombi>();
        myPos = new Position(startPos);
        currentNrRuns = 0;
        this.startPos = new Position(startPos);
        preyPos = new Position(startPosPrey);
        int [] reldis = QValuesBehavior.getRelDistance(myPos, preyPos);
        state = QValuesBehavior.relDistanceToLinearIndex(reldis[0], reldis[1]);
        rewards = new ArrayList<ArrayList<ArrayList<Double>>>();
        for(int s = 0; s<StateRepresentation.nrStates;s++){
            rewards.add(new ArrayList<ArrayList<Double>>());
            for(int a = 0; a<StateRepresentation.nrActions;a++){
                rewards.get(s).add(new ArrayList<Double>());
            }
        }
    }

    @Override
    public void doMove(Position other) {
        if(useBehaviorPolicy){
            doMoveBehaviorPolicy(other);
        }
        else{
            doMoveEstimationPolicy(other);
        }
    }
    
    private void doMoveBehaviorPolicy(Position other) {
         //Pick action using softmax algorithm on Q-values of behavior policy
        
        //Receive action values from state space representation
        double[] values = QValuesBehavior.getStateActionPairValues(state);
        if(softMax){
            double[] probabilities = softMaxProbabilities(values);             
            //Make probabilities cumulative
            for(int i = 1; i< StateRepresentation.nrActions-1;i++){
                probabilities[i] = probabilities[i]+probabilities[i-1];
            }
            probabilities[StateRepresentation.nrActions-1] = 1.0;
            //Draw value
            double p = Math.random();
            //Determine action, save action and return action
            for(int i = 0;i<StateRepresentation.nrActions;i++){
                if(p<=probabilities[i]){
                    int a = QValuesBehavior.getMove(myPos, preyPos, i, print);
                    currentAction = QValuesBehavior.returnAction(i);
                    myPos.adjustPosition(a);
                    break;
                }
            }
        }
    }
    
    private double [] softMaxProbabilities(double[] values){
        //Calculate probability per action
            
        //Calculate probabilities
        //Calculate total
        double total =0.0;
        for(int i = 0; i< StateRepresentation.nrActions;i++){
            total += Math.exp(values[i]/tau);
        }
        double[] probabilities = new double [StateRepresentation.nrActions];
        for(int i = 0; i< StateRepresentation.nrActions;i++){
            probabilities[i] = Math.exp(values[i]/tau)/total;
        }
        return probabilities;
    }

    private void doMoveEstimationPolicy(Position other) {
        //Take greedy action based on Q values of Determination Policy
        
        //Receive action values from state space representation
        double[] values = QValuesEstimation.getStateActionPairValues(state);
        //Determine action with highest values;
        int highestValueAction = highestValueAction(values);
        //Translate state space representation action to real world action
        int action = QValuesEstimation.getMove(myPos, other, highestValueAction, print);
        //Execute action
        myPos.adjustPosition(action);
    }
    
    private int highestValueAction(double[] values){
        double highestValue = -1.0;
        int highestValueAction = -1;
        for(int a = 0 ; a<StateRepresentation.nrActions;a++){
            if(values[a]>highestValue){
                highestValue = values[a];
                highestValueAction=a;
            }
        }
        return highestValueAction;
    }

    @Override
    public void observeReward(double reward, Position prey) {
        int [] reldis = QValuesBehavior.getRelDistance(myPos, prey);
        preyPos = new Position(prey);        
        SARcombis.add(new SARcombi(state, currentAction, reward));   
        state = QValuesBehavior.relDistanceToLinearIndex(reldis[0], reldis[1]);
    }
    
    private void fill(int[][] stateActionPairs) {
        for(int i = 0; i<stateActionPairs.length;i++){
            stateActionPairs[i][0]=-1;
            stateActionPairs[i][1]=-1;
        }
    }
    
    public void learnAfterEpisode(){
        int timeStep = lastUnequalAction();
        int [][] stateActionPairs = new int [SARcombis.size()][2];
        fill(stateActionPairs);
        for(int i = timeStep; i<SARcombis.size();i++){
            if(!contains(stateActionPairs, SARcombis.get(i))){
                SARcombi current = SARcombis.get(i);
                double w = 1.0;
                double R = current.getReward();
                for(int j = i+1; j<SARcombis.size();j++){
                    double [] prob = softMaxProbabilities(QValuesBehavior.getStateActionPairValues(SARcombis.get(j).getState()));
                    w = w * prob[SARcombis.get(j).getAction().getIntValue()];
                    R = R + SARcombis.get(j).getReward();
                }                
                double value = numerator.getValue(current.getState(), current.getAction())+w*R;
                numerator.setValue(current.getState(), current.getAction(), value);
                denominator.setValue(current.getState(), current.getAction(),w);
                value = numerator.getValue(current.getState(),current.getAction())/denominator.getValue(current.getState(),current.getAction());
                QValuesEstimation.setValue(current.getState(), current.getAction(), value);
            }
        }        
        SARcombis = new ArrayList<SARcombi>();
        currentNrRuns++;
    }
    
    private boolean contains(int[][] stateActionPairs, SARcombi sar) {
        for(int i = 0; i<stateActionPairs.length;i++){
                if(sar.getAction().getIntValue()==stateActionPairs[i][1] && sar.getState()==stateActionPairs[i][0] ){
                    return true;
                }
        }
        return false;
    }

    @Override
    public double[] policy(Position prey, Position predator) {
        int[] reldis=QValuesEstimation.getRelDistance(predator, prey);
        int linearIndex = QValuesEstimation.relDistanceToLinearIndex(reldis[0], reldis[1]);
        double [] prob = new double [StateRepresentation.nrActions];
        for(int i = 0; i<StateRepresentation.nrActions;i++){
            prob[i]=0.0;
        }
        double [] values = QValuesEstimation.getStateActionPairValues(linearIndex);
        prob[highestValueAction(values)]=1.0;
              
        
        double[]probabilitiesRealWorld = new double [StateRepresentation.nrActions];
        for(int i = 0; i< StateRepresentation.nrActions;i++){
            int realWorldAction = QValuesEstimation.getMove(predator, prey, i, false);
            probabilitiesRealWorld[realWorldAction] = prob[i];
        }
       return probabilitiesRealWorld;
    }
    
    
    public void useBehaviorPolicy(boolean value){
        useBehaviorPolicy = value;
    }
    
    public void setBehaviorPolicy(boolean type, StateRepresentation Qvalues){
        softMax = type;
        QValuesBehavior = Qvalues;        
    }
    
    @Override
    public Position getPos() {
        return myPos;
    }

    @Override
    public void reset() {
        myPos = new Position(startPos);        
    }

    @Override
    public boolean isConverged() {
        return nrRuns < currentNrRuns;
    }
    
    public void resetSAR(){
        SARcombis=new ArrayList<>();
    }

    private int lastUnequalAction() {
        for(int i = 0; i<SARcombis.size();i++){
            SARcombi sar = SARcombis.get(i);
            int behaviorState = sar.getState();
            int behaviorAction = sar.getAction().getIntValue();
            double[] valuesEstimation = QValuesEstimation.getStateActionPairValues(behaviorState);
            int estimationAction = highestValueAction(valuesEstimation);
            if(estimationAction!=behaviorAction){
                return i;
            }
        }
        return 0;
    }
    
    public StateRepresentation getQValuesBehavior(){
        return QValuesBehavior;
    }
    
    public StateRepresentation getQValuesEst(){
        return QValuesEstimation;
    }
    
    private class SARcombi{
        private int state;
        private StateRepresentation.Action action;
        private double reward;
        
        public SARcombi(int s, StateRepresentation.Action a, double r){
            state = s;
            action = a;
            reward = r;
        }
        
        public int getState(){
            return state;
        }
        
        public StateRepresentation.Action getAction(){
            return action;
        }
        
        public double getReward(){
            return reward;
        }
        
    }
}
