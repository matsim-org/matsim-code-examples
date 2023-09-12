package org.matsim.codeexamples.strategies.pluggablePlanStrategyInCode;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.PlanStrategyImpl;

import jakarta.inject.Inject;
import jakarta.inject.Provider;


class MyPlanStrategyProvider implements Provider<PlanStrategy> {

    private EventsManager eventsManager;
    private Scenario scenario;
    
    private static final Logger log = LogManager.getLogger(MyPlanSelector.class);

    @Inject
    MyPlanStrategyProvider(EventsManager eventsManager, Scenario scenario) {
        this.eventsManager = eventsManager;
        this.scenario = scenario;
    }

    @Override
	public PlanStrategy get() {
    	
    	log.error("calling PlanStradegy.get()");
        // A PlanStrategy is something that can be applied to a Person (not a Plan).
        // It first selects one of the plans:
        MyPlanSelector planSelector = new MyPlanSelector();
        PlanStrategyImpl.Builder builder = new PlanStrategyImpl.Builder(planSelector);

        // the plan selector may, at the same time, collect events:
        eventsManager.addHandler(planSelector);

        // if you just want to select plans, you can stop here.


        // Otherwise, to do something with that plan, one needs to add modules into the strategy.  If there is at least
        // one module added here, then the plan is copied and then modified.
        MyPlanStrategyModule mod = new MyPlanStrategyModule(scenario);
        builder.addStrategyModule(mod);

        // these modules may, at the same time, be events listeners (so that they can collect information):
        eventsManager.addHandler(mod);

        return builder.build();
	}

}
