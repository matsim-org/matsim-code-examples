package org.matsim.codeexamples.mdp;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.router.TripRouter;

import java.util.ArrayList;
import java.util.List;

public class Policy implements IPolicy {
    private TripRouter router;

    private Scenario scenario;

    private MobsimTimer mobsimTimer;

    private ActorCriticInterface actorCriticInterface;

    public Policy(TripRouter router,
                  Scenario scenario,
                  MobsimTimer mobsimTimer,
                  ActorCriticInterface actorCriticInterface) {
        this.router = router;
        this.scenario = scenario;
        this.mobsimTimer = mobsimTimer;
        this.actorCriticInterface = actorCriticInterface;
    }

    @Override
    public Id<Link> getBestOutgoingLink(MDPState mdpState, Id<Link> currentLink) {
        Object[] outLinks = this.scenario.getNetwork().getLinks().get(currentLink).getToNode().getOutLinks().keySet().toArray();
        List<Integer> outgoingLinks = new ArrayList<Integer>();
        if (mobsimTimer.getTimeOfDay() >= 24*3600) {
            return null;
        }
        for(Object obj : outLinks) {
            String strLink = ((Id<Link>) obj).toString();
            int link = Integer.valueOf(strLink);
            outgoingLinks.add(link);
        }
        if(outgoingLinks.size() == 0) return null;
        int action = actorCriticInterface.getAction(mdpState.getStateVector(),outgoingLinks);
        Id<Link> chosenLink = Id.createLinkId(action + 1);
        return chosenLink;

    }

    @Override
    public void updatePolicy(List<Experience> experiences) {
       actorCriticInterface.updateModels();
    }

    @Override
    public void addReward(double reward) {
        actorCriticInterface.addReward(reward);
    }
}
