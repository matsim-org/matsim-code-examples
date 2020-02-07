package org.matsim.codeexamples.mdp;

import org.matsim.api.core.v01.events.Event;
import org.matsim.core.events.handler.BasicEventHandler;

public class ModelUpdateMonitor implements BasicEventHandler {

    ActorCriticInterface actorCriticInterface;

    public ModelUpdateMonitor(ActorCriticInterface actorCriticInterface) {
        this.actorCriticInterface = actorCriticInterface;
    }

    @Override
    public void handleEvent(Event event) {

    }

    @Override
    public void reset(int iteration) {
        // RUN BACKPROPAGATION AFTER EVERY ITERATION
        if(iteration == 0) return;
        this.actorCriticInterface.updateModels();
    }
}
