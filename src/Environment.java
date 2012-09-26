/**
 * Master AI UvA 2012/2013
 * Autonomous Agents
 * Assignment 1
 *
 * @authors Group 7: Agnes van Belle, Maaike Fleuren, Norbert Heijne, Lydia Mennes
 */

public class Environment {

    public final static int HEIGHT = 11;
    public final static int WIDTH = 11;
    private boolean isEnded;
    private Agent predator;
    private Agent prey;
    private int predatorType = -1;
    public static final double maximumReward = 10;
    public static final double minimumReward = 0;
    public static final double normalReward = 0;

    public Environment(Agent predator) {
        this.isEnded = false;
        this.predator = predator;
    }

    public void nextTimeStep() {
        predator.doMove(getPreyPos());
        if (!checkForEnd()) {
            prey.doMove(getPredatorPos());
        }
        predator.observeReward();
    }

    public boolean isEnded() {
        return isEnded;
    }

    public Position getPreyPos() {
        return prey.getPos();
    }

    public Position getPredatorPos() {
        return predator.getPos();
    }

    private boolean checkForEnd() {
        isEnded = getPredatorPos().equals(getPreyPos());
        return isEnded;
    }

    public void reset() {

    	
        prey = new Prey(new Position(5, 5));
        isEnded = false;
        //predator.reset();
    }

    public static double reward(Position prey, Position predator) {
        if (prey.getX() == predator.getX() && prey.getY() == predator.getY()) {
            return maximumReward;
        }
        else {
            return normalReward;
        }
    }

    public void setPredatorType(int nr) {

        predatorType = nr;
    }
}
