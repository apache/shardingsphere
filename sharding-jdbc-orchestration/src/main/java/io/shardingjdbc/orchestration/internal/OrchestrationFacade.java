package io.shardingjdbc.orchestration.internal;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.api.config.OrchestrationMasterSlaveConfiguration;
import io.shardingjdbc.orchestration.api.config.OrchestrationShardingConfiguration;
import io.shardingjdbc.orchestration.internal.config.ConfigurationService;
import io.shardingjdbc.orchestration.internal.state.InstanceStateService;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Orchestration service facade.
 *
 * @author zhangliang
 */
public final class OrchestrationFacade {
    
    private final ConfigurationService configurationService;
    
    private final InstanceStateService instanceStateService;
    
    public OrchestrationFacade(final String name, final CoordinatorRegistryCenter regCenter) {
        configurationService = new ConfigurationService(name, regCenter);
        instanceStateService = new InstanceStateService(name, regCenter);
    }
    
    /**
     * Initial all orchestration actions for sharding data source.
     * 
     * @param config orchestration sharding configuration
     * @param props sharding properties
     * @param shardingDataSource sharding datasource
     */
    public void initShardingOrchestration(final OrchestrationShardingConfiguration config, final Properties props, final ShardingDataSource shardingDataSource) {
        config.getRegistryCenter().init();
        if (config.getShardingRuleConfig().getMasterSlaveRuleConfigs().isEmpty()) {
            reviseShardingRuleConfigurationForMasterSlave(config.getDataSourceMap(), config.getShardingRuleConfig());
        }
        OrchestrationShardingConfiguration actualConfig = new OrchestrationShardingConfiguration(
                config.getName(), config.isOverwrite(), config.getRegistryCenter(), getActualDataSourceMapForMasterSlave(config.getDataSourceMap()), config.getShardingRuleConfig());
        configurationService.persistShardingConfiguration(actualConfig, props, shardingDataSource);
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
     * Initial all orchestration actions for master-slave data source.
     * 
     * @param config orchestration master-slave configuration
     * @param masterSlaveDataSource master-slave datasource
     */
    public void initMasterSlaveOrchestration(final OrchestrationMasterSlaveConfiguration config, final MasterSlaveDataSource masterSlaveDataSource) {
        config.getRegistryCenter().init();
        configurationService.persistMasterSlaveConfiguration(config, masterSlaveDataSource);
        instanceStateService.persistMasterSlaveInstanceOnline(masterSlaveDataSource);
    }
}
