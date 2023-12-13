package org.matsim.codeexamples.extensions.drt;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.matsim.testcases.MatsimTestUtils;

public class RunMelunPrebookingTest {
	@Rule
	public MatsimTestUtils utils = new MatsimTestUtils();

	@Test
	public void runExample() {
		RunMelunPrebooking.RunSettings settings = new RunMelunPrebooking.RunSettings();

		settings.prebookingShare = 0.5;
		settings.submissionSlack = 3600.0;
		settings.enableExclusivity = true;

		RunMelunPrebooking.runSingle(new File(RunMelunPrebooking.DEFAULT_POPULATION_PATH),
				new File(RunMelunPrebooking.DEFAULT_NETWORK_PATH), new File(utils.getOutputDirectory()), settings);
	}
}
