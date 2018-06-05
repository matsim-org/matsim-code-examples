package tutorial.scoring.individualizedScoringParameters;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.scoring.functions.CharyparNagelScoringFunctionFactory;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.examples.ExamplesUtils;

/**
 * @author thibautd
 */
class RunIndividualizedScoringExample {
	static final String NET_INCOME_PER_MONTH="netIncomePerMonth" ;

	public static void main(String... args) {
		final Config config = ConfigUtils.loadConfig(IOUtils.newUrl(ExamplesUtils.getTestScenarioURL("equil"), "config.xml"));
		config.controler().setOutputDirectory( "output/exampleIndividualScores/");

		// ---

		final Scenario scenario = ScenarioUtils.loadScenario(config) ;

		// give each person an individual income:
		for (Person person : scenario.getPopulation().getPersons().values() ) {
			person.getAttributes().putAttribute(NET_INCOME_PER_MONTH, 500. + MatsimRandom.getRandom().nextDouble() * 4000. ) ;
		}

		// ---

		final Controler controler = new Controler( scenario );
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
			    bindScoringFunctionFactory().to(CharyparNagelScoringFunctionFactory.class);
			    bind(ScoringParametersForPerson.class).to(ExampleIndividualizedScoringParametersPerPerson.class);
			}
		});
		controler.run();
	}
}
