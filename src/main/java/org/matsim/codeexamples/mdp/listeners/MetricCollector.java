package org.matsim.codeexamples.mdp.listeners;


import org.matsim.api.core.v01.Scenario;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;

import java.util.logging.Logger;

public class MetricCollector implements MobsimBeforeSimStepListener {
    private static Logger logger = Logger.getLogger(MetricCollector.class.getName());
    private Scenario scenario;

    public MetricCollector(Scenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public void notifyMobsimBeforeSimStep(MobsimBeforeSimStepEvent e) {
      //PUT CODE HERE
    }
}
