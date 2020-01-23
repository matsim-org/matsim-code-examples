package org.matsim.codeexamples.mdp;


import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

public class MDPState {

    private Id<Link> currentLinkId;

    private Id<Link> destinationLinkId;

    public MDPState(Id<Link> currentLinkId, Id<Link> destinationLinkId) {
        this.currentLinkId = currentLinkId;
        this.destinationLinkId = destinationLinkId;
    }

    public Id<Link> getCurrentLinkId() {
        return currentLinkId;
    }

    public void setCurrentLinkId(Id<Link> currentLinkId) {
        this.currentLinkId = currentLinkId;
    }

    public Id<Link> getDestinationLinkId() {
        return destinationLinkId;
    }

    public void setDestinationLinkId(Id<Link> destinationLinkId) {
        this.destinationLinkId = destinationLinkId;
    }
}
