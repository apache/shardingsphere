package io.shardingjdbc.orchestration.reg.etcd.internal;

import io.shardingjdbc.orchestration.reg.base.ChangeEvent;

/**
 * Watcher listener.
 * 
 * @author junxiong
 */
public interface WatcherListener {
    
    /**
     * fires when watched key changed.
     *
     * @param changeEvent change event
     */
    void onWatch(ChangeEvent changeEvent);
}
