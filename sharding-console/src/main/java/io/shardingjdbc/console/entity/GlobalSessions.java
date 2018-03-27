package io.shardingjdbc.console.entity;

import java.sql.Connection;

import java.util.HashMap;
import java.util.Map;

/**
 * Global user session.
 * 
 * @author panjuan
 */
public class GlobalSessions {
    
    private static Map<String, Connection> sessionInfo;
    
    /**
     * Create the map of id and connection.
     * 
     * @return  user and connection info
     */
    public static Map<String, Connection> getSessionInfo() {
        if (null == sessionInfo) {
            sessionInfo = new HashMap<>();
        }
        return sessionInfo;
    }
}
