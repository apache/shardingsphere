package com.saaavsaaa.client.section;

import org.apache.zookeeper.WatchedEvent;

/**
 * Created by aaa
 */
public interface Listener {
    void process(WatchedEvent event);
}
