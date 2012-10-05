import java.util.ArrayList;


public class PredatorQLearning implements Agent {

    private double gamma;
    private double alpha;
    private Position myPos;
    private Position startPos;
    private StateRepresentation stateSpace;
    private double initialValue = 15;
    private Position oldPos;
    private Position oldPreyPos;
    private final double doubleComparisonEpsilon = 0.000001;
    private StateRepresentation.Action oldAction;
    private double currentReward;
    private int oldState;
    private int oldActionNumber;

    private int nrStepsUsed;
    private int nrStepsUsedOld;

    private double largestChange;
    private double oldLargestChange;
    private double maxChange;

    private int currentState;

    private ActionSelection actionSelectionMethod;
    private double actionSelectionParameter;

    private int nrStateActionPairsVisited;
    private boolean allStateActionPairsVisited;
    private ArrayList<StateActionPair> stateActionPairsVisited;


    /**
     * Private class for a state and action pair,
     * can be used for statistics
     */
    private class StateActionPair {
    	public int stateNr;
    	public int actionNr;

    	public StateActionPair (int s, int a) {
    		stateNr=s;
    		actionNr=a;
    	}

		@Override
		public int hashCode() {
			return actionNr + (stateNr * (StateRepresentation.nrActions));
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StateActionPair other = (StateActionPair) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (actionNr != other.actionNr)
				return false;
			if (stateNr != other.stateNr)
				return false;
			return true;
		}
		private PredatorQLearning getOuterType() {
			return PredatorQLearning.this;
		}
    }


    /**
     * Enum to denote which method of action selection is used
     * Epsilon-greedy, or Softmax
     */
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


    /**
     * Constructor
     *
     * @param gamma                     : discount factor
     * @param alpha                     : learning rate
     * @param maxChange                 : this is for when you want to invok the agent until its maximal change in all  Q(s,a) values is maxChange
     * @param actionSelectionParameter  : epsilon or tau, for any of the two action selection method
     * @param a                         : action selection method used, Epsilon-greedy or Softmax
     * @param startPos
     */
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
        nrStateActionPairsVisited=0;
        allStateActionPairsVisited=false;
        nrStepsUsed=0;
        nrStepsUsedOld=0;

        stateActionPairsVisited = new ArrayList<StateActionPair>();

