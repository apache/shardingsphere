package io.shardingjdbc.orchestration.reg.base;

import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;

/**
 * Instance State Service
 *
 * @author junxiong
 */
public interface InstanceStateService {
    /**
     * Persist sharding instance online.
     *
     * @param shardingDataSource sharding datasource
     */
    void persistShardingInstanceOnline(ShardingDataSource shardingDataSource);

    /**
     * Persist master-salve instance online.
     *
     * @param masterSlaveDataSource master-slave datasource
     */
    void persistMasterSlaveInstanceOnline(MasterSlaveDataSource masterSlaveDataSource);
}
