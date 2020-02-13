package org.matsim.codeexamples.mdp.listeners;


import org.jfree.util.Log;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.codeexamples.mdp.CustomScoreLeg;
import org.matsim.codeexamples.mdp.LinkDataRecord;
import org.matsim.codeexamples.mdp.ScoreDataRecord;
import org.matsim.codeexamples.mdp.event.handlers.CustomScoring;
import org.matsim.codeexamples.mdp.event.handlers.StateMonitor;
import org.matsim.codeexamples.mdp.utilities.JSONStringUtil;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;
import org.matsim.core.mobsim.qsim.interfaces.MobsimVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicleImpl;
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
    private List<LinkDataRecord> linkLst = new ArrayList<>();
    private List<ScoreDataRecord> linkScore = new ArrayList<>();
    private String storageLocation = "/Volumes/HP v236w/";


    public MetricCollector(StateMonitor stateMonitor, Scenario scenario, CustomScoring customScoring) {
        this.stateMonitor = stateMonitor;
        this.timeStep=0L;
        this.scenario=scenario;
        this.linkFileName="links.json";
        this.scoreFileName="scores.json";
        this.customScoring=customScoring;
    }

    @Override
    public void notifyMobsimBeforeSimStep(MobsimBeforeSimStepEvent e) {
      //PUT CODE HERE
        Map<Id<Vehicle>, Id<Link>> vehMap=stateMonitor.getVehicleLinkMap();
        if((timeStep%600)==0){
            for(Id<Vehicle> vehicleId : vehMap.keySet()) {
                String linkId = vehMap.get(vehicleId).toString();
                String vehId = vehicleId.toString();
                LinkDataRecord lnkRecrd = new LinkDataRecord(linkId,vehId,timeStep);
                linkLst.add(lnkRecrd);
                String linkData = JSONStringUtil.convertToJSONString(linkLst);
                File linkFile = new File(storageLocation+linkFileName);
                PrintWriter out = null;
                if ( linkFile.exists() && !linkFile.isDirectory() ) {
                    try {
                        Log.info("file appending");
                        out = new PrintWriter(new FileOutputStream(new File(storageLocation+linkFileName), true));
                        Log.info("write complete");
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
                else {
                    try {
                        Log.info("file creating");
                        out = new PrintWriter(storageLocation+linkFileName);
                        Log.info("write complete");
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
                out.append(linkData);
                out.close();

                double score = customScoring.getScore(vehicleId);
                ScoreDataRecord scoreDataRecord = new ScoreDataRecord(vehicleId.toString(),score,timeStep);
                linkScore.add(scoreDataRecord);
                String scoreData = JSONStringUtil.convertToJSONString(linkScore);
                File scoreFile = new File(storageLocation+scoreFileName);
                if ( scoreFile.exists() && !scoreFile.isDirectory() ) {
                    try {
                        Log.info("file appending");
                        out = new PrintWriter(new FileOutputStream(new File(storageLocation+scoreFileName), true));
                        Log.info("write complete");
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
                else {
                    try {
                        Log.info("file creating");
                        out = new PrintWriter(storageLocation+scoreFileName);
                        Log.info("write complete");
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
                out.append(scoreData);
                out.close();

            }
        }
        timeStep= timeStep+1;
    }
}
