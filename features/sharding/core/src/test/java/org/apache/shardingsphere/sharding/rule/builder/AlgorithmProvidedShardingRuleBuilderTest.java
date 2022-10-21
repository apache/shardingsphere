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

import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRuleBuilder;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRuleBuilderFactory;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class AlgorithmProvidedShardingRuleBuilderTest {
    
    private AlgorithmProvidedShardingRuleConfiguration ruleConfig;
    
    @SuppressWarnings("rawtypes")
    private DatabaseRuleBuilder builder;
    
    @Before
    public void setUp() {
        ruleConfig = new AlgorithmProvidedShardingRuleConfiguration();
        builder = DatabaseRuleBuilderFactory.getInstanceMap(Collections.singletonList(ruleConfig)).get(ruleConfig);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertBuild() {
        assertThat(builder.build(ruleConfig, "sharding_db",
                Collections.singletonMap("name", mock(DataSource.class, RETURNS_DEEP_STUBS)), Collections.emptyList(), mock(InstanceContext.class)), instanceOf(ShardingRule.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithNullDataSourceMap() {
        assertThat(builder.build(ruleConfig, "sharding_db", null, Collections.emptyList(), mock(InstanceContext.class)), instanceOf(ShardingRule.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyDataSourceMap() {
        assertThat(builder.build(ruleConfig, "sharding_db", Collections.emptyMap(), Collections.emptyList(), mock(InstanceContext.class)), instanceOf(ShardingRule.class));
    }
}
