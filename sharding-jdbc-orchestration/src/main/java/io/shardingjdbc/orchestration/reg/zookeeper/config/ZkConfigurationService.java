/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.orchestration.reg.zookeeper.config;

import com.google.common.base.Strings;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.MasterSlaveRule;
import io.shardingjdbc.orchestration.internal.json.DataSourceJsonConverter;
import io.shardingjdbc.orchestration.internal.json.GsonFactory;
import io.shardingjdbc.orchestration.internal.json.ShardingRuleConfigurationConverter;
import io.shardingjdbc.orchestration.reg.base.ConfigurationService;
import io.shardingjdbc.orchestration.reg.zookeeper.state.StateNodeStatus;
import io.shardingjdbc.orchestration.reg.zookeeper.state.datasource.DataSourceStateNode;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import io.shardingjdbc.orchestration.reg.base.RegistryChangeEvent;
import io.shardingjdbc.orchestration.reg.base.RegistryChangeListener;
import io.shardingjdbc.orchestration.reg.base.RegistryChangeType;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration service.
 * 
 * @author caohao
 */
public final class ZkConfigurationService implements ConfigurationService {
    
    private final ConfigurationNode configNode;
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final String name;
    
    private final boolean isOverwrite;
    
    public ZkConfigurationService(String name, boolean isOverwrite, CoordinatorRegistryCenter registryCenter) {
        this.name = name;
        this.isOverwrite = isOverwrite;
        this.regCenter = registryCenter;
        this.configNode = new ConfigurationNode(name);
    }
    
    /**
     * Persist sharding configuration.
     *
     * @param dataSourceMap data source map
     * @param shardingRuleConfig sharding rule configuration
     * @param props sharding properties
     * @param shardingDataSource sharding datasource
     */
    @Override
    public void persistShardingConfiguration(
            final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig, final Properties props, final ShardingDataSource shardingDataSource) {
        persistDataSourceConfiguration(dataSourceMap);
        persistShardingRuleConfiguration(shardingRuleConfig);
        persistShardingProperties(props);
        addShardingConfigurationChangeListener(shardingDataSource);
    }
    
