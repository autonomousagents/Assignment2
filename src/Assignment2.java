import java.io.File;
import java.io.FileNotFoundException;
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

    }



    /**
     * Q-Learning
     */
    /**
     * 11 758 700  episodes wehere agent = new PredatorQLearning(0.9, 0.5, 0.01, 0.1, PredatorQLearning.ActionSelection.epsilonGreedy, new Position(0, 0));
     */
    public void firstMust() {

    	//double gamma, double alpha,  double maxChange, double a.s.Parameter, ActionSelection actionSelectionMethod, Position startPos
        PredatorQLearning agent = new PredatorQLearning(0.9, 0.5, 0.1, 0.01, PredatorQLearning.ActionSelection.epsilonGreedy, new Position(0, 0));
        Environment env = new Environment(agent, new Position(5, 5));
        View view = new View(env);

      //  int nrEpisodes = 2000;
        int episodes = 0;
        do {
            env.doRun();
            episodes++;
            // if (episodes % 100 == 0) {
             //   System.out.println(String.format("%.5f",agent.getOldLargestChange()));
           // }
        } while (! agent.isConverged());
        System.out.println(" Took "  +  episodes + "  episodes to converge." );
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
              if(episodeAllStatesVisited==-1 && agent.allStatesVisited()) episodeAllStatesVisited = episodes;
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

    public static void main(String[] args) {
        Assignment2 a = new Assignment2();
      // a.onPolicyMonteCarlo(0.8, 15, 15, 0.9);
        a.firstMust();
        // a.secondMust();
       //  a.firstShould();
        // a.secondShould();
        // a.thirdShould();
    }
}
