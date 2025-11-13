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

package org.apache.shardingsphere.data.pipeline.sharding;

import org.apache.shardingsphere.data.pipeline.core.datasource.yaml.PipelineYamlRuleConfigurationReviser;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;

/**
 * Sharding pipeline YAML rule configuration reviser.
 */
public final class ShardingPipelineYamlRuleConfigurationReviser implements PipelineYamlRuleConfigurationReviser<YamlShardingRuleConfiguration> {
    
    @Override
    public void revise(final YamlShardingRuleConfiguration yamlRuleConfig) {
        enableRangeQueryForInline(yamlRuleConfig);
        removeAuditStrategy(yamlRuleConfig);
    }
    
    private void enableRangeQueryForInline(final YamlShardingRuleConfiguration yamlRuleConfig) {
        for (YamlAlgorithmConfiguration each : yamlRuleConfig.getShardingAlgorithms().values()) {
            if ("INLINE".equalsIgnoreCase(each.getType()) || "COMPLEX_INLINE".equalsIgnoreCase(each.getType())) {
                each.getProps().setProperty("allow-range-query-with-inline-sharding", Boolean.TRUE.toString());
            }
        }
    }
    
    private void removeAuditStrategy(final YamlShardingRuleConfiguration yamlRuleConfig) {
        yamlRuleConfig.setDefaultAuditStrategy(null);
        yamlRuleConfig.setAuditors(null);
        if (null != yamlRuleConfig.getTables()) {
            yamlRuleConfig.getTables().forEach((key, value) -> value.setAuditStrategy(null));
        }
        if (null != yamlRuleConfig.getAutoTables()) {
            yamlRuleConfig.getAutoTables().forEach((key, value) -> value.setAuditStrategy(null));
        }
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<YamlShardingRuleConfiguration> getTypeClass() {
        return YamlShardingRuleConfiguration.class;
    }
}
