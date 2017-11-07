package io.shardingjdbc.orchestration.reg.base;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;

/**
 * Configuration Center
 *
 * @author junxiong
 */
public interface ConfigServer extends AutoCloseable {

    /**
     * Open sharding-jdbc config server, setup connection to backend storage and event listeners.
     */
    void open();

    /**
     * save or update master slave rule configuration
     *
     * caution: this may conflict with distribute configuration management center.
     *
     * MasterSlaveRuleConfiguration is an aggregation of configuration items for CQRS data source strategy.
     *
     * @param masterSlaveRuleConfiguration master slave rule configuration
     */
    void persistMasterSlaveRuleConfiguration(MasterSlaveRuleConfiguration masterSlaveRuleConfiguration);

    /**
     * load master slave rule configuration
     *
     * @return master slave rule configuration
     */
    MasterSlaveRuleConfiguration loadMasterSlaveRuleConfiguration();

    /**
     * load sharding rule configuration
     *
     * @return sharding rule configuration
     */
    ShardingRuleConfiguration loadShardingRuleConfiguration();

    /**
     * save or update sharding rule configuration
     *
     * @param shardingRuleConfiguration sharding rule configuration
     */
    void presistShardingRuleConfiguration(ShardingRuleConfiguration shardingRuleConfiguration);

    /**
     * Register config change listener
     *
     * @param configChangeListener configuration change listener
     */
    void addConfigChangeListener(ConfigChangeListener configChangeListener);
}
