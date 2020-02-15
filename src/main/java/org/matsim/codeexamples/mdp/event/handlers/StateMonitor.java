package org.matsim.codeexamples.mdp.event.handlers;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.codeexamples.mdp.customclasses.ActivityRecord;
import org.matsim.codeexamples.mdp.utilities.JSONStringUtil;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.vehicles.Vehicle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

// Keeps track of the number of vehicles in each road link
public class StateMonitor implements BasicEventHandler {




    private static Logger log = Logger.getLogger("StateMonitor");

    Map<Id<Link>,Integer> nVehs = new HashMap<>();
    Map<Id<Vehicle>,Id<Link>> vehLink = new HashMap<>();
    Map<String,ActivityRecord> activityRecordMapWork = new HashMap<>();
    Map<String,ActivityRecord> activityRecordMapHome = new HashMap<>();


    private void write() throws FileNotFoundException {
        String hActivityFileRecord = "/Users/luckysonkhaidem/Desktop/hactivity.json";
        String wActivityFileRecord = "/Users/luckysonkhaidem/Desktop/wactivity.json";

        PrintWriter out = null;

        out = new PrintWriter(new FileOutputStream(new File(hActivityFileRecord)));
        out.write(JSONStringUtil.convertToJSONString(activityRecordMapHome));
        out.close();

        out = new PrintWriter(new FileOutputStream(new File(wActivityFileRecord)));
        out.write(JSONStringUtil.convertToJSONString(activityRecordMapWork));
        out.close();
    }

    @Override
    public void handleEvent(Event event) {
        Id<Link> linkId = null ;

        if(event instanceof ActivityStartEvent) {
            String activityType = ((ActivityStartEvent)event).getActType();
            String id = ((ActivityStartEvent) event).getPersonId().toString();

           if(activityType.equals("h")) {
               linkId = Id.createLinkId(24); // assign link id 24 if it is home
               if(activityRecordMapHome.get(id) == null) {
                    activityRecordMapHome.put(id, new ActivityRecord());
               }
               activityRecordMapHome.get(id).setStartTime(event.getTime());

           }
           else if(activityType.equals("w")) {
               linkId = Id.createLinkId(25); // assign link id 25 if it is work
               if(activityRecordMapWork.get(id) == null) {
                   activityRecordMapWork.put(id, new ActivityRecord());
               }
               activityRecordMapWork.get(id).setStartTime(event.getTime());
           }


           if(nVehs.get(linkId) == null) {
               nVehs.put(linkId,0);
           }
           int n = nVehs.get(linkId);
           n += 1;
           nVehs.put(linkId,n);
            try {
                write();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return;
        }

        if(event instanceof ActivityEndEvent) {
            String activityType = ((ActivityEndEvent)event).getActType();
            String id = ((ActivityEndEvent) event).getPersonId().toString();
            if(activityType.equals("h")) {
                linkId = Id.createLinkId(24); // assign link id 24 if it is home

                if(activityRecordMapHome.get(id) == null) {
                    activityRecordMapHome.put(id, new ActivityRecord());
                }
                activityRecordMapHome.get(id).setEndTime(event.getTime());
            }
            else if(activityType.equals("w")) {
                linkId = Id.createLinkId(25); // assign link id 25 if it is work
                if(activityRecordMapWork.get(id) == null) {
                    activityRecordMapWork.put(id, new ActivityRecord());
                }
                activityRecordMapWork.get(id).setEndTime(event.getTime());
            }

            if(nVehs.get(linkId) == null) {
               return;
            }
            int n = nVehs.get(linkId);
            n -= 1;
            nVehs.put(linkId,n);
            try {
                write();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return;
        }

        if ( event instanceof LinkEnterEvent ) {
            linkId = ((LinkEnterEvent) event).getLinkId() ;
            Id<Vehicle> vehicleId = ((LinkEnterEvent) event).getVehicleId();
            vehLink.put(vehicleId,linkId);

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

    public Map<Id<Vehicle>, Id<Link>> getVehicleLinkMap(){return this.vehLink;}
}
