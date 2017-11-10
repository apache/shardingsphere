package io.shardingjdbc.orchestration.reg.etcd;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import io.shardingjdbc.orchestration.reg.base.ConfigChangeListener;
import io.shardingjdbc.orchestration.reg.base.RegistryServer;
import io.shardingjdbc.orchestration.reg.base.ShardInstance;
import io.shardingjdbc.orchestration.reg.base.ShardState;
import io.shardingjdbc.orchestration.reg.etcd.internal.EtcdClient;
import io.shardingjdbc.orchestration.reg.etcd.internal.EtcdClientBuilder;
import io.shardingjdbc.orchestration.reg.etcd.internal.Watcher;
import lombok.NonNull;
import lombok.Synchronized;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;

/**
 * registration server implemented with etcd.
 *
 * @author junxiong
 */
public class EtcdRegistryServer implements RegistryServer {
    private static EtcdRegistryServer etcdRegistryServer;
    private String namespace;
    private long ttl;
    private EtcdClient etcdClient;
    private Set<ConfigChangeListener> configChangeListeners = Sets.newCopyOnWriteArraySet();
    private Gson gson = new Gson();
    private AtomicBoolean opened = new AtomicBoolean(false);
    private List<Watcher> watchers;

    private EtcdRegistryServer(@NonNull EtcdConfiguration etcdConfiguration) {
        //this.ttl = etcdConfiguration.ttl();
        this.namespace = etcdConfiguration.getNamespace();
        this.etcdClient = EtcdClientBuilder.newBuilder()
                .endpoints(etcdConfiguration.getServerLists())
                .build();
    }

    @Synchronized
    public static EtcdRegistryServer from(@NonNull EtcdConfiguration etcdConfiguration) {
        if (etcdRegistryServer == null) {
            etcdRegistryServer = new EtcdRegistryServer(etcdConfiguration);
        }
        return etcdRegistryServer;
    }


    @Override
    public void open() {
    }

    @Override
    public void registerShard(@NonNull ShardInstance shardInstance) {
        final String key = getRegistryKey(shardInstance.getId());
        etcdClient.put(key, gson.toJson(shardInstance), ttl);
    }

    @Override
    public void setShardState(@NonNull String id, @NonNull final ShardState shardState) {
        final String key = getRegistryKey(id);
        final Optional<ShardInstance> shardInstanceOptional = etcdClient.get(key).transform(new Function<String, ShardInstance>() {
            @Override
            public ShardInstance apply(@NonNull String input) {
                return gson.fromJson(input, ShardInstance.class);
            }
        }).transform(new Function<ShardInstance, ShardInstance>() {
            @Nullable
            @Override
            public ShardInstance apply(@Nullable ShardInstance input) {
                return input.withState(shardState);
            }
        });
        if (shardInstanceOptional.isPresent()) {
            registerShard(shardInstanceOptional.get());
        }
    }

    @Override
    public ShardState getShardState(@NonNull String id) {
        final String key = getRegistryKey(id);
        final Optional<ShardInstance> shardInstanceOptional = etcdClient.get(key).transform(new Function<String, ShardInstance>() {
            @Override
            public ShardInstance apply(@NonNull String input) {
                return gson.fromJson(input, ShardInstance.class);
            }
        });
        if (shardInstanceOptional.isPresent()) {
            return shardInstanceOptional.get().getState();
        } else {
            return null;
        }
    }

    @Override
    public void unregisterShard(@NonNull String id) {
        final String key = getRegistryKey(id);
        etcdClient.delete(key);
    }

    @Override
    public void close() throws Exception {

    }

    private String getRegistryKey(@NonNull String id) {
        return format("/registry/instances/%s", id);
    }
}
