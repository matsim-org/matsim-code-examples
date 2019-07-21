/* *********************************************************************** *
 * project: org.matsim.*
 * MyWithinDayMobsimListener.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package tutorial.withinday.withinDayReplanningFromPlans;

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.agents.WithinDayAgentUtils;
import org.matsim.core.mobsim.qsim.interfaces.MobsimVehicle;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.mobsim.qsim.interfaces.NetsimLink;
import org.matsim.core.router.TripRouter;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelTime;
import org.matsim.withinday.utils.EditPlans;
import org.matsim.withinday.utils.EditTrips;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author nagel
 *
 */
@Singleton
class MyWithinDayMobsimListener implements MobsimBeforeSimStepListener {
    
	private static final Logger log = Logger.getLogger("dummy");
	
	@Inject private TripRouter tripRouter;
	@Inject private Scenario scenario;
	
	@Inject private LeastCostPathCalculatorFactory pathCalculatorFactory ;
	
	@Inject private Map<String, TravelTime> travelTimes ;
	@Inject private Map<String, TravelDisutilityFactory> travelDisutilityFactories ;

	@Inject private PlanCalcScoreConfigGroup planCalcScoreConfigGroup;
	
	@Override
	public void notifyMobsimBeforeSimStep(@SuppressWarnings("rawtypes") MobsimBeforeSimStepEvent event) {
		
		QSim mobsim = (QSim) event.getQueueSimulation() ;

		Collection<MobsimAgent> agentsToReplan = getAgentsToReplan(mobsim); 
				
		for (MobsimAgent ma : agentsToReplan) {
			doReplanning(ma, mobsim);
		}
	}
	
	private static List<MobsimAgent> getAgentsToReplan(Netsim mobsim ) {

		List<MobsimAgent> set = new ArrayList<MobsimAgent>();

		// don't do anything for most time steps:
		if (Math.floor(mobsim.getSimTimer().getTimeOfDay()) !=  22000.0) {
			return set;
		}
		
		// find agents that are en-route (more interesting case)
		for (NetsimLink link:mobsim.getNetsimNetwork().getNetsimLinks().values()){
			for (MobsimVehicle vehicle : link.getAllNonParkedVehicles()) {
				MobsimDriverAgent agent=vehicle.getDriver();
				System.out.println(agent.getId());
				if ( true ) { // some condition ...
					System.out.println("found agent");
					set.add(agent);
				}
			}
		}

		return set;

	}

	private boolean doReplanning(MobsimAgent agent, QSim mobsim ) {
		Plan plan = WithinDayAgentUtils.getModifiablePlan( agent ) ;
		
		Gbl.assertNotNull(plan);

		final Integer planElementsIndex = WithinDayAgentUtils.getCurrentPlanElementIndex(agent);

		// This, as an example, changes the activity (i.e. the destination of the current leg).
		// There is no guarantee that this works: it is relatively new code.  Please report bugs to
		// matsim.org/issuetracker .
		
		PopulationFactory pf = mobsim.getScenario().getPopulation().getFactory();;
		
		Id<Link> newDestinationLinkId = Id.create("22", Link.class) ;
		Activity newAct = pf.createActivityFromLinkId("w", newDestinationLinkId ) ;
		newAct.setMaximumDuration(3600);
		
		EditTrips editTrips = new EditTrips(tripRouter, scenario) ;
		EditPlans editPlans = new EditPlans(mobsim, tripRouter, editTrips, pf) ;
		
		int indexOfActivityToReplace = editPlans.findIndexOfRealActAfter(agent, planElementsIndex);;
		
		editPlans.replaceActivity( agent, indexOfActivityToReplace, newAct ) ;
		
		return true;
	}


}
