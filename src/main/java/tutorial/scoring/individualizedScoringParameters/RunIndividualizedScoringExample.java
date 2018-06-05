package tutorial.scoring.individualizedScoringParameters;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scoring.functions.CharyparNagelScoringFunctionFactory;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.examples.ExamplesUtils;

/**
 * @author thibautd
 */
public class RunIndividualizedScoringExample {
	public static void main(String... args) {
		final Config config = ConfigUtils.loadConfig(IOUtils.newUrl(ExamplesUtils.getTestScenarioURL("equil"), "config.xml"));
		config.controler().setOutputDirectory( "output/exampleIndividualScores/");

		final Controler controler = new Controler( config );
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
