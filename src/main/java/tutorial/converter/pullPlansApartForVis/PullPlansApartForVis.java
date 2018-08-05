package tutorial.converter.pullPlansApartForVis;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.ScenarioUtils;

class PullPlansApartForVis {
	
	public static void main ( String [] args ) {
	
		String filename = "/Users/kainagel/git/berlin-matsim/output-from-1pct-full-w-diversity-2018-07-02/b5_1.output_plans.xml.gz" ;
		
		Config config = ConfigUtils.createConfig() ;
		config.plans().setInputFile( filename );
		
		Scenario scenario = ScenarioUtils.loadScenario( config ) ;
		Scenario newScenario = ScenarioUtils.createScenario( ConfigUtils.createConfig() ) ;
		Population newPop = newScenario.getPopulation() ;
		
		for ( Person person : scenario.getPopulation().getPersons().values() ) {
			int ii = 0 ;
			for ( Plan plan : person.getPlans()) {
				Person newPerson = newPop.getFactory().createPerson( Id.createPersonId( person.getId().toString() + "_" + Integer.toString( ii ) ) ) ;
				newPerson.addPlan( plan ) ;
				newPop.addPerson( newPerson );
				ii++ ;
			}
		}
		
		PopulationUtils.writePopulation( newPop, "pop.xml.gz");
	
	}
	
}
