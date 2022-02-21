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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRuleBuilder;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.ordered.OrderedSPIRegistry;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class ShardingRuleBuilderTest {
    
    static {
        ShardingSphereServiceLoader.register(SchemaRuleBuilder.class);
    }
    
    private ShardingRuleConfiguration ruleConfig;
    
    @SuppressWarnings("rawtypes")
    private SchemaRuleBuilder builder;
    
    @Before
    public void setUp() {
        ruleConfig = new ShardingRuleConfiguration();
        builder = OrderedSPIRegistry.getRegisteredServices(SchemaRuleBuilder.class, Collections.singletonList(ruleConfig)).get(ruleConfig);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertBuild() {
        assertThat(builder.build(ruleConfig, "test_schema", 
                Collections.singletonMap("name", mock(DataSource.class, RETURNS_DEEP_STUBS)), Collections.emptyList(), new ConfigurationProperties(new Properties())), instanceOf(ShardingRule.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithNullDataSourceMap() {
        assertThat(builder.build(ruleConfig, "test_schema", null, Collections.emptyList(), new ConfigurationProperties(new Properties())), instanceOf(ShardingRule.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyDataSourceMap() {
        assertThat(builder.build(ruleConfig, "test_schema", Collections.emptyMap(), Collections.emptyList(), new ConfigurationProperties(new Properties())), instanceOf(ShardingRule.class));
    }
}
