package tutorial.analysis;

import org.matsim.analysis.TransportPlanningMainModeIdentifier;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class RunModeChoiceAnalysisExample {
	// yyyy I did not test this; please report.  kai, aug'18
	
	public static void main( String [] args ) {
		MainModeIdentifier mainModeIdentifier = new TransportPlanningMainModeIdentifier() ;
		StageActivityTypes stageActivities = new StageActivityTypesImpl(  ) ;
		
		
		Config config = ConfigUtils.createConfig() ;
		
		config.plans().setInputFile( null );  // replace by some input file
		
		Scenario scenario = ScenarioUtils.loadScenario( config ) ;
		
		Population population = scenario.getPopulation() ;
		
		Map<String,Double> modeCnt = new TreeMap<>() ;
		for ( Person person : population.getPersons().values()) {
			Plan plan = person.getSelectedPlan() ;
			List<TripStructureUtils.Trip> trips = TripStructureUtils.getTrips(plan, stageActivities) ;
			for ( TripStructureUtils.Trip trip : trips ) {
				String mode = mainModeIdentifier.identifyMainMode( trip.getTripElements() ) ;
				
				Double cnt = modeCnt.get( mode );
				if ( cnt==null ) {
					cnt = 0. ;
				}
				modeCnt.put( mode, cnt + 1 ) ;
			}
		}
		
		for ( Map.Entry<String, Double> entry : modeCnt.entrySet() ) {
			System.out.println( entry.getKey() + "=" + entry.getValue() ) ;
		}
		
	}
}
