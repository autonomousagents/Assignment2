/**
 * Master AI UvA 2012/2013
 * Autonomous Agents
 * Assignment 1
 *
 * @authors Group 7: Agnes van Belle, Maaike Fleuren, Norbert Heijne, Lydia Mennes
 */

public class Prey implements Agent {

	private final static double WAIT = 0.8;
	private Position myPos;
	private Position startPos;
	
	public Prey(Position startPos){
		myPos = new Position(startPos);
		this.startPos = new Position(startPos);
	}
	
	@Override
	public void doMove(Position other) {
		
		if(myPos.getX()==other.getX()&& myPos.getY()==other.getY())return;
		
		double chance = Math.random();
		if (chance < WAIT) return;
		
		int myPosX = myPos.getX();
		int myPosY = myPos.getY();
		
		Position[] pos = new Position[4]; 
		pos[0] = new Position(myPosX, (myPosY+Environment.HEIGHT-1)% Environment.HEIGHT);
		pos[1] = new Position((myPosX +Environment.WIDTH+1) % Environment.WIDTH, myPosY);
		pos[2] = new Position(myPosX, (myPosY+Environment.HEIGHT+1)% Environment.HEIGHT);
		pos[3] = new Position((myPosX +Environment.WIDTH-1) % Environment.WIDTH, myPosY);
		
		int index;
		do {
			index = (int)(Math.random()* 4.0);
		} while(pos[index].equals(other));
		
		myPos = pos[index];
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
		return false;
	}

	@Override
	public void observeReward() {
		return;	
	}

    

}
