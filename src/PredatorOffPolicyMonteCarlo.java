
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
    
    /**
     * Initializes all values
     * @param tau parameter for softmax
     * @param nrRuns how many episodes the agent will learn
     * @param init At which value the Q-values are initialized
     * @param startPos Starting position of agent
     * @param startPosPrey Starting position of prey
     * @param discount Discount factor for learning
     */
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

    /**
     * Executes an action based on the behavior policy or estimation policy
     * @param other Position of the prey
     */
    @Override
    public void doMove(Position other) {
        if(useBehaviorPolicy){
            doMoveBehaviorPolicy(other);
        }
        else{
            doMoveEstimationPolicy(other);
        }
    }
    
    /**
     * Executes action based on the behavior policy using softmax
     * @param other 
     */
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
    
    /**
     * Generates the probabilities for each action according to softmax
     * @param values Q-values for each action
     * @return probabilities for each action
     */
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

    /**
     * Executes action according to greedy estimation policy
     * @param other position of the prey
     */
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
    
    /**
     * returns the index of the action with the highest Q-value
     * @param values Q values
     * @return index of action with the highest Q-value
     */    
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

    /**
     * Observes reward and incorporates it in the list of state-action-reward triples
     * and updates state
     * @param reward received reward
     * @param prey  position of prey to determine new state
     */
    @Override
    public void observeReward(double reward, Position prey) {
        int [] reldis = QValuesBehavior.getRelDistance(myPos, prey);
        preyPos = new Position(prey);        
        SARcombis.add(new SARcombi(state, currentAction, reward));   
        state = QValuesBehavior.relDistanceToLinearIndex(reldis[0], reldis[1]);
    }
    /**
     * Fills state action pairs with initial values
     * @param stateActionPairs 
     */
    private void fill(int[][] stateActionPairs) {
        for(int i = 0; i<stateActionPairs.length;i++){
            stateActionPairs[i][0]=-1;
            stateActionPairs[i][1]=-1;
        }
    }
    
    /**
     * actual learning after episode
     */
    public void learnAfterEpisode(){
        int timeStep = lastUnequalAction();
        int [][] stateActionPairs = new int [SARcombis.size()][2];
        fill(stateActionPairs);
        //For each state-action-reward triple
        for(int i = timeStep; i<SARcombis.size();i++){
            if(!contains(stateActionPairs, SARcombis.get(i))){
                SARcombi current = SARcombis.get(i);
                double w = 1.0;
                double R = current.getReward();
                //For each remaining state-action-reward triple
                for(int j = i+1; j<SARcombis.size();j++){
                    //incorporate weight and reward
                    double [] prob = softMaxProbabilities(QValuesBehavior.getStateActionPairValues(SARcombis.get(j).getState()));
                    w = w * prob[SARcombis.get(j).getAction().getIntValue()];
                    R = R + SARcombis.get(j).getReward();
                }                
                double value = numerator.getValue(current.getState(), current.getAction())+w*R;
                numerator.setValue(current.getState(), current.getAction(), value);
                denominator.setValue(current.getState(), current.getAction(),w);
                //Update Q value
                value = numerator.getValue(current.getState(),current.getAction())/denominator.getValue(current.getState(),current.getAction());
                QValuesEstimation.setValue(current.getState(), current.getAction(), value);
            }
        }        
        SARcombis = new ArrayList<>();
        currentNrRuns++;
    }
    
    /**
     * determines if a state action pair is already present in list
     * @param stateActionPairs
     * @param sar
     * @return 
     */
    private boolean contains(int[][] stateActionPairs, SARcombi sar) {
        for(int i = 0; i<stateActionPairs.length;i++){
                if(sar.getAction().getIntValue()==stateActionPairs[i][1] && sar.getState()==stateActionPairs[i][0] ){
                    return true;
                }
        }
        return false;
    }

    /**
     * returns probability distribution over actions given the position of the prey and predator
     * @param prey position of prey
     * @param predator position of predator
     * @return probability distribution
     */
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
    
    /**
     * Boolean can be set on which policy is used when a move is requested
     * @param value whether or not behavior policy should be used
     */
    public void useBehaviorPolicy(boolean value){
        useBehaviorPolicy = value;
    }
    
    /**
     * Provides Q values for behavior policy
     * @param type type of policy
     * @param Qvalues actual Q-values of policy
     */
    public void setBehaviorPolicy(boolean type, StateRepresentation Qvalues){
        softMax = type;
        QValuesBehavior = Qvalues;        
    }
    
    /**
     * Returns position of agent
     */
    @Override
    public Position getPos() {
        return myPos;
    }

    /**
     * resets agent
     */
    @Override
    public void reset() {
        myPos = new Position(startPos);        
    }

    /**     * 
     * @return Whether or not the number of runs to learn have been done
     */
    @Override
    public boolean isConverged() {
        return nrRuns < currentNrRuns;
    }
    
    /**
     * Reset list of state-action-reward triples
     */
    public void resetSAR(){
        SARcombis=new ArrayList<>();
    }

    
    /**
     * returns index of last moment where behavior policy and estimation policy would have taken a different action
     * @return index
     */
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
    
    /**
     * returns Q values of behavior policy
     */
    public StateRepresentation getQValuesBehavior(){
        return QValuesBehavior;
    }
    
    /*
     * Returns Q values of the estimation policy
     */
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
