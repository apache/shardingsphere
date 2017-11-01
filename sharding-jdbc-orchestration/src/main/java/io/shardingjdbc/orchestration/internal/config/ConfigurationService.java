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
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ConfigurationNode configNode;
    
    public ConfigurationService(final String name, final CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
        configNode = new ConfigurationNode(name);
    }
    
    /**
     * Persist sharding configuration.
     *
     * @param config orchestration sharding configuration
     * @param props sharding properties
     */
    public void persistShardingConfiguration(final OrchestrationShardingConfiguration config, final Properties props, final ShardingDataSource shardingDataSource) {
        persistShardingRuleConfiguration(config.getShardingRuleConfig(), config.isOverwrite());
        persistShardingProperties(props, config.isOverwrite());
        persistDataSourceConfiguration(config.getDataSourceMap(), config.isOverwrite());
        addShardingConfigurationChangeListener(shardingDataSource);
    }
    
    private void persistShardingRuleConfiguration(final ShardingRuleConfiguration config, final boolean isOverwrite) {
        if (isOverwrite || !regCenter.isExisted(configNode.getFullPath(ConfigurationNode.SHARDING_NODE_PATH))) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.SHARDING_NODE_PATH), ShardingRuleConfigurationConverter.toJson(config));
        }
    }
    
    private void persistShardingProperties(final Properties props, final boolean isOverwrite) {
        if (isOverwrite || !regCenter.isExisted(configNode.getFullPath(ConfigurationNode.PROPS_NODE_PATH))) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.PROPS_NODE_PATH), GsonFactory.getGson().toJson(props));
        }
    }
    
    private void persistDataSourceConfiguration(final Map<String, DataSource> dataSourceMap, final boolean isOverwrite) {
        if (isOverwrite || !regCenter.isExisted(configNode.getFullPath(ConfigurationNode.DATA_SOURCE_NODE_PATH))) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.DATA_SOURCE_NODE_PATH), DataSourceJsonConverter.toJson(dataSourceMap));
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
     * @param config orchestration master-slave configuration
     */
    public void persistMasterSlaveConfiguration(final OrchestrationMasterSlaveConfiguration config, final MasterSlaveDataSource masterSlaveDataSource) {
        persistMasterSlaveRuleConfiguration(config.getMasterSlaveRuleConfiguration(), config.isOverwrite());
        persistDataSourceConfiguration(config.getDataSourceMap(), config.isOverwrite());
        addMasterSlaveConfigurationChangeListener(masterSlaveDataSource);
    }
    
    private void persistMasterSlaveRuleConfiguration(final MasterSlaveRuleConfiguration config, final boolean isOverwrite) {
        if (isOverwrite || !regCenter.isExisted(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_NODE_PATH))) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_NODE_PATH), GsonFactory.getGson().toJson(config));
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
