
public class PredatorQLearning implements Agent{

	private double gamma;
	private double alpha;
	private double epsilon;
	private Position myPos;
	private Position startPos;
	private StateRepresentation stateSpace;
	private static final double initialValue = 15;
	private Position oldPos;
	private int linearIndex;
	
	public PredatorQLearning (double gamma, double alpha, double epsilon, Position startPos){
		this.gamma = gamma;
		this.alpha = alpha;
		this.epsilon = epsilon;
		this.startPos = new Position(startPos);
		this.myPos = new Position(startPos);
		stateSpace = new StateRepresentation(initialValue);
		
	}
	
	public void observeReward(double reward, Position other){
		
		
		//observe newState & reward
		  //update oldstate with newState state action pair values
		  //currentstate = newstate
		
	}
	
	@Override
	public void doMove(Position other) {
		int[] reldistance = stateSpace.getRelDistance(myPos, other);
		linearIndex = StateRepresentation.relDistanceToLinearIndex(reldistance[0], reldistance[1]);
		StateRepresentation.Action action;
		//remember oldState
		oldPos = new Position(myPos);
		
		//epsilon greedy
		if(Math.random() <= epsilon){
		//falls within epsilon
			//return uniformly random action
			action = StateRepresentation.returnAction((int)(Math.random() * 5)); 
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
		//adjust position with converted action
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
