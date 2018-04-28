package com.saaavsaaa.client.utility.section;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import static org.apache.zookeeper.Watcher.Event.EventType.NodeDeleted;

/**
 * Created by aaa
 */
public class WatcherCreator {
    public static Watcher deleteWatcher(final String path, Listener listener){
        return new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (path.equals(event.getPath()) && NodeDeleted.equals(event.getType())){
                    listener.process(event);
                    
                    System.out.println("----------------------------------------------delete root");
                    System.out.println(event.getPath());
                    System.out.println(event.getState());
                    System.out.println(event.getType());
                    System.out.println("----------------------------------------------delete root");
                }
            }
        };
    }
}
