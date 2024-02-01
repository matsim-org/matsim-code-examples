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

package org.matsim.codeexamples.strategies.planStrategyForRemoval;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;

/**
* @author ikaddoura
*/
public class RunPlanSelectorForRemovalExample {

	private static final String SELECTOR_NAME = "selectorName";

	public static void main(String[] args) {
		
		Config config = ConfigUtils.createConfig();	
		config.controller().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controller().setLastIteration(1);
		
		config.replanning().setPlanSelectorForRemoval( SELECTOR_NAME );
		
		Controler controler = new Controler(config);

		controler.addOverridingModule(new AbstractModule(){
			
			@Override
			public void install() {
				if (getConfig().replanning().getPlanSelectorForRemoval().equals(SELECTOR_NAME)) {
					bindPlanSelectorForRemoval().toProvider(MyExpBetaPlanChangerForRemovalProvider.class);
				}
			}
		});
		
		controler.run();
		
	}

}

