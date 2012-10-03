
/**
 * Master AI UvA 2012/2013
 * Autonomous Agents
 * Assignment 1
 *
 * @authors Group 7: Agnes van Belle, Maaike Fleuren, Norbert Heijne, Lydia Mennes
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class View {

    private final static String EMPTY = "_";
    private final static String PREY = "b";
    private final static String PREDATOR = "P";
    private Environment env;
    String[][] world;

    public View(Environment env) {
        this.env = env;
        world = new String[Environment.HEIGHT][Environment.WIDTH];
        fill();
    }

    /**
     * Print the gridworld as a gridworld
     */
    public void print() {
        world[env.getPreyPos().getY()][env.getPreyPos().getX()] = PREY;
        world[env.getPredatorPos().getY()][env.getPredatorPos().getX()] = PREDATOR;

        for (String[] row : world) {
            for (String place : row) {
                System.out.print(place + "  ");
            }
            System.out.println();
        }
        System.out.println();

        world[env.getPredatorPos().getY()][env.getPredatorPos().getX()] = EMPTY;
        world[env.getPreyPos().getY()][env.getPreyPos().getX()] = EMPTY;
    }
    
    /*
     * /prints the environment the way it is
     */
    public void printSimple(){
        for(int i =0;i<Environment.HEIGHT;i++){
            for(int j = 0; j<Environment.WIDTH;j++){
                if(env.getPreyPos().equals(new Position(j,i))){
                    System.out.print(PREY);
                }
                else if(env.getPredatorPos().equals(new Position(j,i))){
                    System.out.print(PREDATOR);
                }
                else{
                    System.out.print(EMPTY);
                }
                System.out.print(" ");
            }
            System.out.print("\n");
        }
        System.out.print("\n");
    }

    private void fill() {
        for (String[] row : world) {
            Arrays.fill(row, EMPTY);
        }
    }

    /**
     * Write a matlab script that plots a colormap of 2D VMatrix to a file
     * @param filename : filename of that script
     */
    public static void writeVMatrix(String filename, double[][] VMatrix) {
        try {
            FileWriter fstream = new FileWriter(filename, false);
            BufferedWriter out = new BufferedWriter(fstream);

            out.write("clear;clc;");
            out.newLine();
            for (int i = 0; i < VMatrix.length; i++) {
                out.write(String.format("C(%d,:)=[", i + 1));
                for (int j = 0; j < VMatrix[i].length; j++) {
                    if (j != 0) {
                        out.write(",");
                    }

                    String number;
                    number = String.format("%.3f", VMatrix[i][j]);
                    out.write(number.replaceAll(",", "."));
                }
                out.write("];");
                out.newLine();
                out.flush();
            }
            out.write("imagesc(C, [ min(min(C)), max(max(C)) ] );");
            out.write("colormap(gray);");
            out.write("axis image");
            out.flush();
            fstream.close();
            out.close();
        }
        catch (IOException e) {
            System.out.println("Error in writeVMatrix(): " + e);
        }
    }
    
    /*
     * " \\begin{table}[htbp]\n"+
"\\caption{policy}\n"+
"\\centering\n"+
"\\begin{footnotesize}\n"+
"\\begin{tabular}{c|c|c|c|c|c|c|c|c|c|c|c|}\n"+
"&0&1&2&3&4&5&6&7&8&9&10\\\\ \\hline\\\\"

"\\end{tabular}\n"+
"\\end{footnotesize}\n"+
"\\end{table}\n"
     */
    
    public void printPolicy(Agent agent,int xPrey,int yPrey){
        System.out.println(" \\begin{table}[htbp]\n"+
                            "\\caption{policy}\n"+
                            "\\label{policyLabel}\n"+
                            "\\centering\n"+
                            "\\begin{footnotesize}\n"+
                            "\\begin{tabular}{c|c|c|c|c|c|c|c|c|c|c|c|}\n"+
                            "&0&1&2&3&4&5&6&7&8&9&10\\\\ \\hline\\\\");
        Position prey = new Position(xPrey,yPrey);
        double [][] probabilities = new double [Environment.WIDTH][StateRepresentation.nrActions];
        //For each y-coordinate
        for(int row = 0; row <Environment.HEIGHT;row++){ 
            //for each x-coordinate
            for(int col = 0; col <Environment.WIDTH;col++){
                //If it is not the goal state
                if(xPrey != col || yPrey!=row){
                    //Get the probabilities for real world actions
                    double [] prob = agent.policy(prey, new Position(col,row));  
                    for(int i = 0; i<StateRepresentation.nrActions;i++){
                        probabilities[col][i]=prob[i];
                    }
                } 
            }
            String [] directions = {"U", "R", "D", "L","W"};
            //Print probabilities for each action
            for(int i = 0; i<StateRepresentation.nrActions;i++){
                    if(i==0){
                        System.out.print(row + "&");
                    }
                    else{
                        System.out.print("&");
                    }
                    for(int col = 0; col<Environment.WIDTH;col++){
                        if(xPrey != col || yPrey!=row){
                            System.out.print(directions[i] + " " + String.format("%.3f",probabilities[col][i]));
                            
                        }
                        else if(i == 2){
                            System.out.print("Prey");
                        }
                        if(col == Environment.WIDTH-1){
                            System.out.println("\\\\");
                        }
                        else{
                            System.out.print("&");
                        }
                    }
            }
            System.out.println("\\hline \\\\");
            
        }
        System.out.print("\\end{tabular}\n"+
                        "\\end{footnotesize}\n"+
                        "\\end{table}\n");
    }
}