        for (int i=0; i < StateRepresentation.nrActions; i++)
            stateSpace.setValue(0, StateRepresentation.returnAction(i), 0);
    }


    /**
     * Calculate value you can get by taking the best action from current state    *
     *
     * @return max_{a'} Q(s', a'),  myPos should be s' when  invoked
     *
     */
    private double getBestActionValue(Position other) {

        int[] reldistance = stateSpace.getRelDistance(myPos, other);
        currentState = StateRepresentation.relDistanceToLinearIndex(reldistance[0], reldistance[1]);

        double[] actionvalues = stateSpace.getStateActionPairValues(currentState); // return stateRep[pos.getY()][pos.getX()];
        double bestActionValue = Environment.minimumReward;

        //get best action value
        for (int i = 0; i < StateRepresentation.nrActions; i++) {

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
     * myPos should already be adjusted
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

        /** Update Q(s,a) value **/
        stateSpace.setValue(oldState, oldAction, newQValue);

       // System.out.println("old state: " + oldState);
//       if (newQValue != 15)
//            System.out.println(//"(set pred pos " + stateSpace.linearIndexToPosition(oldState).getX() + ","+ stateSpace.linearIndexToPosition(oldState).getY() + ")" +
//                                "(Statenr: " + oldState + ") " +
//                              "set pred pos " + oldPos.getX() + ","+ oldPos.getY() +
//                                " and prey pos " + oldPreyPos.getX() + ","+ oldPreyPos.getY() +
//                                " and action " + oldAction +
//                                  " (oldAction nr.:" + oldActionNumber + ") " +
//                                  " to value " + newQValue +
//                                  "\nNew pos is: Predator: " + myPos.getX() + "," + myPos.getY() + " , Prey: " + other.getX() + "," + other.getY() +
//                                  " (new Statenr is " + currentState + ")");
//

        /** Keep track of statistics **/
        double change = Math.abs(newQValue - oldQValue);
        if (change > largestChange)
        	largestChange=change;

        StateActionPair saP = new StateActionPair(oldState, oldAction.getIntValue());
        if (!allStateActionPairsVisited && !stateActionPairsVisited.contains(saP)){
        	stateActionPairsVisited.add(saP);
        	nrStateActionPairsVisited++;
        }
        if(!allStateActionPairsVisited){
        	allStateActionPairsVisited = (nrStateActionPairsVisited == (StateRepresentation.nrStateActionPairs - StateRepresentation.nrAbsorbingStateActionPairs));
        }
    }



    public StateRepresentation.Action pickAction(Position other) {
    	switch (actionSelectionMethod) {
    		case epsilonGreedy :  return pickEpsilonGreedyAction(other);
    		case softmax :   return pickSoftmaxAction(other);
    		default: return null;
    	}
    }


    /**
     * Pick an action with Softmax action selection considering the prey's location
     *
     * @remark remembers old state and position
     *
     * @param other : position of the prey
     * @return      : a StateRepresentation.Action
     */
    public StateRepresentation.Action pickSoftmaxAction(Position other) {

    	int[] reldistance = stateSpace.getRelDistance(myPos, other);
        int state = StateRepresentation.relDistanceToLinearIndex(reldistance[0], reldistance[1]);

        // remember oldState
        oldPos = new Position(myPos);
        oldPreyPos=other;
        oldState = state;

        //ask action values
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

        double p = Math.random();

        //decide on action
        for(int i = 0;i<StateRepresentation.nrActions;i++){
            if(p<=probabilities[i]){
                return StateRepresentation.Action.actionValues[i];
            }
        }
    	return null;
    }


    /**
     * Pick an action with Epsilon-greedy action selection considering the prey's location
     *
     * @remark remembers old state and position
     *
     * @param other : position of the prey
     * @return      : a StateRepresentation.Action
     */
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
            //falls within epsilon - return uniformly random action
            action = StateRepresentation.returnAction((int) (Math.random() * Direction.nrMoves));
        }
        else {
            //falls outside epsilon
            ArrayList<StateRepresentation.Action> bestActions = new ArrayList<StateRepresentation.Action>();

            double[] actionvalues = stateSpace.getStateActionPairValues(linearIndex);
            double bestActionValue = Math.min(Environment.minimumReward, initialValue);

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
     * Pick an action (StateRepresentation.Action), convert this to a real-world action (up/right/down/left/wait)
     * and adjust (real-world) position (myPos).
     *
     * @see StateRepresentation.getMove(..)
     *
     * @param other : position of the prey
     */
    @Override
    public void doMove(Position other) {

        oldAction = pickAction(other); // HA, HR, VA, etc.

        oldActionNumber = stateSpace.getMove(myPos, other, oldAction.getIntValue(),false);

        myPos.adjustPosition(oldActionNumber);
        nrStepsUsed++;
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
        nrStepsUsedOld= nrStepsUsed;
        nrStepsUsed=0;

    }

    public void setMaxChange(double m) {
    	maxChange= m;
    }

    public double getOldLargestChange() {
        return oldLargestChange;
    }

    @Override
    public boolean isConverged() {
        return oldLargestChange <= maxChange;
    }

    public double getPercentageStateActionPairsVisited() {
    	return 100 *
    		(nrStateActionPairsVisited / (StateRepresentation.nrStateActionPairs - StateRepresentation.nrAbsorbingStateActionPairs));
    }

    public void setInitialValue(double iV) {
    	initialValue=iV;
    	stateSpace = new StateRepresentation(initialValue);
    }

    public double getInitialValue() {
    	return initialValue;
    }

    public void setActionSelectionParameter(double as) {
    	actionSelectionParameter=as;
    }
    public double getActionSelectionParameter() {
    	return actionSelectionParameter;
    }

    public void setActionSelectionMethod(ActionSelection a) {
    	actionSelectionMethod = a;
    }
    public ActionSelection getActionSelectionMethod() {
    	return actionSelectionMethod;
    }

   public double getValueStateAction(int stateNr, StateRepresentation.Action action ) {
    	return stateSpace.getValue(stateNr, action);
   }

    public double[] getStateActionPairValues(int linearIndex){

        return stateSpace.getStateActionPairValues(linearIndex);
    }

    public int getNrStepsUsed() {
    	return nrStepsUsedOld;
    }

    public boolean allStatesVisited(){
    	return allStateActionPairsVisited;
    }

    public double[] policy(Position prey, Position predator) {
        throw new UnsupportedOperationException("Not supported yet.");
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
