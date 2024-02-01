package org.matsim.codeexamples.extensions.emissions;

import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.testcases.MatsimTestUtils;

import static org.junit.jupiter.api.Assertions.fail;

public class RunAverageEmissionToolOfflineExampleTest{
	private static final Logger log = LogManager.getLogger( RunAverageEmissionToolOfflineExampleTest.class );
	@RegisterExtension public MatsimTestUtils utils = new MatsimTestUtils();

	@Test
	void testMain(){

		try{
			RunAverageEmissionToolOfflineExample.main( new String []{ "./scenarios/sampleScenario/testv2_Vehv2/config_average.xml"
					, "--config:controler.outputDirectory=" + utils.getOutputDirectory()
			} );

			{
				String expected = utils.getInputDirectory() + RunAverageEmissionToolOfflineExample.emissionEventOutputFileName;
				String actual = utils.getOutputDirectory() + RunAverageEmissionToolOfflineExample.emissionEventOutputFileName;
				EventsUtils.compareEventsFiles( expected, actual );
			}
//			{
//				final Population expected = PopulationUtils.createPopulation( ConfigUtils.createConfig() );
//				PopulationUtils.readPopulation( expected, utils.getInputDirectory() + "/output_plans.xml.gz" );
//				final Population actual = PopulationUtils.createPopulation( ConfigUtils.createConfig() );
//				PopulationUtils.readPopulation( actual, utils.getOutputDirectory() + "/output_plans.xml.gz" );
//				PopulationUtils.comparePopulations( expected, actual ) ;
//			}

		} catch ( Exception ee ) {
			log.fatal(ee) ;
			fail() ;
		}


	}
}
