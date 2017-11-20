package io.shardingjdbc.orchestration.reg.etcd;

import com.google.common.base.Strings;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.MasterSlaveRule;
import io.shardingjdbc.orchestration.internal.json.DataSourceJsonConverter;
import io.shardingjdbc.orchestration.internal.json.GsonFactory;
import io.shardingjdbc.orchestration.internal.json.ShardingRuleConfigurationConverter;
import io.shardingjdbc.orchestration.reg.base.*;
import io.shardingjdbc.orchestration.reg.etcd.internal.RegistryPath;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author junxiong
 */
@Slf4j
public class EtcdConfigurationServiceImpl implements ConfigurationService {
    private String name;
    private boolean overwrite;
    private CoordinatorRegistryCenter registryCenter;

    private RegistryPath configurationRootPath;
    private RegistryPath stateRootPath;

    public EtcdConfigurationServiceImpl(String name, boolean overwrite, CoordinatorRegistryCenter registryCenter) {
        this.name = name;
        this.overwrite = overwrite;
        this.registryCenter = registryCenter;
        configurationRootPath = RegistryPath.from(this.name, "config");
        stateRootPath = RegistryPath.from(this.name, "state");
    }

    @Override
    public void persistShardingConfiguration(final Map<String, DataSource> dataSourceMap,
                                             final ShardingRuleConfiguration shardingRuleConfig,
                                             final Properties props,
                                             final ShardingDataSource shardingDataSource) {
        if (overwrite) {
            persistDataSourceMap(dataSourceMap);
            persistShardingRuleConfiguration(shardingRuleConfig);
            persistShardingProperties(props);
        }
        addShardingConfigurationChangeListener(shardingDataSource);
    }

    private void addShardingConfigurationChangeListener(final ShardingDataSource shardingDataSource) {
        String shardingConfigPrefix = configurationRootPath.asNodePath();
        registryCenter.addRegistryChangeListener(shardingConfigPrefix, new RegistryChangeListener() {
            @Override
            public void onRegistryChange(RegistryChangeEvent registryChangeEvent) throws Exception {
                if (RegistryChangeType.UPDATED == registryChangeEvent.getType() && registryChangeEvent.getPayload().isPresent()) {
                    ShardingRuleConfiguration shardingRuleConfiguration =  loadShardingRuleConfiguration();
                    if (shardingRuleConfiguration != null) {
                        shardingDataSource.renew(shardingRuleConfiguration.build(loadDataSourceMap()), loadShardingProperties());
                    } else {
                        log.warn("sharding rule is not refreshed due to sharding rule config is missing");
                    }
                }
            }
        });
    }

    @Override
    public void persistMasterSlaveConfiguration(Map<String, DataSource> dataSourceMap, MasterSlaveRuleConfiguration masterSlaveRuleConfig, MasterSlaveDataSource masterSlaveDataSource) {
        if (overwrite) {
            persistDataSourceMap(dataSourceMap);
            persistMasterSlaveRuleConfiguration(masterSlaveRuleConfig);
        }
        addMasterSlaveConfigurationChangeListener(masterSlaveDataSource);
    }

    private void addMasterSlaveConfigurationChangeListener(final MasterSlaveDataSource masterSlaveDataSource) {
        String shardingConfigPrefix = configurationRootPath.asNodePath();
        registryCenter.addRegistryChangeListener(shardingConfigPrefix, new RegistryChangeListener() {
            @Override
            public void onRegistryChange(RegistryChangeEvent registryChangeEvent) throws Exception {
                if (RegistryChangeType.UPDATED == registryChangeEvent.getType() && registryChangeEvent.getPayload().isPresent()) {
                    masterSlaveDataSource.renew(getAvailableMasterSlaveRule());
                }
            }
        });
    }

    @Override
    public Map<String, DataSource> loadDataSourceMap() {
        String key = configurationRootPath.join("datasource").asNodeKey();
        String dataSourceMap = registryCenter.get(key);
        return DataSourceJsonConverter.fromJson(dataSourceMap);
    }

    private void persistDataSourceMap(Map<String, DataSource> dataSourceMap) {
        String key = configurationRootPath.join("datasource").asNodeKey();
        registryCenter.persist(key, DataSourceJsonConverter.toJson(dataSourceMap));
    }

    @Override
    public ShardingRuleConfiguration loadShardingRuleConfiguration() {
        String key = configurationRootPath.join("sharding").asNodeKey();
        String shardingRuleConfiguration = registryCenter.get(key);
        return ShardingRuleConfigurationConverter.fromJson(shardingRuleConfiguration);
    }

    private void persistShardingRuleConfiguration(ShardingRuleConfiguration shardingRuleConfiguration) {
        String key = configurationRootPath.join("sharding").asNodeKey();
        registryCenter.persist(key, ShardingRuleConfigurationConverter.toJson(shardingRuleConfiguration));
    }

    @Override
    public Properties loadShardingProperties() {
        String key = configurationRootPath.join("props").asNodeKey();
        String props = registryCenter.get(key);
        return Strings.isNullOrEmpty(props) ? new Properties() : GsonFactory.getGson().fromJson(props, Properties.class);
    }

    private void persistShardingProperties(Properties properties) {
        String key = configurationRootPath.join("props").asNodeKey();
        registryCenter.persist(key, GsonFactory.getGson().toJson(properties));
    }

    @Override
    public MasterSlaveRuleConfiguration loadMasterSlaveRuleConfiguration() {
        String key = configurationRootPath.join("masterslave").asNodeKey();
        String masterSlaveRuleConfiguration = registryCenter.get(key);
        return GsonFactory.getGson().fromJson(masterSlaveRuleConfiguration, MasterSlaveRuleConfiguration.class);
    }

    private void persistMasterSlaveRuleConfiguration(MasterSlaveRuleConfiguration masterSlaveRuleConfiguration) {
        String key = configurationRootPath.join("masterslave").asNodeKey();
        registryCenter.persist(key, GsonFactory.getGson().toJson(masterSlaveRuleConfiguration));
    }

    @Override
    public MasterSlaveRule getAvailableMasterSlaveRule() {
        Map<String, DataSource> dataSourceMap = loadDataSourceMap();
        String dataSourcesPrefix = stateRootPath.join("datasources").asNodePath();
        List<String> dataSources = registryCenter.getChildrenKeys(dataSourcesPrefix);
        MasterSlaveRuleConfiguration ruleConfig = loadMasterSlaveRuleConfiguration();
        for (String dataSource : dataSources) {
            String dataSourceName = dataSource.substring(dataSource.lastIndexOf("/") + 1);
            String dataSourceKey = dataSourcesPrefix + "/" + dataSource;
            String dataSourceStatus = registryCenter.get(dataSourceKey);
            if (StateNodeStatus.DISABLED.toString().equalsIgnoreCase(dataSourceStatus) && dataSourceMap.containsKey(dataSourceName)) {
                dataSourceMap.remove(dataSourceName);
                ruleConfig.getSlaveDataSourceNames().remove(dataSourceName);
            }
        }
        return ruleConfig.build(dataSourceMap);
    }
}
