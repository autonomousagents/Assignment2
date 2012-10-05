import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


/**
 * Master AI UvA 2012/2013
 * Autonomous Agents
 * Assignment 1
 *
 * @authors Group 7: Agnes van Belle, Maaike Fleuren, Norbert Heijne, Lydia Mennes
 */
public class Assignment2 {
	
	private StateRepresentation bestStateRep;

    public Assignment2() {

        bestStateRep = new StateRepresentation(-1);
        readBestStateRep("Assignment2/src/qLearning_11_million_episodes.txt");
    }

  

    /**
     * Made to process a file were each staterep-traingle of doubles if precede by "Action = ..."
     *
     * @param filename
     */
    public void readBestStateRep(String filename) {
    	try {
            Scanner scanner = new Scanner(new File(filename));
            
            int stateNumber=0;
            int actionNumber=0;

            while(scanner.hasNext() && actionNumber < StateRepresentation.nrActions){

                String notAction = scanner.next();
                while(!notAction.equals("Action")) {
                     notAction = scanner.next();
                  //  System.out.println(notAction);
                }
                scanner.nextLine(); // vb. " = Hor.Approach"

                while(scanner.hasNextDouble()){
                   double stateActionValue = scanner.nextDouble();
                  // System.out.println(stateActionValue + " at state " + stateNumber + " action " + actionNumber);

                   bestStateRep.setValue(stateNumber, StateRepresentation.Action.actionValues[actionNumber], stateActionValue);
                   stateNumber++;
                }

                stateNumber=0;
                actionNumber++;
            }
    	}
    	catch(FileNotFoundException e) {
    		System.out.println(" File "  + filename + " not found!");
    	}

    }

    /**
     * Calculate the variance of an array of ints
     * @param array
     * @param average
     * @return
     */
    public double calculateVariance(int[] array, double average) {

        double variance = 0;
        for (int trial : array) {
            variance += Math.pow(trial, 2);
        }
        variance /= array.length;
        variance -= Math.pow(average, 2);
        return variance;
    }
    
    private void startOnLineMonteCarlo(int nrTrials,double tau, int nrRuns, double init, double discount) {
        ArrayList<ArrayList<Integer>> stepsPerRun = new ArrayList<>();
        for(int i = 0; i<nrTrials;i++){
            stepsPerRun.add(onPolicyMonteCarlo(tau,  nrRuns,  init,  discount));
            System.out.println("Trial: " + i);
        }
        int[] average = new int [nrRuns];
        System.out.print("\n[");
        for(int i = 0; i<nrRuns;i++){
            int total = 0;
            for(int j =0;j<nrTrials;j++){
                total+=stepsPerRun.get(j).get(i);
            }
            average[i] = total/nrTrials;
            if(i==nrRuns-1){
                System.out.print(average[i]);
            }
            else{
                System.out.print(average[i]+",");
            }
        }
        System.out.println("]");
    }

    public ArrayList<Integer> onPolicyMonteCarlo(double tau, int nrRuns, double init, double discount) {
        //double tau, int nrRuns, double init, Position startPos, Position startPosPrey
        ArrayList<Integer> stepsPerRun = new ArrayList<>();
        PredatorOnPolicyMonteCarlo agent = new PredatorOnPolicyMonteCarlo(tau, nrRuns, init, new Position(0, 0), new Position(5, 5), discount);
        Environment env = new Environment(agent, new Position(5, 5));
        View view = new View(env);
        int runNr = 0;
        do {
            env.doRun();
            runNr++;
            if (runNr % 20 == 0) {
//                System.out.println(runNr);
            }
            agent.learnAfterEpisode();
            env.reset();
            stepsPerRun.add(env.getNrSteps());
            env.resetNrSteps();
        } while (!agent.isConverged());        
//        view.printSimple();
        agent.setPrint(false);
//        while (!env.isEnded()) {
//            env.nextTimeStep();
//            view.printSimple();
//        }        
//        agent.printQValues(false, -1);
//        view.printPolicy(agent, 5, 5);
        return stepsPerRun;
    }


    /**
     * root mean square error
     * averaged over state-action pairs
     * for qlearning comparing
     */
    public double calculateRMSE(PredatorQLearning agent) {

        double RMSE=0;
        double numerator=0;

        for (int stateNr=0; stateNr < StateRepresentation.nrStates; stateNr++) {
            for(int actionNr=0; actionNr < StateRepresentation.nrActions; actionNr++) {

                double difference = bestStateRep.getValue(stateNr, StateRepresentation.Action.actionValues[actionNr]) -
                                    agent.getValueStateAction(stateNr, StateRepresentation.Action.actionValues[actionNr] ) ;

                numerator += Math.pow(difference, 2);
            }
        }
        RMSE = Math.sqrt( numerator / StateRepresentation.nrStateActionPairs);
        return RMSE;
    }


