package org.matsim.codeexamples.extensions.emissions;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.emissions.EmissionModule;
import org.matsim.contrib.emissions.EmissionUtils;
import org.matsim.contrib.emissions.utils.EmissionsConfigGroup;
import org.matsim.contrib.emissions.utils.EmissionsConfigGroup.DetailedVsAverageLookupBehavior;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.examples.ExamplesUtils;
import org.matsim.vehicles.EngineInformation;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

class RunEmissionsForPtExample{

	public static void main( String [] args ) {

		Config config = ConfigUtils.loadConfig( IOUtils.extendUrl( ExamplesUtils.getTestScenarioURL( "pt-tutorial"), "0.config.xml" ) );

		EmissionsConfigGroup emissionConfig = ConfigUtils.addOrGetModule( config, EmissionsConfigGroup.class );

		emissionConfig.setDetailedVsAverageLookupBehavior( DetailedVsAverageLookupBehavior.directlyTryAverageTable );
		// (start with simplest option and debug from there)

		// define correct emissionConfig here:
		//		emissionConfig.set...

		Scenario scenario = ScenarioUtils.loadScenario( config );

		for( VehicleType vehicleType : scenario.getTransitVehicles().getVehicleTypes().values() ){
			EngineInformation ei = vehicleType.getEngineInformation();
			VehicleUtils.setHbefaVehicleCategory( ei, "bus" ); // or whatever is in your file; maybe just set it to some truck type first
			VehicleUtils.setHbefaTechnology( ei, "..." );
			// ... define the public transport vehicle types such that there are corresponding lookups in the hbefa files.
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

