package io.shardingjdbc.orchestration.reg.etcd;

import com.google.common.base.Optional;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import io.shardingjdbc.orchestration.reg.base.DataChangedEvent;
import io.shardingjdbc.orchestration.reg.base.EventListener;
import io.shardingjdbc.orchestration.reg.etcd.internal.EtcdClient;
import io.shardingjdbc.orchestration.reg.etcd.internal.EtcdClientBuilder;
import io.shardingjdbc.orchestration.reg.etcd.internal.Watcher;

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
    public void watch(final String path, final EventListener eventListener) {
        Optional<Watcher> watcher = etcdClient.watch(getFullPathWithNamespace(path));
        if (!watcher.isPresent()) {
            return;
        }
        watcher.get().addEventListener(new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                eventListener.onChange(event);
            }
        });
    }
    
    private String getFullPathWithNamespace(final String path) {
        return String.format("/%s/%s", namespace, path);
    }
}
