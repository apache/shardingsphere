package com.saaavsaaa.client.section;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.zookeeper.Watcher.Event.EventType.NodeDeleted;

/**
 * Created by aaa
 */
public class WatcherCreator {
    private static final Logger logger = LoggerFactory.getLogger(WatcherCreator.class);
    public static Watcher deleteWatcher(final String path, Listener listener){
        return new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (path.equals(event.getPath()) && NodeDeleted.equals(event.getType())){
                    listener.process(event);
                    logger.debug("delete node event:{}", event.toString());
                }
            }
        };
    }
}
