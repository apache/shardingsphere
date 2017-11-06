package io.shardingjdbc.orchestration.reg.base;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;

/**
 * Configuration Center
 *
 * @author junxiong
 */
public interface ShardConfigCenter extends AutoCloseable {

    /**
     * Open shard config center, setup event listeners.
     */
    void open();

    /**
     * save or update master slave rule configuration
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
