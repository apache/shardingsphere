package io.shardingjdbc.orchestration.reg.etcd;

import com.google.common.base.Optional;
import io.shardingjdbc.orchestration.reg.base.ChangeEvent;
import io.shardingjdbc.orchestration.reg.base.ChangeListener;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import io.shardingjdbc.orchestration.reg.etcd.internal.EtcdClient;
import io.shardingjdbc.orchestration.reg.etcd.internal.EtcdClientBuilder;
import io.shardingjdbc.orchestration.reg.etcd.internal.WatchEvent;
import io.shardingjdbc.orchestration.reg.etcd.internal.Watcher;
import io.shardingjdbc.orchestration.reg.etcd.internal.WatcherListener;
import io.shardingjdbc.orchestration.reg.exception.RegException;

import java.util.Collections;
import java.util.List;

/**
 * Etcd based registry center.
 *
 * @author junxiong
 */
public class EtcdRegistryCenter implements CoordinatorRegistryCenter {
    
    private String namespace;
    
    private long timeToLive;
    
    private EtcdClient etcdClient;
    
    public EtcdRegistryCenter(final EtcdConfiguration etcdConfiguration) {
        namespace = etcdConfiguration.getNamespace();
        timeToLive = etcdConfiguration.getTimeToLive();
        etcdClient = EtcdClientBuilder.newBuilder()
                .endpoints(etcdConfiguration.getServerLists())
                .timeout(etcdConfiguration.getTimeout())
                .maxRetry(etcdConfiguration.getMaxRetries())
                .build();
    }
    
    public EtcdRegistryCenter(final String namespace, final long timeToLive, final EtcdClient etcdClient) {
        this.namespace = namespace;
        this.timeToLive = timeToLive;
        this.etcdClient = etcdClient;
    }
    
    @Override
    public void init() {
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public String get(final String key) {
        return etcdClient.get(getFullPathWithNamespace(key)).orNull();
    }
    
    @Override
    public boolean isExisted(final String key) {
        return null != get(getFullPathWithNamespace(key));
    }
    
    @Override
    public void persist(final String key, final String value) {
        etcdClient.put(getFullPathWithNamespace(key), value);
    }
    
    @Override
    public void update(final String key, final String value) {
        etcdClient.put(getFullPathWithNamespace(key), value);
    }
    
    @Override
    public String getDirectly(final String key) {
        return get(getFullPathWithNamespace(key));
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
        etcdClient.put(getFullPathWithNamespace(key), value, timeToLive);
    }
    
    @Override
    public List<String> getChildrenKeys(final String path) {
        Optional<List<String>> children = etcdClient.list(getFullPathWithNamespace(path));
        return children.isPresent() ? children.get() : Collections.<String>emptyList();
    }
    
    @Override
    public void watch(final String path, final ChangeListener changeListener) {
        Optional<Watcher> watcher = etcdClient.watch(getFullPathWithNamespace(path));
        if (!watcher.isPresent()) {
            return;
        }
        watcher.get().addWatcherListener(new WatcherListener() {
            
            @Override
            public void onWatch(final WatchEvent watchEvent) {
                try {
                    changeListener.onChange(createWatchEvent(watchEvent));
                    // CHECKSTYLE:OFF
                } catch (final Exception ex) {
                    // CHECKSTYLE:ON
                    throw new RegException(ex);
                }
            }
        });
    }
    
    private ChangeEvent createWatchEvent(final WatchEvent watchEvent) {
        ChangeEvent.ChangeData changeData = new ChangeEvent.ChangeData(watchEvent.getKey(), watchEvent.getValue());
        switch (watchEvent.getWatchEventType()) {
            case DELETED:
                return new ChangeEvent(ChangeEvent.ChangeType.DELETED, changeData);
            case UPDATED:
                return new ChangeEvent(ChangeEvent.ChangeType.UPDATED, changeData);
            case UNKNOWN:
            default: 
                return new ChangeEvent(ChangeEvent.ChangeType.UNKNOWN, changeData);
        }
    }
    
    private String getFullPathWithNamespace(final String path) {
        return String.format("/%s/%s", namespace, path);
    }
}
