/**
 * Master AI UvA 2012/2013
 * Autonomous Agents
 * Assignment 1
 *
 * @authors Group 7: Agnes van Belle, Maaike Fleuren, Norbert Heijne, Lydia Mennes
 */

/**
 * Class for Cartesian coordinates position
 */
public class Position {
	private int x;
	private int y;

	public Position(){
		this.x = 0;
		this.y = 0;
	}

	public Position(Position other){
		this.x = other.x;
		this.y = other.y;
	}

	public Position(int x, int y){
		this.x = x;
		this.y = y;
	}

	public int getX(){
		return this.x;
	}

	public int getY(){
		return this.y;
	}

	public void setX(int x){
		this.x = x;
	}

	public void setY(int y){
		this.y = y;
	}

	public boolean equals(Position other){
		return (this.x == other.getX() && this.y == other.getY()) ;
	}

    /**
     * @param number of a position (index of row/column in VMatrix) in the non-efficient state representation
     * @return a Position object with correct x and y coordinates (fields), denoting the corresponding Cartesian position
     */
    public static Position getPosition(int posNr) {

        int x = posNr % Environment.WIDTH;
        int y = (int)(  (posNr - x) / Environment.WIDTH);
        return new Position(x, y);
    }

    /**
     * @param Position object with correct x and y coordinates (fields), denoting a Cartesian position
     * @return the corresponding number of that position (index of row/column in VMatrix) in the non-efficient state representation
     */
    public int getPosNr() {

        return this.getY() * Environment.WIDTH + this.getX();
    }

    /**
     * Adjusts the real-world position baed on a real-world move (up/right/down/left/wait)
     *
     * @param move  :    real-world move
     */
    public void adjustPosition(int move) {
        switch(move){
        //Up
        case 0: y = (y +Environment.HEIGHT-1)% Environment.HEIGHT;break;
        //Right
        case 1: x = (x +Environment.WIDTH+1) % Environment.WIDTH;break;
        //Down
        case 2: y = (y +Environment.HEIGHT+1)% Environment.HEIGHT;break;
        //Left
        case 3: x = (x +Environment.WIDTH-1) % Environment.WIDTH;break;
        //Wait
        case 4: return;
        }
    }

    public void printPosition (){
        System.out.println("x = "+ x + ", y = "+y);
    }

}
