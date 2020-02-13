package org.matsim.codeexamples.mdp;

import org.matsim.codeexamples.mdp.utilities.JSONStringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test {

    public static void main(String[] args) {
        String jsonString = "{\"action_rate\":[1,2,3]}";
        Map<String,Object> jsonObject = JSONStringUtil.convertToMap(jsonString);
        List<Double> actionRate = (ArrayList<Double>)jsonObject.get("action_rate");
        for(Double rate: actionRate) {
            System.out.println(rate);
        }
    }
}
