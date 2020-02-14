package org.matsim.codeexamples.mdp;

public class DumpRecord {
    private String linkId;
    private String vehicleId;
    private Double time;
    private Double score;

    public DumpRecord(String linkId, String vehicleId, Double time, Double score) {
        this.linkId = linkId;
        this.vehicleId = vehicleId;
        this.time = time;
        this.score = score;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
