package org.matsim.codeexamples.mdp;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.scoring.functions.CharyparNagelLegScoring;
import org.matsim.core.scoring.functions.ModeUtilityParameters;
import org.matsim.core.scoring.functions.ScoringParameters;
import org.matsim.core.utils.misc.Time;

import java.util.*;


public class CustomScoreLeg {

    private static final Logger log = Logger.getLogger(CustomScoreLeg.class);
    protected double score;
    protected final ScoringParameters params;
    protected Network network;
    private boolean nextEnterVehicleIsFirstOfTrip;
    private boolean nextStartPtLegIsFirstOfTrip;
    private boolean currentLegIsPtLeg;
    private double lastActivityEndTime;
    private Set<String> modesAlreadyConsideredForDailyConstants;
    private static int ccc = 0;

    public CustomScoreLeg(ScoringParameters params, Network network) {
        this.params = params;
        this.network = network;
    }

    public double getScore() {
        return this.score;
    }

    protected double calcLegScore(double departureTime, double arrivalTime, Leg leg) {
        double tmpScore = 0.0D;
        double travelTime = arrivalTime - departureTime;
        ModeUtilityParameters modeParams = (ModeUtilityParameters)this.params.modeParams.get(leg.getMode());
        if (modeParams == null) {
            if (!leg.getMode().equals("transit_walk") && !leg.getMode().equals("access_walk") && !leg.getMode().equals("egress_walk")) {
                throw new RuntimeException("just encountered mode for which no scoring parameters are defined: " + leg.getMode());
            }

            modeParams = (ModeUtilityParameters)this.params.modeParams.get("walk");
        }

        tmpScore += travelTime * modeParams.marginalUtilityOfTraveling_s;
        if (modeParams.marginalUtilityOfDistance_m != 0.0D || modeParams.monetaryDistanceCostRate != 0.0D) {
            Route route = leg.getRoute();
            double dist = route.getDistance();
            if (Double.isNaN(dist) && ccc < 10) {
                ++ccc;
                Logger.getLogger(this.getClass()).warn("distance is NaN. Will make score of this plan NaN. Possible reason: Simulation does not report a distance for this trip. Possible reason for that: mode is teleported and router does not write distance into plan.  Needs to be fixed or these plans will die out.");
                if (ccc == 10) {
                    Logger.getLogger(this.getClass()).warn(" Future occurences of this logging statement are suppressed.");
                }
            }

            tmpScore += modeParams.marginalUtilityOfDistance_m * dist;
            tmpScore += modeParams.monetaryDistanceCostRate * this.params.marginalUtilityOfMoney * dist;
        }

        tmpScore += modeParams.constant;
        if (!this.modesAlreadyConsideredForDailyConstants.contains(leg.getMode())) {
            tmpScore += modeParams.dailyUtilityConstant + modeParams.dailyMoneyConstant * this.params.marginalUtilityOfMoney;
            this.modesAlreadyConsideredForDailyConstants.add(leg.getMode());
        }

        return tmpScore;
    }

    public void handleLeg(Leg leg) {
        Gbl.assertIf(!Time.isUndefinedTime(leg.getDepartureTime()));
        Gbl.assertIf(!Time.isUndefinedTime(leg.getTravelTime()));
        double legScore = this.calcLegScore(leg.getDepartureTime(), leg.getDepartureTime() + leg.getTravelTime(), leg);
        if (Double.isNaN(legScore)) {
            log.error("dpTime=" + leg.getDepartureTime() + "; ttime=" + leg.getTravelTime() + "; leg=" + leg);
            throw new RuntimeException("score is NaN");
        } else {
            this.score += legScore;
        }
    }
}
