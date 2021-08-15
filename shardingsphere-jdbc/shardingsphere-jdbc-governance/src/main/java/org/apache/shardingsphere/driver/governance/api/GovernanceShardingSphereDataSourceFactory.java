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

package org.apache.shardingsphere.driver.governance.api;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.driver.governance.internal.datasource.GovernanceShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.mode.config.ModeConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Governance ShardingSphere data source factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GovernanceShardingSphereDataSourceFactory {
    
    /**
     * Create ShardingSphere data source.
     *
     * @param schemaName schema name
     * @param modeConfig mode configuration
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final String schemaName, final ModeConfiguration modeConfig) throws SQLException {
        return new GovernanceShardingSphereDataSource(Strings.isNullOrEmpty(schemaName) ? DefaultSchema.LOGIC_NAME : schemaName, modeConfig);
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param modeConfig mode configuration
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final ModeConfiguration modeConfig) throws SQLException {
        return createDataSource(DefaultSchema.LOGIC_NAME, modeConfig);
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param schemaName schema name
     * @param modeConfig mode configuration
     * @param dataSourceMap data source map
     * @param ruleConfigs rule configurations
     * @param props properties for data source
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final String schemaName, final ModeConfiguration modeConfig, final Map<String, DataSource> dataSourceMap, 
                                              final Collection<RuleConfiguration> ruleConfigs, final Properties props) throws SQLException {
        if (null == ruleConfigs || ruleConfigs.isEmpty()) {
            return createDataSource(schemaName, modeConfig);
        }
        return new GovernanceShardingSphereDataSource(Strings.isNullOrEmpty(schemaName) ? DefaultSchema.LOGIC_NAME : schemaName, modeConfig, dataSourceMap, ruleConfigs, props);
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param schemaName schema name
     * @param dataSource data source
     * @param ruleConfigs rule configurations
     * @param modeConfig mode configuration
     * @param props properties for data source
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final String schemaName, final ModeConfiguration modeConfig, final DataSource dataSource, final Collection<RuleConfiguration> ruleConfigs,
                                              final Properties props) throws SQLException {
        return createDataSource(schemaName, modeConfig, createSingleDataSourceMap(schemaName, dataSource), ruleConfigs, props);
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param modeConfig mode configuration
     * @param dataSourceMap data source map
     * @param ruleConfigs rule configurations
     * @param props properties for data source
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final ModeConfiguration modeConfig, final Map<String, DataSource> dataSourceMap, 
                                              final Collection<RuleConfiguration> ruleConfigs, final Properties props) throws SQLException {
        return createDataSource(DefaultSchema.LOGIC_NAME, modeConfig, dataSourceMap, ruleConfigs, props);
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param modeConfig mode configuration
     * @param dataSource data source
     * @param ruleConfigs rule configurations
     * @param props properties for data source
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final ModeConfiguration modeConfig, final DataSource dataSource, 
                                              final Collection<RuleConfiguration> ruleConfigs, final Properties props) throws SQLException {
        return createDataSource(DefaultSchema.LOGIC_NAME, modeConfig, createSingleDataSourceMap(DefaultSchema.LOGIC_NAME, dataSource), ruleConfigs, props);
    }
    
    private static Map<String, DataSource> createSingleDataSourceMap(final String schemaName, final DataSource dataSource) {
        Map<String, DataSource> result = new HashMap<>(1, 1);
        result.put(Strings.isNullOrEmpty(schemaName) ? DefaultSchema.LOGIC_NAME : schemaName, dataSource);
        return result;
    }
}
