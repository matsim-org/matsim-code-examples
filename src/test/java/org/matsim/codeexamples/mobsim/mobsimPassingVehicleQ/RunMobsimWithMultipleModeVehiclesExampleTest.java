package org.matsim.codeexamples.mobsim.mobsimPassingVehicleQ;

import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.testcases.MatsimTestUtils;

public class RunMobsimWithMultipleModeVehiclesExampleTest {
	@RegisterExtension public MatsimTestUtils utils = new MatsimTestUtils() ;

	@Test
	void test() {
		String[] args = {
			  "scenarios/equil/example5-config.xml",
			  "--config:controler.outputDirectory", utils.getOutputDirectory(),
			  "--config:controler.lastIteration=2",
			  "--config:controler.writeEventsInterval=1"
		} ;

		try{
			RunMobsimWithMultipleModeVehiclesExample.main( args );
		} catch ( Exception ee ) {
			ee.printStackTrace();
			Assertions.fail();
		}

	}

}
