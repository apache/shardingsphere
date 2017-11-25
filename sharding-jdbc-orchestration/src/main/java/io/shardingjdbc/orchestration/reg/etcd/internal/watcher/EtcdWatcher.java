package io.shardingjdbc.orchestration.reg.etcd.internal.watcher;

import io.shardingjdbc.orchestration.reg.base.DataChangedEvent;
import io.shardingjdbc.orchestration.reg.base.EventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Etcd event watcher.
 * 
 * @author junxiong
 */
public final class EtcdWatcher {
    
    private final List<EventListener> listeners = new ArrayList<>();
    
    /**
     * Add watcher listener.
     *
     * @param eventListener WatcherListener
     */
    public void addEventListener(final EventListener eventListener) {
        listeners.add(eventListener);
    }
    
    /**
     * Notify listener when event received.
     * 
     * @param event event
     */
    public void notify(final DataChangedEvent event) {
        for (EventListener listener : listeners) {
            listener.onChange(event);
        }
    }
}
