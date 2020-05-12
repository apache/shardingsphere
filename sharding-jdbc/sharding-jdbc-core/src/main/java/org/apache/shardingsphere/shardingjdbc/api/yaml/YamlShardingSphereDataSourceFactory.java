/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingjdbc.api.yaml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.core.yaml.constructor.YamlRootRuleConfigurationsConstructor;
import org.apache.shardingsphere.core.yaml.swapper.root.RuleRootConfigurationsYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.underlying.common.database.DefaultSchema;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * ShardingSphere data source factory for YAML.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlShardingSphereDataSourceFactory {
    
    /**
     * Create ShardingSphere data source.
     *
     * @param yamlFile YAML file for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final File yamlFile) throws SQLException, IOException {
        YamlRootRuleConfigurations configurations = YamlEngine.unmarshal(yamlFile, YamlRootRuleConfigurations.class, new YamlRootRuleConfigurationsConstructor());
        return ShardingSphereDataSourceFactory.createDataSource(configurations.getDataSources(), new RuleRootConfigurationsYamlSwapper().swap(configurations), configurations.getProps());
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param yamlBytes YAML bytes for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final byte[] yamlBytes) throws SQLException, IOException {
        YamlRootRuleConfigurations configurations = YamlEngine.unmarshal(yamlBytes, YamlRootRuleConfigurations.class, new YamlRootRuleConfigurationsConstructor());
        return ShardingSphereDataSourceFactory.createDataSource(configurations.getDataSources(), new RuleRootConfigurationsYamlSwapper().swap(configurations), configurations.getProps());
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param dataSourceMap data source map
     * @param yamlFile YAML file for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final File yamlFile) throws SQLException, IOException {
        YamlRootRuleConfigurations configurations = YamlEngine.unmarshal(yamlFile, YamlRootRuleConfigurations.class, new YamlRootRuleConfigurationsConstructor());
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, new RuleRootConfigurationsYamlSwapper().swap(configurations), configurations.getProps());
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param dataSource data source
     * @param yamlFile YAML file for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final DataSource dataSource, final File yamlFile) throws SQLException, IOException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put(DefaultSchema.LOGIC_NAME, dataSource);
        return createDataSource(dataSourceMap, yamlFile);
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param dataSourceMap data source map
     * @param yamlBytes YAML bytes for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final byte[] yamlBytes) throws SQLException, IOException {
        YamlRootRuleConfigurations configurations = YamlEngine.unmarshal(yamlBytes, YamlRootRuleConfigurations.class, new YamlRootRuleConfigurationsConstructor());
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, new RuleRootConfigurationsYamlSwapper().swap(configurations), configurations.getProps());
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param dataSource data source
     * @param yamlBytes YAML bytes for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final DataSource dataSource, final byte[] yamlBytes) throws SQLException, IOException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put(DefaultSchema.LOGIC_NAME, dataSource);
        return createDataSource(dataSourceMap, yamlBytes);
    }
}
