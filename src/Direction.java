/**
 * Master AI UvA 2012/2013
 * Autonomous Agents
 * Assignment 1
 *
 * @authors Group 7: Agnes van Belle, Maaike Fleuren, Norbert Heijne, Lydia Mennes
 */


public enum Direction {
    UP (0),
    RIGHT (1),
    DOWN (2),
    LEFT (3),
    STILL (4),
    
    //Wind directions
    NNW(5),
    NNO(6),
    ONO(7),
    OZO(8),
    ZZO(9),
    ZZW(10),
    WZW(11),
    WNW(12);
    

    public final static int nrMoves = 5;
    public final int intValue;
    public  final static Direction[] enumValues = Direction.values();

    Direction(int intValue) {
        this.intValue = intValue;
    }

}