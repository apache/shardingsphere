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

package org.apache.shardingsphere.sharding.rule.builder;

import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRuleBuilder;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.exception.metadata.MissingRequiredShardingConfigurationException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class ShardingRuleBuilderTest {
    
    private ShardingRuleConfiguration ruleConfig;
    
    @SuppressWarnings("rawtypes")
    private DatabaseRuleBuilder builder;
    
    @BeforeEach
    void setUp() {
        ruleConfig = new ShardingRuleConfiguration();
        builder = OrderedSPILoader.getServices(DatabaseRuleBuilder.class, Collections.singleton(ruleConfig)).get(ruleConfig);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertBuild() {
        assertThat(builder.build(ruleConfig, "sharding_db", mock(), mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), Collections.emptyList(), mock(ComputeNodeInstanceContext.class)),
                isA(ShardingRule.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertBuildWithEmptyDataSourceMap() {
        assertThrows(MissingRequiredShardingConfigurationException.class,
                () -> builder.build(ruleConfig, "sharding_db", mock(), mock(ResourceMetaData.class), Collections.emptyList(), mock(ComputeNodeInstanceContext.class)));
    }
}
