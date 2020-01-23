package org.matsim.codeexamples.mdp;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.router.TripRouter;

import java.util.List;

public class Policy implements IPolicy {
    private TripRouter router;

    private Scenario scenario;

    private MobsimTimer mobsimTimer;

    public Policy(TripRouter router, Scenario scenario, MobsimTimer mobsimTimer) {
        this.router = router;
        this.scenario = scenario;
        this.mobsimTimer = mobsimTimer;
    }

    @Override
    public Id<Link> getBestOutgoingLink(MDPState mdpState) {
        //currently it is random
        Object[] outLinks = this.scenario.getNetwork().getLinks().get(mdpState.getCurrentLinkId()).getToNode().getOutLinks().keySet().toArray();
        int idx = MatsimRandom.getRandom().nextInt(outLinks.length) ;
        if ( this.mobsimTimer.getTimeOfDay() < 24.*3600 ) {
            return (Id<Link>) outLinks[idx];
        }
        return null;

    }

    @Override
    public void updatePolicy(List<Experience> experiences) {
        throw new RuntimeException("Not implemented yet");
    }
}
