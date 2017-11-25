package io.shardingjdbc.orchestration.reg.etcd;

import com.google.common.base.Optional;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import io.shardingjdbc.orchestration.reg.base.EventListener;
import io.shardingjdbc.orchestration.reg.etcd.internal.EtcdChannelFactory;
import io.shardingjdbc.orchestration.reg.etcd.internal.EtcdClient;
import io.shardingjdbc.orchestration.reg.etcd.internal.Watcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Etcd based registry center.
 *
 * @author junxiong
 */
public final class EtcdRegistryCenter implements CoordinatorRegistryCenter {
    
    private final EtcdConfiguration etcdConfiguration;
    
    private EtcdClient etcdClient;
    
    public EtcdRegistryCenter(final EtcdConfiguration etcdConfiguration) {
        this.etcdConfiguration = etcdConfiguration;
        etcdClient = new EtcdClient(EtcdChannelFactory.getInstance(Arrays.asList(etcdConfiguration.getServerLists().split(","))), 
                etcdConfiguration.getTimeoutMilliseconds(), etcdConfiguration.getMaxRetries(), etcdConfiguration.getRetryIntervalMilliseconds());
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
        etcdClient.put(getFullPathWithNamespace(key), value, etcdConfiguration.getTimeToLiveMilliseconds());
    }
    
    @Override
    public List<String> getChildrenKeys(final String path) {
        Optional<List<String>> children = etcdClient.list(getFullPathWithNamespace(path));
        return children.isPresent() ? children.get() : Collections.<String>emptyList();
    }
    
    @Override
    public void watch(final String path, final EventListener eventListener) {
        Optional<Watcher> watcher = etcdClient.watch(getFullPathWithNamespace(path));
        if (watcher.isPresent()) {
            watcher.get().addEventListener(eventListener);
        }
    }
    
    private String getFullPathWithNamespace(final String path) {
        return String.format("/%s/%s", etcdConfiguration.getNamespace(), path);
    }
}
