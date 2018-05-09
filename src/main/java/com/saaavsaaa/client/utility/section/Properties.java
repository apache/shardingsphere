package com.saaavsaaa.client.utility.section;

import com.saaavsaaa.client.utility.StringUtil;

import java.util.ResourceBundle;

/**
 * Created by aaa
 */
public enum Properties {
    INSTANCE;
    
    private final ResourceBundle bundle;
    
    private Properties(){
        bundle = ResourceBundle.getBundle("client");
    }
    
    public String getClientId(){
        // ResourceBundle caches the value in Thread
        String clientId = bundle.getString("client.id");
        if (StringUtil.isNullOrBlank(clientId)){
            throw new IllegalArgumentException("client.id doesn't exist");
        }
        return clientId;
    }
    
    public boolean watchOn(){
        String result = bundle.getString("client.watch.on");
        if (StringUtil.isNullOrBlank(result)){
            throw new IllegalArgumentException("client.watch.on doesn't exist");
        }
        return "up".equals(result);
    }
    
    public long getThreadInitialDelay(){
        String result = bundle.getString("client.thread.delay");
        if (StringUtil.isNullOrBlank(result)){
            throw new IllegalArgumentException("client.thread.delay doesn't exist");
        }
        return Long.valueOf(result);
    }
    
    public long getThreadPeriod(){
        String result = bundle.getString("client.thread.period");
        if (StringUtil.isNullOrBlank(result)){
            throw new IllegalArgumentException("client.thread.period doesn't exist");
        }
        return Long.valueOf(result);
    }
}
