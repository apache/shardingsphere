package io.shardingjdbc.orchestration.reg.etcd;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.orchestration.reg.base.ConfigChangeEvent;
import io.shardingjdbc.orchestration.reg.base.ConfigChangeListener;
import io.shardingjdbc.orchestration.reg.base.ConfigServer;
import io.shardingjdbc.orchestration.reg.etcd.internal.*;
import io.shardingjdbc.orchestration.reg.exception.RegException;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.val;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;

/**
 * @author junxiong
 */
public class EtcdConfigServer implements ConfigServer, WatcherListener {
    private static EtcdConfigServer etcdConfigServer;
    private String namespace;
    private EtcdClient etcdClient;
    private Set<ConfigChangeListener> configChangeListeners = Sets.newCopyOnWriteArraySet();
    private Gson gson = new Gson();
    private AtomicBoolean opened = new AtomicBoolean(false);
    private List<Watcher> watchers;

    private EtcdConfigServer(@NonNull EtcdConfiguration etcdConfiguration) {
        this.namespace = etcdConfiguration.getNamespace();
        this.etcdClient = EtcdClientBuilder.newBuilder()
                .endpoints(etcdConfiguration.getServerLists())
                .build();
    }

    @Synchronized
    public static EtcdConfigServer from(@NonNull EtcdConfiguration etcdConfiguration) {
        if (etcdConfigServer == null) {
            etcdConfigServer = new EtcdConfigServer(etcdConfiguration);
        }
        return etcdConfigServer;
    }


    @Override
    public void open() {
        if (opened.get()) {
            return;
        }
        val shardingKey = getShardingKey();
        val masterSlaveKey = getMasterSlaveKey();
        for (String key : Lists.newArrayList(shardingKey, masterSlaveKey)) {
            val watcher = etcdClient.watch(key);
            if (watcher.isPresent()) {
                watcher.get().addWatcherListener(this);
            }
        }
        opened.compareAndSet(false, true);
    }

    private String getShardingKey() {
        return format("/%s/config/sharding", namespace);
    }

    @Override
    public void persistMasterSlaveRuleConfiguration(MasterSlaveRuleConfiguration masterSlaveRuleConfiguration) {
        val key = getMasterSlaveKey();
        etcdClient.put(key, gson.toJson(masterSlaveRuleConfiguration));
    }

    private String getMasterSlaveKey() {
        return format("/%s/config/masterslave", namespace);
    }

    @Override
    public MasterSlaveRuleConfiguration loadMasterSlaveRuleConfiguration() {
        val key = getMasterSlaveKey();
        val response = etcdClient.get(key).transform(new Function<String, MasterSlaveRuleConfiguration>() {
            @Nullable
            @Override
            public MasterSlaveRuleConfiguration apply(@Nullable String input) {
                return gson.fromJson(input, MasterSlaveRuleConfiguration.class);
            }
        });
        if (response.isPresent()) {
            return response.get();
        } else {
            throw new RegException("key %s does not exist", key);
        }
    }

    @Override
    public ShardingRuleConfiguration loadShardingRuleConfiguration() {
        val key = getShardingKey();
        val response = etcdClient.get(key).transform(new Function<String, ShardingRuleConfiguration>() {
            @Nullable
            @Override
            public ShardingRuleConfiguration apply(@Nullable String input) {
                return gson.fromJson(input, ShardingRuleConfiguration.class);
            }
        });
        if (response.isPresent()) {
            return response.get();
        } else {
            throw new RegException("key %s does not exist", key);
        }
    }

    @Override
    public void presistShardingRuleConfiguration(ShardingRuleConfiguration shardingRuleConfiguration) {

    }

    @Override
    public void addConfigChangeListener(@NonNull ConfigChangeListener configChangeListener) {
        configChangeListeners.add(configChangeListener);
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public void onWatch(WatchEvent watchEvent) {
        for (ConfigChangeListener listener : configChangeListeners) {
            if (getShardingKey().equals(watchEvent.getKey())) {
                val shardingRuleConfiguration = gson.fromJson(watchEvent.getValue(), ShardingRuleConfiguration.class);
                listener.onConfigChange(ConfigChangeEvent.with(shardingRuleConfiguration));
            }
            if (getMasterSlaveKey().equals(watchEvent.getKey())) {
                val masterSlaveRuleConfiguration = gson.fromJson(watchEvent.getValue(), MasterSlaveRuleConfiguration.class);
                listener.onConfigChange(ConfigChangeEvent.with(masterSlaveRuleConfiguration));
            }
        }
    }
}
