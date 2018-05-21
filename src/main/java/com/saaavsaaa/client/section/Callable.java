package com.saaavsaaa.client.section;

import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa on 18-5-21.
 */
public interface Callable {
    void call() throws KeeperException, InterruptedException;
}
