package org.matsim.codeexamples.mdp.event.handlers;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.codeexamples.mdp.CustomScoreLeg;
import org.matsim.codeexamples.mdp.customclasses.LinkTransition;
import org.matsim.codeexamples.mdp.customclasses.VehicleRecord;
import org.matsim.codeexamples.mdp.utilities.JSONStringUtil;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.core.router.TripRouter;
import org.matsim.core.scoring.functions.ScoringParameters;
import org.matsim.facilities.Facility;
import org.matsim.vehicles.Vehicle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class CustomScoring implements BasicEventHandler {


    private static Logger log = Logger.getLogger(CustomScoring.class.getName());

    private MobsimTimer mobsimTimer;

    private Scenario sc;


    private Map<Id<Vehicle>, Double > scoreForVehicles = new HashMap<>();
    private Map<Id<Vehicle>, LinkTransition> lastKnownTransitions = new HashMap<>();
    private CustomScoreLeg customScoreLeg;
    private TripRouter tripRouter;
    private Map<Id<Person>, MobsimAgent> personToAgent;
    public Map<Id<Person>, MobsimAgent> getPersonToAgent() {
        return personToAgent;
    }

    public void setPersonToAgent(Map<Id<Person>, MobsimAgent> personToAgent) {
        this.personToAgent = personToAgent;
    }

    public CustomScoring(MobsimTimer mobsimTimer, Scenario sc, TripRouter tripRouter, CustomScoreLeg customScoreLeg) {
        this.mobsimTimer = mobsimTimer;
        this.sc = sc;
        this.tripRouter = tripRouter;
        this.customScoreLeg = customScoreLeg;
    }

    public TripRouter getTripRouter() {
        return tripRouter;
    }

    public void setTripRouter(TripRouter tripRouter) {
        this.tripRouter = tripRouter;
    }

    public MobsimTimer getMobsimTimer() {
        return mobsimTimer;
    }

    public void setMobsimTimer(MobsimTimer mobsimTimer) {
        this.mobsimTimer = mobsimTimer;
    }




    private void processLeaveEvent(Id<Vehicle> vehicleId, Id<Link> fromLink) {
        if(vehicleId.toString().startsWith("myVeh")) {

            if(lastKnownTransitions.get(vehicleId) == null) {
                lastKnownTransitions.put(vehicleId, new LinkTransition());
            }

            if(lastKnownTransitions.get(vehicleId).getToLinkId() != null) {
                lastKnownTransitions.get(vehicleId).setArrivalTime(mobsimTimer.getTimeOfDay());
                updateScore(vehicleId);
            }
            lastKnownTransitions.get(vehicleId).setFromLinkId(fromLink);
            lastKnownTransitions.get(vehicleId).setDepartureTime(mobsimTimer.getTimeOfDay());
        }


    }

    private void processEnterEvent(Id<Vehicle> vehicleId, Id<Link> toLink) {
        if(vehicleId.toString().startsWith("myVeh")) {
                if(lastKnownTransitions.get(vehicleId) == null) {
                    lastKnownTransitions.put(vehicleId, new LinkTransition());
                }

                lastKnownTransitions.get(vehicleId).setToLinkId(toLink);

        }
    }

    private void updateScore(Id<Vehicle> vehicleId) {
        if(lastKnownTransitions.get(vehicleId).getFromLinkId() == null || lastKnownTransitions.get(vehicleId).getToLinkId() == null) return;
        double departureTime = lastKnownTransitions.get(vehicleId).getDepartureTime();
        double travelTime = lastKnownTransitions.get(vehicleId).getArrivalTime() - departureTime;
        String mainMode = TransportMode.car;
        Id<Link> linkId = lastKnownTransitions.get(vehicleId).getFromLinkId();
        Id<Link> destinationLinkId = lastKnownTransitions.get(vehicleId).getToLinkId();
        Facility fromFacility = new LinkWrapperFacility(this.sc.getNetwork().getLinks().get(linkId));
        Facility toFacility = new LinkWrapperFacility(this.sc.getNetwork().getLinks().get(destinationLinkId));
        List<? extends PlanElement> trip = tripRouter.calcRoute(mainMode, fromFacility, toFacility, departureTime, null);
        Leg leg = (Leg) trip.get(0);
        leg.setTravelTime(travelTime);
        customScoreLeg.handleLeg(leg);
        scoreForVehicles.put(vehicleId,customScoreLeg.getScore());
    }

    public double getScore(Id<Vehicle> vehicleId) {
        if(scoreForVehicles.get(vehicleId) == null) return  -1.0;
        return scoreForVehicles.get(vehicleId);

    }

    @Override
    public void reset(int iteration) {

    }


    @Override
    public void handleEvent(Event event) {
        Id<Link> toLink = null;
        Id<Link> fromLink = null;
        Id<Vehicle> vehicleId = null;

        if(event instanceof LinkLeaveEvent) {
            fromLink = ((LinkLeaveEvent) event).getLinkId();
            vehicleId = ((LinkLeaveEvent) event).getVehicleId();
            processLeaveEvent(vehicleId, fromLink);
            return;
        }

        if(event instanceof LinkEnterEvent) {
            toLink = ((LinkEnterEvent) event).getLinkId();
            vehicleId = ((LinkEnterEvent) event).getVehicleId();
            processEnterEvent(vehicleId,toLink);
            return;

        }
    }
}
