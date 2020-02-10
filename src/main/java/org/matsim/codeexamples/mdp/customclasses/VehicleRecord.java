package org.matsim.codeexamples.mdp.customclasses;

public class VehicleRecord {
    private double startTime;

    private double endTime;

    private double lastRecordedTime;

    private double distanceTravelled;

    public VehicleRecord(double startTime, double endTime, double lastRecordedTime, double distanceTravelled) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.lastRecordedTime = lastRecordedTime;
        this.distanceTravelled = distanceTravelled;
    }

    public double getDistanceTravelled() {
        return distanceTravelled;
    }

    public void setDistanceTravelled(double distanceTravelled) {
        this.distanceTravelled = distanceTravelled;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public double getLastRecordedTime() {
        return lastRecordedTime;
    }

    public void setLastRecordedTime(double lastRecordedTime) {
        this.lastRecordedTime = lastRecordedTime;
    }
}
