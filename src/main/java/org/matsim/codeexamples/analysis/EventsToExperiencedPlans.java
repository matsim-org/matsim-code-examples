
/* *********************************************************************** *
 * project: org.matsim.*
 * ScoringFunctionsForPopulationTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
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

package org.matsim.codeexamples.analysis;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.scoring.EventsToActivities;
import org.matsim.core.scoring.EventsToLegs;
import org.matsim.core.scoring.ExperiencedPlansService;
import org.matsim.core.scoring.ExperiencedPlansServiceFactory;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;

/**
 * @author jbischoff / SBB
 *
 * A script to create an experienced Plans file from an Event file in postprocessing.
 * Useful if you forgot dumping experienced plans.
 */
public class EventsToExperiencedPlans {

	public static void main(String[] args) {
		String eventsFile = args[0];
		String networkFile = args[1];
		String outputPlans = args[2];
		String transitScheduleFile = args[3];
		String experiencedPlansFile = args[4];

		new EventsToExperiencedPlans().run(eventsFile,networkFile,outputPlans, transitScheduleFile,experiencedPlansFile);
	}

	private void run(String eventsFile, String networkFile, String outputPlans , String transitScheduleFile, String experiencedPlansFile) {
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		scenario.getConfig().transit().setUseTransit(true);
		new PopulationReader(scenario).readFile(outputPlans);
		new TransitScheduleReader(scenario).readFile(transitScheduleFile);
		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkFile);
		EventsManager events = EventsUtils.createEventsManager();
		EventsToLegs eventsToLegs = new EventsToLegs(scenario);
		EventsToActivities eventsToActivities = new EventsToActivities();
		events.addHandler(eventsToActivities);
		events.addHandler(eventsToLegs);
		ExperiencedPlansService experiencedPlansService = ExperiencedPlansServiceFactory.create(scenario,eventsToActivities,eventsToLegs);
		new MatsimEventsReader(events).readFile(eventsFile);
		eventsToActivities.finish();
		experiencedPlansService.writeExperiencedPlans(experiencedPlansFile);
	}
}
