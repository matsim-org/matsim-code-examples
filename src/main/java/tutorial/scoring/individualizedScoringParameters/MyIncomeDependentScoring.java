package tutorial.scoring.individualizedScoringParameters;

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.functions.ScoringParameters;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;

/**
 * @author thibautd
 */
class MyIncomeDependentScoring implements ScoringParametersForPerson {
	private static final Logger log = Logger.getLogger(MyIncomeDependentScoring.class) ;
	
	private final Scenario scenario;

	@Inject
	public MyIncomeDependentScoring(final Scenario scenario ) {
		this.scenario = scenario;
	}

	@Override
	public ScoringParameters getScoringParameters(Person person) {
		
		// Since we need to return a variable of type ScoringParameters, we would expect something like
		//    ScoringParameters params = new ...
		// There is, however, no public constructor.  Instead, there is a builder.  So we use that.
		// (A major reason to use a builder is to avoid a constructor with a long argument list.)
		final ScoringParameters.Builder builder = new ScoringParameters.Builder(scenario, person.getId());
		
		double income = (double) person.getAttributes().getAttribute( RunIndividualizedScoringExample.NET_INCOME_PER_MONTH );
		final double marginalUtilityOfMoney = 2000. / income;
		log.warn( "marginalUtilityOfMoney=" + marginalUtilityOfMoney ) ;
		builder.setMarginalUtilityOfMoney(marginalUtilityOfMoney) ;

		final ScoringParameters params = builder.build();

		return params;
	}
}
