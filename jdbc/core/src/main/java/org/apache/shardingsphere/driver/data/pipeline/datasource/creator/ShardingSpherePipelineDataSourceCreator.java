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

package org.apache.shardingsphere.driver.data.pipeline.datasource.creator;

import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.spi.datasource.creator.PipelineDataSourceCreator;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationConverter;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * ShardingSphere pipeline data source creator.
 */
public final class ShardingSpherePipelineDataSourceCreator implements PipelineDataSourceCreator {
    
    @Override
    public DataSource createPipelineDataSource(final Object dataSourceConfig) throws SQLException {
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(YamlEngine.marshal(dataSourceConfig), YamlRootConfiguration.class);
        enableStreamingQuery(rootConfig);
        ShardingRuleConfigurationConverter.findYamlShardingRuleConfiguration(rootConfig.getRules()).ifPresent(this::enableRangeQueryForInline);
        rootConfig.setDatabaseName(null);
        rootConfig.setSchemaName(null);
        return YamlShardingSphereDataSourceFactory.createDataSourceWithoutCache(rootConfig);
    }
    
    // TODO Another way is improving ExecuteQueryCallback.executeSQL to enable streaming query, then remove it
    private void enableStreamingQuery(final YamlRootConfiguration rootConfig) {
        // Set a large enough value to enable ConnectionMode.MEMORY_STRICTLY, make sure streaming query work.
        rootConfig.getProps().put(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY.getKey(), 100000);
    }
    
    private void enableRangeQueryForInline(final YamlShardingRuleConfiguration shardingRuleConfig) {
        for (YamlAlgorithmConfiguration each : shardingRuleConfig.getShardingAlgorithms().values()) {
            if ("INLINE".equalsIgnoreCase(each.getType())) {
                each.getProps().put("allow-range-query-with-inline-sharding", Boolean.TRUE.toString());
            }
        }
    }
    
    @Override
    public String getType() {
        return ShardingSpherePipelineDataSourceConfiguration.TYPE;
    }
}
