package org.matsim.codeexamples.mdp;


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
import org.matsim.core.scoring.functions.ScoringParameters;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.examples.ExamplesUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.VehiclesFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.net.URL;
import java.util.logging.Logger;

public class RunMDPExample {

    private static Logger log = Logger.getLogger("RunMDPExample");

    public static void main(String[] args) {
        Config config = null;

        URL url = IOUtils.newUrl(ExamplesUtils.getTestScenarioURL("equil"), "config.xml");
        config = ConfigUtils.loadConfig(url);
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.controler().setLastIteration(20);

        Scenario scenario = ScenarioUtils.loadScenario(config);


        final Controler controler = new Controler(scenario);

        final StateMonitor stateMonitor  = new StateMonitor();

        StateTransitionCalculator stateTransitionCalculator = new StateTransitionCalculator();

        final ScoringParameters params = new ScoringParameters.Builder(scenario, Id.createPersonId(1)).build();

        CustomScoreLeg customScoreLeg = new CustomScoreLeg(params,scenario.getNetwork());

        final CustomScoring customScoring = new CustomScoring(null, scenario,null,customScoreLeg);

        controler.getEvents().addHandler(stateMonitor);
        controler.getEvents().addHandler(stateTransitionCalculator);

        ActorCriticInterface actorCriticInterface = new ActorCriticInterface();
        //State includes number of vehicles in 23 links, work, home, the current link id and time of day.
        actorCriticInterface.initializeModels(27,23);

        final ModelUpdateMonitor modelUpdateMonitor = new ModelUpdateMonitor(actorCriticInterface);

        controler.getEvents().addHandler(modelUpdateMonitor);


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
                                for(int i = 1; i <= 1;i++) {
                                    final Id<Link> startingLinkId = Id.createLinkId(i);
                                    final Id<Vehicle> vehicleId = Id.create("myVeh"+String.valueOf(i), Vehicle.class);
                                    final VehicleType vehType = VehicleUtils.getDefaultVehicleType();
                                    final VehiclesFactory vehiclesFactory = VehicleUtils.getFactory();
                                    final Vehicle vehicle = vehiclesFactory.createVehicle(vehicleId, vehType);
                                    final QVehicle qveh = new QVehicleImpl(vehicle);


                                    qsim.addParkedVehicle(qveh, startingLinkId);



                                    IPolicy iPolicy = new Policy(null, sc, qsim.getSimTimer(),actorCriticInterface );

                                    String agentName = "MyAgent"+String.valueOf(i);

                                    MobsimAgent ag = new CustomMobSimAgent(iPolicy,
                                                                          qsim.getSimTimer(),
                                                                          sc,
                                                                          vehicleId,
                                                                          startingLinkId,
                                                                          agentName,
                                                                          stateMonitor,
                                                                          customScoring);

//                                    PopulationFactory populationFactory = sc.getPopulation().getFactory();
//
//                                    //CREATE CORRESPONDING PERSON OBJECT FOR THE AGENT
//                                    Person person = populationFactory.createPerson(ag.getId());
//                                    Plan plan = populationFactory.createPlan();
//
//                                    //Copy selected plan of person id 1 to our plan
//                                    copyPlans(plan,sc.getPopulation().getPersons().get(Id.createPersonId(1)).getSelectedPlan());
//
//                                    plan.setPerson(person);
//                                    person.addPlan(plan);
//                                    person.setSelectedPlan(plan);
//
////                                    //ADD IT TO POPULATION
////                                    sc.getPopulation().addPerson(person);
//
//                                    ((CustomMobSimAgent)ag).setPerson(person);

                                    qsim.insertAgentIntoMobsim(ag);
                                }

                            }
                        });
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
