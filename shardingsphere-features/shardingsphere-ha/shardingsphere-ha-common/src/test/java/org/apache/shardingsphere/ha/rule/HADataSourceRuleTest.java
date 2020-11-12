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

package org.apache.shardingsphere.ha.rule;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.ha.api.config.rule.HADataSourceRuleConfiguration;
import org.apache.shardingsphere.ha.algorithm.RandomReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.ha.algorithm.RoundRobinReplicaLoadBalanceAlgorithm;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class HADataSourceRuleTest {
    
    private final HADataSourceRule haDataSourceRule = new HADataSourceRule(
            new HADataSourceRuleConfiguration("test_pr", "primary_ds", Arrays.asList("replica_ds_0", "replica_ds_1"), "random", true), new RandomReplicaLoadBalanceAlgorithm());
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewHADataSourceRuleWithoutName() {
        new HADataSourceRule(new HADataSourceRuleConfiguration("", "primary_ds", Collections.singletonList("replica_ds"), null, true), new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewHADataSourceRuleWithoutPrimaryDataSourceName() {
        new HADataSourceRule(new HADataSourceRuleConfiguration("ds", "", Collections.singletonList("replica_ds"), null, true), new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewHADataSourceRuleWithNullReplicaDataSourceName() {
        new HADataSourceRule(new HADataSourceRuleConfiguration("ds", "primary_ds", null, null, true), new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewHADataSourceRuleWithEmptyReplicaDataSourceName() {
        new HADataSourceRule(new HADataSourceRuleConfiguration("ds", "primary_ds", Collections.emptyList(), null, true), new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test
    public void assertGetReplicaDataSourceNamesWithoutDisabledDataSourceNames() {
        assertThat(haDataSourceRule.getReplicaDataSourceNames(), is(Arrays.asList("replica_ds_0", "replica_ds_1")));
    }
    
    @Test
    public void assertGetReplicaDataSourceNamesWithDisabledDataSourceNames() {
        haDataSourceRule.updateDisabledDataSourceNames("replica_ds_0", true);
        assertThat(haDataSourceRule.getReplicaDataSourceNames(), is(Collections.singletonList("replica_ds_1")));
    }
    
    @Test
    public void assertUpdateDisabledDataSourceNamesForDisabled() {
        haDataSourceRule.updateDisabledDataSourceNames("replica_ds_0", true);
        assertThat(haDataSourceRule.getReplicaDataSourceNames(), is(Collections.singletonList("replica_ds_1")));
    }
    
    @Test
    public void assertUpdateDisabledDataSourceNamesForEnabled() {
        haDataSourceRule.updateDisabledDataSourceNames("replica_ds_0", true);
        haDataSourceRule.updateDisabledDataSourceNames("replica_ds_0", false);
        assertThat(haDataSourceRule.getReplicaDataSourceNames(), is(Arrays.asList("replica_ds_0", "replica_ds_1")));
    }
    
    @Test
    public void assertGetDataSourceMapper() {
        Map<String, Collection<String>> actual = haDataSourceRule.getDataSourceMapper();
        Map<String, Collection<String>> expected = ImmutableMap.of("test_pr", Arrays.asList("primary_ds", "replica_ds_0", "replica_ds_1"));
        assertThat(actual, is(expected));
    }
}
