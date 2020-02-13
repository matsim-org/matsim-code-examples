package org.matsim.codeexamples.mdp;

public class ScoreDataRecord {
    private String vehicleId;
    private double score;
    private long time;

    public ScoreDataRecord(String vehicle, double score, long time){
        this.vehicleId=vehicle;
        this.score=score;
        this.time=time;
    }

}
