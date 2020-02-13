package org.matsim.codeexamples.mdp;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.matsim.codeexamples.mdp.utilities.JSONStringUtil;
import sun.rmi.runtime.Log;

import java.util.*;
import java.util.logging.Logger;

public class PolicyNetworkInterface {

    private final String BASEURL = "http://localhost:8085";
    private final String GETACTION = "get_action";
    private static Logger logger = Logger.getLogger(PolicyNetworkInterface.class.getName());

    private ClientResponse clientGet(String url){
        Client client = Client.create();
        WebResource webResource = client.resource(url);
        ClientResponse clientResponse = webResource.accept("application/json").get(ClientResponse.class);
        return clientResponse;
    }

    private  ClientResponse clientPost(String url, Map<String,Object> parameters) {
        Client client = Client.create();
        WebResource webResource = client
                .resource(url);
        ClientResponse response = webResource.accept("application/json").type("application/json")
                .post(ClientResponse.class, JSONStringUtil.convertToJSONString(parameters));
        return response;
    }

    public List<Double> getAction(List<Double> state, List<Integer> outgoingLinks) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("state", state);
        parameters.put("outgoing_links", outgoingLinks);
        String url = String.format("%s/%s",BASEURL,GETACTION);
        ClientResponse clientResponse = clientPost(url,parameters);
        if(clientResponse.getStatus() != 200) {
            logger.info(clientResponse.toString());
            throw new RuntimeException("Failed: " + clientResponse.getStatus());
        }
        String jsonResponse = clientResponse.getEntity(String.class);
        Map<String,Object> jsonObject = JSONStringUtil.convertToMap(jsonResponse);
        logger.info(jsonObject.toString());
        List<Double> actionRate = new ArrayList<>();
        List<Object> strActionRate = JSONStringUtil.convertStringToList(jsonObject.get("action_rate").toString());
        for(Object strRate: strActionRate) {
            if(strRate.toString().equals("nan")) {
                actionRate.add(0.0);
            }
            else{
                actionRate.add(Double.valueOf(strRate.toString()));
            }
        }
        return actionRate;
    }


}
