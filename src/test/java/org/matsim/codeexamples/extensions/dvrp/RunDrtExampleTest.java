package org.matsim.codeexamples.extensions.dvrp;

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

public class RunDrtExampleTest{
	private static final Logger log = LogManager.getLogger( RunDrtExampleTest.class );
	@RegisterExtension public MatsimTestUtils utils = new MatsimTestUtils();

	@Test
	void testMain(){
		try{
			RunDrtExample.run(false,
					"scenarios/multi_mode_one_shared_taxi/multi_mode_one_shared_taxi_config.xml"
					, "--config:controler.outputDirectory=" + utils.getOutputDirectory()
					, "--config:controler.lastIteration=1");
			{
				String expected = utils.getInputDirectory() + "/output_events.xml.gz" ;
				String actual = utils.getOutputDirectory() + "/output_events.xml.gz" ;
				EventsUtils.compareEventsFiles( expected, actual );
			}
			{
				final Population expected = PopulationUtils.createPopulation( ConfigUtils.createConfig() );
				PopulationUtils.readPopulation( expected, utils.getInputDirectory() + "/output_plans.xml.gz" );
				final Population actual = PopulationUtils.createPopulation( ConfigUtils.createConfig() );
				PopulationUtils.readPopulation( actual, utils.getOutputDirectory() + "/output_plans.xml.gz" );
				PopulationUtils.comparePopulations( expected, actual ) ;
			}


		} catch ( Exception ee ) {
			log.fatal(ee) ;
			fail() ;
		}
	}
}
