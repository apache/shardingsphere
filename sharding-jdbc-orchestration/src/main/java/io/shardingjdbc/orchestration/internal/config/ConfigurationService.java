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
import io.shardingjdbc.orchestration.api.config.OrchestrationMasterSlaveConfiguration;
import io.shardingjdbc.orchestration.api.config.OrchestrationShardingConfiguration;
import io.shardingjdbc.orchestration.internal.json.DataSourceJsonConverter;
import io.shardingjdbc.orchestration.internal.json.GsonFactory;
import io.shardingjdbc.orchestration.internal.json.ShardingRuleConfigurationConverter;
import io.shardingjdbc.orchestration.internal.storage.DataNodeStorage;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Data configuration service.
 * 
 * @author caohao
 */
public final class ConfigurationService {
    
    private final DataNodeStorage dataNodeStorage;
    
    public ConfigurationService(final CoordinatorRegistryCenter regCenter, final String name) {
        dataNodeStorage = new DataNodeStorage(regCenter, name);
    }
    
    public void addShardingConfiguration(final OrchestrationShardingConfiguration config, final Properties props, final ShardingDataSource shardingDataSource) throws SQLException {
        persistShardingConfiguration(config, props);
        addShardingConfigurationChangeListener(config.getName(), config.getRegistryCenter(), shardingDataSource);
    }
    
    public void addMasterSlaveConfiguration(final OrchestrationMasterSlaveConfiguration config, final MasterSlaveDataSource masterSlaveDataSource) throws SQLException {
        persistMasterSlaveConfiguration(config);
        addMasterSlaveConfigurationChangeListener(config.getName(), config.getRegistryCenter(), masterSlaveDataSource);
    }
    
    private void persistShardingConfiguration(final OrchestrationShardingConfiguration config, final Properties props) throws SQLException {
        persistShardingRuleConfiguration(config.getShardingRuleConfig(), config.isOverwrite());
        persistShardingProperties(props, config.isOverwrite());
        persistDataSourceConfiguration(config.getDataSourceMap(), config.isOverwrite());
    }
    
    private void persistMasterSlaveConfiguration(final OrchestrationMasterSlaveConfiguration config) throws SQLException {
        persistMasterSlaveRuleConfiguration(config.getMasterSlaveRuleConfiguration(), config.isOverwrite());
        persistDataSourceConfiguration(config.getDataSourceMap(), config.isOverwrite());
    }
    
