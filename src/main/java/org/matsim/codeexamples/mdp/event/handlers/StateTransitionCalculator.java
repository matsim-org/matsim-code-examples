package org.matsim.codeexamples.mdp.event.handlers;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.vehicles.Vehicle;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


//calcuate state transition
public class StateTransitionCalculator implements BasicEventHandler {

    class LinkTransition {
        private Id<Link> fromLinkId = null;
        private Id<Link> toLinkId = null;

        LinkTransition() {

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
    }


    private static Logger log = Logger.getLogger("StateTransitionCalculator");

    private double[][] transitionCount = new double[23][23]; // Divide each row by the sum of that row to get the linkTransition probabilities
    private Map<Id<Vehicle>, LinkTransition> lastKnownTransition = new HashMap<>();

    private void  updateTransitionCount(Id<Vehicle> vehicleId) {
        LinkTransition linkTransition = lastKnownTransition.get(vehicleId);
        if(linkTransition.getFromLinkId() != null && linkTransition.getToLinkId() != null) {
            int i = Integer.valueOf(linkTransition.getFromLinkId().toString()) - 1;
            int j = Integer.valueOf(linkTransition.getToLinkId().toString()) - 1;
            transitionCount[i][j] += 1;
        }
    }

    @Override
    public void handleEvent(Event event) {
        Id<Link> toLink = null;
        Id<Link> fromLink = null;
        Id<Vehicle> vehicleId = null;

        if(event instanceof LinkEnterEvent) {
            toLink = ( (LinkEnterEvent)event).getLinkId();
            vehicleId = ((LinkEnterEvent)event).getVehicleId();
            if(lastKnownTransition.get(vehicleId) == null) {
                lastKnownTransition.put(vehicleId, new LinkTransition());
            }
            lastKnownTransition.get(vehicleId).setToLinkId(toLink);
            updateTransitionCount(vehicleId);
            return;
        }
        if(event instanceof LinkLeaveEvent) {
            fromLink = ((LinkLeaveEvent) event).getLinkId();
            vehicleId = ((LinkLeaveEvent) event).getVehicleId();
            if(lastKnownTransition.get(vehicleId) == null) {
                lastKnownTransition.put(vehicleId, new LinkTransition());
            }
            lastKnownTransition.get(vehicleId).setFromLinkId(fromLink);
            return; //no need to update transition matrix on Link Leave event

        }

    }

    @Override
    public void reset(int iteration) {
        //Reset only last known transition
        lastKnownTransition.clear();
    }
}
