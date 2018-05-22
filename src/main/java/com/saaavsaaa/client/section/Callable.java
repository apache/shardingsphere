package com.saaavsaaa.client.section;

import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa on 18-5-21.
 */
public interface Callable<T> {
    T call() throws KeeperException, InterruptedException;
}
