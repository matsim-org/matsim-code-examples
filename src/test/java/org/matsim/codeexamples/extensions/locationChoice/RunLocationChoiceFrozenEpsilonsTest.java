package org.matsim.codeexamples.extensions.locationChoice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.codeexamples.RunAbcSimpleExample;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.examples.ExamplesUtils;
import org.matsim.testcases.MatsimTestUtils;

import static org.junit.Assert.fail;

public class RunLocationChoiceFrozenEpsilonsTest{
	private static final Logger log = LogManager.getLogger( RunLocationChoiceFrozenEpsilonsTest.class ) ;

	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;

	@Test public void testMain(){
		// I don't know if this is overall doing something reasonable, thus commenting out the regression check.  kai, jun'23

		try{
			RunLocationChoiceFrozenEpsilonsExample.main( new String []{ IOUtils.extendUrl( ExamplesUtils.getTestScenarioURL( "chessboard" ), "config.xml" ).toString()
				  , "--config:controler.outputDirectory=" + utils.getOutputDirectory()
				  , "--config:controler.lastIteration=2"
			} );
//			{
//				String expected = utils.getInputDirectory() + "/output_events.xml.gz" ;
//				String actual = utils.getOutputDirectory() + "/output_events.xml.gz" ;
//				EventsUtils.compareEventsFiles( expected, actual );
//			}
//			{
//				final Population expected = PopulationUtils.createPopulation( ConfigUtils.createConfig() );
//				PopulationUtils.readPopulation( expected, utils.getInputDirectory() + "/output_plans.xml.gz" );
//				final Population actual = PopulationUtils.createPopulation( ConfigUtils.createConfig() );
//				PopulationUtils.readPopulation( actual, utils.getOutputDirectory() + "/output_plans.xml.gz" );
//				PopulationUtils.comparePopulations( expected, actual ) ;
//
//				for( Person expectedPerson : expected.getPersons().values() ){
//					Person actualPerson = actual.getPersons().get( Id.createPersonId( expectedPerson.getId() ) );
//					Assert.assertEquals( expectedPerson.getSelectedPlan().getScore(), actualPerson.getSelectedPlan().getScore() );
//				}
//			}


		} catch ( Exception ee ) {
			log.fatal(ee) ;
			fail() ;
		}
	}

}
