package org.matsim.codeexamples.scoring.aaIncomeDependentScoring;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.examples.ExamplesUtils;
import org.matsim.testcases.MatsimTestUtils;

import static org.junit.jupiter.api.Assertions.fail;

public class RunIncomeDependentScoringExampleTest{
	private static final Logger log = LogManager.getLogger( RunIncomeDependentScoringExampleTest.class );
	@RegisterExtension
	public MatsimTestUtils utils = new MatsimTestUtils();

	@Test
	void testMain(){
		try{
			RunIncomeDependentScoringExample.main( new String []{ IOUtils.extendUrl( ExamplesUtils.getTestScenarioURL( "equil" ), "config.xml" ).toString()
					, "--config:controler.outputDirectory=" + utils.getOutputDirectory()
					, "--config:controler.lastIteration=0"
			} );
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

				for( Person expectedPerson : expected.getPersons().values() ){
					Person actualPerson = actual.getPersons().get( Id.createPersonId( expectedPerson.getId() ) );
					Assertions.assertEquals( expectedPerson.getSelectedPlan().getScore(), actualPerson.getSelectedPlan().getScore() );
				}
			}


		} catch ( Exception ee ) {
			log.fatal(ee) ;
			fail() ;
		}

	}
}