    private void persistDataSourceConfiguration(final Map<String, DataSource> dataSourceMap) {
        if (isOverwrite || !regCenter.isExisted(configNode.getFullPath(ConfigurationNode.DATA_SOURCE_NODE_PATH))) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.DATA_SOURCE_NODE_PATH), DataSourceJsonConverter.toJson(dataSourceMap));
        }
    }
    
    private void persistShardingRuleConfiguration(final ShardingRuleConfiguration shardingRuleConfig) {
        if (isOverwrite || !regCenter.isExisted(configNode.getFullPath(ConfigurationNode.SHARDING_NODE_PATH))) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.SHARDING_NODE_PATH), ShardingRuleConfigurationConverter.toJson(shardingRuleConfig));
        }
    }
    
    private void persistShardingProperties(final Properties props) {
        if (isOverwrite || !regCenter.isExisted(configNode.getFullPath(ConfigurationNode.PROPS_NODE_PATH))) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.PROPS_NODE_PATH), GsonFactory.getGson().toJson(props));
        }
    }
    
    private void addShardingConfigurationChangeListener(final ShardingDataSource shardingDataSource) {
        addShardingConfigurationNodeChangeListener(ConfigurationNode.DATA_SOURCE_NODE_PATH, shardingDataSource);
        addShardingConfigurationNodeChangeListener(ConfigurationNode.SHARDING_NODE_PATH, shardingDataSource);
        addShardingConfigurationNodeChangeListener(ConfigurationNode.PROPS_NODE_PATH, shardingDataSource);
    }
    
    private void addShardingConfigurationNodeChangeListener(final String node, final ShardingDataSource shardingDataSource) {
        String cachePath = configNode.getFullPath(node);
        regCenter.addCacheData(cachePath);
        regCenter.addRegistryChangeListener(cachePath, new RegistryChangeListener() {
            @Override
            public void onRegistryChange(RegistryChangeEvent registryChangeEvent) throws Exception {
                if (RegistryChangeType.UPDATED == registryChangeEvent.getType() && registryChangeEvent.getPayload().isPresent()) {
                    shardingDataSource.renew(loadShardingRuleConfiguration().build(loadDataSourceMap()), loadShardingProperties());
                }
            }
        });
    }
    
    /**
     * Persist master-slave configuration.
     *
     * @param dataSourceMap data source map
     * @param masterSlaveRuleConfig master-slave rule configuration
     * @param masterSlaveDataSource master-slave datasource
     */
    @Override
    public void persistMasterSlaveConfiguration(
            final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final MasterSlaveDataSource masterSlaveDataSource) {
        persistDataSourceConfiguration(dataSourceMap);
        persistMasterSlaveRuleConfiguration(masterSlaveRuleConfig);
        addMasterSlaveConfigurationChangeListener(masterSlaveDataSource);
    }
    
    private void persistMasterSlaveRuleConfiguration(final MasterSlaveRuleConfiguration masterSlaveRuleConfig) {
        if (isOverwrite || !regCenter.isExisted(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_NODE_PATH))) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_NODE_PATH), GsonFactory.getGson().toJson(masterSlaveRuleConfig));
        }
    }
    
    private void addMasterSlaveConfigurationChangeListener(final MasterSlaveDataSource masterSlaveDataSource) {
        addMasterSlaveConfigurationChangeListener(ConfigurationNode.DATA_SOURCE_NODE_PATH, masterSlaveDataSource);
        addMasterSlaveConfigurationChangeListener(ConfigurationNode.MASTER_SLAVE_NODE_PATH, masterSlaveDataSource);
    }
    
    private void addMasterSlaveConfigurationChangeListener(final String node, final MasterSlaveDataSource masterSlaveDataSource) {
        String cachePath = configNode.getFullPath(node);
        regCenter.addCacheData(cachePath);
        regCenter.addRegistryChangeListener(cachePath, new RegistryChangeListener() {
            @Override
            public void onRegistryChange(RegistryChangeEvent registryChangeEvent) throws Exception {
                if (RegistryChangeType.UPDATED == registryChangeEvent.getType() && registryChangeEvent.getPayload().isPresent()) {
                    masterSlaveDataSource.renew(getAvailableMasterSlaveRule());
                }
            }
        });
    }
    
    /**
     * Load data source configuration.
     * 
     * @return data source configuration map
     */
    @Override
    public Map<String, DataSource> loadDataSourceMap() {
        return DataSourceJsonConverter.fromJson(regCenter.get(configNode.getFullPath(ConfigurationNode.DATA_SOURCE_NODE_PATH)));
    }
    
    /**
     * Load sharding rule configuration.
     * 
     * @return sharding rule configuration
     */
    @Override
    public ShardingRuleConfiguration loadShardingRuleConfiguration() {
        return ShardingRuleConfigurationConverter.fromJson(regCenter.get(configNode.getFullPath(ConfigurationNode.SHARDING_NODE_PATH)));
    }
    
    /**
     * Load sharding properties configuration.
     * 
     * @return sharding properties
     */
    @Override
    public Properties loadShardingProperties() {
        String data = regCenter.get(configNode.getFullPath(ConfigurationNode.PROPS_NODE_PATH));
        return Strings.isNullOrEmpty(data) ? new Properties() : GsonFactory.getGson().fromJson(data, Properties.class);
    }
    
    /**
     * Load master-slave rule configuration.
     *
     * @return master-slave rule configuration
     */
    @Override
    public MasterSlaveRuleConfiguration loadMasterSlaveRuleConfiguration() {
        return GsonFactory.getGson().fromJson(regCenter.get(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_NODE_PATH)), MasterSlaveRuleConfiguration.class);
    }
    
    /**
     * Get available master-slave rule.
     *
     * @return available master-slave rule
     */
    @Override
    public MasterSlaveRule getAvailableMasterSlaveRule() {
        Map<String, DataSource> dataSourceMap = loadDataSourceMap();
        String dataSourcesNodePath = new DataSourceStateNode(name).getFullPath();
        List<String> dataSources = regCenter.getChildrenKeys(dataSourcesNodePath);
        MasterSlaveRuleConfiguration ruleConfig = loadMasterSlaveRuleConfiguration();
        for (String each : dataSources) {
            String dataSourceName = each.substring(each.lastIndexOf("/") + 1);
            String path = dataSourcesNodePath + "/" + each;
            if (StateNodeStatus.DISABLED.toString().equalsIgnoreCase(regCenter.get(path)) && dataSourceMap.containsKey(dataSourceName)) {
                dataSourceMap.remove(dataSourceName);
                ruleConfig.getSlaveDataSourceNames().remove(dataSourceName);
            }
        }
        return ruleConfig.build(dataSourceMap);
    }
}
