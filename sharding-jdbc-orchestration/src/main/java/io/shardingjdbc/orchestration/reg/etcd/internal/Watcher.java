package io.shardingjdbc.orchestration.reg.etcd.internal;

import io.shardingjdbc.orchestration.reg.base.DataChangedEvent;
import io.shardingjdbc.orchestration.reg.base.EventListener;
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
    
    private final List<EventListener> listeners = new ArrayList<>();
    
    /**
     * Add watcher listener.
     *
     * @param eventListener WatcherListener
     */
    public void addEventListener(final EventListener eventListener) {
        listeners.add(eventListener);
    }
    
    public void notify(final DataChangedEvent event) {
        for (EventListener listener : listeners) {
            listener.onChange(event);
        }
    }
}
