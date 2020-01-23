package org.matsim.codeexamples.mdp;


import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

public class Experience {
    private MDPState odlState;

    private Id<Link> action;

    private Double reward;

    private MDPState newState;

    public Experience(MDPState odlState, Id<Link> action, Double reward, MDPState newState) {
        this.odlState = odlState;
        this.action = action;
        this.reward = reward;
        this.newState = newState;
    }

    public MDPState getOdlState() {
        return odlState;
    }

    public void setOdlState(MDPState odlState) {
        this.odlState = odlState;
    }

    public Id<Link> getAction() {
        return action;
    }

    public void setAction(Id<Link> action) {
        this.action = action;
    }

    public Double getReward() {
        return reward;
    }

    public void setReward(Double reward) {
        this.reward = reward;
    }

    public MDPState getNewState() {
        return newState;
    }

    public void setNewState(MDPState newState) {
        this.newState = newState;
    }

    @Override
    public String toString() {
        return "Experience{" +
                "odlState=" + odlState +
                ", action=" + action +
                ", reward=" + reward +
                ", newState=" + newState +
                '}';
    }
}
