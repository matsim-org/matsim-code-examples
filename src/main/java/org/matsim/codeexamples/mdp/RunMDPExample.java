package org.matsim.codeexamples.mdp;


import com.jogamp.common.util.ArrayHashMap;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.*;
import org.matsim.codeexamples.mdp.event.handlers.CustomScoring;
import org.matsim.codeexamples.mdp.event.handlers.StateMonitor;
import org.matsim.codeexamples.mdp.event.handlers.StateTransitionCalculator;
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
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.functions.ScoringParameters;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.examples.ExamplesUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.VehiclesFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import  org.matsim.codeexamples.mdp.CustomMobSimAgent;

public class RunMDPExample {

    private static Logger log = Logger.getLogger("RunMDPExample");

    private static Scenario scenario;

    public static void main(String[] args) {
        Config config = null;

        URL url = IOUtils.newUrl(ExamplesUtils.getTestScenarioURL("equil"), "config.xml");
        config = ConfigUtils.loadConfig(url);
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.controler().setLastIteration(0);

        scenario = ScenarioUtils.loadScenario(config);
        final Controler controler = new Controler(scenario);

        final StateMonitor stateMonitor  = new StateMonitor();

        StateTransitionCalculator stateTransitionCalculator = new StateTransitionCalculator();

        final ScoringParameters params = new ScoringParameters.Builder(scenario, Id.createPersonId(1)).build();

        CustomScoreLeg customScoreLeg = new CustomScoreLeg(params,scenario.getNetwork());

        final CustomScoring customScoring = new CustomScoring(null, scenario,null,customScoreLeg);

        final Map<String, ScoringParameters> hackStuff = new HashMap<>();

        controler.getEvents().addHandler(stateMonitor);
//        controler.getEvents().addHandler(stateTransitionCalculator);

//        ActorCriticInterface actorCriticInterface = new ActorCriticInterface();
        //State includes number of vehicles in 23 links, work, home, the current link id and time of day.
//        actorCriticInterface.initializeModels(27,23);

//        final ModelUpdateMonitor modelUpdateMonitor = new ModelUpdateMonitor(actorCriticInterface);

//        controler.getEvents().addHandler(modelUpdateMonitor);

        Person person = scenario.getPopulation().getPersons().get(Id.createPersonId(1));
        scenario.getPopulation().getPersons().clear();
        scenario.getPopulation().addPerson(person);


        log.info("THE SIZE OF THE POPULATION IS "+scenario.getPopulation().getPersons().size());

        controler.addOverridingModule(new AbstractModule() {

            @Override
            public void install() {


                bindMobsim().toProvider(new Provider<Mobsim>() {
                    @Inject Scenario sc;
                    @Inject EventsManager eventsManager;

                    @Override
                    public Mobsim get() {

                        final QSim qsim = new QSimBuilder(getConfig()).useDefaults().build(scenario, eventsManager);
                        customScoring.setMobsimTimer(qsim.getSimTimer());
                        customScoring.setTripRouter(controler.getTripRouterProvider().get());
                        qsim.addAgentSource(new AgentSource() {
                            @Override
                            public void insertAgentsIntoMobsim() {
                                for(int i = 1; i <= 70;i++) {
                                    final Id<Link> startingLinkId = Id.createLinkId(1);
                                    final Id<Vehicle> vehicleId = Id.create("myVeh"+String.valueOf(i), Vehicle.class);
                                    final VehicleType vehType = VehicleUtils.getDefaultVehicleType();
                                    final VehiclesFactory vehiclesFactory = VehicleUtils.getFactory();
                                    final Vehicle vehicle = vehiclesFactory.createVehicle(vehicleId, vehType);
                                    final QVehicle qveh = new QVehicleImpl(vehicle);


                                    qsim.addParkedVehicle(qveh, startingLinkId);



                                    IPolicy iPolicy = new PNPolicy(new PolicyNetworkInterface(),qsim.getSimTimer(),sc);

                                    String agentName = "MyAgent"+String.valueOf(i);

                                    MobsimAgent ag = new CustomMobSimAgent(iPolicy,
                                                                          qsim.getSimTimer(),
                                                                          sc,
                                                                          vehicleId,
                                                                          startingLinkId,
                                                                          agentName,
                                                                          stateMonitor,
                                                                          customScoring,
                                            eventsManager );


                                    qsim.insertAgentIntoMobsim(ag);
                                }

                            }
                        });


                        ScoringParameters scoringParameters =  new ScoringParameters.Builder(sc,Id.createPersonId(1)).build();

                        MetricCollector metricCollector = new MetricCollector(stateMonitor,
                                                                            sc,customScoring,
                                                                            controler.getTripRouterProvider().get(),
                                                                            qsim.getAgents(),
                                                                            scoringParameters);
                        qsim.addQueueSimulationListeners(metricCollector);
                        return qsim;
                    }
                });

            }
        });
        controler.getEvents().addHandler(customScoring);
        controler.run();

    }

    private static void copyPlans(Plan destPlan, Plan srcPlan) {
        for(PlanElement planElement: srcPlan.getPlanElements()) {
            if(planElement instanceof Activity) {
                destPlan.addActivity((Activity)planElement);
            }
            if(planElement instanceof  Leg) {
                destPlan.addLeg((Leg)planElement);
            }
        }

    }
}
