
public class PredatorQLearning implements Agent{

	private double gamma;
	private double alpha;
	private double epsilon;
	private Position myPos;
	private Position startPos;
	private StateRepresentation stateSpace;
	private static final double initialValue = 15;
	private Position oldPos;

    private StateRepresentation.Action currentAction;
	private double currentReward;
    private int currentState;
    private int oldState;

	public PredatorQLearning (double gamma, double alpha, double epsilon, Position startPos){
		this.gamma = gamma;
		this.alpha = alpha;
		this.epsilon = epsilon;
		this.startPos = new Position(startPos);
		this.myPos = new Position(startPos);
		stateSpace = new StateRepresentation(initialValue);
		
	}
    
   

    /**
     * TODO
     * 
     * @return max_{a'} Q(s', a')
     * gebruik myPos, dat is nu s'
     *
     */
    private double getBestActionValue() {

        return 0.0;
    }


    /**
     * Is being invoked from Environment
     * @see Environment.nextTimeStep();
     *
     * observe newState & reward,
     * update oldstate with newState state action pair values
     * finally, set state to new state (?)
     * 
     * @param reward : reward gotten in currentState
     * @param other : position of prey
     */
	public void observeReward(double reward, Position other){
		

        currentReward=reward;

        // new state
         int [] reldis = stateSpace.getRelDistance(myPos, other);
         int newState = StateRepresentation.relDistanceToLinearIndex(reldis[0], reldis[1]);

         // old state
         int [] reldisOld = stateSpace.getRelDistance(oldPos, other);
         oldState = StateRepresentation.relDistanceToLinearIndex(reldisOld[0], reldisOld[1]);



         double oldQValue = stateSpace.getValue(oldState, currentAction);

         double maxActionValue = getBestActionValue(); // TODO

         double TDvalue = alpha * ( currentReward + (gamma * (maxActionValue - oldQValue)));
         double newQValue = oldQValue + TDvalue;
         stateSpace.setValue(oldState, currentAction, newQValue);

         //currentState = newState;
	}


    public StateRepresentation.Action pickEpsilonGreedyAction(Position other) {
        int[] reldistance = stateSpace.getRelDistance(myPos, other);
		int linearIndex = StateRepresentation.relDistanceToLinearIndex(reldistance[0], reldistance[1]);
		StateRepresentation.Action action;
		//remember oldState
		oldPos = new Position(myPos);


		//epsilon greedy
		if(Math.random() <= epsilon){
		//falls within epsilon
			//return uniformly random action
			action = StateRepresentation.returnAction((int)(Math.random() * Direction.nrMoves));
		}
		else {
		//falls outside epsilon

			//get all greedy actions
			int[] index = new int[stateSpace.nrActions];
			index[0]= 0;
			int length = 1;
			double[] actionvalues = stateSpace.getStateActionPairValues(linearIndex);
			for(int i = 1;i<stateSpace.nrActions;i++){
				if(actionvalues[i] > index[0]){
					index[0] = i;
				}
			}
			for(int i = 1;i<stateSpace.nrActions;i++){
				if(actionvalues[i] == index[0]){
					index[length] = i;
					length++;
				}
			}
			//take random greedy action from the greedy actions.
			action = StateRepresentation.returnAction(index[(int)(Math.random()*length)]);
		}
        return action;
    }

    /**
     * pick action (epsilon greedy)
     * adjust position
     */
	@Override
	public void doMove(Position other) {

        currentAction = pickEpsilonGreedyAction(other);

        int actionNumber = stateSpace.getMove(myPos, other, currentAction.getIntValue());
        myPos.adjustPosition(actionNumber);
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
		// TODO Auto-generated method stub
		return false;
	}

	

}
