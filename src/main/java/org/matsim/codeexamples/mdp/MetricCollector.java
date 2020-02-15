package org.matsim.codeexamples.mdp;


import org.jfree.util.Log;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.codeexamples.mdp.CustomScoreLeg;
import org.matsim.codeexamples.mdp.DumpRecord;
import org.matsim.codeexamples.mdp.LinkDataRecord;
import org.matsim.codeexamples.mdp.ScoreDataRecord;
import org.matsim.codeexamples.mdp.customclasses.CustomActivity;
import org.matsim.codeexamples.mdp.event.handlers.CustomScoring;
import org.matsim.codeexamples.mdp.event.handlers.StateMonitor;
import org.matsim.codeexamples.mdp.utilities.JSONStringUtil;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;
import org.matsim.core.mobsim.qsim.interfaces.MobsimVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicleImpl;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.core.router.TripRouter;
import org.matsim.core.scoring.functions.CharyparNagelActivityScoring;
import org.matsim.core.scoring.functions.CharyparNagelLegScoring;
import org.matsim.core.scoring.functions.ScoringParameters;
import org.matsim.facilities.Facility;
import org.matsim.vehicles.Vehicle;
import org.matsim.vis.snapshotwriters.VisVehicle;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Logger;


public class MetricCollector implements MobsimBeforeSimStepListener {
    private static Logger logger = Logger.getLogger(MetricCollector.class.getName());
    private StateMonitor stateMonitor;
    private Scenario scenario;

    private long timeStep;
    private String linkFileName;
    private String scoreFileName;
    private CustomScoring customScoring;
    private List<DumpRecord> dumpRecordsList = new ArrayList<>();
    private String storageLocation = "/Users/luckysonkhaidem/Desktop/";
    private TripRouter tripRouter;
    private Map<Id<Person>, MobsimAgent> agents;
    private Map<Id<Vehicle>, Double> scores = new HashMap<>();
    private final ScoringParameters scoringParameters;

    public MetricCollector(StateMonitor stateMonitor,
                           Scenario scenario,
                           CustomScoring customScoring,
                           TripRouter tripRouter,
                           Map<Id<Person>,MobsimAgent> agents,
                           ScoringParameters scoringParameters) {
        this.stateMonitor = stateMonitor;
        this.timeStep=0L;
        this.scenario=scenario;
        this.linkFileName="links.json";
        this.scoreFileName="scores.json";
        this.customScoring=customScoring;
        this.tripRouter = tripRouter;
        this.agents = agents;
        this.stateMonitor = stateMonitor;
        this.scoringParameters = scoringParameters;

        logger.info(scoringParameters.toString());
    }

    private Leg getLeg(Id<Link> currentLink, Id<Link> previousLink, double departureTime, double now) {
        Facility fromFacility = new LinkWrapperFacility(this.scenario.getNetwork().getLinks().get(previousLink));
        Facility toFacility = new LinkWrapperFacility(this.scenario.getNetwork().getLinks().get(currentLink));
        List<? extends PlanElement> trip = tripRouter.calcRoute(TransportMode.car, fromFacility, toFacility, departureTime, null);
        Leg leg = (Leg) trip.get(0);
        leg.setTravelTime(now - departureTime);
        return leg;
    }

    private void getScores(double now) {
        for(Id<Person> personId: this.agents.keySet()) {
            MobsimAgent ag = this.agents.get(personId);
            if(ag instanceof  CustomMobSimAgent) {
                MobsimAgent.State state = ag.getState();
                Id<Link> currentLink = ag.getCurrentLinkId();
                Id<Link> previousLink = ((CustomMobSimAgent)ag).getPreviousLinkId();
                if(state.equals(MobsimAgent.State.LEG)) {
                    Leg leg = getLeg(currentLink,previousLink, ((CustomMobSimAgent)ag).getDepartureTime(),now);
                    CharyparNagelLegScoring scoring = new CharyparNagelLegScoring(scoringParameters, scenario.getNetwork());
                    scoring.handleLeg(leg);
                    Id<Vehicle> vehicleId = ((CustomMobSimAgent) ag).getPlannedVehicleId();
                    scores.put(vehicleId,scoring.getScore());
                }
                else if (state.equals(MobsimAgent.State.ACTIVITY)) {
                    CharyparNagelActivityScoring scoring = new CharyparNagelActivityScoring(scoringParameters);
                    CustomActivity  act = new CustomActivity();
                    act.setLinkId(currentLink);
                    act.setType(((CustomMobSimAgent) ag).getCurrentActivityType());
                    act.setStartTime(((CustomMobSimAgent) ag).getActivityStartTime());
                    act.setEndTime(now);
                    scoring.handleActivity(act);
                    scores.put(((CustomMobSimAgent) ag).getPlannedVehicleId(),scoring.getScore());

                }
            }

        }
    }

    @Override
    public void notifyMobsimBeforeSimStep(MobsimBeforeSimStepEvent e) {
        //PUT CODE HERE
        Map<Id<Vehicle>, Id<Link>> vehMap=stateMonitor.getVehicleLinkMap();
        //every minute
        if((timeStep%60)==0){
            getScores(e.getSimulationTime());
            for(Id<Vehicle> vehicleId : vehMap.keySet()) {
                String linkId = vehMap.get(vehicleId).toString();
                String vehId = vehicleId.toString();
                if(!vehId.startsWith("myVeh")) continue;
                double score = scores.get(vehicleId);
                DumpRecord dumpRecord = new DumpRecord(linkId,vehId,e.getSimulationTime(),score);
                dumpRecordsList.add(dumpRecord);
            }
        }
        timeStep= timeStep+1;

        String dumpData = JSONStringUtil.convertToJSONString(dumpRecordsList);
        File linkFile = new File(storageLocation+linkFileName);
        PrintWriter out = null;
        if ( linkFile.exists() && !linkFile.isDirectory() ) {
            try {
                Log.info("file appending");
                out = new PrintWriter(new FileOutputStream(new File(storageLocation+"dump.json")));
                Log.info("write complete");
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        else {
            try {
                Log.info("file creating");
                out = new PrintWriter(storageLocation+"dump.json");
                Log.info("write complete");
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        out.append(dumpData);
        out.close();


    }
}

