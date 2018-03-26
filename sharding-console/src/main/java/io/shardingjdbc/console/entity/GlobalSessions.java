package io.shardingjdbc.console.entity;

import java.sql.Connection;

import java.util.HashMap;
import java.util.Map;

public class GlobalSessions {

    private static Map<String, Connection> sessionInfo;

    /**
     * create the map of uuid and connection .
     * @return  Map<String, Connection>
     */
    public static Map<String, Connection> getSessionInfo() {
        if (null == sessionInfo) {
            sessionInfo = new HashMap<>();
        }
        return sessionInfo;
    }
}