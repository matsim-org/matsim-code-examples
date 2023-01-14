package org.matsim.codeexamples.extensions.emissions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.emissions.EmissionModule;
import org.matsim.contrib.emissions.HbefaVehicleCategory;
import org.matsim.contrib.emissions.utils.EmissionsConfigGroup;
import org.matsim.contrib.emissions.utils.EmissionsConfigGroup.DetailedVsAverageLookupBehavior;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Injector;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.algorithms.EventWriterXML;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.EngineInformation;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

/**
 *
 * An example for calculating emissions for transit vehicles while running the matsim scenario.
 *
 * @author haowu, nagel
 */
class RunEmissionsForPtOfflineExample {
	private static final Logger log = LogManager.getLogger(RunEmissionsForPtOfflineExample.class );
	private static final String eventsFile =  "/output/pt-tutorial/output_events.xml.gz"; // The output event
	private static final String emissionEventOutputPath = "/output/pt-tutorial/emission.events.offline.xml.gz";

	public static void main( String [] args ) {

		Config config = ConfigUtils.loadConfig( "config.xml" ); // Enter the path of config file with which you run the matsim scenario
		config.vehicles().setVehiclesFile("/output/pt-tutorial/output_vehicles.xml.gz"); // The output vehicle files which contains the vehicle information which will be used later!
		config.qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData);
		config.controler().setLastIteration(3);
		config.plansCalcRoute().clearTeleportedModeParams();

		EmissionsConfigGroup emissionConfig = ConfigUtils.addOrGetModule( config, EmissionsConfigGroup.class );
		{
		emissionConfig.setAverageColdEmissionFactorsFile( "../EFA_ColdStart_Vehcat_2020_Average_perVehCat_Bln_carOnly.csv" );
		emissionConfig.setAverageWarmEmissionFactorsFile( "../EFA_HOT_Vehcat_2020_Average_perVehCat_Bln_carOnly.csv" );
		emissionConfig.setHbefaTableConsistencyCheckingLevel(EmissionsConfigGroup.HbefaTableConsistencyCheckingLevel.allCombinations);
		emissionConfig.setHbefaVehicleDescriptionSource(EmissionsConfigGroup.HbefaVehicleDescriptionSource.asEngineInformationAttributes);
		emissionConfig.setHandlesHighAverageSpeeds(false);
		emissionConfig.setWritingEmissionsEvents(true);

		// (start with simplest option and debug from there)
		//settings from old emission contrib version!
		//emissionConfig.setHbefaRoadTypeSource(HbefaRoadTypeSource.fromLinkAttributes);

		// define correct emissionConfig here:
		emissionConfig.setDetailedVsAverageLookupBehavior( DetailedVsAverageLookupBehavior.directlyTryAverageTable );
		emissionConfig.setNonScenarioVehicles(EmissionsConfigGroup.NonScenarioVehicles.ignore);
		//emissionConfig.setNonScenarioVehicles( EmissionsConfigGroup.NonScenarioVehicles.abort );
		emissionConfig.setEmissionsComputationMethod(EmissionsConfigGroup.EmissionsComputationMethod.AverageSpeed);
		}

		Scenario scenario = ScenarioUtils.loadScenario( config );

		// feed info of vehicles
		// non-public transit vehicles should be considered as non-hbefa vehicles
		for (VehicleType type : scenario.getVehicles().getVehicleTypes().values()) {
			EngineInformation engineInformation = type.getEngineInformation();
			VehicleUtils.setHbefaVehicleCategory( engineInformation, HbefaVehicleCategory.NON_HBEFA_VEHICLE.toString());
			VehicleUtils.setHbefaTechnology( engineInformation, "average" );
			VehicleUtils.setHbefaSizeClass( engineInformation, "average" );
			VehicleUtils.setHbefaEmissionsConcept( engineInformation, "average" );
			log.info("handled vehicle: " + type.getId());
		}
		log.info("the number of vehicles handled: " + scenario.getVehicles().getVehicleTypes().values().size());
		/*		Id<VehicleType> carVehicleTypeId = Id.create("car", VehicleType.class);
		VehicleType carVehicleType = scenario.getVehicles().getVehicleTypes().get(carVehicleTypeId);
		EngineInformation carEngineInformation = carVehicleType.getEngineInformation();
		VehicleUtils.setHbefaVehicleCategory( carEngineInformation, HbefaVehicleCategory.PASSENGER_CAR.toString());
		VehicleUtils.setHbefaTechnology( carEngineInformation, "average" );
		VehicleUtils.setHbefaSizeClass( carEngineInformation, "average" );
		VehicleUtils.setHbefaEmissionsConcept( carEngineInformation, "average" );*/

