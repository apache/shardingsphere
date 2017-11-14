package io.shardingjdbc.orchestration.internal;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.api.Orchestrator;
import io.shardingjdbc.orchestration.api.config.OrchestratorConfiguration;
import io.shardingjdbc.orchestration.reg.base.ConfigurationService;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import io.shardingjdbc.orchestration.reg.base.DataSourceService;
import io.shardingjdbc.orchestration.reg.base.InstanceStateService;
import io.shardingjdbc.orchestration.reg.zookeeper.ZookeeperConfiguration;
import io.shardingjdbc.orchestration.reg.zookeeper.ZookeeperRegistryCenter;
import io.shardingjdbc.orchestration.reg.zookeeper.config.ZkConfigurationService;
import io.shardingjdbc.orchestration.reg.zookeeper.state.datasource.ZkDataSourceService;
import io.shardingjdbc.orchestration.reg.zookeeper.state.instance.ZkInstanceStateService;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Orchestration service facade， default implementation.
 *
 * @author zhangliang
 */
public class OrchestratorImpl implements Orchestrator {

    private final ConfigurationService configurationService;
    private final InstanceStateService instanceStateService;
    private final DataSourceService dataSourceService;

    public OrchestratorImpl(ConfigurationService configurationService,
                            InstanceStateService instanceStateService,
                            DataSourceService dataSourceService) {
        this.configurationService = configurationService;
        this.instanceStateService = instanceStateService;
        this.dataSourceService = dataSourceService;
    }

    /**
     * Initial all registryCenter actions for sharding data source.
     *
     * @param dataSourceMap data source map
     * @param shardingRuleConfig sharding rule configuration
     * @param props sharding properties
     * @param shardingDataSource sharding datasource
     */
    @Override
    public void orchestrateShardingDatasource(final Map<String, DataSource> dataSourceMap,
                                              final ShardingRuleConfiguration shardingRuleConfig,
                                              final ShardingDataSource shardingDataSource,
                                              final Properties props) {
        if (shardingRuleConfig.getMasterSlaveRuleConfigs().isEmpty()) {
            reviseShardingRuleConfigurationForMasterSlave(dataSourceMap, shardingRuleConfig);
        }
        configurationService.persistShardingConfiguration(getActualDataSourceMapForMasterSlave(dataSourceMap), shardingRuleConfig, props, shardingDataSource);
        instanceStateService.persistShardingInstanceOnline(shardingDataSource);
    }
    
    private void reviseShardingRuleConfigurationForMasterSlave(final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            if (entry.getValue() instanceof MasterSlaveDataSource) {
                MasterSlaveDataSource masterSlaveDataSource = (MasterSlaveDataSource) entry.getValue();
                shardingRuleConfig.getMasterSlaveRuleConfigs().add(getMasterSlaveRuleConfiguration(masterSlaveDataSource));
            }
        }
    }
    
    private Map<String, DataSource> getActualDataSourceMapForMasterSlave(final Map<String, DataSource> dataSourceMap) {
        Map<String, DataSource> result = new HashMap<>();
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            if (entry.getValue() instanceof MasterSlaveDataSource) {
                MasterSlaveDataSource masterSlaveDataSource = (MasterSlaveDataSource) entry.getValue();
                result.putAll(masterSlaveDataSource.getAllDataSources());
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private MasterSlaveRuleConfiguration getMasterSlaveRuleConfiguration(final MasterSlaveDataSource masterSlaveDataSource) {
        MasterSlaveRuleConfiguration result = new MasterSlaveRuleConfiguration();
        result.setName(masterSlaveDataSource.getMasterSlaveRule().getName());
        result.setMasterDataSourceName(masterSlaveDataSource.getMasterSlaveRule().getMasterDataSourceName());
        result.setSlaveDataSourceNames(masterSlaveDataSource.getMasterSlaveRule().getSlaveDataSourceMap().keySet());
        result.setLoadBalanceAlgorithmClassName(masterSlaveDataSource.getMasterSlaveRule().getStrategy().getClass().getName());
        return result;
    }
    
    /**
     * Initial all registryCenter actions for master-slave data source.
     *
     * @param dataSourceMap data source map
     * @param masterSlaveRuleConfig sharding rule configuration
     * @param masterSlaveDataSource master-slave datasource
     */
    @Override
    public void orchestrateMasterSlaveDatasource(
            final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final MasterSlaveDataSource masterSlaveDataSource) {
        configurationService.persistMasterSlaveConfiguration(dataSourceMap, masterSlaveRuleConfig, masterSlaveDataSource);
        instanceStateService.persistMasterSlaveInstanceOnline(masterSlaveDataSource);
        dataSourceService.persistDataSourcesNodeOnline(masterSlaveDataSource);
        masterSlaveDataSource.renew(configurationService.getAvailableMasterSlaveRule());
    }
}
