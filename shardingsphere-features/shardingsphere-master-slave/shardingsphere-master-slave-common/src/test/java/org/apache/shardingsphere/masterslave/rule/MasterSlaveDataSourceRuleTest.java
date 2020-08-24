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

package org.apache.shardingsphere.masterslave.rule;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration;
import org.apache.shardingsphere.masterslave.algorithm.RandomMasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.masterslave.algorithm.RoundRobinMasterSlaveLoadBalanceAlgorithm;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MasterSlaveDataSourceRuleTest {
    
    private final MasterSlaveDataSourceRule masterSlaveDataSourceRule = new MasterSlaveDataSourceRule(
            new MasterSlaveDataSourceRuleConfiguration("test_ms", "master_db", Arrays.asList("slave_db_0", "slave_db_1"), "random"), new RandomMasterSlaveLoadBalanceAlgorithm());
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewMasterSlaveDataSourceRuleWithoutName() {
        new MasterSlaveDataSourceRule(new MasterSlaveDataSourceRuleConfiguration("", "master_ds", Collections.singletonList("slave_ds"), null), new RoundRobinMasterSlaveLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewMasterSlaveDataSourceRuleWithoutMasterDataSourceName() {
        new MasterSlaveDataSourceRule(new MasterSlaveDataSourceRuleConfiguration("ds", "", Collections.singletonList("slave_ds"), null), new RoundRobinMasterSlaveLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewMasterSlaveDataSourceRuleWithNullSlaveDataSourceName() {
        new MasterSlaveDataSourceRule(new MasterSlaveDataSourceRuleConfiguration("ds", "master_ds", null, null), new RoundRobinMasterSlaveLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewMasterSlaveDataSourceRuleWithEmptySlaveDataSourceName() {
        new MasterSlaveDataSourceRule(new MasterSlaveDataSourceRuleConfiguration("ds", "master_ds", Collections.emptyList(), null), new RoundRobinMasterSlaveLoadBalanceAlgorithm());
    }
    
    @Test
    public void assertGetSlaveDataSourceNamesWithoutDisabledDataSourceNames() {
        assertThat(masterSlaveDataSourceRule.getSlaveDataSourceNames(), is(Arrays.asList("slave_db_0", "slave_db_1")));
    }
    
    @Test
    public void assertGetSlaveDataSourceNamesWithDisabledDataSourceNames() {
        masterSlaveDataSourceRule.updateDisabledDataSourceNames("slave_db_0", true);
        assertThat(masterSlaveDataSourceRule.getSlaveDataSourceNames(), is(Collections.singletonList("slave_db_1")));
    }
    
    @Test
    public void assertUpdateDisabledDataSourceNamesForDisabled() {
        masterSlaveDataSourceRule.updateDisabledDataSourceNames("slave_db_0", true);
        assertThat(masterSlaveDataSourceRule.getSlaveDataSourceNames(), is(Collections.singletonList("slave_db_1")));
    }
    
    @Test
    public void assertUpdateDisabledDataSourceNamesForEnabled() {
        masterSlaveDataSourceRule.updateDisabledDataSourceNames("slave_db_0", true);
        masterSlaveDataSourceRule.updateDisabledDataSourceNames("slave_db_0", false);
        assertThat(masterSlaveDataSourceRule.getSlaveDataSourceNames(), is(Arrays.asList("slave_db_0", "slave_db_1")));
    }
    
    @Test
    public void assertGetDataSourceMapper() {
        Map<String, Collection<String>> actual = masterSlaveDataSourceRule.getDataSourceMapper();
        Map<String, Collection<String>> expected = ImmutableMap.of("test_ms", Arrays.asList("master_db", "slave_db_0", "slave_db_1"));
        assertThat(actual, is(expected));
    }
}
