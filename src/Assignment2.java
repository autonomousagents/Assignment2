/**
 * Master AI UvA 2012/2013
 * Autonomous Agents
 * Assignment 1
 *
 * @authors Group 7: Agnes van Belle, Maaike Fleuren, Norbert Heijne, Lydia Mennes
 */

public class Assignment2 {

    private Environment env;
    private View view;
    private int timesteps;

    public Assignment2() {
        view = new View(env);
        timesteps = 0;
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

    public static void main(String[] args) {
        Assignment2 a = new Assignment2();
        //a.firstMust();
        // a.secondMust();
        // a.firstShould();
        // a.secondShould();
        // a.thirdShould();
    }
}
