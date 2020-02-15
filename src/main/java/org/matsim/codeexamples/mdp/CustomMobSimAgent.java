package org.matsim.codeexamples.mdp;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.codeexamples.mdp.event.handlers.CustomScoring;
import org.matsim.codeexamples.mdp.event.handlers.StateMonitor;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.qsim.interfaces.MobsimVehicle;
import org.matsim.facilities.Facility;
import org.matsim.vehicles.Vehicle;

import javax.xml.crypto.dom.DOMCryptoContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * MobsimDriverAgent that (1) selects a random destination; (2) computes best path to it at every turn; (3) at destination selects a new random
 * destination.
 *
 * Not tested ...
 *
 * @author nagel
 */

class CustomMobSimAgent implements MobsimDriverAgent {

    private static Logger log = Logger.getLogger("CustomMobSimAgent");

    private Id<Vehicle> plannedVehicleId;
    private Id<Person> id;
    private IPolicy iPolicy;
    private Id<Link> linkId;
    private MobsimVehicle vehicle;
    private MobsimTimer mobsimTimer;
    private Id<Link> destinationLinkId;
    private Scenario scenario;
    private Random rnd = new Random(4711) ;
    private MDPState prevState = null;
    private Id<Link> prevAction = null;
    private double prevReward = 0.0;
    private State state = State.ACTIVITY;
    private List<Experience> experiences = new ArrayList<Experience>();
    private final StateMonitor stateMonitor;
    private CustomScoring customScoring;
    private Person person;
    private boolean reachedWork = false;
    private boolean leaveWork = false;
    private double activityEndTime = 8*3600;
    private EventsManager eventsManager;
    private Id<Link> previousLinkId = null;
    private double departureTime;
    private String currentActivityType = "h";
    private double activityStartTime = 0.0;

    CustomMobSimAgent(IPolicy iPolicy,
                      MobsimTimer mobsimTimer,
                      Scenario scenario,
                      Id<Vehicle> plannedVehicleId,
                      Id<Link> startingLinkId,
                      String agentName,
                      StateMonitor stateMonitor,
                      CustomScoring customScoring,
                      EventsManager eventsManager) {

        this.id = Id.createPersonId(agentName);
        this.iPolicy = iPolicy;
        this.mobsimTimer = mobsimTimer;
        this.scenario = scenario;
        this.linkId = startingLinkId;
        this.destinationLinkId = Id.createLinkId(20);
        this.plannedVehicleId = plannedVehicleId;
        this.stateMonitor =  stateMonitor;
        this.customScoring = customScoring;
        this.eventsManager = eventsManager;
        this.departureTime = mobsimTimer.getTimeOfDay();

    }

    @Override
    public Id<Link> getCurrentLinkId() {
        return this.linkId ;
    }

    @Override
    public Id<Link> getDestinationLinkId() {
        return this.destinationLinkId ;
    }

    @Override
    public Id<Person> getId() {
        return this.id ;
    }

    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public double getActivityEndTime() {
        return this.activityEndTime;
    }

    @Override
    public void endActivityAndComputeNextState(double now) {
        eventsManager.processEvent(new PersonDepartureEvent(now,id,this.linkId,getMode()));
        if(this.linkId.equals(Id.createLinkId(1))) {
            log.info("LEAVING HOME");

            eventsManager.processEvent(new ActivityEndEvent(now,id,this.linkId,null,"h"));
            this.state = State.LEG;
        }
        else if(this.linkId.equals(Id.createLinkId(20))) {
            log.info("LEAVING WORK");
            eventsManager.processEvent(new ActivityEndEvent(now,id,this.linkId,null,"w"));
            this.leaveWork = true;
            this.state = State.LEG;
        }
    }

    public double getActivityStartTime() {
        return activityStartTime;
    }

    public void setActivityStartTime(double activityStartTime) {
        this.activityStartTime = activityStartTime;
    }

