
import java.util.ArrayList;
import java.util.Random;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author 10333843
 */
public class PredatorOnPolicyMonteCarlo implements Agent{

    private double tau;
    private int nrRuns, currentNrRuns;
    private StateRepresentation representation;
    private ArrayList <SARcombi> SARcombis;
    private Position myPos;
    private Position startPos;
    private Position preyPos;
    private int state;
    private StateRepresentation.Action currentAction;
    private ArrayList<ArrayList<ArrayList<Double>>> rewards;
    private double discountFactor;
    boolean print;
    
    
    //Policy is impliciet vastgelegd in statespaceRepresentation
    public PredatorOnPolicyMonteCarlo(double tau, int nrRuns, double init, Position startPos, Position startPosPrey, double discount){
        print = false;
        
        this.nrRuns = nrRuns;
        discountFactor = discount;
        this.tau = tau;
        representation = new StateRepresentation(init);
        SARcombis = new ArrayList<SARcombi>();
        myPos = new Position(startPos);
        currentNrRuns = 0;
        this.startPos = new Position(startPos);
        preyPos = new Position(startPosPrey);
        int [] reldis = representation.getRelDistance(myPos, preyPos);
        state = representation.relDistanceToLinearIndex(reldis[0], reldis[1]);
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
        //Pick action using softmax
        
        //Receive action values from state space representation
        double[] values = representation.getStateActionPairValues(state);
        //Calculate probabilities
        //Calculate total
        double total =0.0;
        for(int i = 0; i< StateRepresentation.nrActions;i++){
            total += Math.exp(values[i]/tau);
        }
        //Calculate probability per action
        double[] probabilities = new double [StateRepresentation.nrActions];
        for(int i = 0; i< StateRepresentation.nrActions;i++){
            probabilities[i] = Math.exp(values[i]/tau)/total;
        }
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
                int a = representation.getMove(myPos, preyPos, i, print);
                currentAction = representation.returnAction(i);
                myPos.adjustPosition(a);
                break;
            }
        }
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
        return nrRuns<currentNrRuns;
    }

    @Override
    public void observeReward(double reward, Position prey) {
        int [] reldis = representation.getRelDistance(myPos, prey);
        preyPos = new Position(prey);        
        SARcombis.add(new SARcombi(state, currentAction, reward));   
        state = representation.relDistanceToLinearIndex(reldis[0], reldis[1]);
    }

    private boolean contains(int[][] stateActionPairs, SARcombi sar) {
        for(int i = 0; i<stateActionPairs.length;i++){
                if(sar.getAction().getIntValue()==stateActionPairs[i][1] && sar.getState()==stateActionPairs[i][0] ){
                    return true;
                }
        }
        return false;
    }
    
    
    private class SARcombi{
        int state;
        StateRepresentation.Action action;
        double reward;
        
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
    
    public void learnAfterEpisode(){
        int [][] stateActionPairs = new int [SARcombis.size()][2]; 
        fill(stateActionPairs);
        double R;
        //Voor elk visited s,a pair
        for(int i = 0; i< SARcombis.size();i++){
            if(!contains(stateActionPairs,SARcombis.get(i))){
                R = SARcombis.get(i).getReward();
                for(int j = i+1; j<SARcombis.size();j++){
                    double discountTotal = Math.pow(discountFactor, j-i);
                    R+=discountTotal*SARcombis.get(j).getReward();
                }
                int stateCurrent =SARcombis.get(i).getState();
                int actionCurrent = SARcombis.get(i).getAction().getIntValue();
                stateActionPairs[i][0] = stateCurrent;
                stateActionPairs[i][1] = actionCurrent;
                //Add to list of rewards
                rewards.get(stateCurrent).get(actionCurrent).add(R);
            }
        }
        //Update Q(s,a) values
        for(int i = 0; i<stateActionPairs.length;i++){
            if(stateActionPairs[i][0]!=-1){    
                ArrayList <Double> rewardsOfCombi =rewards.get(stateActionPairs[i][0]).get(stateActionPairs[i][1]);
                double total = 0;
                for(int j = 0; j< rewardsOfCombi.size();j++){
                    total+= rewardsOfCombi.get(j);
                }
                total = total/rewardsOfCombi.size();
                if(stateActionPairs[i][0]!=0){
                    representation.setValue(stateActionPairs[i][0], StateRepresentation.returnAction(stateActionPairs[i][1]), total);
                }
                else{
                    representation.setValue(stateActionPairs[i][0], StateRepresentation.returnAction(stateActionPairs[i][1]), 0.0);
                }
            }
        }   
       
        ///Empty SAR arrayList for next run.
        SARcombis = new ArrayList<SARcombi>();
        currentNrRuns++;
    }
    
    private void fill(int[][] stateActionPairs) {
        for(int i = 0; i<stateActionPairs.length;i++){
            stateActionPairs[i][0]=-1;
            stateActionPairs[i][1]=-1;
        }
    }
    
    public void printQValues(boolean latex, int action){
        if(action==-1){
        representation.printAll(latex);
        }
        else{
            representation.printForOneAction(latex, action);
        }
    }
    
    public void setPrint(boolean p){
        print = p;
    }
    
    public StateRepresentation getQvalues(){
        return representation;
    }

   @Override
    public double[] policy(Position prey, Position predator) {
       String[] actionNamesStateRep= StateRepresentation.Action.actionNames;
        Direction[] actionNamesReal = Direction.enumValues;
        int[] relDis = representation.getRelDistance(predator, prey);
        int currentState = representation.relDistanceToLinearIndex(relDis[0], relDis[1]);
        //vraag action values op
        double[] values = representation.getStateActionPairValues(currentState);
        //bereken kansen
        //calculate total
        double total =0.0;
        for(int i = 0; i< StateRepresentation.nrActions;i++){
            total += Math.exp(values[i]/tau);
        }
        //calculate probability per action
        double[] probabilities = new double [StateRepresentation.nrActions];
        for(int i = 0; i< StateRepresentation.nrActions;i++){
            probabilities[i] = Math.exp(values[i]/tau)/total;
        }
        //convert to real worldword actions
       double[]probabilitiesRealWorld = new double [StateRepresentation.nrActions];
        for(int i = 0; i< StateRepresentation.nrActions;i++){
            int realWorldAction = representation.getMove(predator, prey, i, false);
            probabilitiesRealWorld[realWorldAction] = probabilities[i];
        }
       return probabilitiesRealWorld;
    }

    
}
