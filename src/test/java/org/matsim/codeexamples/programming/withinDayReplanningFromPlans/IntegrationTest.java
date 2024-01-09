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
package org.matsim.codeexamples.programming.withinDayReplanningFromPlans;

import java.io.File;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.testcases.MatsimTestUtils;
import org.matsim.codeexamples.withinday.withinDayReplanningFromPlans.RunWithinDayReplanningFromPlansExample;

/**
 * @author nagel
 *
 */
public class IntegrationTest {
	@RegisterExtension public MatsimTestUtils utils = new MatsimTestUtils() ;

	/**
	 * Test method for {@link RunWithinDayReplanningFromPlansExample}
	 */
	@SuppressWarnings("static-method")
	@Test
	final void testMain() {
		final String pathname = "./output/within-day";
		try {
			IOUtils.deleteDirectoryRecursively(new File(pathname).toPath());
		} catch ( IllegalArgumentException ee ) {
			// (normally, the directory should NOT be there initially.  It might, however, be there if someone ran the main class in some other way,
			// and did not remove the directory afterwards.)
		} catch ( UncheckedIOException ee ) {
			
		}
		RunWithinDayReplanningFromPlansExample.main(null);

		IOUtils.deleteDirectoryRecursively(new File(pathname).toPath());
		// (here, the directory should have been there)
	}

}


