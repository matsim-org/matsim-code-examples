package org.matsim.codeexamples.extensions.dvrp;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.drt.optimizer.insertion.extensive.ExtensiveInsertionSearchParams;
import org.matsim.contrib.drt.routing.DrtRoute;
import org.matsim.contrib.drt.routing.DrtRouteFactory;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtConfigs;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtModule;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.run.DvrpQSimComponents;
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.ScoringConfigGroup.ModeParams;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup.SnapshotStyle;
import org.matsim.core.config.groups.ReplanningConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultSelector;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultStrategy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.objectattributes.attributable.Attributes;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

import static org.matsim.contrib.dvrp.fleet.DvrpVehicleSpecificationWithMatsimVehicle.*;

class RunDrtExample{
	// todo:
	// * have at least one drt use case in the "examples" project, so it can be addressed via ExamplesUtils
	//    (TS, jul '24: have a look at DrtTestScenario in MATSimApplication. It does ExamplesUtils.getTestScenarioURL("kelheim")
	//    and then ConfigUtils.loadConfig(IOUtils.extendUrl(context, "config-with-drt.xml")).
	//    There is also the mielec example and the dvrp-grid example.)
	// * remove the DrtRoute.class thing; use Attributable instead (Route will have to be made implement Attributable).  If impossible, move the DrtRoute
	// class thing to the core.
	// * move consistency checkers into the corresponding config groups.
	// * make MultiModeDrt and normal DRT the same.  Make config accordingly so that 1-mode drt is just multi-mode with one entry.

	private static final Logger log = LogManager.getLogger( RunDrtExample.class );
	private static final String DRT_A = "drt_A";
	private static final String DRT_B = "drt_B";
	private static final String DRT_C = "drt_C";

	public static void main( String... args ) {
		run(true, args);
	}