    @Override
    public void endLegAndComputeNextState(double now) {
        log.info("END LEG");
        eventsManager.processEvent(new PersonArrivalEvent(now,id,this.linkId,getMode()));
        if(this.linkId.equals(Id.createLinkId(1))) {
            this.activityEndTime = Double.POSITIVE_INFINITY;
            this.state = State.ACTIVITY;
            eventsManager.processEvent(new ActivityStartEvent(now,id,this.linkId,null,"h"));
            currentActivityType = "h";
            activityStartTime = now;
        }
        if(this.linkId.equals(Id.createLinkId(20))) {
            this.activityEndTime = 14*3600;
            this.state = State.ACTIVITY;
            eventsManager.processEvent(new ActivityStartEvent(now,id,this.linkId,null,"w"));
            currentActivityType = "w";
            activityStartTime = now;
        }
    }

    public String getCurrentActivityType() {
        return currentActivityType;
    }

    public void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
    }

    @Override
    public void setStateToAbort(double now) {
        this.state = State.ABORT;
    }

    @Override
    public Double getExpectedTravelTime() {
        return null ;
    }

    @Override
    public Double getExpectedTravelDistance() {
        return null;
    }

    @Override
    public String getMode() {
        return TransportMode.car ;
    }

    @Override
    public void notifyArrivalOnLinkByNonNetworkMode(Id<Link> linkId) {
        // TODO Auto-generated method stub
        throw new RuntimeException("not implemented") ;
    }

    public Id<Link> getPreviousLinkId() {
        return this.previousLinkId;
    }


    private MDPState getCurrentMDPState() {
        MDPState state = new MDPState(this.stateMonitor.getState(), getCurrentLinkId(),mobsimTimer.getTimeOfDay());
        return state;
    }

    private double getLastActionReward() {
        double reward = customScoring.getScore(this.plannedVehicleId) / 100;
        if(this.linkId.equals(this.destinationLinkId)) {
            reward = 0.0;
            if(this.destinationLinkId.toString().equals("20")) {
                this.destinationLinkId = Id.createLinkId(1);
            }
            else{
                this.destinationLinkId = Id.createLinkId(20);
            }
        }
        this.iPolicy.addReward(reward);
        return reward;
    }

    private void addToExperiences() {
        if(this.prevState == null) {
            return;
        }

        Experience experience = new Experience(this.prevState, this.prevAction,this.prevReward,getCurrentMDPState());
        experiences.add(experience);

    }

    @Override
    public Id<Link> chooseNextLinkId() {
        previousLinkId = this.getCurrentLinkId();
        Id<Link> nextLink = iPolicy.getBestOutgoingLink(getCurrentMDPState(), this.linkId);
        return nextLink;
    }

    @Override
    public void notifyMoveOverNode(Id<Link> newLinkId) {
        this.departureTime = mobsimTimer.getTimeOfDay();
        this.linkId = newLinkId ;
    }


    @Override
    public boolean isWantingToArriveOnCurrentLink() {
        if(leaveWork == false && this.linkId.equals(Id.createLinkId(20))) {
            reachedWork = true;
            return true;

        }
        if(reachedWork == true && this.linkId.equals(Id.createLinkId(1))) {
            return true;
        }
        return false;
    }


    public double getDepartureTime() {
        return this.departureTime;
    }

    @Override
    public void setVehicle(MobsimVehicle veh) {
        this.vehicle = veh ;
    }

    @Override
    public MobsimVehicle getVehicle() {
        return this.vehicle ;
    }

    @Override
    public Id<Vehicle> getPlannedVehicleId() {
        return this.plannedVehicleId ;
    }

    @Override
    public Facility getCurrentFacility() {
        // TODO Auto-generated method stub
        throw new RuntimeException("not implemented") ;
    }

    @Override
    public Facility getDestinationFacility() {
        // TODO Auto-generated method stub
        throw new RuntimeException("not implemented") ;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
