package org.matsim.codeexamples.mdp.utilities;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class JSONStringUtil {
    private static Gson gson  = new Gson();

    private  JSONStringUtil() {

    }
    public static String convertToJSONString(Object obj) {
       return  gson.toJson(obj).toString();
    }

    public static Map<String, Object> convertToMap(String jsonString) {
        Type type = new TypeToken<Map<String,Object>>(){}.getType();
        return gson.fromJson(jsonString,type);
    }

    public static List<Object> convertStringToList(String strList) {
        Type type = new TypeToken<List<Object>>(){}.getType();
        return gson.fromJson(strList,type);
    }
}
