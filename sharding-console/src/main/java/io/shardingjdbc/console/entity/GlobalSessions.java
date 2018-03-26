package io.shardingjdbc.console.entity;

import java.sql.Connection;

import java.util.HashMap;
import java.util.Map;

public class GlobalSessions {

    private static Map<String, Connection> sessionInfos;

    public static Map<String, Connection> getSessionInfos() {
        if(null == sessionInfos) {
            sessionInfos = new HashMap<>();
        }
        return sessionInfos;
    }
}