package org.matsim.codeexamples.mdp;


import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.mobsim.framework.*;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.QSimBuilder;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicleImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.examples.ExamplesUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.VehiclesFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.net.URL;
import java.util.Collection;

public class RunMDPExample {

    public static void main(String[] args) {
        Config config = null;

        URL url = IOUtils.newUrl(ExamplesUtils.getTestScenarioURL("equil"), "config.xml");
        config = ConfigUtils.loadConfig(url);
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.controler().setLastIteration(2);

        Scenario scenario = ScenarioUtils.loadScenario(config);

        final Controler controler = new Controler(scenario);

        final StateMonitor stateMonitor  = new StateMonitor();

        StateTransitionCalculator stateTransitionCalculator = new StateTransitionCalculator();

        controler.getEvents().addHandler(stateMonitor);
        controler.getEvents().addHandler(stateTransitionCalculator);


        controler.addOverridingModule(new AbstractModule() {

            @Override
            public void install() {

                bindMobsim().toProvider(new Provider<Mobsim>() {
                    @Inject Scenario sc;
                    @Inject EventsManager eventsManager;
                    @Override
                    public Mobsim get() {

                        final QSim qsim = new QSimBuilder(getConfig()).useDefaults().build(scenario, eventsManager);

                        qsim.addAgentSource(new AgentSource() {
                            @Override
                            public void insertAgentsIntoMobsim() {
                                for(int i = 1; i <= 1;i++) {
                                    final Id<Link> startingLinkId = Id.createLinkId(i);
                                    final Id<Vehicle> vehicleId = Id.create("myVeh"+String.valueOf(i), Vehicle.class);
                                    final VehicleType vehType = VehicleUtils.getDefaultVehicleType();
                                    final VehiclesFactory vehiclesFactory = VehicleUtils.getFactory();
                                    final Vehicle vehicle = vehiclesFactory.createVehicle(vehicleId, vehType);
                                    final QVehicle qveh = new QVehicleImpl(vehicle);

                                    qsim.addParkedVehicle(qveh, startingLinkId);

                                    IPolicy iPolicy = new Policy(null, sc, qsim.getSimTimer());

                                    String agentName = "MyAgent"+String.valueOf(i);

                                    MobsimAgent ag = new CustomMobSimAgent(iPolicy,
                                                                          qsim.getSimTimer(),
                                                                          sc,
                                                                          vehicleId,
                                                                          startingLinkId,
                                                                          agentName,
                                                                          stateMonitor);

                                    qsim.insertAgentIntoMobsim(ag);
                                }

                            }
                        });
                        return qsim;
                    }
                });

            }
        });

        controler.run();

    }
}
