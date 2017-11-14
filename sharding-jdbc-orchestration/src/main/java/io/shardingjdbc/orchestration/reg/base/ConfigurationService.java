package io.shardingjdbc.orchestration.reg.base;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.MasterSlaveRule;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration Service
 *
 * @author junxiong
 */
public interface ConfigurationService {
    /**
     * Persist sharding configuration.
     *
     * @param dataSourceMap data source map
     * @param shardingRuleConfig sharding rule configuration
     * @param props sharding properties
     * @param shardingDataSource sharding datasource
     */
    void persistShardingConfiguration(
            Map<String, DataSource> dataSourceMap, ShardingRuleConfiguration shardingRuleConfig, Properties props, ShardingDataSource shardingDataSource);

    /**
     * Persist master-slave configuration.
     *
     * @param dataSourceMap data source map
     * @param masterSlaveRuleConfig master-slave rule configuration
     * @param masterSlaveDataSource master-slave datasource
     */
    void persistMasterSlaveConfiguration(
            Map<String, DataSource> dataSourceMap, MasterSlaveRuleConfiguration masterSlaveRuleConfig, MasterSlaveDataSource masterSlaveDataSource);

    /**
     * Load data source configuration.
     *
     * @return data source configuration map
     */
    Map<String, DataSource> loadDataSourceMap();

    /**
     * Load sharding rule configuration.
     *
     * @return sharding rule configuration
     */
    ShardingRuleConfiguration loadShardingRuleConfiguration();

    /**
     * Load sharding properties configuration.
     *
     * @return sharding properties
     */
    Properties loadShardingProperties();

    /**
     * Load master-slave rule configuration.
     *
     * @return master-slave rule configuration
     */
    MasterSlaveRuleConfiguration loadMasterSlaveRuleConfiguration();

    /**
     * Get available master-slave rule.
     *
     * @return available master-slave rule
     */
    MasterSlaveRule getAvailableMasterSlaveRule();
}
