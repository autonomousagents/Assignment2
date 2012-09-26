
public class PredatorQLearning implements Agent{

	private double gamma;
	private double alpha;
	private double epsilon;
	private Position myPos;
	private Position startPos;
	private StateRepresentation stateSpace;
	private static final double initialValue = 15;
	
	public PredatorQLearning (double gamma, double alpha, double epsilon, Position startPos){
		this.gamma = gamma;
		this.alpha = alpha;
		this.epsilon = epsilon;
		this.startPos = new Position(startPos);
		this.myPos = new Position(startPos);
		stateSpace = new StateRepresentation(initialValue);
	}
	
	private void qLearning(){
		//epsilon greedy
		if(Math.random() <= epsilon){
		//falls within epsilon
			//return uniformly random action
			StateRepresentation.Action action = StateRepresentation.returnAction((int)Math.random() * 5); 
		}
		else {
		  
		//falls outside epsilon
		  //remember oldState
		  //take greedy action
		}
	}
	
	public void observeReward(){
		//observe newState & reward
		  //update oldstate with newState state action pair values
		  //currentstate = newstate
		
	}
	
	@Override
	public void doMove(Position other) {
		
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
