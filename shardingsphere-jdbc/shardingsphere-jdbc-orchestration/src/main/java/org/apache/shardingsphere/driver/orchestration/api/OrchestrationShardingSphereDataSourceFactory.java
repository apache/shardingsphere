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

package org.apache.shardingsphere.driver.orchestration.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.driver.orchestration.internal.datasource.OrchestrationShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration ShardingSphere data source factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrchestrationShardingSphereDataSourceFactory {
    
    /**
     * Create ShardingSphere data source.
     *
     * @param dataSourceMap data source map
     * @param ruleConfigurations rule configurations
     * @param orchestrationConfig orchestration configuration
     * @param props properties for data source
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final Collection<RuleConfiguration> ruleConfigurations,
                                              final Properties props, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        if (null == ruleConfigurations || ruleConfigurations.isEmpty()) {
            return createDataSource(orchestrationConfig);
        }
        return new OrchestrationShardingSphereDataSource(dataSourceMap, ruleConfigurations, props, orchestrationConfig);
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param dataSource data source
     * @param ruleConfigurations rule configurations
     * @param orchestrationConfig orchestration configuration
     * @param props properties for data source
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final DataSource dataSource, final Collection<RuleConfiguration> ruleConfigurations,
                                              final Properties props, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put(DefaultSchema.LOGIC_NAME, dataSource);
        return createDataSource(dataSourceMap, ruleConfigurations, props, orchestrationConfig);
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param orchestrationConfig orchestration configuration
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        return new OrchestrationShardingSphereDataSource(orchestrationConfig);
    }
}
