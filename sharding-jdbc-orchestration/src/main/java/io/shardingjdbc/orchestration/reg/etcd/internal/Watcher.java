package io.shardingjdbc.orchestration.reg.etcd.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Watcher.
 * 
 * @author junxiong
 */
@RequiredArgsConstructor
@Getter
public final class Watcher {
    
    private final String key;
    
    private final List<WatcherListener> listeners = new ArrayList<>();
    
    /**
     * Add watcher listener.
     *
     * @param watcherListener WatcherListener
     */
    public void addWatcherListener(final WatcherListener watcherListener) {
        this.listeners.add(watcherListener);
    }
    
    public void notify(final WatchEvent watchEvent) {
        for (WatcherListener listener : listeners) {
            listener.onWatch(watchEvent);
        }
    }
}
