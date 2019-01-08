/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.shardingjdbc.api.yaml;

import io.shardingsphere.core.yaml.sharding.YamlShardingConfiguration;
import io.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Sharding data source factory for YAML.
 * 
 * @author zhangliang 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlShardingDataSourceFactory {
    
    /**
     * Create sharding data source.
     *
     * @param yamlFile yaml file for rule configuration of databases and tables sharding with data sources
     * @return sharding data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final File yamlFile) throws SQLException, IOException {
        YamlShardingConfiguration config = YamlShardingConfiguration.unmarshal(yamlFile);
        return ShardingDataSourceFactory.createDataSource(
                config.getDataSources(), config.getShardingRule().getShardingRuleConfiguration(), config.getConfigMap(), config.getProps());
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
        YamlShardingConfiguration config = YamlShardingConfiguration.unmarshal(yamlBytes);
        return ShardingDataSourceFactory.createDataSource(
                config.getDataSources(), config.getShardingRule().getShardingRuleConfiguration(), config.getConfigMap(), config.getProps());
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
        YamlShardingConfiguration config = YamlShardingConfiguration.unmarshal(yamlFile);
        return ShardingDataSourceFactory.createDataSource(
                dataSourceMap, config.getShardingRule().getShardingRuleConfiguration(), config.getConfigMap(), config.getProps());
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
        YamlShardingConfiguration config = YamlShardingConfiguration.unmarshal(yamlBytes);
        return ShardingDataSourceFactory.createDataSource(
                dataSourceMap, config.getShardingRule().getShardingRuleConfiguration(), config.getConfigMap(), config.getProps());
    }
}
