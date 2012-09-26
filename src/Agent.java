
public interface Agent {

	
	
	public void doMove(Position other);
	
	public Position getPos();
	
	public void reset();
	
	public boolean isConverged();
	
	public void observeReward(double reward);

}
