package com.example.cricketscorer;

import java.io.Serializable;

public class Player extends Cricketer implements Serializable {
    private int ballsFaced;
    private int runsScored;
    private int ballsBowled;
    private int runsConceded;
    private int wicketsTaken;

    public Player(String name, String team, int ballsFaced, int runsScored, int ballsBowled, int runsConceded, int wicketsTaken) {
        super(name);
        this.ballsFaced = ballsFaced;
        this.runsScored = runsScored;
        this.ballsBowled = ballsBowled;
        this.runsConceded = runsConceded;
        this.wicketsTaken = wicketsTaken;
    }

    public Player(String name){
        super(name);
        ballsFaced = 0;
        runsScored = 0;
        ballsBowled = 0;
        runsConceded = 0;
        wicketsTaken = 0;
    }

    public int getBallsFaced() {
        return ballsFaced;
    }

    public void setBallsFaced(int ballsFaced) {
        this.ballsFaced = ballsFaced;
    }

    public int getRunsScored() {
        return runsScored;
    }

    public void setRunsScored(int runsScored) {
        this.runsScored = runsScored;
    }

    public int getBallsBowled() {
        return ballsBowled;
    }

    public void setBallsBowled(int ballsBowled) {
        this.ballsBowled = ballsBowled;
    }

    public int getRunsConceded() {
        return runsConceded;
    }

    public void setRunsConceded(int runsConceded) {
        this.runsConceded = runsConceded;
    }

    public int getWicketsTaken() {
        return wicketsTaken;
    }

    public void setWicketsTaken(int wicketsTaken) {
        this.wicketsTaken = wicketsTaken;
    }

    public String getOvers(){
        String s = (ballsBowled) / 6 + "." + (ballsBowled % 6);
        return s;
    }

    public double getEconomy(){
        return (ballsBowled > 0) ? ((double) runsConceded /ballsBowled)*6 : 0.0;
    }

    public double getStrikeRate(){
        return (ballsFaced > 0) ? ((double) runsScored/ballsFaced) * 100 : 0.0;
    }

    public String getName(){
        return super.getName();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
