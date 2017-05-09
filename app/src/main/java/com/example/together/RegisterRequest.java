package com.example.together;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 김민제 on 2017-04-29.
 */

public class RegisterRequest extends StringRequest {

    final static private String URL = "http://minza1215.cafe24.com/UserRegister.php";
    private Map<String, String> parameters;

    public RegisterRequest(String userID, String userPassword, String userName, String userEmail, Response.Listener<String> listener){
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("userID", userID);
        parameters.put("userPassword", userPassword);
        parameters.put("userName", userName);
        parameters.put("userEmail", userEmail);

    }

    @Override
    public Map<String, String> getParams(){
        return parameters;
    }
}
