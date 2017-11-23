package io.shardingjdbc.orchestration.reg.etcd.internal;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Watcher implementation.
 *
 * @author junxiong
 */
@Data
public class WatcherImpl implements Watcher {
    
    private long id;
    
    private String key;
    
    private List<WatcherListener> listeners = Lists.newArrayList();

    public WatcherImpl(final String key) {
        this.key = key;
    }

    @Override
    public void addWatcherListener(final WatcherListener watcherListener) {
        this.listeners.add(watcherListener);
    }

    public void notify(final WatchEvent watchEvent) {
        for (final WatcherListener listener : listeners) {
            listener.onWatch(watchEvent);
        }
    }

    @Override
    public void cancel() {
        // TODO cancel watcher
    }
}
