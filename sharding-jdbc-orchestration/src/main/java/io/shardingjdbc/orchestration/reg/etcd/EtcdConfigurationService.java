package io.shardingjdbc.orchestration.reg.etcd;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.MasterSlaveRule;
import io.shardingjdbc.orchestration.reg.base.ConfigurationService;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * @author junxiong
 */
public class EtcdConfigurationService implements ConfigurationService {
    private CoordinatorRegistryCenter registryCenter;

    public EtcdConfigurationService(CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
    }

    @Override
    public void persistShardingConfiguration(Map<String, DataSource> dataSourceMap, ShardingRuleConfiguration shardingRuleConfig, Properties props, ShardingDataSource shardingDataSource) {

    }

    @Override
    public void persistMasterSlaveConfiguration(Map<String, DataSource> dataSourceMap, MasterSlaveRuleConfiguration masterSlaveRuleConfig, MasterSlaveDataSource masterSlaveDataSource) {

    }

    @Override
    public Map<String, DataSource> loadDataSourceMap() {
        return null;
    }

    @Override
    public ShardingRuleConfiguration loadShardingRuleConfiguration() {
        return null;
    }

    @Override
    public Properties loadShardingProperties() {
        return null;
    }

    @Override
    public MasterSlaveRuleConfiguration loadMasterSlaveRuleConfiguration() {
        return null;
    }

    @Override
    public MasterSlaveRule getAvailableMasterSlaveRule() {
        return null;
    }
}
