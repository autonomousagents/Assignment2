
import java.util.ArrayList;


public class PredatorQLearning implements Agent {

    private double gamma;
    private double alpha;
   // private double epsilon;
    private Position myPos;
    private Position startPos;
    private StateRepresentation stateSpace;
    private static final double initialValue = 15;
    private Position oldPos;
    private Position oldPreyPos;
    private final double doubleComparisonEpsilon = 0.000001;
    private StateRepresentation.Action oldAction;
    private double currentReward;
    private int oldState;
    private int oldActionNumber;
    
    private double largestChange;
    private double oldLargestChange;
    private double maxChange;

    private int currentState;
    
    private ActionSelection actionSelectionMethod;
    private double actionSelectionParameter;
    
    public enum ActionSelection {

    	epsilonGreedy(0),
    	softmax(1);
        
    	public  final static ActionSelection[]  actionSelectionValues = ActionSelection.values();
    	

    	private int i;
    	
    	ActionSelection(int index){
    		i = index;
    	}
        
    	int getIntValue(){
    		return i;
    	}
    }
    

    public PredatorQLearning(double gamma, double alpha, double maxChange, double actionSelectionParameter,  ActionSelection a, Position startPos) {
        this.gamma = gamma;
        this.alpha = alpha;
        this.actionSelectionParameter = actionSelectionParameter;
        this.startPos = new Position(startPos);
        this.myPos = new Position(startPos);
        this.largestChange = 0;
        this.maxChange = maxChange;
        this.actionSelectionMethod =  a;
        stateSpace = new StateRepresentation(initialValue);

        for (int i=0; i < StateRepresentation.nrActions; i++)
            stateSpace.setValue(0, StateRepresentation.returnAction(i), 0);

    }

    /**
     *
     * @return max_{a'} Q(s', a')
     * gebruik myPos, dat is nu s'
     *
     */
    private double getBestActionValue(Position other) {

        int[] reldistance = stateSpace.getRelDistance(myPos, other);
        currentState = StateRepresentation.relDistanceToLinearIndex(reldistance[0], reldistance[1]);

        double[] actionvalues = stateSpace.getStateActionPairValues(currentState); // return stateRep[pos.getY()][pos.getX()];
        double bestActionValue = Environment.minimumReward;

        //get best action value
        for (int i = 0; i < StateRepresentation.nrActions; i++) {

            //System.out.println("action value of "+StateRepresentation.Action.actionNames[i]+" = " + String.format("%.3f",actionvalues[i]));

            if (actionvalues[i] > bestActionValue) {
                bestActionValue = actionvalues[i];
            }
        }
        return bestActionValue;

    }

    /**
     * Is being invoked from Environment
     * @see Environment.nextTimeStep();
     *
     * this.myPos is already adjusted
     * @see this.doMove(...)
     * 
     * observe newState & reward,
     * update oldstate with newState state action pair values
     *
     * @param reward : reward gotten in currentState
     * @param other : position of prey
     */
    public void observeReward(double reward, Position other) {


        currentReward = reward;


        double oldQValue = stateSpace.getValue(oldState, oldAction);

        double maxActionValue = getBestActionValue(other);

     
        double TDvalue = alpha * (currentReward + (gamma * maxActionValue) - oldQValue);
        double newQValue = oldQValue + TDvalue;      
        
        stateSpace.setValue(oldState, oldAction, newQValue);

      //  System.out.println("old state: " + oldState);
     //  if (newQValue != 15)
//            System.out.println(//"(set pred pos " + stateSpace.linearIndexToPosition(oldState).getX() + ","+ stateSpace.linearIndexToPosition(oldState).getY() + ")" +
//                                "(Statenr: " + oldState + ") " +                              
//                              "set pred pos " + oldPos.getX() + ","+ oldPos.getY() +
//                                " and prey pos " + oldPreyPos.getX() + ","+ oldPreyPos.getY() +
//                                " and action " + oldAction +
//                                  " (oldAction nr.:" + oldActionNumber + ") " +
//                                  " to value " + newQValue +
//                                  "\nNew pos is: Predator: " + myPos.getX() + "," + myPos.getY() + " , Prey: " + other.getX() + "," + other.getY() +
//                                  " (new Statenr is " + currentState + ")");
        
        double change = Math.abs(newQValue - oldQValue);
        if (change > largestChange)
        	largestChange=change;

    }
    
