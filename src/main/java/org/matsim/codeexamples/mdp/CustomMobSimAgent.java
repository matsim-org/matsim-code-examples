package org.matsim.codeexamples.mdp;

import org.jfree.util.Log;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.codeexamples.mdp.Experience;
import org.matsim.codeexamples.mdp.MDPState;
import org.matsim.codeexamples.mdp.Policy;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.interfaces.MobsimVehicle;
import org.matsim.facilities.Facility;
import org.matsim.vehicles.Vehicle;

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
    private State state = State.LEG;
    private List<Experience> experiences = new ArrayList<Experience>();
    private final StateMonitor stateMonitor;

    CustomMobSimAgent(IPolicy iPolicy,
                      MobsimTimer mobsimTimer,
                      Scenario scenario,
                      Id<Vehicle> plannedVehicleId,
                      Id<Link> startingLinkId,
                      String agentName,
                      StateMonitor stateMonitor) {

        this.id = Id.createPersonId(agentName);
        this.iPolicy = iPolicy;
        this.mobsimTimer = mobsimTimer;
        this.scenario = scenario;
        this.linkId = startingLinkId;
        this.destinationLinkId = getRandomLink();
        this.plannedVehicleId = plannedVehicleId;
        this.stateMonitor =  stateMonitor;

        log.info("Number of links: " + this.scenario.getNetwork().getLinks().size());

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
        return state;
    }

    @Override
    public double getActivityEndTime() {
        return Double.POSITIVE_INFINITY ;
    }

    @Override
    public void endActivityAndComputeNextState(double now) {
        throw new UnsupportedOperationException() ;
    }

    @Override
    public void endLegAndComputeNextState(double now) {
        throw new UnsupportedOperationException() ;
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


    private MDPState getCurrentMDPState() {
        MDPState state = new MDPState(this.stateMonitor.getState());
        log.info("Current State : "+ state.getStateVector());
        return state;
    }

    private double getLastActionReward() {
        if(this.linkId.equals(this.destinationLinkId)) {
            return 10.0;
        }
        return -1.0;
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

        log.info("Agent Id: " + this.id + " Current Link Id: " + this.getCurrentLinkId());

        log.info("Agent Id: " + this.id+" Choosing next link Id");

        addToExperiences(); // collect experience to be used for training

        Id<Link> nextLink = iPolicy.getBestOutgoingLink(getCurrentMDPState(), this.linkId);

        log.info("Agent Id: "+this.id+" Chose link: " + nextLink);

        this.prevAction = nextLink;
        this.prevState = getCurrentMDPState();
        this.prevReward = getLastActionReward();

        return nextLink;
    }

    @Override
    public void notifyMoveOverNode(Id<Link> newLinkId) {
        this.linkId = newLinkId ;
    }

    @Override
    public boolean isWantingToArriveOnCurrentLink() {
        if ( this.linkId.equals( this.destinationLinkId ) ) {
            getRandomLink();
        }
        return false ;
    }

    private Id<Link> getRandomLink() {
        // if we are at the final destination, select a random new destination:
        Map<Id<Link>, ? extends Link> links = this.scenario.getNetwork().getLinks() ;
        int idx = rnd.nextInt(links.size()) ;
        int cnt = 0 ;
        for ( Link link : links.values() ) {
            if ( cnt== idx ) {
                return link.getId() ;
            }
            cnt++;
        }

        throw new RuntimeException("should not happen");
    }

    @Override
    public void setVehicle(MobsimVehicle veh) {
        this.vehicle = veh ;
    }

    @Override
    public MobsimVehicle getVehicle() {
        log.info("RETURNING VEHICLE");
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

}
