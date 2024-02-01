package org.matsim.codeexamples.strategies.pluggablePlanStrategyInCode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.codeexamples.strategies.pluggablePlanStrategyInCode.MyPlanSelector;
import org.matsim.core.population.PopulationUtils;

public class MyPlanSelectorTest {

	@Test
	final void selectPlanTest()
	{
		//set up 
		Person person = PopulationUtils.getFactory().createPerson(Id.create(1, Person.class));
		Plan plan0 = PopulationUtils.createPlan(person);
		Plan plan1 = PopulationUtils.createPlan(person);
		person.addPlan(plan0);
		person.addPlan(plan1);
		MyPlanSelector selector = new MyPlanSelector();
		
		//act
		Plan resultPlan = selector.selectPlan(person);
		
		//assert
		Assertions.assertEquals(plan0, resultPlan);		
	}
}
