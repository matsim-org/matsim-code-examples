package org.matsim.codeexamples.mdp.customclasses;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.facilities.ActivityFacility;
import org.matsim.utils.objectattributes.attributable.Attributes;

public class CustomActivity implements Activity {
    private double endTime;
    private String type;
    private double startTime;
    private double maximumDuration;
    private Id<Link> linkId;


    @Override
    public double getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(double seconds) {
        this.endTime= seconds;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Coord getCoord() {
        return null;
    }

    @Override
    public double getStartTime() {
         return startTime ;
    }

    @Override
    public void setStartTime(double seconds) {
        this.startTime = seconds;
    }

    @Override
    public double getMaximumDuration() {
        return maximumDuration;
    }

    @Override
    public void setMaximumDuration(double seconds) {
        this.maximumDuration = seconds;
    }

    @Override
    public Id<Link> getLinkId() {
        return linkId;
    }

    @Override
    public Id<ActivityFacility> getFacilityId() {
        return null;
    }

    @Override
    public void setLinkId(Id<Link> id) {
        this.linkId = id;
    }

    @Override
    public void setFacilityId(Id<ActivityFacility> id) {

    }

    @Override
    public void setCoord(Coord coord) {

    }

    @Override
    public Attributes getAttributes() {
        return null;
    }
}
