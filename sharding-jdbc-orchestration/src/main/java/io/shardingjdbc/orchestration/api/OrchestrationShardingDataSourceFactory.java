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

package io.shardingjdbc.orchestration.api;

import io.shardingjdbc.core.api.MasterSlaveDataSourceFactory;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.yaml.sharding.YamlShardingRuleConfiguration;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.OrchestrationShardingDataSource;
import io.shardingjdbc.orchestration.yaml.YamlOrchestrationShardingRuleConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration sharding data source factory.
 *
 * @author zhangliang
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrchestrationShardingDataSourceFactory {
    
    /**
     * Create sharding data source.
     *
     * @param dataSourceMap data source map
     * @param shardingRuleConfig sharding rule configuration
     * @param orchestrationConfig orchestration master-slave configuration
     * @param configMap config map
     * @param props properties for data source
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(
            final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig, 
            final Map<String, Object> configMap, final Properties props, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        processDataSourceMapWithMasterSlave(dataSourceMap, shardingRuleConfig);
        OrchestrationShardingDataSource result = new OrchestrationShardingDataSource(dataSourceMap, shardingRuleConfig, configMap, props, orchestrationConfig);
        result.init();
        return result;
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
        YamlOrchestrationShardingRuleConfiguration config = unmarshal(yamlFile);
        YamlShardingRuleConfiguration shardingRuleConfig = config.getShardingRule();
        return createDataSource(config.getDataSources(), shardingRuleConfig.getShardingRuleConfiguration(), 
                shardingRuleConfig.getConfigMap(), shardingRuleConfig.getProps(), config.getOrchestration().getOrchestrationConfiguration());
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
        YamlOrchestrationShardingRuleConfiguration config = unmarshal(yamlFile);
        YamlShardingRuleConfiguration shardingRuleConfig = config.getShardingRule();
        return createDataSource(dataSourceMap, shardingRuleConfig.getShardingRuleConfiguration(), 
                shardingRuleConfig.getConfigMap(), shardingRuleConfig.getProps(), config.getOrchestration().getOrchestrationConfiguration());
    }
    
    /**
     * Create sharding data source.
     *
     * @param yamlByteArray yaml byte array for rule configuration of databases and tables sharding with data sources
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final byte[] yamlByteArray) throws SQLException {
        YamlOrchestrationShardingRuleConfiguration config = unmarshal(yamlByteArray);
        YamlShardingRuleConfiguration shardingRuleConfig = config.getShardingRule();
        return createDataSource(config.getDataSources(), shardingRuleConfig.getShardingRuleConfiguration(),  
                shardingRuleConfig.getConfigMap(), shardingRuleConfig.getProps(), config.getOrchestration().getOrchestrationConfiguration());
    }
    
    /**
     * Create sharding data source.
     *
     * @param dataSourceMap data source map
     * @param yamlByteArray yaml byte array for rule configuration of databases and tables sharding without data sources
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final byte[] yamlByteArray) throws SQLException {
        YamlOrchestrationShardingRuleConfiguration config = unmarshal(yamlByteArray);
        YamlShardingRuleConfiguration shardingRuleConfig = config.getShardingRule();
        return createDataSource(dataSourceMap, shardingRuleConfig.getShardingRuleConfiguration(),  
                shardingRuleConfig.getConfigMap(), shardingRuleConfig.getProps(), config.getOrchestration().getOrchestrationConfiguration());
    }
    
    private static YamlOrchestrationShardingRuleConfiguration unmarshal(final File yamlFile) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            return new Yaml(new Constructor(YamlOrchestrationShardingRuleConfiguration.class)).loadAs(inputStreamReader, YamlOrchestrationShardingRuleConfiguration.class);
        }
    }
    
    private static YamlOrchestrationShardingRuleConfiguration unmarshal(final byte[] yamlByteArray) {
        return new Yaml(new Constructor(YamlOrchestrationShardingRuleConfiguration.class)).loadAs(new ByteArrayInputStream(yamlByteArray), YamlOrchestrationShardingRuleConfiguration.class);
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
