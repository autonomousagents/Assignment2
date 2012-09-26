/**
 * Master AI UvA 2012/2013
 * Autonomous Agents
 * Assignment 1
 *
 * @authors Group 7: Agnes van Belle, Maaike Fleuren, Norbert Heijne, Lydia Mennes
 */

import java.util.Arrays;


public class StateRepresentation {
    private double stateRep[][];
    public static final int stateRepWidth = Math.round(Environment.WIDTH/2)+1;
    public static final int stateRepHeight = Math.round(Environment.HEIGHT/2)+1;
    public static final int nrActions = 5;
    public static final int nrStates = 21;
    
    /**
     * Constructor which initializes the state space representation
     */
    public StateRepresentation ( ){
        stateRep = new double[stateRepHeight][stateRepWidth];
        for(double[] row: stateRep)Arrays.fill(row, 0.0);
        fillUnused();
    }
    
    /**
     * Enumerator listing all possible actions in the state space
     */
    public enum Action {
    	HorizontalApproach,
    	HorizontalRetreat,
    	VerticalApproach,
    	VerticalRetreat,
    	Wait;
    }
    
    /**
     * Provides the v-value for a linear index in the state space
     * @param linearIndex of state s
     * @return v-value corresponding to s
     */
    public double getVvalue(int linearIndex){
    	Position pos = linearIndexToPosition(linearIndex);
    	return stateRep[pos.getY()][pos.getX()];
    }

    /**
     * Returns the state s' for state s corresponding to linear index and the provided action 
     * (without considering possible movement of other agent)
     * @param linearIndex = linear index of state s
     * @param action = action taken from state s
     * @return linear index of state s' resulting from that action.
     */
    public int getLinearIndexForAction(int linearIndex, Action action){
    	Position pos = linearIndexToPosition(linearIndex);
    	if(linearIndex == 0)return 0;
    	switch(action){
    	case HorizontalApproach: 
    		if(pos.getY() == 0) return linearIndex +1;
    		else return linearIndex -1;
    	case HorizontalRetreat: 
    		Position tempPos = linearIndexToPosition(linearIndex+1);
    		if(pos.getX()== stateRepWidth-1 && pos.getY() == stateRepHeight-1) return linearIndex;
    		if(tempPos.getY() == pos.getY()) return linearIndex+1;
    		else return linearIndex+pos.getX()+1;
    	case VerticalApproach:
    		if(pos.getX() == pos.getY()) return linearIndex-1;
    		else return linearIndex - pos.getY();
    	case VerticalRetreat: 
    		if(pos.getY() == stateRepHeight-1) return linearIndex;
    		else return linearIndex + pos.getY() +1;
    	case Wait: return linearIndex;
    	}
    	return 0;
    }
    
    /**
     * Returns the reward of state s corresponding to the provided linear index
     * @param linearIndex = linear index of state s
     * @return reward received in state s
     */
    public double getReward(int linearIndex){
    	if(linearIndex == 0)return Environment.maximumReward;
    	else return Environment.minimumReward;
    }
    
    /**
     * Provides the relative distance for two positions
     * @param predator = position of predator agent
     * @param prey = position of prey agent
     * @return relative distance (horizontal distance and vertical distance) between the two positions.
     */
    public int[] getRelDistance(Position predator, Position prey){
    	int [] relativeDistance = new int[2];
    	//horizontal Distance
    	relativeDistance[0] = Math.abs(prey.getX()-predator.getX());
    	if(relativeDistance[0] > (Environment.WIDTH)/2) relativeDistance[0] = Environment.WIDTH - relativeDistance[0];
    	//vertical Distance
    	relativeDistance[1] = Math.abs(prey.getY()-predator.getY());
    	if(relativeDistance[1] > (Environment.HEIGHT)/2) relativeDistance[1] = Environment.HEIGHT - relativeDistance[1];
    	return relativeDistance;
    }

    /**
     * Provides the linear index corresponding to a horizontal and vertical distance between two agents
     * @param x = horizontal distance
     * @param y = vertical distance
     * @return = linear index of s in the statespace
     */
    public static int relDistanceToLinearIndex(int x, int y){
    	if(x > y){ 
    		int i = x;
    		x = y;
    		y = i;
    	}
    	int sumOfY = 0;
    	for(int j = 1;j<=y;j++){
    		sumOfY += j;
    	}
    	int linearIndex = sumOfY + x;
    	return linearIndex;
    }
    
    /**
     * Returns the linear position in the state space as an x and y coordinate within a Position object
     * @param linearIndex = linear index of state s in state space
     * @return position of state s in state space as x and y coordinate in a Position object
     */
    public static Position linearIndexToPosition(int linearIndex){
    	int y = 0;
    	int oldSumY = 0;
    	int sumY = 0;
    	for(int j = 0;sumY<=linearIndex;j++){
    		y = j;
    		oldSumY = sumY;
    		sumY += j+1;
    	}
    	int x = linearIndex - oldSumY;
    	
    	return new Position(x, y);
    }
    
    /**
     * Fill the unused half of the state space with -1.0 
     */
    private void fillUnused(){
    	for(int i = 0;i<stateRepHeight;i++){
    		for(int j = i+1;j<stateRepWidth;j++){
    			stateRep[i][j] =  -1.0;
    		}
    	}
    }

    public double[][] getMatrix(){
        return stateRep;
       }
       
    public void printLatexTable(){
        for(int i = 0;i< stateRepHeight;i++){
        	System.out.println(i + " & ");
        	for(int j = 0; j<stateRepWidth;j++){
        		System.out.format("%7.4f",stateRep[i][j]);
        		if(j!=stateRepWidth-1){
        			System.out.print(" & ");
        		}
        	}
        System.out.println("\\\\");
        }
    } 
    
    public void setVvalue(int linearIndex, double value){
    	Position pos = linearIndexToPosition(linearIndex);
    	stateRep[pos.getY()][pos.getX()] = value;
    }
}