     public double percentageOptimalAction(PredatorQLearning agent) {

        double nrOptimalAction=0;

        for (int stateNr=0; stateNr < StateRepresentation.nrStates; stateNr++) {

            int bestActionNr1=-1;
            int bestActionNr2=-1;

            double bestActionValue1 = Math.min(Environment.minimumReward, agent.getInitialValue());
            double bestActionValue2 = bestActionValue1;

            double values1[] = bestStateRep.getStateActionPairValues(stateNr) ;
            double values2[] = agent.getStateActionPairValues(stateNr);

            // for both statereps,
            // just check one, last "best action"
            for (int actionNr=0; actionNr < StateRepresentation.nrActions; actionNr++) {
                if (values1[actionNr] > bestActionValue1) {
                    bestActionValue1 = values1[actionNr];
                    bestActionNr1 = actionNr;
                }
                if (values2[actionNr] > bestActionValue2) {
                    bestActionValue2 = values2[actionNr];
                    bestActionNr2 = actionNr;
                }
            }
            if (bestActionNr1 == bestActionNr2)
                nrOptimalAction++;
        }

        return (nrOptimalAction /  StateRepresentation.nrStates) * 100;
    }

    /**
     * Q-Learning
     */
    /**
     * 11 758 700  episodes wehere agent = new PredatorQLearning(0.9, 0.5, 0.01, 0.1, PredatorQLearning.ActionSelection.epsilonGreedy, new Position(0, 0));
     */
    public void firstMust() {

                                    //double gamma, double alpha,  double maxChange, double a.s.Parameter, ActionSelection actionSelectionMethod, Position startPos
        PredatorQLearning agent = new PredatorQLearning(0.9, 0.5, 0.001, 0.5, PredatorQLearning.ActionSelection.epsilonGreedy, new Position(0, 0));
        Environment env = new Environment(agent, new Position(5, 5));
        View view = new View(env);

        int nrEpisodes = 200000000;
        int episodes = 0;
        do {
            env.doRun();
            episodes++;
         //    if (episodes % 1000 == 0) {
           //     System.out.println(String.format("%.5f",agent.getOldLargestChange()));
         //   }
          //  System.out.println("RMSE: " + calculateRMSE( agent ));
          //  System.out.println("Percentage optimal (same) action: " + percentageOptimalAction(agent));
        //} while (! agent.isConverged());
        } while (episodes < nrEpisodes);
        System.out.println(" Took "  +  episodes + "  episodes to converge." );
        System.out.println(String.format("%.5f",agent.getOldLargestChange()));
//        show last episode
//        env.reset();
//        view.print();
//        while (!env.isEnded()) {
//            env.nextTimeStep();
//            view.print();
//        }
        
        // different alpha
        // different gamma (d.f.)
        
        //plots:
        // nr. steps before reaching goal
        // average rms error over states 
        agent.printQValues(false, -1);
    }
    
    public void firstShould() {
    	  PredatorQLearning agent = new PredatorQLearning(0.9, 0.5, 0.1, 0.1, PredatorQLearning.ActionSelection.softmax, new Position(0, 0)); //double gamma, double alpha,  double maxChange, double a.s.Parameter, ActionSelection actionSelectionMethod, Position startPos
          Environment env = new Environment(agent, new Position(5, 5));
          View view = new View(env);

        //  int nrEpisodes = 2000;
          int episodes = 0;
          int episodeAllStatesVisited = -1;
          do {
              env.doRun();
              episodes++;
              if(episodeAllStatesVisited==-1 && agent.allStatesVisited())
                  episodeAllStatesVisited = episodes;
          } while (! agent.isConverged());
          System.out.println(" Took "  +  episodes + "  episodes to converge." );
//          show last episode
//          env.reset();
//          view.print();
//          while (!env.isEnded()) {
//              env.nextTimeStep();
//              view.print();
//          }
          //input: tau
          
          agent.printQValues(false, -1);
    }
    
    public void secondMust() {
    	  PredatorQLearning agent = new PredatorQLearning(0.9, 0.5, 0.1, 0.1, PredatorQLearning.ActionSelection.softmax, new Position(0, 0)); //double gamma, double alpha,  double maxChange, double a.s.Parameter, ActionSelection actionSelectionMethod, Position startPos
          Environment env = new Environment(agent, new Position(5, 5));
          
          
    }    
    
