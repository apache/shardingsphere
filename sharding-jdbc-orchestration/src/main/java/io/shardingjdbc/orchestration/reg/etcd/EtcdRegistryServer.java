package io.shardingjdbc.orchestration.reg.etcd;

import io.shardingjdbc.orchestration.reg.base.RegistryServer;
import io.shardingjdbc.orchestration.reg.base.ShardInstance;
import io.shardingjdbc.orchestration.reg.base.ShardState;

/**
 * registration server implemented with etcd.
 *
 * @author junxiong
 */
public class EtcdRegistryServer implements RegistryServer {

    @Override
    public void open() {

    }

    @Override
    public void registerShard(ShardInstance shardInstance) {

    }

    @Override
    public void setShardState(String id, ShardState shardState) {

    }

    @Override
    public ShardState getShardState(String id) {
        return null;
    }

    @Override
    public void unregisterShard(String id) {

    }

    @Override
    public void close() throws Exception {

    }
}
