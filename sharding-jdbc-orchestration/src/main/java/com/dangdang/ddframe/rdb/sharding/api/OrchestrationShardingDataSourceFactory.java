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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
        registryCenter.init();
        registryCenter.persist("/" + name + "/config/datasource", DataSourceJsonConverter.toJson(dataSourceMap));
        registryCenter.persist("/" + name + "/config/sharding", GsonFactory.getGson().toJson(shardingRuleConfig));
        return new ShardingDataSource(shardingRuleConfig.build(dataSourceMap));
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
        registryCenter.init();
        registryCenter.persist(name + "/config/datasource", DataSourceJsonConverter.toJson(dataSourceMap));
        registryCenter.persist(name + "/config/sharding", GsonFactory.getGson().toJson(shardingRuleConfig));
        // TODO props
        return new ShardingDataSource(shardingRuleConfig.build(dataSourceMap), props);
    }
}
