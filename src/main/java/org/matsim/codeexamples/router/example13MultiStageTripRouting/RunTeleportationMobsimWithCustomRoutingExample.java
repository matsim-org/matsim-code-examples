/* *********************************************************************** *
 * project: org.matsim.*
 * SimulateTeleportation.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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
package org.matsim.codeexamples.router.example13MultiStageTripRouting;

import java.net.URL;
import java.util.Map;

import com.google.inject.Key;
import com.google.inject.name.Names;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.ScoringConfigGroup;
import org.matsim.core.config.groups.ScoringConfigGroup.ActivityParams;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.examples.ExamplesUtils;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.utils.objectattributes.attributable.Attributes;

/**
 * @author thibautd
 */
public class RunTeleportationMobsimWithCustomRoutingExample {

	public static void main(final String[] args) {
		// make sure we get all the log messages in the logfile
		OutputDirectoryLogging.catchLogEntries();
		
		final URL url = IOUtils.extendUrl( ExamplesUtils.getTestScenarioURL( "pt-tutorial" ), "0.config.xml" );;
		
		// load the config ...
		final Config config = ConfigUtils.loadConfig( url );
		// ... and add local changes:
		tuneConfig( config );
		
		config.controller().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );

		// load the scenario:
		final Scenario scenario = ScenarioUtils.loadScenario( config );
		
		// load the controler:
		final Controler controler = new Controler( scenario );

		// create the teleportation station on a central link
		final ActivityFacility teleport =
			createFacility(
					Id.create( "teleport" , ActivityFacility.class),
					controler.getScenario().getNetwork().getLinks().get( Id.create( "2333", Link.class ) ));

		// now, plug our stuff in
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				addRoutingModuleBinding(MyRoutingModule.TELEPORTATION_MAIN_MODE).toProvider(
						new MyRoutingModuleProvider(
								// the module uses the trip router for the PT part.
								// This allows to automatically adapt to user settings,
								// including if they are specified at a later stage
								// in the initialisation process.
								binder().getProvider(Key.get(RoutingModule.class, Names.named(TransportMode.pt))),
								scenario.getPopulation().getFactory(),
								teleport));
				// we still need to provide a way to identify our trips
				// as being teleportation trips.
				// This is for instance used at re-routing.
				bind(MainModeIdentifier.class).toInstance(new MyMainModeIdentifier(new MainModeIdentifierImpl()));
			}
		});


		// run the controler:
		controler.run();
	}

	private static void tuneConfig(final Config config) {
//		config.getModule( "changeMode" ).addParam( "modes" , "car,pt,"+MyRoutingModule.TELEPORTATION_MAIN_MODE );
		config.changeMode().setModes( new String[] {"car", "pt", MyRoutingModule.TELEPORTATION_MAIN_MODE} );
		
		final ActivityParams scoreTelepInteract = new ActivityParams( MyRoutingModule.STAGE );
		scoreTelepInteract.setTypicalDuration( 2 * 60 );
		scoreTelepInteract.setOpeningTime( 0 );
		scoreTelepInteract.setClosingTime( 0 );
		config.scoring().addActivityParams( scoreTelepInteract );


		final ScoringConfigGroup.ModeParams modeParams = new ScoringConfigGroup.ModeParams(
				MyRoutingModule.TELEPORTATION_LEG_MODE);
		config.scoring().addModeParams(modeParams);
	}

	private static ActivityFacility createFacility(
			final Id<ActivityFacility> id,
			final Link link) {
		if ( link == null ) throw new IllegalArgumentException( "link == null");
		
		return new ActivityFacility() {
			@Override
			public Attributes getAttributes() {
				// TODO Auto-generated method stub
				throw new RuntimeException("not implemented") ;
			}
			
			@Override
			public Coord getCoord() {
				return link.getFromNode().getCoord();
			}

			@Override
			public Id<ActivityFacility> getId() {
				return id;
			}

			@Override
			public Map<String, Object> getCustomAttributes() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Id<Link> getLinkId() {
				return link.getId();
			}
			
			@Override
			public void addActivityOption(ActivityOption option) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public Map<String, ActivityOption> getActivityOptions() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void setCoord(Coord coord) {
				// TODO Auto-generated method stub
				throw new RuntimeException("not implemented") ;
			}
		};
	}
}

