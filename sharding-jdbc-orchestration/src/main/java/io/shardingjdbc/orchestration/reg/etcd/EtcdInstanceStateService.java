package io.shardingjdbc.orchestration.reg.etcd;

import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import io.shardingjdbc.orchestration.reg.base.InstanceStateService;

/**
 * @author junxiong
 */
public class EtcdInstanceStateService implements InstanceStateService {
    private CoordinatorRegistryCenter registryCenter;

    public EtcdInstanceStateService(CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
    }

    @Override
    public void persistShardingInstanceOnline(ShardingDataSource shardingDataSource) {

    }

    @Override
    public void persistMasterSlaveInstanceOnline(MasterSlaveDataSource masterSlaveDataSource) {

    }
}
