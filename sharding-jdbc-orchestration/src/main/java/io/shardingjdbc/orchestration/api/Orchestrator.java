package io.shardingjdbc.orchestration.api;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestrate sharding rule and master slave rule
 *
 * @author junxiong
 */
public interface Orchestrator {
    /**
     * Orchestrate all actions for sharding data source.
     *
     * @param dataSourceMap data source map
     * @param shardingRuleConfig sharding rule configuration
     * @param props sharding properties
     * @param shardingDataSource sharding datasource
     */
    void orchestrateShardingDatasource(Map<String, DataSource> dataSourceMap,
                                       ShardingRuleConfiguration shardingRuleConfig,
                                       ShardingDataSource shardingDataSource,
                                       Properties props);


    /**
     * Orchestrate all actions for master-slave data source.
     *
     * @param dataSourceMap data source map
     * @param masterSlaveRuleConfig sharding rule configuration
     * @param masterSlaveDataSource master-slave datasource
     */
    void orchestrateMasterSlaveDatasource(Map<String, DataSource> dataSourceMap,
                                          MasterSlaveRuleConfiguration masterSlaveRuleConfig,
                                          MasterSlaveDataSource masterSlaveDataSource);
}
