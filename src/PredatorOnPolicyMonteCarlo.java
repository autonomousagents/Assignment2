
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

    double tau;
    int nrRuns;
    StateRepresentation representation;
    ArrayList <SARcombi> SARcombis;
    Position myPos;
    Position startPos;
    Position preyPos;
    int state;
    StateRepresentation.Action currentAction;
    
    
    //Policy is impliciet vastgelegd in statespaceRepresentation
    public PredatorOnPolicyMonteCarlo(double tau, int nrRuns, double init, Position startPos, Position startPosPrey){
        this.nrRuns = nrRuns;
        this.tau = tau;
        representation = new StateRepresentation(init);
        SARcombis = new ArrayList<>();
        myPos = new Position(startPos);
        this.startPos = new Position(startPos);
        int [] reldis = representation.getRelDistance(myPos, preyPos);
        state = representation.relDistanceToLinearIndex(reldis[0], reldis[1]);
    }
    
    @Override
    public void doMove(Position other) {
        //pick action based on greedy action krijgt grootste kans en rest verdelen 
        //with respect to Q (softmax of epsilon greedy)
        
        //vraag action values op
        double[] values = representation.getStateActionPairValues(state);
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
        //Make probabilities cumulative
        for(int i = 1; i< StateRepresentation.nrActions-1;i++){
            probabilities[i] = probabilities[i]+probabilities[i-1];
        }
        probabilities[StateRepresentation.nrActions-1] = 1.0;
        //trek waarde
        double p = Math.random();
        //bepaal actie, sla op en voer uit
        for(int i = 0;i<StateRepresentation.nrActions;i++){
            if(p<=probabilities[i]){
                int a = representation.getMove(myPos, preyPos, i);
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void observeReward(double reward, Position prey) {
        int [] reldis = representation.getRelDistance(myPos, prey);
        state = representation.relDistanceToLinearIndex(reldis[0], reldis[1]);
        SARcombis.add(new SARcombi(state, currentAction, reward));
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
        //leren
        //SAR rij weer leeg maken voor volgende episode.
    }
}
