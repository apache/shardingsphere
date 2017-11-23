package io.shardingjdbc.orchestration.reg.etcd.internal;

/**
 * Watcher listener.
 * 
 * @author junxiong
 */
public interface WatcherListener {
    
    /**
     * fires when watched key changed.
     *
     * @param watchEvent watch event
     */
    void onWatch(WatchEvent watchEvent);
}