    private void addMasterSlaveConfigurationChangeListener(final String name, final CoordinatorRegistryCenter registryCenter, final MasterSlaveDataSource masterSlaveDataSource) {
        String cachePath = "/" + name + "/" + ConfigurationNode.ROOT;
        registryCenter.addCacheData(cachePath);
        TreeCache cache = (TreeCache) registryCenter.getRawCache(cachePath);
        cache.getListenable().addListener(new TreeCacheListener() {
            
            @Override
            public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
                ChildData childData = event.getData();
                if (null == childData || null == childData.getData()) {
                    return;
                }
                String path = childData.getPath();
                if (path.isEmpty()) {
                    return;
                }
                masterSlaveDataSource.renew(loadMasterSlaveRuleConfiguration().build(loadDataSourceMap()));
            }
        });
    }
    
    private void addShardingConfigurationChangeListener(final String name, final CoordinatorRegistryCenter registryCenter, final ShardingDataSource shardingDataSource) {
        addShardingConfigurationChangeListener(name, ConfigurationNode.DATA_SOURCE_NODE_PATH, registryCenter, shardingDataSource);
        addShardingConfigurationChangeListener(name, ConfigurationNode.MASTER_SLAVE_NODE_PATH, registryCenter, shardingDataSource);
        addShardingConfigurationChangeListener(name, ConfigurationNode.SHARDING_NODE_PATH, registryCenter, shardingDataSource);
        addShardingConfigurationChangeListener(name, ConfigurationNode.PROPS_NODE_PATH, registryCenter, shardingDataSource);
    }
    
    private void addShardingConfigurationChangeListener(final String name, final String node, final CoordinatorRegistryCenter registryCenter, final ShardingDataSource shardingDataSource) {
        String cachePath = "/" + name + "/" + node;
        registryCenter.addCacheData(cachePath);
        TreeCache cache = (TreeCache) registryCenter.getRawCache(cachePath);
        cache.getListenable().addListener(new TreeCacheListener() {
            
            @Override
            public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws Exception {
                ChildData childData = event.getData();
                if (null == childData || null == childData.getData()) {
                    return;
                }
                String path = childData.getPath();
                if (path.isEmpty()) {
                    return;
                }
                shardingDataSource.renew(loadShardingRuleConfiguration().build(loadDataSourceMap()), loadShardingProperties());
            }
        });
    }
    
    /**
     * Load sharding properties.
     *
     * @return sharding properties
     */
    public Properties loadShardingProperties() {
        String data = dataNodeStorage.getNodeData(ConfigurationNode.PROPS_NODE_PATH);
        return Strings.isNullOrEmpty(data) ? new Properties() : GsonFactory.getGson().fromJson(data, Properties.class);
    }
    
    /**
     * Load sharding rule configuration.
     * 
     * @return sharding rule configuration
     */
    public ShardingRuleConfiguration loadShardingRuleConfiguration() {
        return ShardingRuleConfigurationConverter.fromJson(dataNodeStorage.getNodeData(ConfigurationNode.SHARDING_NODE_PATH));
    }
    
    /**
     * Load master slave rule configuration.
     *
     * @return master slave rule configuration
     */
    public MasterSlaveRuleConfiguration loadMasterSlaveRuleConfiguration() {
        return GsonFactory.getGson().fromJson(dataNodeStorage.getNodeData(ConfigurationNode.MASTER_SLAVE_NODE_PATH), MasterSlaveRuleConfiguration.class);
    }
    
    /**
     * Load data source configuration.
     *
     * @return master data source configuration
     */
    public Map<String, DataSource> loadDataSourceMap() {
        return DataSourceJsonConverter.fromJson(dataNodeStorage.getNodeData(ConfigurationNode.DATA_SOURCE_NODE_PATH));
    }
    
    /**
     * Persist sharding rule configuration.
     * 
     * @param config sharding rule configuration
     * @param isOverwrite is overwrite config
     */
    public void persistShardingRuleConfiguration(final ShardingRuleConfiguration config, final boolean isOverwrite) {
        if (!dataNodeStorage.isNodeExisted(ConfigurationNode.SHARDING_NODE_PATH) || isOverwrite) {
            dataNodeStorage.fillNode(ConfigurationNode.SHARDING_NODE_PATH, ShardingRuleConfigurationConverter.toJson(config));
        }
    }
    
    /**
     * Persist sharding properties.
     *
     * @param props sharding properties
     * @param isOverwrite is overwrite
     */
    public void persistShardingProperties(final Properties props, final boolean isOverwrite) {
        if (!dataNodeStorage.isNodeExisted(ConfigurationNode.PROPS_NODE_PATH) || isOverwrite) {
            dataNodeStorage.fillNode(ConfigurationNode.PROPS_NODE_PATH, GsonFactory.getGson().toJson(props));
        }
    }
    
    /**
     * Persist master slave rule configuration.
     *
     * @param config master slave rule configuration
     * @param isOverwrite is overwrite config
     */
    public void persistMasterSlaveRuleConfiguration(final MasterSlaveRuleConfiguration config, final boolean isOverwrite) {
        if (!dataNodeStorage.isNodeExisted(ConfigurationNode.MASTER_SLAVE_NODE_PATH) || isOverwrite) {
            dataNodeStorage.fillNode(ConfigurationNode.MASTER_SLAVE_NODE_PATH, GsonFactory.getGson().toJson(config));
        }
    }
    
    /**
     * Persist data source configuration.
     *
     * @param dataSourceMap data source map
     * @param isOverwrite is overwrite config
     */
    public void persistDataSourceConfiguration(final Map<String, DataSource> dataSourceMap, final boolean isOverwrite) {
        if (!dataNodeStorage.isNodeExisted(ConfigurationNode.DATA_SOURCE_NODE_PATH) || isOverwrite) {
            dataNodeStorage.fillNode(ConfigurationNode.DATA_SOURCE_NODE_PATH, DataSourceJsonConverter.toJson(dataSourceMap));
        }
    }
}
