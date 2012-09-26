
public class PredatorQLearning implements Agent{

	private double gamma;
	private double alpha;
	private double epsilon;
	
	public PredatorQLearning (double gamma, double alpha, double epsilon){
		this.gamma = gamma;
		this.alpha = alpha;
		this.epsilon = epsilon;
	}
	
	@Override
	public void doMove(Position other) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Position getPos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isConverged() {
		// TODO Auto-generated method stub
		return false;
	}
	

}
