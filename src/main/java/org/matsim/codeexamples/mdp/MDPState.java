package org.matsim.codeexamples.mdp;



import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import java.util.ArrayList;
import java.util.List;

public class MDPState {

    private List<Double> stateVector = new ArrayList<>();

    public MDPState(List<Integer> stateVector, Id<Link> currentLinkId, double timeOfDay) {
        for(Integer i: stateVector) {
            this.stateVector.add((double)i);
        }
        this.stateVector.add(Double.valueOf(currentLinkId.toString()));
        this.stateVector.add(timeOfDay/(24*60*60)); //scale
    }

    public List<Double> getStateVector() {
        return this.stateVector;
    }


}
