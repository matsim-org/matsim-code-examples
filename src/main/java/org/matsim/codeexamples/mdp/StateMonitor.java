package org.matsim.codeexamples.mdp;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.events.handler.BasicEventHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

// Keeps track of the number of vehicles in each road link
public class StateMonitor implements BasicEventHandler {
    private static Logger log = Logger.getLogger("StateMonitor");

    Map<Id<Link>,Integer> nVehs = new HashMap<>() ;

    @Override
    public void handleEvent(Event event) {
        Id<Link> linkId = null ;

        if(event instanceof ActivityStartEvent) {
            String activityType = ((ActivityStartEvent)event).getActType();
           if(activityType.equals("h")) {
               linkId = Id.createLinkId(24); // assign link id 24 if it is home
           }
           else if(activityType.equals("w")) {
               linkId = Id.createLinkId(25); // assign link id 25 if it is work
           }

           if(nVehs.get(linkId) == null) {
               nVehs.put(linkId,0);
           }
           int n = nVehs.get(linkId);
           n += 1;
           nVehs.put(linkId,n);
           return;
        }

        if(event instanceof ActivityEndEvent) {
            String activityType = ((ActivityEndEvent)event).getActType();
            if(activityType.equals("h")) {
                linkId = Id.createLinkId(24); // assign link id 24 if it is home
            }
            else if(activityType.equals("w")) {
                linkId = Id.createLinkId(25); // assign link id 25 if it is work
            }

            if(nVehs.get(linkId) == null) {
               return;
            }
            int n = nVehs.get(linkId);
            n -= 1;
            nVehs.put(linkId,n);
            return;
        }

        if ( event instanceof LinkEnterEvent ) {
            linkId = ((LinkEnterEvent) event).getLinkId() ;
        } else if ( event instanceof VehicleEntersTrafficEvent ) {
            linkId = ((VehicleEntersTrafficEvent) event).getLinkId() ;
        }
        if(linkId != null) {
            if (nVehs.get(linkId) != null) {
                Integer val = nVehs.get(linkId);
                val = val + 1;
                nVehs.put(linkId, val);
            } else {
                nVehs.put(linkId, 1);
            }
            return;
        }


        String arrivalMode = TransportMode.car ;

        if ( event instanceof LinkLeaveEvent ) {
            linkId = ((LinkLeaveEvent) event).getLinkId() ;
        } else if ( event instanceof VehicleLeavesTrafficEvent ) {
            linkId = ((VehicleLeavesTrafficEvent)event).getLinkId() ;
            arrivalMode = ((VehicleLeavesTrafficEvent)event).getNetworkMode() ;
        }

        if(linkId == null) return;

        if ( arrivalMode.equals( TransportMode.car ) ) {
            if ( nVehs.get( linkId ) != null ) {
                Integer val = nVehs.get( linkId ) ;
                if(val == 0) return;
                val = val - 1;
                nVehs.put( linkId,  val ) ;
            } else {
                throw new RuntimeException("should not happen; a car should always have to enter a link before leaving it") ;
            }
        }


    }

    @Override
    public void reset(int iteration) {
        log.info("New iteration starting. Resetting state vector");
        //reset state
        nVehs.clear();

    }

    public List<Integer> getState() {
        List<Integer> stateVector = new ArrayList<>();

        for(int i = 1;i <= 25;i++) {
            Id<Link> linkId = Id.createLinkId(i);
            if(nVehs.get(linkId) == null) {
                stateVector.add(0);
            }
            else {
                stateVector.add(nVehs.get(linkId));
            }

        }
        return stateVector;
    }
}