		for( VehicleType vehicleType : scenario.getTransitVehicles().getVehicleTypes().values() ){
			EngineInformation transitVehicleEngineInformation = vehicleType.getEngineInformation();
			VehicleUtils.setHbefaVehicleCategory( transitVehicleEngineInformation, HbefaVehicleCategory.PASSENGER_CAR.toString() ); // or whatever is in your file; maybe just set it to some truck type first
			VehicleUtils.setHbefaTechnology( transitVehicleEngineInformation, "average" );
			VehicleUtils.setHbefaSizeClass( transitVehicleEngineInformation, "average" );
			VehicleUtils.setHbefaEmissionsConcept( transitVehicleEngineInformation, "average" );
			//TODO: Put some random values for the rail vehicles since there is only information of urban bus for transit vehicles in hbefa 4.1.
		}

		// network
		for (Link link : scenario.getNetwork().getLinks().values()) {

			double freespeed = Double.NaN;

			if (link.getFreespeed() <= 13.888889) {
				freespeed = link.getFreespeed() * 2;
				// for non motorway roads, the free speed level was reduced
			} else {
				freespeed = link.getFreespeed();
				// for motorways, the original speed levels seems ok.
			}

			//TODO: also add pt links for the real scenarios
			if(freespeed <= 8.333333333){ //30kmh
				link.getAttributes().putAttribute("hbefa_road_type", "URB/Access/30");
			} else if(freespeed <= 11.111111111){ //40kmh
				link.getAttributes().putAttribute("hbefa_road_type", "URB/Access/40");
			} else if(freespeed <= 13.888888889){ //50kmh
				double lanes = link.getNumberOfLanes();
				if(lanes <= 1.0){
					link.getAttributes().putAttribute("hbefa_road_type", "URB/Local/50");
				} else if(lanes <= 2.0){
					link.getAttributes().putAttribute("hbefa_road_type", "URB/Distr/50");
				} else if(lanes > 2.0){
					link.getAttributes().putAttribute("hbefa_road_type", "URB/Trunk-City/50");
				} else{
					throw new RuntimeException("NoOfLanes not properly defined");
				}
			} else if(freespeed <= 16.666666667){ //60kmh
				double lanes = link.getNumberOfLanes();
				if(lanes <= 1.0){
					link.getAttributes().putAttribute("hbefa_road_type", "URB/Local/60");
				} else if(lanes <= 2.0){
					link.getAttributes().putAttribute("hbefa_road_type", "URB/Trunk-City/60");
				} else if(lanes > 2.0){
					link.getAttributes().putAttribute("hbefa_road_type", "URB/MW-City/60");
				} else{
					throw new RuntimeException("NoOfLanes not properly defined");
				}
			} else if(freespeed <= 19.444444444){ //70kmh
				link.getAttributes().putAttribute("hbefa_road_type", "URB/MW-City/70");
			} else if(freespeed <= 22.222222222){ //80kmh
				link.getAttributes().putAttribute("hbefa_road_type", "URB/MW-Nat./80");
			} else if(freespeed > 22.222222222){ //faster
				link.getAttributes().putAttribute("hbefa_road_type", "RUR/MW/>130");
			} else{
				throw new RuntimeException("Link not considered...");
			}
		}

		// we do not want to run the full Controler.  In consequence, we plug together the infrastructure one needs in order to run the emissions contrib:

		EventsManager eventsManager = EventsUtils.createEventsManager();

		AbstractModule module = new AbstractModule(){
			@Override
			public void install(){
				bind( Scenario.class ).toInstance( scenario );
				bind( EventsManager.class ).toInstance( eventsManager ) ;
				bind( EmissionModule.class ) ;
			}
		};

		com.google.inject.Injector injector = Injector.createInjector( config, module );

		injector.getInstance(EmissionModule.class);

		// ---

		// add events writer into emissions event handler
		final EventWriterXML eventWriterXML = new EventWriterXML( emissionEventOutputPath );
		eventsManager.addHandler(eventWriterXML);

		// read events file into the events reader.  EmissionsModule and events writer have been added as handlers, and will act accordingly.
		EventsUtils.readEvents( eventsManager, eventsFile );

		// events writer needs to be explicitly closed, otherwise it does not work:
		eventWriterXML.closeFile();

/*		// also write vehicles and network and config as a service so we have all out files in one directory:
		new MatsimVehicleWriter( scenario.getVehicles() ).writeFile( config.controler().getOutputDirectory() + "/output_vehicles.xml.gz" );
		NetworkUtils.writeNetwork( scenario.getNetwork(), config.controler().getOutputDirectory() + "/output_network.xml.gz" );
		ConfigUtils.writeConfig( config, config.controler().getOutputDirectory() + "/output_config.xml" );
		ConfigUtils.writeMinimalConfig( config, config.controler().getOutputDirectory() + "/output_config_reduced.xml" );*/

	}

}

