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

package org.apache.shardingsphere.driver.api.yaml;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlGlobalRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.YamlModeConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ShardingSphere data source factory for YAML.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlShardingSphereDataSourceFactory {
    
    private static final YamlRuleConfigurationSwapperEngine SWAPPER_ENGINE = new YamlRuleConfigurationSwapperEngine();
    
    private static final YamlDataSourceConfigurationSwapper DATA_SOURCE_SWAPPER = new YamlDataSourceConfigurationSwapper();
    
    /**
     * Create ShardingSphere data source without cache.
     * 
     * @param rootConfig rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSourceWithoutCache(final YamlRootConfiguration rootConfig) throws SQLException {
        Map<String, DataSource> dataSourceMap = DATA_SOURCE_SWAPPER.swapToDataSources(rootConfig.getDataSources(), false);
        try {
            return createDataSource(dataSourceMap, rootConfig);
            // CHECKSTYLE:OFF
        } catch (final SQLException | RuntimeException ex) {
            // CHECKSTYLE:ON
            dataSourceMap.values().stream().map(DataSourcePoolDestroyer::new).forEach(DataSourcePoolDestroyer::asyncDestroy);
            throw ex;
        }
    }
    
    private static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final YamlRootConfiguration rootConfig) throws SQLException {
        ModeConfiguration modeConfig = null == rootConfig.getMode() ? null : new YamlModeConfigurationSwapper().swapToObject(rootConfig.getMode());
        Collection<RuleConfiguration> ruleConfigs = SWAPPER_ENGINE.swapToRuleConfigurations(rootConfig.getRules());
        return ShardingSphereDataSourceFactory.createDataSource(rootConfig.getDatabaseName(), modeConfig, dataSourceMap, ruleConfigs, rootConfig.getProps());
    }
    
    /**
     * Create ShardingSphere data source.
     * 
     * @param yamlFile YAML file for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final File yamlFile) throws SQLException, IOException {
        YamlJDBCConfiguration rootConfig = YamlEngine.unmarshal(yamlFile, YamlJDBCConfiguration.class);
        return createDataSource(DATA_SOURCE_SWAPPER.swapToDataSources(rootConfig.getDataSources()), rootConfig);
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
        YamlJDBCConfiguration rootConfig = YamlEngine.unmarshal(yamlBytes, YamlJDBCConfiguration.class);
        return createDataSource(DATA_SOURCE_SWAPPER.swapToDataSources(rootConfig.getDataSources()), rootConfig);
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
        return createDataSource(dataSourceMap, YamlEngine.unmarshal(yamlFile, YamlJDBCConfiguration.class));
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
        return createDataSource(dataSource, YamlEngine.unmarshal(yamlFile, YamlJDBCConfiguration.class));
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
        return createDataSource(dataSourceMap, YamlEngine.unmarshal(yamlBytes, YamlJDBCConfiguration.class));
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
        return createDataSource(dataSource, YamlEngine.unmarshal(yamlBytes, YamlJDBCConfiguration.class));
    }
    
    private static DataSource createDataSource(final DataSource dataSource, final YamlJDBCConfiguration jdbcConfig) throws SQLException {
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(
                Collections.singletonMap(Strings.isNullOrEmpty(jdbcConfig.getDatabaseName()) ? DefaultDatabase.LOGIC_NAME : jdbcConfig.getDatabaseName(), dataSource));
        return createDataSource(dataSourceMap, jdbcConfig);
    }
    
    private static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final YamlJDBCConfiguration jdbcConfig) throws SQLException {
        rebuildGlobalRuleConfiguration(jdbcConfig);
        ModeConfiguration modeConfig = null == jdbcConfig.getMode() ? null : new YamlModeConfigurationSwapper().swapToObject(jdbcConfig.getMode());
        Collection<RuleConfiguration> ruleConfigs = SWAPPER_ENGINE.swapToRuleConfigurations(jdbcConfig.getRules());
        return ShardingSphereDataSourceFactory.createDataSource(jdbcConfig.getDatabaseName(), modeConfig, dataSourceMap, ruleConfigs, jdbcConfig.getProps());
    }
    
    private static void rebuildGlobalRuleConfiguration(final YamlJDBCConfiguration jdbcConfiguration) {
        jdbcConfiguration.getRules().removeIf(YamlGlobalRuleConfiguration.class::isInstance);
        if (null != jdbcConfiguration.getAuthority()) {
            jdbcConfiguration.getRules().add(jdbcConfiguration.getAuthority());
        }
        if (null != jdbcConfiguration.getTransaction()) {
            jdbcConfiguration.getRules().add(jdbcConfiguration.getTransaction());
        }
        if (null != jdbcConfiguration.getGlobalClock()) {
            jdbcConfiguration.getRules().add(jdbcConfiguration.getGlobalClock());
        }
        if (null != jdbcConfiguration.getSqlParser()) {
            jdbcConfiguration.getRules().add(jdbcConfiguration.getSqlParser());
        }
        if (null != jdbcConfiguration.getSqlTranslator()) {
            jdbcConfiguration.getRules().add(jdbcConfiguration.getSqlTranslator());
        }
        if (null != jdbcConfiguration.getTraffic()) {
            jdbcConfiguration.getRules().add(jdbcConfiguration.getTraffic());
        }
        if (null != jdbcConfiguration.getLogging()) {
            jdbcConfiguration.getRules().add(jdbcConfiguration.getLogging());
        }
        if (null != jdbcConfiguration.getSqlFederation()) {
            jdbcConfiguration.getRules().add(jdbcConfiguration.getSqlFederation());
        }
    }
}
