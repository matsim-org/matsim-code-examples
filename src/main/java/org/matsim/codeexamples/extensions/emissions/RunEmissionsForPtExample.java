package org.matsim.codeexamples.extensions.emissions;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.emissions.EmissionModule;
import org.matsim.contrib.emissions.HbefaVehicleCategory;
import org.matsim.contrib.emissions.example.CreateEmissionConfig;
import org.matsim.contrib.emissions.utils.EmissionsConfigGroup;
import org.matsim.contrib.emissions.utils.EmissionsConfigGroup.DetailedVsAverageLookupBehavior;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.examples.ExamplesUtils;
import org.matsim.vehicles.EngineInformation;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

/**
 *
 * An example for calculating emissions for transit vehicles while running the matsim scenario.
 *
 * @author haowu, nagel
 */
class RunEmissionsForPtExample{

	public static void main( String [] args ) {

		Config config = ConfigUtils.loadConfig( IOUtils.extendUrl( ExamplesUtils.getTestScenarioURL( "pt-tutorial"), "0.config.xml" ) );
		config.vehicles().setVehiclesFile("https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.5-10pct/input/berlin-v5-mode-vehicle-types.xml");
		config.qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData);
		config.controler().setLastIteration(3);

		EmissionsConfigGroup emissionConfig = ConfigUtils.addOrGetModule( config, EmissionsConfigGroup.class );
		{
		//default setting seems OK.
		emissionConfig.setAverageColdEmissionFactorsFile( "/Users/haowu/Documents/TSE/UAM/AMI-AirShuttle/WP3-4/emissions/hBEFA4.1-original/test_pt/EFA_ColdStart_Vehcat_2020_Average_perVehCat_Bln_carOnly.csv" );
		emissionConfig.setAverageWarmEmissionFactorsFile( "/Users/haowu/Documents/TSE/UAM/AMI-AirShuttle/WP3-4/emissions/hBEFA4.1-original/test_pt/EFA_HOT_Vehcat_2020_Average_perVehCat_Bln_carOnly.csv" );
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
		emissionConfig.setEmissionsComputationMethod(EmissionsConfigGroup.EmissionsComputationMethod.AverageSpeed);
		}

		Scenario scenario = ScenarioUtils.loadScenario( config );

		//feed info for vehicles
		// non-public transit vehicles should be considered as non-hbefa vehicles
		System.out.println("vehicles could be handled: " + scenario.getVehicles().getVehicleTypes().values().size());
		for (VehicleType type : scenario.getVehicles().getVehicleTypes().values()) {
			EngineInformation engineInformation = type.getEngineInformation();
			VehicleUtils.setHbefaVehicleCategory( engineInformation, HbefaVehicleCategory.NON_HBEFA_VEHICLE.toString());
			VehicleUtils.setHbefaTechnology( engineInformation, "average" );
			VehicleUtils.setHbefaSizeClass( engineInformation, "average" );
			VehicleUtils.setHbefaEmissionsConcept( engineInformation, "average" );
			System.out.println("handled vehicle: " + type.getId());
		}
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
			// ... define the public transport vehicle types such that there are corresponding lookups in the hbefa files.
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

		Controler controler = new Controler( scenario );

		controler.addOverridingModule( new AbstractModule(){
			@Override public void install(){
				this.bind( EmissionModule.class ).asEagerSingleton();
			}
		} );

		controler.run();

	}

}

