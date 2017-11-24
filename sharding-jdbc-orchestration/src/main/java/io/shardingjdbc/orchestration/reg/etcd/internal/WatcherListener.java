package io.shardingjdbc.orchestration.reg.etcd.internal;

import io.shardingjdbc.orchestration.reg.base.DataChangedEvent;

/**
 * Watcher listener.
 * 
 * @author junxiong
 */
public interface WatcherListener {
    
    /**
     * fires when watched key changed.
     *
     * @param event data changed event
     */
    void onWatch(DataChangedEvent event);
}
