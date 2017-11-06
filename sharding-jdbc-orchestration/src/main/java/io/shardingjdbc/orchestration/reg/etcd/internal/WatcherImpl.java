package io.shardingjdbc.orchestration.reg.etcd.internal;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.val;

import java.util.List;

/**
 * WatcherImpl
 *
 * @author junxiong
 */
@Data
public class WatcherImpl implements Watcher {
    private long id;
    private String key;
    private List<WatcherListener> listeners = Lists.newArrayList();

    public WatcherImpl(String key) {
        this.key = key;
    }

    @Override
    public void addWatcherListener(WatcherListener watcherListener) {
        this.listeners.add(watcherListener);
    }

    public void notify(WatchEvent watchEvent) {
        for (val listener : listeners) {
            listener.onWatch(watchEvent);
        }
    }

    @Override
    public void cancel() {
        // TODO cancel watcher
    }
}
