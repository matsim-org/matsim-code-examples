/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package tutorial.network.transformNetworkCoordinates;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

/**
 * This example demonstrates how to convert a MATSim network from one CRS to another.
 */

public class ConvertNet {

    private static final String INPUT_FILENAME = "path/to/inputnet.xml";
    private static final String OUTPUT_FILENAME = "path/to/inputnet.xml";

    //Use EPSG codes for CRS
    private static final String FROM_SYSTEM = "EPSG:3857";
    private static final String TO_SYSTEM = "EPSG:32633";

    public static void main(String[] args) {
        final Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new MatsimNetworkReader(scenario.getNetwork()).readFile(INPUT_FILENAME);
        CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(FROM_SYSTEM, TO_SYSTEM);
        scenario.getNetwork().getNodes().values().forEach(n -> n.setCoord(ct.transform(n.getCoord())));
        new NetworkWriter(scenario.getNetwork()).write(OUTPUT_FILENAME);
    }
}
