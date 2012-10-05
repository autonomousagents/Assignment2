
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
        readBestStateRep("src/qLearning_200_million_episodes_eclipse.txt");
    }

    /**
     * Made to process a file were each staterep-traingle of doubles if precede by "Action = ..."
     *
     * @param filename
     */
    public void readBestStateRep(String filename) {
        try {
            Scanner scanner = new Scanner(new File(filename));

            int stateNumber = 0;
            int actionNumber = 0;

            while (scanner.hasNext() && actionNumber < StateRepresentation.nrActions) {

                String notAction = scanner.next();
                while (!notAction.equals("Action")) {
                    notAction = scanner.next();
                    //System.out.println(notAction);
                }
                scanner.nextLine(); // vb. " = Hor.Approach"

                while (scanner.hasNextDouble()) {
                    double stateActionValue = scanner.nextDouble();
                    //System.out.println(stateActionValue + " at state " + stateNumber + " action " + actionNumber);

                    bestStateRep.setValue(stateNumber, StateRepresentation.Action.actionValues[actionNumber], stateActionValue);
                    stateNumber++;
                }

                stateNumber = 0;
                actionNumber++;
            }
        }
        catch (FileNotFoundException e) {
            System.out.println(" File " + filename + " not found!");
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

    public void onPolicyMonteCarlo(double tau, int nrRuns, double init, double discount) {
        //double tau, int nrRuns, double init, Position startPos, Position startPosPrey
        PredatorOnPolicyMonteCarlo agent = new PredatorOnPolicyMonteCarlo(tau, nrRuns, init, new Position(0, 0), new Position(5, 5), discount);
        Environment env = new Environment(agent, new Position(5, 5));
        View view = new View(env);
        int runNr = 0;
        do {
            env.doRun();
            runNr++;
            if (runNr % 20 == 0) {
                System.out.println(runNr);
            }
            agent.learnAfterEpisode();
            env.reset();
        } while (!agent.isConverged());
//        view.printSimple();
        agent.setPrint(true);
        while (!env.isEnded()) {
            env.nextTimeStep();
//            view.printSimple();
        }
        agent.printQValues(false, -1);
        view.printPolicy(agent, 5, 5);
    }

    /**
     * root mean square error
     * averaged over state-action pairs
     * for qlearning comparing
     */
    public double calculateNRMSE(PredatorQLearning agent) {

        double highestActionValue = Math.min(Environment.minimumReward, agent.getInitialValue());
        double lowestActionValue = Math.max(Environment.maximumReward, agent.getInitialValue());

        double RMSE = 0;
        double numerator = 0;

        for (int stateNr = 0; stateNr < StateRepresentation.nrStates; stateNr++) {
            for (int actionNr = 0; actionNr < StateRepresentation.nrActions; actionNr++) {

                double v1 = bestStateRep.getValue(stateNr, StateRepresentation.Action.actionValues[actionNr]);
                double v2 = agent.getValueStateAction(stateNr, StateRepresentation.Action.actionValues[actionNr]);
                double difference = v1 - v2;

                numerator += Math.pow(difference, 2);

                if (v1 > highestActionValue) {
                    highestActionValue = v1;
                }
                if (v2 > highestActionValue) {
                    highestActionValue = v2;
                }
                if (v1 < lowestActionValue) {
                    lowestActionValue = v1;
                }
                if (v2 < lowestActionValue) {
                    lowestActionValue = v2;
                }


            }
        }
        RMSE = Math.sqrt(numerator / StateRepresentation.nrStateActionPairs);
        double NRMSE = RMSE / (highestActionValue - lowestActionValue);
        return NRMSE;
    }

    public double percentageOptimalAction(PredatorQLearning agent) {

        double nrOptimalAction = 0;

        for (int stateNr = 0; stateNr < StateRepresentation.nrStates; stateNr++) {

            int bestActionNr1 = -1;
            int bestActionNr2 = -1;

            double bestActionValue1 = Math.min(Environment.minimumReward, agent.getInitialValue());
            double bestActionValue2 = bestActionValue1;

            double values1[] = bestStateRep.getStateActionPairValues(stateNr);
            double values2[] = agent.getStateActionPairValues(stateNr);

            // for both statereps,
            // just check one, last "best action"
            for (int actionNr = 0; actionNr < StateRepresentation.nrActions; actionNr++) {
                if (values1[actionNr] > bestActionValue1) {
                    bestActionValue1 = values1[actionNr];
                    bestActionNr1 = actionNr;
                }
                if (values2[actionNr] > bestActionValue2) {
                    bestActionValue2 = values2[actionNr];
                    bestActionNr2 = actionNr;
                }
            }
            if (bestActionNr1 == bestActionNr2) {
                nrOptimalAction++;
            }
        }

        return (nrOptimalAction / StateRepresentation.nrStates) * 100;
    }

    public void QLearningDFStats(int nrTestRuns, int nrEpisodes, Environment env, PredatorQLearning agent) {

        double DFvalues[] = new double[]{0.1, 0.5, 0.7, 0.9};

        double NRMSEvalues[][] = new double[DFvalues.length][nrEpisodes];
        double optimalActionValues[][] = new double[DFvalues.length][nrEpisodes];

       
        for (int j = 0; j < DFvalues.length; j++) {
            for (int i = 0; i < nrTestRuns; i++) {

                agent = new PredatorQLearning(DFvalues[j], 0.5, 0.1, 0.1, PredatorQLearning.ActionSelection.epsilonGreedy, new Position(0, 0));
                env = new Environment(agent, new Position(5, 5));
                int episode = 0;
                do {
                    env.doRun();

                    NRMSEvalues[j][episode] += calculateNRMSE(agent) / nrTestRuns;
                    optimalActionValues[j][episode] += percentageOptimalAction(agent) / nrTestRuns;
                    episode++;

                } while (episode < nrEpisodes);
            }
        }
        View.episodeMatrixToMatlabScript("qLearning_DF_NRMSE.m", NRMSEvalues, DFvalues, "df", "NRMSE", new int[]{0, 1});
        View.episodeMatrixToMatlabScript("qLearning_DF_POA.m", optimalActionValues, DFvalues, "df", "% Optimal Action", new int[]{0, 100});

        //agent.printQValues(false, -1);
    }

     public void QLearningAlphaStats(int nrTestRuns, int nrEpisodes, Environment env, PredatorQLearning agent) {

        double alphaValues[] = new double[]{0.1, 0.2, 0.3, 0.5, 0.7};

        double NRMSEvalues[][] = new double[alphaValues.length][nrEpisodes];
        double optimalActionValues[][] = new double[alphaValues.length][nrEpisodes];

        for (int j = 0; j < alphaValues.length; j++) {
            for (int i = 0; i < nrTestRuns; i++) {

                agent = new PredatorQLearning(0.9, alphaValues[j], 0.1, 0.1, PredatorQLearning.ActionSelection.epsilonGreedy, new Position(0, 0));
                env = new Environment(agent, new Position(5, 5));
                int episode = 0;
                do {
                    env.doRun();

                    NRMSEvalues[j][episode] += calculateNRMSE(agent) / nrTestRuns;
                    optimalActionValues[j][episode] += percentageOptimalAction(agent) / nrTestRuns;
                    episode++;

                } while (episode < nrEpisodes);
            }
        }
        View.episodeMatrixToMatlabScript("qLearning_Alpha_NRMSE.m", NRMSEvalues, alphaValues, "alpha", "NRMSE", new int[]{0, 1});
        View.episodeMatrixToMatlabScript("qLearning_Alpha_POA.m", optimalActionValues, alphaValues, "alpha", "% Optimal Action", new int[]{0, 100});

        //agent.printQValues(false, -1);
        
       
    }


    /**
     * Q-Learning
     * Show plots on the performance of the agent over time for different alpha
     * (at least 0.1, ..., 0.5), for different discount factors (at least 0.1, 0.5, 0.7 and 0.9).
     */
    public void firstMust() {

        //double gamma, double alpha,  double maxChange, double a.s.Parameter, ActionSelection actionSelectionMethod, Position startPos
        PredatorQLearning agent = new PredatorQLearning(0.9, 0.5, 0.1, 0.1, PredatorQLearning.ActionSelection.epsilonGreedy, new Position(0, 0));
        Environment env = new Environment(agent, new Position(5, 5));

        int nrTestRuns = 100;
        int nrEpisodes = 1000;

        QLearningDFStats(nrTestRuns, nrEpisodes, env, agent);
        QLearningAlphaStats(nrTestRuns, nrEpisodes, env, agent);

        
    }

    

    public void secondMust() {
    	
    	 PredatorQLearning agent = new PredatorQLearning(0.9, 0.5, 0.1, 0.1, PredatorQLearning.ActionSelection.epsilonGreedy, new Position(0, 0));
         Environment env = new Environment(agent, new Position(5, 5));

         int nrTestRuns = 100;
         int nrEpisodes = 1000;
    	

        //double epsilonValues[] = new double[]{0.1, 0.9};
       // double initValues[] = new double[]{0, 15};
        
        double initEpsilonValues[][]= new double[][]{{0,0.1},{0,0.9},{15,0.1},{15,0.9}};
        
        double optimalActionValues[][] = new double[initEpsilonValues.length][nrEpisodes];
        double percentageStateActionPairsVisited[][] = new double[initEpsilonValues.length][nrEpisodes];
        double nrStepsUsed[][] = new double[initEpsilonValues.length][nrEpisodes];
                                       
        	
	        for (int j = 0; j < initEpsilonValues.length; j++) {
	            for (int i = 0; i < nrTestRuns; i++) {
	
	                agent = new PredatorQLearning(0.9, 0.5, 0.1, initEpsilonValues[j][1], PredatorQLearning.ActionSelection.epsilonGreedy, new Position(0, 0));
	                env = new Environment(agent, new Position(5, 5));
	                agent.setInitialValue(initEpsilonValues[j][0]);
	                
	                int episode = 0;
	                do {
	                    env.doRun();
	
	                    percentageStateActionPairsVisited[j][episode] += agent.getPercentageStateActionPairsVisited() / nrTestRuns;
	                    optimalActionValues[j][episode] += percentageOptimalAction(agent) / nrTestRuns;
	                    nrStepsUsed[j][episode] += ((double)agent.getNrStepsUsed()) / nrTestRuns;
	                    
	                   // System.out.println(agent.getNrStepsUsed());
	                    episode++;
	
	                } while (episode < nrEpisodes);
	            }
	        }
	        View.episodeMatrixToMatlabScript2D("qLearning_optimalValues.m", optimalActionValues, initEpsilonValues, "init.val.", "epsilon", "% Optimal Action", new int[]{0, 100});
	        View.episodeMatrixToMatlabScript2D("qLearning_visitedPairs.m", percentageStateActionPairsVisited, initEpsilonValues, "init.val." , "epsilon", "% State-Action pairs visited", new int[]{0, 100});
	        View.episodeMatrixToMatlabScript2D("qLearning_nrSteps.m", nrStepsUsed, initEpsilonValues, "init.val.", "epsilon", "Number of steps", new int[]{0, 100});

        }

    
    public void firstShouldGraphs(boolean epsilonGreedy) {
    	PredatorQLearning agent = new PredatorQLearning(0.9, 0.5, 0.1, 0.1, PredatorQLearning.ActionSelection.epsilonGreedy, new Position(0, 0));
        Environment env = new Environment(agent, new Position(5, 5));
        int nrTestRuns = 100;
        int nrEpisodes = 1000;
   	

       double values[] = new double[]{ 0.01, 0.1, 0.5, 1 ,10};
       
       
       double optimalActionValues[][] = new double[values.length][nrEpisodes];
       double percentageStateActionPairsVisited[][] = new double[values.length][nrEpisodes];
       double nrStepsUsed[][] = new double[values.length][nrEpisodes];
                                      
	        for (int j = 0; j < values.length; j++) {
	            for (int i = 0; i < nrTestRuns; i++) {
	
	                agent = epsilonGreedy ? new PredatorQLearning(0.9, 0.5, 0.1, values[j], PredatorQLearning.ActionSelection.epsilonGreedy, new Position(0, 0)) :
	                						new PredatorQLearning(0.9, 0.5, 0.1, values[j], PredatorQLearning.ActionSelection.softmax, new Position(0, 0));
	                
	                env = new Environment(agent, new Position(5, 5));
	                
	                int episode = 0;
	                do {
	                    env.doRun();
	
	                    percentageStateActionPairsVisited[j][episode] += agent.getPercentageStateActionPairsVisited() / nrTestRuns;
	                    optimalActionValues[j][episode] += percentageOptimalAction(agent) / nrTestRuns;
	                    nrStepsUsed[j][episode] += ((double)agent.getNrStepsUsed()) / nrTestRuns;
	                    
	                   // System.out.println(agent.getNrStepsUsed());
	                    episode++;
	
	                } while (episode < nrEpisodes);
	            }
	        }
	        
	        String valueName = epsilonGreedy ? "epsilon" : "tau";
	        View.episodeMatrixToMatlabScript("firstShould_optimalValues_"+valueName+".m", optimalActionValues, values, valueName, "% Optimal Action", new int[]{0, 100});
	        View.episodeMatrixToMatlabScript("firstShould_visitedPairs_"+valueName+".m", percentageStateActionPairsVisited, values, valueName, "% State-Action pairs visited", new int[]{0, 100});
	        View.episodeMatrixToMatlabScript("firstShould_nrSteps_"+valueName+".m", nrStepsUsed, values, valueName, "Number of steps", new int[]{0, 100});

	        View view = new View(env);

//	        while (!env.isEnded()) {
//	            env.nextTimeStep();
//	            view.printSimple();
//	        }
//	        
//	        agent.printQValues(false, -1);
       }
    
    public void firstShould() {
    	boolean epsilonGreedy =  true;
    	boolean softmax = !epsilonGreedy;
    	
    	firstShouldGraphs(epsilonGreedy);
    	firstShouldGraphs(softmax);
    }


    

    public static void main(String[] args) {
        Assignment2 a = new Assignment2();
        // a.onPolicyMonteCarlo(0.8, 15, 15, 0.9);
     //   a.firstMust();
      //   a.secondMust();
          a.firstShould();
        // a.secondShould();
        // a.thirdShould();
    }
}
