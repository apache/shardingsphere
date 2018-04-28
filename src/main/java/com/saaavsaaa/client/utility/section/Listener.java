package com.saaavsaaa.client.utility.section;

import org.apache.zookeeper.WatchedEvent;

/**
 * Created by aaa
 */
public interface Listener {
    void process(WatchedEvent event);
}
