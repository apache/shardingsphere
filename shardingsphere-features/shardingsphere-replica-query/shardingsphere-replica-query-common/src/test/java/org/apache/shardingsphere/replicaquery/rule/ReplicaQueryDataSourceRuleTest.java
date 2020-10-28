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

package org.apache.shardingsphere.replicaquery.rule;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.replicaquery.api.config.rule.ReplicaQueryDataSourceRuleConfiguration;
import org.apache.shardingsphere.replicaquery.algorithm.RandomReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.replicaquery.algorithm.RoundRobinReplicaLoadBalanceAlgorithm;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ReplicaQueryDataSourceRuleTest {
    
    private final ReplicaQueryDataSourceRule replicaQueryDataSourceRule = new ReplicaQueryDataSourceRule(
            new ReplicaQueryDataSourceRuleConfiguration("test_pr", "primary_ds", Arrays.asList("replica_ds_0", "replica_ds_1"), "random"), new RandomReplicaLoadBalanceAlgorithm());
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewReplicaQueryDataSourceRuleWithoutName() {
        new ReplicaQueryDataSourceRule(new ReplicaQueryDataSourceRuleConfiguration("", "primary_ds", Collections.singletonList("replica_ds"), null), new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewReplicaQueryDataSourceRuleWithoutPrimaryDataSourceName() {
        new ReplicaQueryDataSourceRule(new ReplicaQueryDataSourceRuleConfiguration("ds", "", Collections.singletonList("replica_ds"), null), new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewReplicaQueryDataSourceRuleWithNullReplicaDataSourceName() {
        new ReplicaQueryDataSourceRule(new ReplicaQueryDataSourceRuleConfiguration("ds", "primary_ds", null, null), new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewReplicaQueryDataSourceRuleWithEmptyReplicaDataSourceName() {
        new ReplicaQueryDataSourceRule(new ReplicaQueryDataSourceRuleConfiguration("ds", "primary_ds", Collections.emptyList(), null), new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test
    public void assertGetReplicaDataSourceNamesWithoutDisabledDataSourceNames() {
        assertThat(replicaQueryDataSourceRule.getReplicaDataSourceNames(), is(Arrays.asList("replica_ds_0", "replica_ds_1")));
    }
    
    @Test
    public void assertGetReplicaDataSourceNamesWithDisabledDataSourceNames() {
        replicaQueryDataSourceRule.updateDisabledDataSourceNames("replica_ds_0", true);
        assertThat(replicaQueryDataSourceRule.getReplicaDataSourceNames(), is(Collections.singletonList("replica_ds_1")));
    }
    
    @Test
    public void assertUpdateDisabledDataSourceNamesForDisabled() {
        replicaQueryDataSourceRule.updateDisabledDataSourceNames("replica_ds_0", true);
        assertThat(replicaQueryDataSourceRule.getReplicaDataSourceNames(), is(Collections.singletonList("replica_ds_1")));
    }
    
    @Test
    public void assertUpdateDisabledDataSourceNamesForEnabled() {
        replicaQueryDataSourceRule.updateDisabledDataSourceNames("replica_ds_0", true);
        replicaQueryDataSourceRule.updateDisabledDataSourceNames("replica_ds_0", false);
        assertThat(replicaQueryDataSourceRule.getReplicaDataSourceNames(), is(Arrays.asList("replica_ds_0", "replica_ds_1")));
    }
    
    @Test
    public void assertGetDataSourceMapper() {
        Map<String, Collection<String>> actual = replicaQueryDataSourceRule.getDataSourceMapper();
        Map<String, Collection<String>> expected = ImmutableMap.of("test_pr", Arrays.asList("primary_ds", "replica_ds_0", "replica_ds_1"));
        assertThat(actual, is(expected));
    }
}