    public void startOffLineMonteCarlo(double tau, int nrRuns, double init, double discount, int nrTrials, int nrRunsBehavior){
        PredatorOnPolicyMonteCarlo agentOnLineMC = new PredatorOnPolicyMonteCarlo(tau, nrRunsBehavior, init, new Position(0, 0), new Position(5, 5), discount);
        Environment env = new Environment(agentOnLineMC, new Position(5,5));
        
        do {
            env.doRun();
            agentOnLineMC.learnAfterEpisode();
            env.reset();
        } while (!agentOnLineMC.isConverged()); 
        StateRepresentation QvaluesBehavior = agentOnLineMC.getQvalues();
        ArrayList<ArrayList<Integer>> stepsPerRun = new ArrayList<>();
        for(int i = 0; i<nrTrials;i++){
            System.out.println("Trial: " + i);
            stepsPerRun.add(offLineMonteCarlo(tau, nrRuns, init, discount, QvaluesBehavior));            
        }
        int[] average = new int [nrRuns];
        System.out.print("\n[");
        for(int i = 0; i<nrRuns;i++){
            int total = 0;
            for(int j =0;j<nrTrials;j++){
                total+=stepsPerRun.get(j).get(i);
            }
            average[i] = total/nrTrials;
            if(i==nrRuns-1){
                System.out.print(average[i]);
            }
            else{
                System.out.print(average[i]+",");
            }
        }
        System.out.println("]");
        
    }
    
    public ArrayList<Integer> offLineMonteCarlo(double tau, int nrRuns, double init, double discount, StateRepresentation QvaluesBehavior){
        PredatorOffPolicyMonteCarlo agentOffLineMC = new PredatorOffPolicyMonteCarlo(tau, nrRuns, init, new Position(0, 0), new Position(5, 5), discount);
        agentOffLineMC.setBehaviorPolicy(true, QvaluesBehavior);
        Environment env = new Environment(agentOffLineMC, new Position(5,5));
        View v = new View(env);
        ArrayList<Integer> stepsPerRun = new ArrayList<>();
        int runView = 10;
        int runNr = 0;
        do {
            doRunMain(env,agentOffLineMC,v, false);
            runNr++;
            if (runNr % runView == 0) {
                System.out.println(runNr);
                System.out.println("behavior finished");
            }
            agentOffLineMC.learnAfterEpisode();
            env.reset();       
            env.resetNrSteps();
            
            agentOffLineMC.useBehaviorPolicy(false);
            doRunMain(env,agentOffLineMC,v, false);
            stepsPerRun.add(env.getNrSteps());
            env.reset();
            env.resetNrSteps();
            agentOffLineMC.useBehaviorPolicy(true);
            if (runNr % runView == 0) {
                System.out.println("estimation finished");
            }            
        } while (!agentOffLineMC.isConverged());
        v.printPolicy(agentOffLineMC, 5, 5);
        
//        doRunMain(env,agentOffLineMC,v, true);
//        System.out.println("behavior");
//        agentOffLineMC.getQValuesBehavior().printAll(true);
//        System.out.println("estimation");
//        agentOffLineMC.getQValuesEst().printAll(true);
        return stepsPerRun;
    }
    
    public void doRunMain(Environment env, PredatorOffPolicyMonteCarlo agent, View v, boolean print){
        boolean validRun = false;
        int invalidRun = 0;
        int nrSteps = 0;
        while(!validRun){
            while(!env.isEnded()){
                if(nrSteps<80000){
                    env.nextTimeStep();
                    if(print){
                        v.printSimple();
                    }
                    nrSteps ++;
                    nrSteps++;
                    if(env.isEnded()){
                        validRun = true;
                    }
                }
                else{
                    nrSteps = 0;                    
                    System.out.println("invalid run" +invalidRun);
                    invalidRun++;
                    agent.resetSAR();
                    env.resetNrSteps();
                    env.reset();
                    break;
                }
            }
            env.reset();
    }
    }

    public static void main(String[] args) {
        Assignment2 a = new Assignment2();
        //int nrTrials,double tau, int nrRuns, double init, double discount
//        a.startOnLineMonteCarlo(5, 0.8,500,15.0,0.8);
        a.startOffLineMonteCarlo(0.8, 400, 15.0, 0.9, 4,600);
//        a.firstMust();
        // a.secondMust();
       //  a.firstShould();
        // a.secondShould();
        // a.thirdShould();
    }

    
}