	public static void run(boolean otfvis, String... args ){
		Config config;
		if ( args!=null && args.length>=1 ) {
			config = ConfigUtils.loadConfig( args );
		} else {
			// config = ConfigUtils.loadConfig( IOUtils.extendUrl( ExamplesUtils.getTestScenarioURL( "dvrp-grid" ), "multi_mode_one_shared_taxi_config.xml" ) );
			// the above is there, but is totally different.  --> consolidate.  kai, jan'23

			config = ConfigUtils.loadConfig( "./scenarios/multi_mode_one_shared_taxi/multi_mode_one_shared_taxi_config.xml" );
			config.controller().setOverwriteFileSetting( OverwriteFileSetting.deleteDirectoryIfExists );
		}
		
		config.controller().setLastIteration( 1 );

		config.qsim().setSimStarttimeInterpretation( QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime );
		config.qsim().setInsertingWaitingVehiclesBeforeDrivingVehicles(true);
		config.qsim().setSnapshotStyle(SnapshotStyle.queue);

		@SuppressWarnings("unused")
		DvrpConfigGroup dvrpConfig = ConfigUtils.addOrGetModule( config, DvrpConfigGroup.class );
		// (config group needs to be "materialized")

		MultiModeDrtConfigGroup multiModeDrtCfg = ConfigUtils.addOrGetModule(config, MultiModeDrtConfigGroup.class);
		{
			DrtConfigGroup drtConfig = new DrtConfigGroup();
			drtConfig.mode = DRT_A;
			drtConfig.stopDuration = 60.;
			drtConfig.maxWaitTime=900;
			drtConfig.maxTravelTimeAlpha = 1.3;
			drtConfig.maxTravelTimeBeta=10. * 60.;
			drtConfig.rejectRequestIfMaxWaitOrTravelTimeViolated= false ;
			drtConfig.vehiclesFile="one_shared_taxi_vehicles_A.xml";
			drtConfig.changeStartLinkToLastLinkInSchedule=true;
			drtConfig.addParameterSet( new ExtensiveInsertionSearchParams() );
			multiModeDrtCfg.addParameterSet(drtConfig);
		}
		{
			DrtConfigGroup drtConfig = new DrtConfigGroup();
			drtConfig.mode = DRT_B;
			drtConfig.stopDuration = 60.;
			drtConfig.maxWaitTime=900;
			drtConfig.maxTravelTimeAlpha = 1.3;
			drtConfig.maxTravelTimeBeta=10. * 60.;
			drtConfig.rejectRequestIfMaxWaitOrTravelTimeViolated= false ;
			drtConfig.vehiclesFile="one_shared_taxi_vehicles_B.xml";
			drtConfig.changeStartLinkToLastLinkInSchedule=true;
			drtConfig.addParameterSet( new ExtensiveInsertionSearchParams() );
			multiModeDrtCfg.addParameterSet(drtConfig);
		}
		{
			DrtConfigGroup drtConfig = new DrtConfigGroup();
			drtConfig.mode = DRT_C;
			drtConfig.stopDuration = 60.;
			drtConfig.maxWaitTime=900;
			drtConfig.maxTravelTimeAlpha = 1.3;
			drtConfig.maxTravelTimeBeta=10. * 60.;
			drtConfig.rejectRequestIfMaxWaitOrTravelTimeViolated= false ;

			//we will provide the vehicle via standard MATSim vehicles, see below
//			drtConfig.vehiclesFile="one_shared_taxi_vehicles_C.xml";

			drtConfig.changeStartLinkToLastLinkInSchedule=true;
			drtConfig.addParameterSet( new ExtensiveInsertionSearchParams() );
			multiModeDrtCfg.addParameterSet(drtConfig);
		}

		for (DrtConfigGroup drtCfg : multiModeDrtCfg.getModalElements()) {
			DrtConfigs.adjustDrtConfig(drtCfg, config.scoring(), config.routing());
		}
		{
			// add params so that scoring works:
			config.scoring().addModeParams( new ModeParams( DRT_A ) );
			config.scoring().addModeParams( new ModeParams( DRT_B ) );
			config.scoring().addModeParams( new ModeParams( DRT_C ) );
		}
		{
			// clear strategy settings from config file:
			config.replanning().clearStrategySettings();

			// configure mode innovation so that travellers start using drt:
			config.replanning().addStrategySettings( new StrategySettings().setStrategyName( DefaultStrategy.ChangeSingleTripMode ).setWeight( 0.1 ) );
			config.changeMode().setModes( new String[]{TransportMode.car, DRT_A, DRT_B, DRT_C} );

			// have a "normal" plans choice strategy:
			config.replanning().addStrategySettings( new StrategySettings().setStrategyName( DefaultSelector.ChangeExpBeta ).setWeight( 1. ) );
		}

		// ===
		Scenario scenario = ScenarioUtils.createScenario( config ) ;
		scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory( DrtRoute.class, new DrtRouteFactory() );
		ScenarioUtils.loadScenario( scenario );
		// yyyy in long run, try to get rid of the route factory thing

		// ===

		//we use the standard vehicle type for DRT_C. you could also create your own vehicle type and e.g. set a different maximum velocity.
		VehicleType vehicleType = VehicleUtils.getDefaultVehicleType();
		vehicleType.getCapacity().setSeats(2);
		scenario.getVehicles().addVehicleType(vehicleType);

		// this is how you can provide DRT vehicles via code (ot within the standard MATSim vehicles (file),
		// i.e. not from a drt-mode-specific input file
		Vehicle vehicle_c = VehicleUtils.createVehicle(Id.createVehicleId("taxi_one_C"), vehicleType);
		Attributes attributes = vehicle_c.getAttributes();
		attributes.putAttribute(DVRP_MODE, DRT_C);
		attributes.putAttribute(START_LINK, "215");
		attributes.putAttribute(SERVICE_BEGIN_TIME, 0d);
		attributes.putAttribute(SERVICE_END_TIME, 8000d);
		scenario.getVehicles().addVehicle(vehicle_c);

		Controler controler = new Controler( scenario ) ;

		controler.addOverridingModule( new DvrpModule() ) ;
		controler.addOverridingModule( new MultiModeDrtModule( ) ) ;

		controler.configureQSimComponents( DvrpQSimComponents.activateModes( DRT_A, DRT_B, DRT_C ) ) ;
		// yyyy in long run, try to get rid of the above line

		if (otfvis) {
			OTFVisConfigGroup otfVisConfigGroup = ConfigUtils.addOrGetModule(config, OTFVisConfigGroup.class);
			otfVisConfigGroup.setLinkWidth(5);
			otfVisConfigGroup.setDrawNonMovingItems(true);
			controler.addOverridingModule(new OTFVisLiveModule());
		}

		controler.run() ;
	}

}
