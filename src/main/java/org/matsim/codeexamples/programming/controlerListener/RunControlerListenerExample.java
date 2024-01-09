package org.matsim.codeexamples.programming.controlerListener;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.scenario.ScenarioUtils;




public class RunControlerListenerExample {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//set a default config for convenience...
		String filename = "scenarios/equil/config-with-controlerListener.xml" ;
		
//		ControlerUtils.initializeOutputLogging();
		OutputDirectoryLogging.catchLogEntries();
		
		Config config = ConfigUtils.loadConfig(filename) ;
		config.controller().setLastIteration(1);
		
		Scenario scenario = ScenarioUtils.loadScenario(config) ;

		//Create an instance of the controler and
		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				//add an instance of this class as ControlerListener
				this.addControlerListenerBinding().to(MyControlerListener.class);

				// also bind an event handler which will be needed in the controler listener:
				this.bind(MyEventHandler.class);
			}
		});
		
		
		//call run() to start the simulation
		controler.run();
	}


}
