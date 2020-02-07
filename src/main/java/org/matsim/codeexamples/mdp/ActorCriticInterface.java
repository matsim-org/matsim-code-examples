package org.matsim.codeexamples.mdp;


import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.matsim.codeexamples.mdp.utilities.JSONStringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ActorCriticInterface {

    private static Logger log = Logger.getLogger("ActorCriticInterface");

    private  String BASEURL = "http://localhost:8080";
    private  String INITIALIZEMODELS = "initialize_models";
    private  String GETACTION = "get_action";
    private  String ADDREWARD = "add_reward";
    private  String UPDATEMODELS = "update_models";
    private  String SAVEMODELS = "save_models";


    public  ClientResponse clientGet(String url){
        Client client = Client.create();
        WebResource webResource = client.resource(url);
        ClientResponse clientResponse = webResource.accept("application/json").get(ClientResponse.class);
        return clientResponse;
    }

    public  ClientResponse clientPost(String url, Map<String,Object> parameters) {
        Client client = Client.create();
        WebResource webResource = client
                .resource(url);
        ClientResponse response = webResource.accept("application/json").type("application/json")
                .post(ClientResponse.class,JSONStringUtil.convertToJSONString(parameters));
        return response;
    }

    public  void initializeModels(int stateSize, int actionSize) {
        String url = String.format("%s/%s?state_size=%d&action_size=%d",BASEURL,INITIALIZEMODELS,stateSize,actionSize);
        ClientResponse clientResponse = clientGet(url);
        if(clientResponse.getStatus() != 200) {
            throw new RuntimeException("Failed: "+clientResponse.getStatus());
        }
    }

    public  void saveModels() {
        String url = String.format("%s/%s",BASEURL,SAVEMODELS);
        ClientResponse clientResponse = clientGet(url);
        if(clientResponse.getStatus() != 200) {
            throw new RuntimeException("Failed: "+clientResponse.getStatus());
        }
    }

    public  void updateModels() {
        //RUN BACKPROPAGATION
        String url = String.format("%s/%s",BASEURL,UPDATEMODELS);
        ClientResponse clientResponse = clientGet(url);
        if(clientResponse.getStatus() != 200) {
            throw new RuntimeException("Failed: "+clientResponse.getStatus());
        }
    }

    public  int getAction(List<Double> state, List<Integer> outgoingLinks) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("state", state);
        parameters.put("outgoing_links", outgoingLinks);
        String url = String.format("%s/%s",BASEURL,GETACTION);
        log.info("Getting action with parameters: "+ parameters);
        ClientResponse clientResponse = clientPost(url,parameters);
        if(clientResponse.getStatus() != 200) {
            throw new RuntimeException("Failed: " + clientResponse.getStatus());
        }
        String jsonResponse = clientResponse.getEntity(String.class);

        Map<String,Object> jsonObject = JSONStringUtil.convertToMap(jsonResponse);

        int action =Integer.valueOf(((String)jsonObject.get("action")));
        log.info("Got action "+ action);
        return action;
    }

    public  void addReward(double reward) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("reward", reward);
        String url = String.format("%s/%s", BASEURL, ADDREWARD);

        ClientResponse clientResponse = clientPost(url,parameters);

        if(clientResponse.getStatus() != 200) {
            throw new RuntimeException("Failed: "+clientResponse.getStatus());
        }

    }


}
