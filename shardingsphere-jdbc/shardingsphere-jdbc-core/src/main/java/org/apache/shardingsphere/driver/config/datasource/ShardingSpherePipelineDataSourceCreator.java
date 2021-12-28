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

package org.apache.shardingsphere.driver.config.datasource;

import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.creator.PipelineDataSourceCreator;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationConverter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;

/**
 * ShardingSphere pipeline data source creator.
 */
public final class ShardingSpherePipelineDataSourceCreator implements PipelineDataSourceCreator {
    
    @Override
    public DataSource createPipelineDataSource(final Object pipelineDataSourceConfig) throws SQLException {
        YamlRootConfiguration rootConfig = (YamlRootConfiguration) pipelineDataSourceConfig;
        ShardingRuleConfiguration shardingRuleConfig = ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(rootConfig.getRules());
        enableRangeQueryForInline(shardingRuleConfig);
        return ShardingSphereDataSourceFactory.createDataSource(rootConfig.getSchemaName(), new YamlDataSourceConfigurationSwapper().swapToDataSources(rootConfig.getDataSources()), 
                Collections.singletonList(shardingRuleConfig), null);
    }
    
    private void enableRangeQueryForInline(final ShardingRuleConfiguration shardingRuleConfig) {
        for (ShardingSphereAlgorithmConfiguration each : shardingRuleConfig.getShardingAlgorithms().values()) {
            if (!"INLINE".equalsIgnoreCase(each.getType())) {
                continue;
            }
            each.getProps().put("allow-range-query-with-inline-sharding", "true");
        }
    }
    
    @Override
    public String getType() {
        return ShardingSpherePipelineDataSourceConfiguration.TYPE;
    }
}
