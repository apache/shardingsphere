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

package org.apache.shardingsphere.shardingjdbc.orchestration.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.rule.builder.ConfigurationBuilder;
import org.apache.shardingsphere.core.rule.builder.RuleBuilder;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationShardingDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration sharding data source factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrchestrationShardingDataSourceFactory {
    
    /**
     * Create sharding data source.
     *
     * @param dataSourceMap data source map
     * @param shardingRuleConfig sharding rule configuration
     * @param orchestrationConfig orchestration configuration
     * @param props properties for data source
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig,
                                              final Properties props, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        if (null == shardingRuleConfig || shardingRuleConfig.getTableRuleConfigs().isEmpty()) {
            return createDataSource(orchestrationConfig);
        }
        ShardingDataSource shardingDataSource = new ShardingDataSource(dataSourceMap, RuleBuilder.build(dataSourceMap.keySet(), ConfigurationBuilder.buildSharding(shardingRuleConfig)), props);
        return new OrchestrationShardingDataSource(shardingDataSource, orchestrationConfig);
    }
    
    /**
     * Create sharding data source.
     *
     * @param orchestrationConfig orchestration configuration
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        return new OrchestrationShardingDataSource(orchestrationConfig);
    }
}
