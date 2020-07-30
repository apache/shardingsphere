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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.generator;

import org.apache.shardingsphere.rdl.parser.binder.context.CreateShardingRuleStatementContext;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class YamlShardingRuleConfigurationGeneratorTest {
    
    private CreateShardingRuleStatementContext context;
    
    @Before
    public void setUp() {
        context = mock(CreateShardingRuleStatementContext.class);
        when(context.getLogicTable()).thenReturn("t_order");
        when(context.getDataSources()).thenReturn(Arrays.asList("ds0", "ds1"));
        when(context.getShardingColumn()).thenReturn("order_id");
        when(context.getAlgorithmType()).thenReturn("MOD");
        Properties properties = new Properties();
        properties.setProperty("sharding.count", "2");
        when(context.getAlgorithmProperties()).thenReturn(properties);
    }
    
    @Test
    public void generate() {
        YamlShardingRuleConfiguration rule = new YamlShardingRuleConfigurationGenerator().generate(context);
        assertTrue(rule.getTables().isEmpty());
        assertThat(rule.getAutoTables().size(), is(1));
        assertThat(rule.getAutoTables().get(context.getLogicTable()).getActualDataSources(), is("ds0,ds1"));
        assertThat(rule.getAutoTables().get(context.getLogicTable()).getShardingStrategy().getStandard().getShardingColumn(), is("order_id"));
        assertThat(rule.getAutoTables().get(context.getLogicTable()).getShardingStrategy().getStandard().getShardingAlgorithmName(), is("t_order_MOD"));
        assertTrue(rule.getShardingAlgorithms().containsKey("t_order_MOD"));
        assertThat(rule.getShardingAlgorithms().get("t_order_MOD").getType(), is("MOD"));
    }
}
