package org.matsim.codeexamples.mdp.customclasses;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

public class LinkTransition {

    private Id<Link> fromLinkId = null;
    private Id<Link> toLinkId = null;
    private double departureTime;
    private double arrivalTime;



    public LinkTransition() {

    }

    public Id<Link> getFromLinkId() {
        return fromLinkId;
    }

    public void setFromLinkId(Id<Link> fromLinkId) {
        this.fromLinkId = fromLinkId;
    }

    public Id<Link> getToLinkId() {
        return toLinkId;
    }

    public void setToLinkId(Id<Link> toLinkId) {
        this.toLinkId = toLinkId;
    }
    public double getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
}
