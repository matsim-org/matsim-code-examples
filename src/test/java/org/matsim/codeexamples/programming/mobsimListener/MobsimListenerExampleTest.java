/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * DefaultControlerModules.java
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2014 by the members listed in the COPYING, *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 */
package org.matsim.codeexamples.programming.mobsimListener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.matsim.codeexamples.mobsim.mobsimListener.RunMobsimListenerExample;
import org.matsim.testcases.MatsimTestUtils;

/**
 * @author tthunig
 *
 */
public class MobsimListenerExampleTest {

	@RegisterExtension public MatsimTestUtils utils = new MatsimTestUtils() ;

	/**
	 * Test method for {@link RunMobsimListenerExample}
	 */
	@SuppressWarnings("static-method")
	@Test
	final void testMain() {
		RunMobsimListenerExample.outputDirectory = utils.getOutputDirectory() + "/mobsim-listener";
		RunMobsimListenerExample.main(null);
	}

	
}
