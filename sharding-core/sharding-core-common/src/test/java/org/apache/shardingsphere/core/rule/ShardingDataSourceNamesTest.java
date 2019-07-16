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

package org.apache.shardingsphere.core.rule;

import com.google.common.collect.Sets;
import org.apache.shardingsphere.api.config.masterslave.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class ShardingDataSourceNamesTest {
    
    @Test
    public void assertGetAllDataSourceNames() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultDataSourceName("default_ds");
        shardingRuleConfig.getMasterSlaveRuleConfigs().add(
                new MasterSlaveRuleConfiguration("ms_ds", "master_ds", Collections.singletonList("slave_ds"), new LoadBalanceStrategyConfiguration("ROUND_ROBIN")));
        Collection<String> actual = new ShardingDataSourceNames(shardingRuleConfig, Arrays.asList("default_ds", "master_ds", "slave_ds")).getDataSourceNames();
        assertThat(actual, CoreMatchers.<Collection<String>>is(Sets.newLinkedHashSet(Arrays.asList("default_ds", "ms_ds"))));
    }
    
    @Test
    public void assertGetDefaultDataSourceNameWithDefaultDataSourceName() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultDataSourceName("default_ds");
        shardingRuleConfig.getMasterSlaveRuleConfigs().add(
                new MasterSlaveRuleConfiguration("ms_ds", "master_ds", Collections.singletonList("slave_ds"), new LoadBalanceStrategyConfiguration("ROUND_ROBIN")));
        String actual = new ShardingDataSourceNames(shardingRuleConfig, Arrays.asList("default_ds", "master_ds", "slave_ds")).getDefaultDataSourceName();
        assertThat(actual, is("default_ds"));
    }
    
    @Test
    public void assertGetDefaultDataSourceNameWithoutDefaultDataSourceName() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getMasterSlaveRuleConfigs().add(
                new MasterSlaveRuleConfiguration("ms_ds", "master_ds", Collections.singletonList("slave_ds"), new LoadBalanceStrategyConfiguration("ROUND_ROBIN")));
        String actual = new ShardingDataSourceNames(shardingRuleConfig, Arrays.asList("default_ds", "master_ds", "slave_ds")).getDefaultDataSourceName();
        assertNull(actual);
    }
    
    @Test
    public void assertGetDefaultDataSourceNameWithOnlyOneDataSourceName() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getMasterSlaveRuleConfigs().add(
                new MasterSlaveRuleConfiguration("ms_ds", "master_ds", Collections.singletonList("slave_ds"), new LoadBalanceStrategyConfiguration("ROUND_ROBIN")));
        String actual = new ShardingDataSourceNames(shardingRuleConfig, Arrays.asList("master_ds", "slave_ds")).getDefaultDataSourceName();
        assertThat(actual, is("ms_ds"));
    }
    
    @Test
    public void assertGetDefaultDataSourceNameWithMasterSlaveDataSourceName() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getMasterSlaveRuleConfigs().add(
                new MasterSlaveRuleConfiguration("ms_ds", "master_ds", Collections.singletonList("slave_ds"), new LoadBalanceStrategyConfiguration("ROUND_ROBIN")));
        String actual = new ShardingDataSourceNames(shardingRuleConfig, Arrays.asList("master_ds", "slave_ds")).getRawMasterDataSourceName("ms_ds");
        assertThat(actual, is("master_ds"));
    }
    
    @Test
    public void assertGetDefaultDataSourceNameWithoutMasterSlaveDataSourceName() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultDataSourceName("default_ds");
        shardingRuleConfig.getMasterSlaveRuleConfigs().add(
                new MasterSlaveRuleConfiguration("ms_ds", "master_ds", Collections.singletonList("slave_ds"), new LoadBalanceStrategyConfiguration("ROUND_ROBIN")));
        String actual = new ShardingDataSourceNames(shardingRuleConfig, Arrays.asList("master_ds", "slave_ds")).getRawMasterDataSourceName("default_ds");
        assertThat(actual, is("default_ds"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertConstructShardingDataSourceNamesWithNullShardingRuleConfiguration() {
        new ShardingDataSourceNames(null, Arrays.asList("master_ds", "slave_ds")).getRawMasterDataSourceName("default_ds");
    }
}
