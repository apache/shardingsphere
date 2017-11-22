package io.shardingjdbc.orchestration.reg.etcd;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import groovy.util.logging.Slf4j;
import io.shardingjdbc.orchestration.reg.base.ChangeEvent;
import io.shardingjdbc.orchestration.reg.base.ChangeListener;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import io.shardingjdbc.orchestration.reg.etcd.internal.*;
import io.shardingjdbc.orchestration.reg.exception.RegException;
import lombok.NonNull;

import java.util.List;

/**
 * ETCD based registry center
 *
 * @author junxiong
 */
@Slf4j
public class EtcdRegistryCenter implements CoordinatorRegistryCenter {
    private long timeToLive;
    private EtcdClient etcdClient;
    private String namespace;

    public EtcdRegistryCenter(EtcdConfiguration etcdConfiguration) {
        this.timeToLive = etcdConfiguration.getTimeToLive();
        this.etcdClient = EtcdClientBuilder.newBuilder()
                .endpoints(etcdConfiguration.getServerLists())
                .timeout(etcdConfiguration.getTimeout())
                .maxRetry(etcdConfiguration.getMaxRetries())
                .build();
    }

    public EtcdRegistryCenter(String namespace, long timeToLive, EtcdClient etcdClient) {
        this.timeToLive = timeToLive;
        this.etcdClient = etcdClient;
        this.namespace = namespace;
    }

    private String namespace(String path) {
        return "/" + namespace + "/" + path;
    }

    @Override
    public String getDirectly(@NonNull final String key) {
        return get(namespace(key));
    }

    /**
     * use default time to live
     *
     * @param key   key of data
     * @param value value of data
     */
    @Override
    public void persistEphemeral(@NonNull final String key, @NonNull final String value) {
        etcdClient.put(namespace(key), value, timeToLive);
    }

    //@Override
    public void addCacheData(String cachePath) {
        // no op for etcd
    }

    @Override
    public List<String> getChildrenKeys(@NonNull String path) {
        Optional<List<String>> children = etcdClient.list(namespace(path));
        return children.isPresent() ? children.get() : Lists.<String>newArrayList();
    }

    @Override
    public Object getRawCache(String cachePath) {
        return etcdClient;
    }

    @Override
    public void watch(@NonNull final String path, @NonNull final ChangeListener changeListener) {
        Optional<Watcher> watcher = etcdClient.watch(namespace(path));
        if (watcher.isPresent()) {
            watcher.get().addWatcherListener(new WatcherListener() {
                @Override
                public void onWatch(WatchEvent watchEvent) {
                    try {
                        changeListener.onChange(fromWatchEvent(watchEvent));
                    } catch (Exception e) {
                        throw new RegException(e);
                    }
                }
            });
        }
    }

    private ChangeEvent fromWatchEvent(@NonNull final WatchEvent watchEvent) {
        final ChangeEvent.ChangeData changeData = new ChangeEvent.ChangeData(watchEvent.getKey(), watchEvent.getValue());
        switch (watchEvent.getWatchEventType()) {
            case DELETE:
                return new ChangeEvent(ChangeEvent.ChangeType.DELETED, changeData);
            case UPDATE:
                return new ChangeEvent(ChangeEvent.ChangeType.UPDATED, changeData);
            case UNKNOWN:
            default: return new ChangeEvent(ChangeEvent.ChangeType.UNKNOWN, changeData);
        }
    }

    @Override
    public void init() {
        // no op
    }

    @Override
    public void close() {
        // no op
    }

    @Override
    public String get(@NonNull final String key) {
        Optional<String> value = etcdClient.get(namespace(key));
        return value.orNull();
    }

    @Override
    public boolean isExisted(@NonNull final String key) {
        return get(namespace(key)) != null;
    }

    @Override
    public void persist(String key, String value) {
        etcdClient.put(namespace(key), value);
    }

    @Override
    public void update(String key, String value) {
        etcdClient.put(namespace(key), value);
    }
}
