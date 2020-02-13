package org.matsim.codeexamples.mdp;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.framework.MobsimTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class PNPolicy implements IPolicy {

    private Logger logger = Logger.getLogger(PNPolicy.class.getName());

    private PolicyNetworkInterface policyNetworkInterface;

    private MobsimTimer mobsimTimer;

    private Scenario scenario;

    public PNPolicy(PolicyNetworkInterface policyNetworkInterface, MobsimTimer mobsimTimer, Scenario scenario) {
        this.policyNetworkInterface = policyNetworkInterface;
        this.mobsimTimer = mobsimTimer;
        this.scenario = scenario;
    }

    private int sample(List<Double> pdf) {
        double r = new Random().nextDouble();
        for(int i = 0; i < pdf.size(); i++) {
            if(r < pdf.get(i))
                return i;
            r -= pdf.get(i);
        }
        return pdf.size()-1;  // should not happen
    }

    @Override
    public Id<Link> getBestOutgoingLink(MDPState mdpState, Id<Link> currentLink) {
        logger.info("TIME OF DAY IS "+mobsimTimer.getTimeOfDay());
        if (mobsimTimer.getTimeOfDay() >= 24*3600) {
            return null;
        }
        Object[] outLinks = this.scenario.getNetwork().getLinks().get(currentLink).getToNode().getOutLinks().keySet().toArray();
        List<Integer> outgoingLinks = new ArrayList<Integer>();
        for(Object obj : outLinks) {
            String strLink = ((Id<Link>) obj).toString();
            int link = Integer.valueOf(strLink);
            outgoingLinks.add(link);
        }
        if(currentLink.equals(Id.createLinkId(1))) {
            List<Double> state = new ArrayList<>();
            state.add(mdpState.getStateVector().get(0)); // Number of vehicles in Link 1
            state.add(mdpState.getStateVector().get(19)); // NUmber of vehicles in Link 20
            state.add(mobsimTimer.getTimeOfDay()/60.0); // Time
            List<Double> actionRate = policyNetworkInterface.getAction(state,null);
            int action = sample(actionRate);
            int linkChosen = action + 2;
            if(outgoingLinks.contains(linkChosen)) {
                return Id.createLinkId(linkChosen);
            }
            linkChosen = new Random().nextInt(outgoingLinks.size());
            return Id.createLinkId(outgoingLinks.get(linkChosen));
        } else {
            int linkChosen = new Random().nextInt(outgoingLinks.size());
            return Id.createLinkId(outgoingLinks.get(linkChosen));
        }
    }

    @Override
    public void updatePolicy(List<Experience> experiences) {

    }

    @Override
    public void addReward(double reward) {

    }
}
