package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.utility.section.Listener;
import org.apache.zookeeper.WatchedEvent;

/**
 * Created by aaa
 */
public class TestSupport {
    public static final String AUTH = "digest";
    public static final String SERVERS = "192.168.2.44:2181";
    public static final int SESSION_TIMEOUT = 200000;//ms
    public static final String ROOT = "test";
    
    public static Listener buildListener(){
        Listener listener = new Listener() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("==========================================================");
                System.out.println(event.getPath());
                System.out.println(event.getState());
                System.out.println(event.getType());
                System.out.println("==========================================================");
            }
        };
        return listener;
    }
}