    public StateRepresentation.Action pickAction(Position other) {
    	switch (actionSelectionMethod) {
    		case epsilonGreedy : return pickEpsilonGreedyAction(other);
    		case softmax : return pickSoftmaxAction(other);
    		default: return null;
    	}
    }
    
    public StateRepresentation.Action pickSoftmaxAction(Position other) {
    	
    	int[] reldistance = stateSpace.getRelDistance(myPos, other);
        int state = StateRepresentation.relDistanceToLinearIndex(reldistance[0], reldistance[1]);
        
        //vraag action values op
        double[] values = stateSpace.getStateActionPairValues(state);
        //bereken kansen
        //calculate total
        double total =0.0;
        for(int i = 0; i< StateRepresentation.nrActions;i++){
            total += Math.exp(values[i]/actionSelectionParameter);
        }
        //calculate probability per action
        double[] probabilities = new double [StateRepresentation.nrActions];
        for(int i = 0; i< StateRepresentation.nrActions;i++){
            probabilities[i] = Math.exp(values[i]/actionSelectionParameter)/total;
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
                return StateRepresentation.Action.actionValues[i];
            }
        }
    	
    	return null;
    }

    public StateRepresentation.Action pickEpsilonGreedyAction(Position other) {
        int[] reldistance = stateSpace.getRelDistance(myPos, other);
        int linearIndex = StateRepresentation.relDistanceToLinearIndex(reldistance[0], reldistance[1]);
        StateRepresentation.Action action;

        // remember oldState
        oldPos = new Position(myPos);
        oldPreyPos=other;
        oldState = linearIndex;

        //epsilon greedy
        if (Math.random() <= actionSelectionParameter) {
            //falls within epsilon
            //return uniformly random action           
            action = StateRepresentation.returnAction((int) (Math.random() * Direction.nrMoves));
        }
        else {
            //falls outside epsilon
            ArrayList<StateRepresentation.Action> bestActions = new ArrayList<StateRepresentation.Action>();

            double[] actionvalues = stateSpace.getStateActionPairValues(linearIndex);
            double bestActionValue = Environment.minimumReward;

            //get all greedy actions
            for (int i = 0; i < StateRepresentation.nrActions; i++) {

                if (actionvalues[i] > bestActionValue) {
                    bestActionValue = actionvalues[i];
                    bestActions.clear();
                    bestActions.add(StateRepresentation.Action.actionValues[i]);
                }
                else if (Math.abs(actionvalues[i] - bestActionValue) < doubleComparisonEpsilon) {
                    bestActions.add(StateRepresentation.Action.actionValues[i]);
                }
            }

            //take random greedy action from the greedy actions.
            action = StateRepresentation.returnAction(bestActions.get((int) (Math.random() * bestActions.size())).getIntValue());
        }
        return action;
    }

    /**
     * pick action (epsilon greedy)
     * adjust position
     */
    @Override
    public void doMove(Position other) {

        oldAction = pickAction(other); // HA, HR, VA, etc.

        oldActionNumber = stateSpace.getMove(myPos, other, oldAction.getIntValue(),false);
        myPos.adjustPosition(oldActionNumber);

    }

    @Override
    public Position getPos() {
        return myPos;
    }

    @Override
    public void reset() {
    	oldLargestChange=largestChange;
    	largestChange=0;
        myPos = new Position(startPos);
    }
    
    public void setMaxChange(double m) {
    	maxChange= m;
    }

    @Override
    public boolean isConverged() {
        // TODO Auto-generated method stub
        return oldLargestChange <= maxChange;
    }

    public void printQValues(boolean latex, int action) {
        if (action == -1) {
            stateSpace.printAll(latex);
        }
        else {
            stateSpace.printForOneAction(latex, action);
        }
    }
}
