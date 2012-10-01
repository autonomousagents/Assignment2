/**
 * Master AI UvA 2012/2013
 * Autonomous Agents
 * Assignment 1
 *
 * @authors Group 7: Agnes van Belle, Maaike Fleuren, Norbert Heijne, Lydia Mennes
 */

public class Assignment2 {


    public Assignment2() {
        
    }

    public void start() {

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
    
    public void onPolicyMonteCarlo(double tau, int nrRuns, double init){        
        //double tau, int nrRuns, double init, Position startPos, Position startPosPrey
        PredatorOnPolicyMonteCarlo agent = new PredatorOnPolicyMonteCarlo(tau, nrRuns, init, new Position(0,0), new Position(5,5));
        Environment env = new Environment(agent, new Position(0,0));
        View view = new View(env);
        int runNr=0;
        do{
            env.doRun();
            runNr++;
            if(runNr%20==0){
                System.out.println(runNr);
            }
            agent.learnAfterEpisode();
        }
        while(!agent.isConverged());
        env.reset();
        view.print();
        while(!env.isEnded()){
            env.nextTimeStep();
            view.print();
        }
        agent.printQValues(false, -1);
        
    }

    public static void main(String[] args) {
        Assignment2 a = new Assignment2();
        a.onPolicyMonteCarlo(0.8, 100, 15);
        //a.firstMust();
        // a.secondMust();
        // a.firstShould();
        // a.secondShould();
        // a.thirdShould();
    }
}
