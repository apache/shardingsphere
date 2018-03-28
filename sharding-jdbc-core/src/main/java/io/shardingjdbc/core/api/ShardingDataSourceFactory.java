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

package io.shardingjdbc.core.api;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.yaml.sharding.YamlShardingConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Sharding data source factory.
 * 
 * @author zhangliang 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingDataSourceFactory {
    
    /**
     * Create sharding data source.
     *
     * @param dataSourceMap data source map
     * @param shardingRuleConfig rule configuration for databases and tables sharding
     * @param configMap config map
     * @param props properties for data source
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(
            final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig, final Map<String, Object> configMap, final Properties props) throws SQLException {
        processDataSourceMapWithMasterSlave(dataSourceMap, shardingRuleConfig);
        return new ShardingDataSource(dataSourceMap, new ShardingRule(shardingRuleConfig, dataSourceMap.keySet()), configMap, props);
    }
    
    /**
     * Create sharding data source.
     *
     * @param yamlFile yaml file for rule configuration of databases and tables sharding with data sources
     * @return sharding data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final File yamlFile) throws SQLException, IOException {
        return createDataSource(YamlShardingConfiguration.unmarshal(yamlFile));
    }
    
    /**
     * Create sharding data source.
     *
     * @param yamlBytes yaml bytes for rule configuration of databases and tables sharding with data sources
     * @return sharding data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final byte[] yamlBytes) throws SQLException, IOException {
        return createDataSource(YamlShardingConfiguration.unmarshal(yamlBytes));
    }
    
    private static DataSource createDataSource(final YamlShardingConfiguration config) throws SQLException {
        return createDataSource(config.getDataSources(), config.getShardingRule().getShardingRuleConfiguration(), config.getShardingRule().getConfigMap(), config.getShardingRule().getProps());
    }
    
    /**
     * Create sharding data source.
     *
     * @param dataSourceMap data source map
     * @param yamlFile yaml file for rule configuration of databases and tables sharding without data sources
     * @return sharding data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final File yamlFile) throws SQLException, IOException {
        return createDataSource(dataSourceMap, YamlShardingConfiguration.unmarshal(yamlFile));
    }
    
    /**
     * Create sharding data source.
     *
     * @param dataSourceMap data source map
     * @param yamlBytes yaml bytes for rule configuration of databases and tables sharding without data sources
     * @return sharding data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final byte[] yamlBytes) throws SQLException, IOException {
        return createDataSource(dataSourceMap, YamlShardingConfiguration.unmarshal(yamlBytes));
    }
    
    private static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final YamlShardingConfiguration config) throws SQLException {
        return createDataSource(dataSourceMap, config.getShardingRule().getShardingRuleConfiguration(), config.getShardingRule().getConfigMap(), config.getShardingRule().getProps());
    }
    
    private static void processDataSourceMapWithMasterSlave(final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfiguration) throws SQLException {
        for (MasterSlaveRuleConfiguration each : shardingRuleConfiguration.getMasterSlaveRuleConfigs()) {
            processDataSourceMapWithMasterSlave(dataSourceMap, each);
        }
    }
    
    private static void processDataSourceMapWithMasterSlave(final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig) throws SQLException {
        Map<String, DataSource> masterSlaveDataSourceMap = new LinkedHashMap<>(masterSlaveRuleConfig.getSlaveDataSourceNames().size() + 1, 1);
        for (String each : masterSlaveRuleConfig.getSlaveDataSourceNames()) {
            masterSlaveDataSourceMap.put(each, dataSourceMap.remove(each));
        }
        masterSlaveDataSourceMap.put(masterSlaveRuleConfig.getMasterDataSourceName(), dataSourceMap.remove(masterSlaveRuleConfig.getMasterDataSourceName()));
        dataSourceMap.put(masterSlaveRuleConfig.getName(), MasterSlaveDataSourceFactory.createDataSource(masterSlaveDataSourceMap, masterSlaveRuleConfig, Collections.<String, Object>emptyMap()));
    }
}
