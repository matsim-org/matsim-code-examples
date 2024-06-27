package org.matsim.codeexamples.extensions.bicycle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.contrib.bicycle.BicycleConfigGroup;
import org.matsim.contrib.bicycle.BicycleModule;
import org.matsim.contrib.bicycle.BicycleUtils;
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.ScoringConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.ReplanningConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.examples.ExamplesUtils;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.VehiclesFactory;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

import java.net.URL;
import java.util.*;

public final class RunBicycleContribExample{
	private static final Logger LOG = LogManager.getLogger( RunBicycleContribExample.class );

	private static final String BICYCLE_MODE = "bicycle";

	public static void main(String[] args) {
		Config config;
		if (args.length >= 1) {
			LOG.info("A user-specified config.xml file was provided. Using it...");
			config = ConfigUtils.loadConfig(args, new BicycleConfigGroup() );
		} else {
			LOG.info("No config.xml file was provided. Using bicycle_example from ExamplesUtils.");

			config = ConfigUtils.createConfig( ExamplesUtils.getTestScenarioURL( "bicycle_example" ) );

			config.qsim().setLinkDynamics( QSimConfigGroup.LinkDynamics.PassingQ );
			config.qsim().setTrafficDynamics( QSimConfigGroup.TrafficDynamics.kinematicWaves );

			config.network().setInputFile("network_normal.xml"); // change this to any of the others that are provided
			config.plans().setInputFile("population_3.xml");

//			config.replanning().addStrategySettings( new ReplanningConfigGroup.StrategySettings().setStrategyName("ChangeExpBeta" ).setWeight(0.8 ) );
//			config.replanning().addStrategySettings( new ReplanningConfigGroup.StrategySettings().setStrategyName("ReRoute" ).setWeight(0.2 ) );

			config.scoring().addActivityParams( new ScoringConfigGroup.ActivityParams("home").setTypicalDuration(12*60*60 ) );
			config.scoring().addActivityParams( new ScoringConfigGroup.ActivityParams("work").setTypicalDuration(8*60*60 ) );

			config.scoring().addModeParams( new ScoringConfigGroup.ModeParams(
					BICYCLE_MODE ).setConstant(0. ).setMarginalUtilityOfDistance(-0.0004 ).setMarginalUtilityOfTraveling(-6.0 ).setMonetaryDistanceRate(0. ) );

			config.global().setNumberOfThreads(1 );
			config.controller().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );

			config.controller().setLastIteration(0);
		}

		BicycleConfigGroup bicycleConfig = ConfigUtils.addOrGetModule( config, BicycleConfigGroup.class );
		bicycleConfig.setBicycleMode( BICYCLE_MODE );
//		bicycleConfig.setMarginalUtilityOfInfrastructure_m(-0.0002);
//		bicycleConfig.setMarginalUtilityOfComfort_m(-0.0002);
//		bicycleConfig.setMarginalUtilityOfGradient_m_100m(-0.02);
//		bicycleConfig.setMaxBicycleSpeedForRouting(4.16666666);

		Set<String> mainModeSet = new LinkedHashSet<>( Arrays.asList( BICYCLE_MODE, TransportMode.car ) );

		config.qsim().setMainModes(mainModeSet );

		config.routing().setNetworkModes(mainModeSet );

		// ===

		Scenario scenario = ScenarioUtils.loadScenario( config );

		// set config such that the mode vehicles come from vehicles data:
		scenario.getConfig().qsim().setVehiclesSource( QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData );

		// now put the mode vehicles into the vehicles data:
		final VehiclesFactory vf = VehicleUtils.getFactory();
		scenario.getVehicles().addVehicleType( vf.createVehicleType( Id.create(TransportMode.car, VehicleType.class ) ).setNetworkMode( TransportMode.car ) );
		scenario.getVehicles().addVehicleType( vf.createVehicleType(Id.create( BICYCLE_MODE, VehicleType.class ) ).setNetworkMode( BICYCLE_MODE ).setMaximumVelocity(4.16666666 ).setPcuEquivalents(0.25 ) );

//		// create a bicycle expressway
//		scenario.getNetwork().getLinks().get( Id.createLinkId( 2 ) ).getAttributes().putAttribute( BicycleUtils.BICYCLE_INFRASTRUCTURE_SPEED_FACTOR, 10. );

//		// allow cars on all links ... and switch one person to car:
//		for( Link link : scenario.getNetwork().getLinks().values() ){
//			link.setAllowedModes( mainModeSet );
//		}
//		Plan plan = scenario.getPopulation().getPersons().get( Id.createPersonId( 1 ) ).getSelectedPlan();
//		for( Leg leg : TripStructureUtils.getLegs( plan ) ){
//			leg.setMode( TransportMode.car );
//		}

		// ===

		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new BicycleModule() );

		controler.addOverridingModule( new OTFVisLiveModule() );
		OTFVisConfigGroup otfConfig = ConfigUtils.addOrGetModule( config, OTFVisConfigGroup.class );
		otfConfig.setAgentSize( otfConfig.getAgentSize()*2 );

		controler.run();
	}

}
