package org.matsim.codeexamples.mdp;



import java.util.ArrayList;
import java.util.List;

public class MDPState {

    private List<Integer> stateVector = new ArrayList<>();

    public MDPState(List<Integer> stateVector) {
        for(Integer i: stateVector) {
            this.stateVector.add(i);
        }
    }

    public List<Integer> getStateVector() {
        return this.stateVector;
    }


}
