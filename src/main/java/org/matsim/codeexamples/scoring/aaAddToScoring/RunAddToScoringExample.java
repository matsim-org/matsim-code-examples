package org.matsim.codeexamples.scoring.aaAddToScoring;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonScoreEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.ScoringEvent;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.core.events.handler.EventHandler;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.*;
import org.matsim.vehicles.Vehicle;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class RunAddToScoringExample{
	static final String DISLIKES_LEAVING_EARLY_AND_COMING_HOME_LATE = "DISLIKES_LEAVING_EARLY_AND_COMING_HOME_LATE";


//	private static final class RainScoringFunctionFactory implements ScoringFunctionFactory {
//		private final  Scenario scenario;
//
//		private RainScoringFunctionFactory( Scenario scenario ) {
//			this.scenario = scenario;
//		}
//
//		@Override
//		public ScoringFunction createNewScoringFunction(Person person) {
//			SumScoringFunction sumScoringFunction = new SumScoringFunction();
//
//			// Score activities, legs, payments and being stuck
//			// with the default MATSim scoring based on utility parameters in the config file.
//			final ScoringParameters params = new ScoringParameters.Builder(scenario, person).build();
//			sumScoringFunction.addScoringFunction(new CharyparNagelActivityScoring(params));
//			sumScoringFunction.addScoringFunction(new CharyparNagelLegScoring(params, scenario.getNetwork()));
//			sumScoringFunction.addScoringFunction(new CharyparNagelMoneyScoring(params));
//			sumScoringFunction.addScoringFunction(new CharyparNagelAgentStuckScoring(params));
//
////			if ((Boolean) personAttributes.getAttribute(person.getId().toString(), DISLIKES_LEAVING_EARLY_AND_COMING_HOME_LATE)) {
//			if ((Boolean) PopulationUtils.getPersonAttribute( person, DISLIKES_LEAVING_EARLY_AND_COMING_HOME_LATE ) ) {
//				sumScoringFunction.addScoringFunction(new ExtremeTimePenaltyScoring() );
//			}
//
//			sumScoringFunction.addScoringFunction(new RainScoring() );
//
//			return sumScoringFunction;
//		}
//	}

	public static void main(String[] args) {
		final Scenario scenario;
		if ( args==null || args.length==0 || args[0]==null ){
			String configFile = "scenarios/equil/example5-config.xml";
			scenario = ScenarioUtils.loadScenario( ConfigUtils.loadConfig( configFile ) );
		} else {
			scenario = ScenarioUtils.loadScenario( ConfigUtils.loadConfig( args ) );
		}

		// Every second person gets a special property which influences their score.
		// ObjectAttributes can be written to and read from files, so in reality,
		// this would come from a census, a pre-processing step, or from anywhere else, 
		// but for this example I just make it up.
//		final ObjectAttributes personAttributes = scenario.getPopulation().getPersonAttributes();
		for (Person person : scenario.getPopulation().getPersons().values()) {
			if (Integer.parseInt(person.getId().toString()) % 2 == 0) {
//				personAttributes.putAttribute(person.getId().toString(), DISLIKES_LEAVING_EARLY_AND_COMING_HOME_LATE, true);
				PopulationUtils.putPersonAttribute( person, DISLIKES_LEAVING_EARLY_AND_COMING_HOME_LATE, true );
			} else {
//				personAttributes.putAttribute(person.getId().toString(), DISLIKES_LEAVING_EARLY_AND_COMING_HOME_LATE, false);
				PopulationUtils.putPersonAttribute( person, DISLIKES_LEAVING_EARLY_AND_COMING_HOME_LATE, false );
			}
		}

		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				// We add a class which reacts on people who enter a link and lets it rain on them
				// if we are within a certain time window.
				// The class registers itself as an EventHandler and also produces events by itself.
				bind(RainEngine.class).asEagerSingleton();
			}
		});

//		controler.addOverridingModule( new AbstractModule(){
//			@Override public void install() {
//				this.bindScoringFunctionFactory().toInstance(new RainScoringFunctionFactory(scenario) ) ;
//
//			}
//
//		});
		controler.run();
	}

	static class RainEngine implements PersonEntersVehicleEventHandler, LinkEnterEventHandler {

		private EventsManager eventsManager;

		private Map<Id<Vehicle>, Id<Person>> vehicle2driver = new HashMap<>();

		@Inject
		RainEngine(EventsManager eventsManager) {
			this.eventsManager = eventsManager;
			this.eventsManager.addHandler(this);
		}

		@Override 
		public void reset(int iteration) {}

		@Override
		public void handleEvent(PersonEntersVehicleEvent event) {
			vehicle2driver.put(event.getVehicleId(), event.getPersonId());
		}

		@Override
		public void handleEvent(LinkEnterEvent event) {
			if (rainingAt(event.getTime(), event.getLinkId())) {
				final Id<Person> driverId = vehicle2driver.get( event.getVehicleId() );
				eventsManager.processEvent(new RainOnPersonEvent(event.getTime(), driverId ) );
				eventsManager.processEvent( new PersonScoreEvent( event.getTime(), driverId, -1000., "rainPenalty" ));
			}
		}

		// It starts raining on link 1 at 7:30.
		private boolean rainingAt(double time, Id<Link> linkId) {
			if (time > (7.5 * 60.0 * 60.0) && linkId.toString().equals("1")) {
				return true;
			} else {
				return false;
			}
		}

	}

}
