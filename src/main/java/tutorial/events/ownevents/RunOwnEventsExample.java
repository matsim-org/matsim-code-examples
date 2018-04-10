package tutorial.events.ownevents;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.core.scenario.ScenarioUtils;
import tutorial.scoring.example16customscoring.RainOnPersonEvent;
import tutorial.scoring.example16customscoring.RainScoring;

import javax.inject.Inject;

/**
 * See {@link RainScoring} and related classes to see how a user-defined {@link RainOnPersonEvent} is
 * generated, thrown, and caught.
 */
class RunOwnEventsExample {
	
	public static void main(String[] args) {
		throw new RuntimeException("see javadoc of this class") ;
	}
	
}
