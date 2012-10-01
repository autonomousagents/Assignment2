
import java.util.ArrayList;

public class PredatorQLearning implements Agent {

    private double gamma;
    private double alpha;
    private double epsilon;
    private Position myPos;
    private Position startPos;
    private StateRepresentation stateSpace;
    private static final double initialValue = 15;
    private Position oldPos;
    private final double doubleComparisonEpsilon = 0.0001;
    private StateRepresentation.Action currentAction;
    private double currentReward;
    private int oldState;
    
    private int nrRuns, currentNrRuns;

    public PredatorQLearning(double gamma, double alpha, double epsilon, Position startPos) {
        this.gamma = gamma;
        this.alpha = alpha;
        this.epsilon = epsilon;
        this.startPos = new Position(startPos);
        this.myPos = new Position(startPos);
        stateSpace = new StateRepresentation(initialValue);


    }

    /**
     *
     * @return max_{a'} Q(s', a')
     * gebruik myPos, dat is nu s'
     *
     */
    private double getBestActionValue(Position other) {

        int[] reldistance = stateSpace.getRelDistance(myPos, other);
        int linearIndex = StateRepresentation.relDistanceToLinearIndex(reldistance[0], reldistance[1]);

        double[] actionvalues = stateSpace.getStateActionPairValues(linearIndex);
        double bestActionValue = Environment.minimumReward;

        //get best action value
        for (int i = 0; i < StateRepresentation.nrActions; i++) {

            System.out.println("action value of "+StateRepresentation.Action.actionNames[i]+" = " + String.format("%.3f",actionvalues[i]));

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
     * this.myPos is already adjusted
     * @see this.doMove(...)
     * 
     * observe newState & reward,
     * update oldstate with newState state action pair values
     *
     * @param reward : reward gotten in currentState
     * @param other : position of prey
     */
    public void observeReward(double reward, Position other) {

      //  if (myPos.equals(other)) {
       //     return;
       // }

        System.out.println("observe reward");

        currentReward = reward;

        // new state
        // int[] reldis = stateSpace.getRelDistance(myPos, other);
        //int newState = StateRepresentation.relDistanceToLinearIndex(reldis[0], reldis[1]);

        // old state
        int[] reldisOld = stateSpace.getRelDistance(oldPos, other);
        oldState = StateRepresentation.relDistanceToLinearIndex(reldisOld[0], reldisOld[1]);



        double oldQValue = stateSpace.getValue(oldState, currentAction);

        double maxActionValue = getBestActionValue(other);

        double TDvalue = alpha * (currentReward + (gamma * (maxActionValue - oldQValue)));
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
        if (Math.random() <= epsilon) {
            //falls within epsilon
            //return uniformly random action
            action = StateRepresentation.returnAction((int) (Math.random() * Direction.nrMoves));
        }
        else {

            //falls outside epsilon
            ArrayList<StateRepresentation.Action> bestActions = new ArrayList<StateRepresentation.Action>();

            double[] actionvalues = stateSpace.getStateActionPairValues(linearIndex);
            double bestActionValue = Environment.minimumReward;

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

    public void printQValues(boolean latex, int action) {
        if (action == -1) {
            stateSpace.printAll(latex);
        }
        else {
            stateSpace.printForOneAction(latex, action);
        }
    }
}
