package com.example.together;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 김민제 on 2017-07-02.
 */

public class MapRequest extends StringRequest {

    final static private String URL = "http://13.124.142.75/TogetherMap.php";
    private Map<String, String> parameters;

    public MapRequest(String LocationRange, String Latitude, String Longitude, Response.Listener<String> listener){
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("LocationRange", LocationRange);
        parameters.put("Latitude", Latitude);
        parameters.put("Longitude", Longitude);
    }

    @Override
    public Map<String, String> getParams(){
        return parameters;
    }
}