package org.matsim.codeexamples.mdp.event.handlers;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.internal.HasPersonId;
import org.matsim.core.events.handler.BasicEventHandler;

import java.util.logging.Logger;

public class CustomScoring implements BasicEventHandler {

    private static Logger log = Logger.getLogger("BasicEventHandler");

    @Override
    public void handleEvent(Event event) {
        if(event instanceof HasPersonId) {
            Id<Person> personId = ((HasPersonId) event).getPersonId();
            if(personId.toString().startsWith("MyAgent")) {
                log.info("YOOOOOOO FOUND IT!!!!!!!!");
            }
        }
    }

    @Override
    public void reset(int iteration) {

    }
}
