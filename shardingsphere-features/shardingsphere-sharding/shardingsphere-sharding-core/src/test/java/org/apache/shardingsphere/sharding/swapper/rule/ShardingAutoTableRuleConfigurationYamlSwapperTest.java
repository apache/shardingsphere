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

package org.apache.shardingsphere.sharding.swapper.rule;

import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.ShardingAutoTableRuleConfigurationYamlSwapper;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingAutoTableRuleConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapToObject() {
        YamlShardingAutoTableRuleConfiguration yamlConfig = mock(YamlShardingAutoTableRuleConfiguration.class);
        when(yamlConfig.getLogicTable()).thenReturn("foo_table");
        when(yamlConfig.getActualDataSources()).thenReturn("ds_0,ds_1");
        ShardingTableRuleConfiguration expect = new ShardingAutoTableRuleConfigurationYamlSwapper().swapToObject(yamlConfig, 5);
        assertThat(expect.getActualDataNodes(), is("ds_1.foo_table_0,ds_0.foo_table_1,ds_1.foo_table_2,ds_0.foo_table_3,ds_1.foo_table_4"));
        assertThat(expect.getLogicTable(), is("foo_table"));
        assertTrue(expect.getDatabaseShardingStrategy() instanceof NoneShardingStrategyConfiguration);
    }
}
