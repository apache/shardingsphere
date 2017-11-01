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

package io.shardingjdbc.orchestration.internal.config;

import com.google.common.base.Strings;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.json.DataSourceJsonConverter;
import io.shardingjdbc.orchestration.internal.json.GsonFactory;
import io.shardingjdbc.orchestration.internal.json.ShardingRuleConfigurationConverter;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration service.
 * 
 * @author caohao
 */
public final class ConfigurationService {
    
    private final ConfigurationNode configNode;
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final boolean isOverwrite;
    
    public ConfigurationService(final OrchestrationConfiguration config) {
        configNode = new ConfigurationNode(config.getName());
        regCenter = config.getRegistryCenter();
        isOverwrite = config.isOverwrite();
    }
    
    /**
     * Persist sharding configuration.
     *
     * @param dataSourceMap data source map
     * @param shardingRuleConfig sharding rule configuration
     * @param props sharding properties
     * @param shardingDataSource sharding datasource
     */
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
        TreeCache cache = (TreeCache) regCenter.getRawCache(cachePath);
        cache.getListenable().addListener(new TreeCacheListener() {
            
            @Override
            public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
                ChildData childData = event.getData();
                if (null == childData || childData.getPath().isEmpty() || null == childData.getData()) {
                    return;
                }
                shardingDataSource.renew(loadShardingRuleConfiguration().build(loadDataSourceMap()), loadShardingProperties());
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
        TreeCache cache = (TreeCache) regCenter.getRawCache(cachePath);
        cache.getListenable().addListener(new TreeCacheListener() {
            
            @Override
            public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
                ChildData childData = event.getData();
                if (null == childData || childData.getPath().isEmpty() || null == childData.getData()) {
                    return;
                }
                masterSlaveDataSource.renew(loadMasterSlaveRuleConfiguration().build(loadDataSourceMap()));
            }
        });
    }
    
    /**
     * Load data source configuration.
     * 
     * @return data source configuration map
     */
    public Map<String, DataSource> loadDataSourceMap() {
        return DataSourceJsonConverter.fromJson(regCenter.get(configNode.getFullPath(ConfigurationNode.DATA_SOURCE_NODE_PATH)));
    }
    
    /**
     * Load sharding rule configuration.
     * 
     * @return sharding rule configuration
     */
    public ShardingRuleConfiguration loadShardingRuleConfiguration() {
        return ShardingRuleConfigurationConverter.fromJson(regCenter.get(configNode.getFullPath(ConfigurationNode.SHARDING_NODE_PATH)));
    }
    
    /**
     * Load sharding properties configuration.
     * 
     * @return sharding properties
     */
    public Properties loadShardingProperties() {
        String data = regCenter.get(configNode.getFullPath(ConfigurationNode.PROPS_NODE_PATH));
        return Strings.isNullOrEmpty(data) ? new Properties() : GsonFactory.getGson().fromJson(data, Properties.class);
    }
    
    /**
     * Load master-slave rule configuration.
     *
     * @return master-slave rule configuration
     */
    public MasterSlaveRuleConfiguration loadMasterSlaveRuleConfiguration() {
        return GsonFactory.getGson().fromJson(regCenter.get(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_NODE_PATH)), MasterSlaveRuleConfiguration.class);
    }
}
