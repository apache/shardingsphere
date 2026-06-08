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

package org.apache.shardingsphere.mcp.feature.sharding;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ShardingFeatureDefinitionTest {
    
    @Test
    void assertConstants() {
        assertThat(ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND.getValue(), is("sharding.table.rule"));
        assertThat(ShardingFeatureDefinition.TABLE_REFERENCE_WORKFLOW_KIND.getValue(), is("sharding.table.reference"));
        assertThat(ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND.getValue(), is("sharding.default.strategy"));
        assertThat(ShardingFeatureDefinition.KEY_GENERATOR_WORKFLOW_KIND.getValue(), is("sharding.key.generator"));
        assertThat(ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND.getValue(), is("sharding.key.generate.strategy"));
        assertThat(ShardingFeatureDefinition.COMPONENT_CLEANUP_WORKFLOW_KIND.getValue(), is("sharding.component.cleanup"));
        assertThat(ShardingFeatureDefinition.PLAN_TABLE_RULE_TOOL_NAME, is("database_gateway_plan_sharding_table_rule"));
        assertThat(ShardingFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI, is("shardingsphere://features/sharding/algorithm-plugins"));
        assertThat(ShardingFeatureDefinition.RULE_COUNT_RESOURCE_URI, is("shardingsphere://features/sharding/databases/{database}/rule-count"));
    }
}
