/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * DefaultControlerModules.java
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2014 by the members listed in the COPYING, *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 */
package org.matsim.codeexamples.programming.ownPrepareForSimExample;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.groups.GlobalConfigGroup;
import org.matsim.core.controler.PrepareForSim;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.population.algorithms.AbstractPersonAlgorithm;
import org.matsim.core.population.algorithms.ParallelPersonAlgorithmUtils;
import org.matsim.core.router.PlanRouter;
import org.matsim.core.router.TripRouter;
import org.matsim.core.scenario.Lockable;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.utils.timing.TimeInterpretation;
import org.matsim.facilities.ActivityFacilities;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.HashSet;

/**
 * This class does the same as PrepareForSimImpl except for a point in run():
 * It uses MyPersonPrepareForSim instead of PersonPrepareForSim.
 * 
 * @author tthunig
 */
public class MyPrepareForSim implements PrepareForSim {

	private static final Logger log = LogManager.getLogger(PrepareForSim.class);

	private final GlobalConfigGroup globalConfigGroup;
	private final Scenario scenario;
	private final Network network;
	private final Population population;
	private final ActivityFacilities activityFacilities;
	private final Provider<TripRouter> tripRouterProvider;
	private final TimeInterpretation timeInterpretation;

	@Inject
	MyPrepareForSim(GlobalConfigGroup globalConfigGroup, Scenario scenario, Network network, Population population, ActivityFacilities activityFacilities, Provider<TripRouter> tripRouterProvider, TimeInterpretation timeInterpretation) {
		this.globalConfigGroup = globalConfigGroup;
		this.scenario = scenario;
		this.network = network;
		this.population = population;
		this.activityFacilities = activityFacilities;
		this.tripRouterProvider = tripRouterProvider;
		this.timeInterpretation = timeInterpretation;
	}


	@Override
	public void run() {
		if (scenario instanceof MutableScenario) {
			((MutableScenario)scenario).setLocked();
			// see comment in ScenarioImpl. kai, sep'14
		}

		/*
		 * Create single-mode network here and hand it over to PersonPrepareForSim. Otherwise, each instance would create its
		 * own single-mode network. However, this assumes that the main mode is car - which PersonPrepareForSim also does. Should
		 * be probably adapted in a way that other main modes are possible as well. cdobler, oct'15.
		 */
		final Network net;
		if (NetworkUtils.isMultimodal(network)) {
			log.info("Network seems to be multimodal. Create car-only network which is handed over to PersonPrepareForSim.");
			TransportModeNetworkFilter filter = new TransportModeNetworkFilter(network);
			net = NetworkUtils.createNetwork();
			HashSet<String> modes = new HashSet<>();
			modes.add(TransportMode.car);
			filter.filter(net, modes);
		} else {
			net = network;
		}

		// make sure all routes are calculated.
		ParallelPersonAlgorithmUtils.run(population, globalConfigGroup.getNumberOfThreads(),
				new ParallelPersonAlgorithmUtils.PersonAlgorithmProvider() {
					@Override
					public AbstractPersonAlgorithm getPersonAlgorithm() {
						return new MyPersonPrepareForSim(new PlanRouter(tripRouterProvider.get(), activityFacilities, timeInterpretation), scenario, net);
					}
				});
		if (population instanceof Lockable) {
			((Lockable) population).setLocked();
		}

	}

}
