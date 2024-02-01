/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
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

package org.matsim.codeexamples.programming.planStrategyForRemoval;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.matsim.codeexamples.strategies.planStrategyForRemoval.RunPlanSelectorForRemovalExample;
import org.matsim.testcases.MatsimTestUtils;

/**
* @author ikaddoura
*/

public class RunPlanStrategyForRemovalExampleTest {

	@RegisterExtension public MatsimTestUtils utils = new MatsimTestUtils() ;

	@Test
	final void testMain() {
		
		try {
			RunPlanSelectorForRemovalExample.main(null);
		} catch(Exception e) {
			Assertions.fail(e.toString());
		}
	}

}

