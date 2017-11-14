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

package io.shardingjdbc.orchestration.api;

import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.yaml.YamlOrchestrationShardingRuleConfiguration;
import io.shardingjdbc.orchestration.yaml.YamlUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.io.*;
import java.sql.SQLException;
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
     * @param orchestrator orchestrator
     *
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(
            final Map<String, DataSource> dataSourceMap,
            final ShardingRuleConfiguration shardingRuleConfig,
            final Orchestrator orchestrator) throws SQLException {
        return createDataSource(dataSourceMap, shardingRuleConfig, orchestrator, new Properties());
    }
    
    /**
     * Create sharding data source.
     *
     * @param dataSourceMap data source map
     * @param shardingRuleConfig sharding rule configuration
     * @param orchestrator orchestrator
     * @param props properties for data source
     *
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(
            final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig,
            final Orchestrator orchestrator, final Properties props) throws SQLException {
        ShardingDataSource shardingDataSource = (ShardingDataSource) ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig);
        orchestrator.orchestrateShardingDatasource(dataSourceMap, shardingRuleConfig, shardingDataSource, props);
        return shardingDataSource;
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
        YamlOrchestrationShardingRuleConfiguration config = YamlUtils.load(yamlFile,
                YamlOrchestrationShardingRuleConfiguration.class);
        return createDataSource(config.getDataSources(),
                config.getShardingRule().getShardingRuleConfiguration(),
                OrchestratorBuilder.newBuilder().with(config.getOrchestration()).build());
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
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap,
                                              final File yamlFile) throws SQLException, IOException {
        YamlOrchestrationShardingRuleConfiguration config = YamlUtils.load(yamlFile,
                YamlOrchestrationShardingRuleConfiguration.class);
        return createDataSource(dataSourceMap,
                config.getShardingRule().getShardingRuleConfiguration(),
                OrchestratorBuilder.newBuilder().with(config.getOrchestration()).build());
    }
    
    /**
     * Create sharding data source.
     *
     * @param yamlByteArray yaml byte array for rule configuration of databases and tables sharding with data sources
     * @return sharding data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final byte[] yamlByteArray) throws SQLException, IOException {
        YamlOrchestrationShardingRuleConfiguration config = YamlUtils.load(yamlByteArray,
                YamlOrchestrationShardingRuleConfiguration.class);
        return createDataSource(config.getDataSources(),
                config.getShardingRule().getShardingRuleConfiguration(),
                OrchestratorBuilder.newBuilder().with(config.getOrchestration()).build());
    }
    
    /**
     * Create sharding data source.
     *
     * @param dataSourceMap data source map
     * @param yamlByteArray yaml byte array for rule configuration of databases and tables sharding without data sources
     * @return sharding data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap,
                                              final byte[] yamlByteArray) throws SQLException, IOException {
        YamlOrchestrationShardingRuleConfiguration config = YamlUtils.load(yamlByteArray,
                YamlOrchestrationShardingRuleConfiguration.class);
        return createDataSource(dataSourceMap,
                config.getShardingRule().getShardingRuleConfiguration(),
                OrchestratorBuilder.newBuilder().with(config.getOrchestration()).build());
    }

}
