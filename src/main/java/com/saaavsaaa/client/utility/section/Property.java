package com.saaavsaaa.client.utility.section;

import com.saaavsaaa.client.utility.StringUtil;

import java.util.ResourceBundle;

/**
 * Created by aaa on 18-4-27.
 */
public enum Property {
    INSTANCE;
    
    public String getClientId(){
        // ResourceBundle caches the value in Thread
        ResourceBundle bundle = ResourceBundle.getBundle("client");
        String clientId = bundle.getString("client.id");
        if (StringUtil.isNullOrBlank(clientId)){
            throw new IllegalArgumentException("client.id doesn't exist");
        }
        return clientId;
    }
}
