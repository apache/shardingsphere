package io.shardingjdbc.orchestration.reg.etcd;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import io.shardingjdbc.orchestration.reg.base.RegistryChangeEvent;
import io.shardingjdbc.orchestration.reg.base.RegistryChangeListener;
import io.shardingjdbc.orchestration.reg.base.RegistryChangeType;
import io.shardingjdbc.orchestration.reg.etcd.internal.*;
import io.shardingjdbc.orchestration.reg.exception.RegExceptionHandler;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * ETCD based registry center
 *
 * @author junxiong
 */
public class EtcdRegistryCenter implements CoordinatorRegistryCenter {
    private EtcdConfiguration etcdConfiguration;
    private EtcdClient etcdClient;

    public EtcdRegistryCenter(EtcdConfiguration etcdConfiguration) {
        this.etcdConfiguration = etcdConfiguration;
        this.etcdClient = EtcdClientBuilder.newBuilder()
                .endpoints(etcdConfiguration.getServerLists())
                .build();
    }

    @Override
    public String getDirectly(@NonNull final String key) {
        return get(key);
    }

    /**
     * use default time to live
     *
     * @param key   key of data
     * @param value value of data
     */
    @Override
    public void persistEphemeral(@NonNull final String key, @NonNull final String value) {
        etcdClient.put(key, value, etcdConfiguration.getTimeToLive());
    }

    @Override
    public void addCacheData(String cachePath) {
        // no op for etcd
    }

    @Override
    public List<String> getChildrenKeys(String path) {
        List<EtcdClient.KeyValue> children = etcdClient.list(path);
        List<String> keys = Lists.newArrayList();
        for (EtcdClient.KeyValue keyValue : children) {
            keys.add(keyValue.getKey());
        }
        return keys;
    }

    @Override
    public void addRegistryChangeListener(final String path, final RegistryChangeListener registryChangeListener) {
        Optional<Watcher> watcherOptional = etcdClient.watch(path);
        WatcherListener listener = new WatcherListener() {
            @Override
            public void onWatch(WatchEvent watchEvent) {
                final Optional<RegistryChangeEvent> registryChangeEventOptional = fromWatchEvent(watchEvent);
                registryChangeEventOptional.transform(new Function<RegistryChangeEvent, Object>() {
                    @Nullable
                    @Override
                    public Object apply(@Nullable RegistryChangeEvent input) {
                        try {
                            registryChangeListener.onRegistryChange(input);
                        } catch (Exception e) {
                            RegExceptionHandler.handleException(e);
                        }
                        return input;
                    }
                });
            }
        };
    }

    private Optional<RegistryChangeEvent> fromWatchEvent(WatchEvent watchEvent) {
        if (WatchEvent.WatchEventType.UPDATE == watchEvent.getWatchEventType()) {
            return Optional.of(new RegistryChangeEvent(RegistryChangeType.UPDATED,
                    Optional.of(new RegistryChangeEvent.Payload(watchEvent.getKey(), watchEvent.getValue()))));
        } else if (WatchEvent.WatchEventType.DELETE == watchEvent.getWatchEventType()) {
            return Optional.of(new RegistryChangeEvent(RegistryChangeType.DELETED,
                    Optional.of(new RegistryChangeEvent.Payload(watchEvent.getKey(), watchEvent.getValue()))));
        } else {
            return Optional.absent();
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
        Optional<String> value = etcdClient.get(key);
        return value.orNull();
    }

    @Override
    public boolean isExisted(@NonNull final String key) {
        return get(key) != null;
    }

    @Override
    public void persist(String key, String value) {
        etcdClient.put(key, value);
    }

    @Override
    public void update(String key, String value) {
        etcdClient.put(key, value);
    }
}
