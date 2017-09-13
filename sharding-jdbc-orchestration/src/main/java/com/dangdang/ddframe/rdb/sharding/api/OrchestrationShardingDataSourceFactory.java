/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.api;

import com.dangdang.ddframe.rdb.sharding.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.json.DataSourceJsonConverter;
import com.dangdang.ddframe.rdb.sharding.json.GsonFactory;
import com.dangdang.ddframe.rdb.sharding.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Charsets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
 * Orchestration sharding data source factory.
 * 
 * @author zhangliang 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrchestrationShardingDataSourceFactory {
    
    /**
     * Create sharding data source.
     * 
     * @param name name of sharding data source
     * @param registryCenter registry center
     * @param dataSourceMap data source map
     * @param shardingRuleConfig rule configuration for databases and tables sharding
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(
            final String name, final CoordinatorRegistryCenter registryCenter, final Map<String, DataSource> dataSourceMap, final ShardingRuleConfig shardingRuleConfig) throws SQLException {
        initRegistryCenter(name, registryCenter, dataSourceMap, shardingRuleConfig);
        ShardingDataSource result = new ShardingDataSource(shardingRuleConfig.build(dataSourceMap));
        addConfigurationChangeListener(name, registryCenter, result);
        return result;
    }
    
    /**
     * Create sharding data source.
     * 
     * @param name name of sharding data source
     * @param registryCenter registry center
     * @param dataSourceMap data source map
     * @param shardingRuleConfig rule configuration for databases and tables sharding
     * @param props properties for data source
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(
            final String name, final CoordinatorRegistryCenter registryCenter, final Map<String, DataSource> dataSourceMap, 
            final ShardingRuleConfig shardingRuleConfig, final Properties props) throws SQLException {
        initRegistryCenter(name, registryCenter, dataSourceMap, shardingRuleConfig);
        // TODO props
        ShardingDataSource result = new ShardingDataSource(shardingRuleConfig.build(dataSourceMap), props);
        addConfigurationChangeListener(name, registryCenter, result);
        return result;
    }
    
    private static void initRegistryCenter(final String name, 
                                           final CoordinatorRegistryCenter registryCenter, final Map<String, DataSource> dataSourceMap, final ShardingRuleConfig shardingRuleConfig) {
        registryCenter.init();
        registryCenter.persist("/" + name + "/config/datasource", DataSourceJsonConverter.toJson(dataSourceMap));
        registryCenter.persist("/" + name + "/config/sharding", GsonFactory.getGson().toJson(shardingRuleConfig));
        registryCenter.addCacheData("/" + name + "/config");
    }
    
    private static void addConfigurationChangeListener(final String name, final CoordinatorRegistryCenter registryCenter, final ShardingDataSource shardingDataSource) {
        TreeCache cache = (TreeCache) registryCenter.getRawCache("/" + name + "/config");
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
                if (("/" + name + "/config/sharding").equals(path) || ("/" + name + "/config/datasource").equals(path)) {
                    ShardingRuleConfig newShardingRuleConfig = GsonFactory.getGson().fromJson(new String(childData.getData(), Charsets.UTF_8), ShardingRuleConfig.class);
                    Map<String, DataSource> newDataSourceMap = DataSourceJsonConverter.fromJson(new String(childData.getData(), Charsets.UTF_8));
                    // TODO props
                    shardingDataSource.renew(newShardingRuleConfig.build(newDataSourceMap), new Properties());
                }
            }
        });
    }
}
