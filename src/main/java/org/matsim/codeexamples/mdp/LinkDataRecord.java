package org.matsim.codeexamples.mdp;

public class LinkDataRecord {
    private String linkId;
    private String vehicleId;
    private Long time;

    public LinkDataRecord(String linkId,String vehicleId, Long time){
        this.linkId=linkId;
        this.vehicleId=vehicleId;
        this.time=time;
    }
}
