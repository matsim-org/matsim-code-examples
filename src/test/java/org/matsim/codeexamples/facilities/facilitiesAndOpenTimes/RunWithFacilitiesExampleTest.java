/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.codeexamples.facilities.facilitiesAndOpenTimes;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;

/**
 * @author nagel
 *
 */
public class RunWithFacilitiesExampleTest {
	private static final double EPS=0.001 ;

	/**
	 * Test method for {@link RunWithFacilitiesExample#run()}.
	 */
	@SuppressWarnings({"static-method", "javadoc"})
	@Test
	final void testRun() {
		RunWithFacilitiesExample example = new RunWithFacilitiesExample() ;
		example.run();
		Scenario scenario = example.getScenario() ;
		Map<Id<Person>, ? extends Person> persons = scenario.getPopulation().getPersons() ;
		Assertions.assertEquals( 124.84230476216275, persons.get(Id.createPersonId(1)).getSelectedPlan().getScore() , EPS ) ;
		Assertions.assertEquals( 112.84230476216275, persons.get(Id.createPersonId(2)).getSelectedPlan().getScore() , EPS ) ;
	}
}
