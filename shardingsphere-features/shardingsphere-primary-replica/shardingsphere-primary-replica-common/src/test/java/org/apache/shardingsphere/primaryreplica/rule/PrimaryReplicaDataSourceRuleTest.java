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

package org.apache.shardingsphere.primaryreplica.rule;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.primaryreplica.api.config.rule.PrimaryReplicaDataSourceRuleConfiguration;
import org.apache.shardingsphere.primaryreplica.algorithm.RandomPrimaryReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.primaryreplica.algorithm.RoundRobinPrimaryReplicaLoadBalanceAlgorithm;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class PrimaryReplicaDataSourceRuleTest {
    
    private final PrimaryReplicaDataSourceRule primaryReplicaDataSourceRule = new PrimaryReplicaDataSourceRule(
            new PrimaryReplicaDataSourceRuleConfiguration("test_ms", "primary_db", Arrays.asList("replica_db_0", "replica_db_1"), "random"), new RandomPrimaryReplicaLoadBalanceAlgorithm());
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewPrimaryReplicaDataSourceRuleWithoutName() {
        new PrimaryReplicaDataSourceRule(new PrimaryReplicaDataSourceRuleConfiguration("", "primary_ds", Collections.singletonList("replica_ds"), null), 
                new RoundRobinPrimaryReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewPrimaryReplicaDataSourceRuleWithoutPrimaryDataSourceName() {
        new PrimaryReplicaDataSourceRule(new PrimaryReplicaDataSourceRuleConfiguration("ds", "", Collections.singletonList("replica_ds"), null), new RoundRobinPrimaryReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewPrimaryReplicaDataSourceRuleWithNullReplicaDataSourceName() {
        new PrimaryReplicaDataSourceRule(new PrimaryReplicaDataSourceRuleConfiguration("ds", "primary_ds", null, null), new RoundRobinPrimaryReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewPrimaryReplicaDataSourceRuleWithEmptyReplicaDataSourceName() {
        new PrimaryReplicaDataSourceRule(new PrimaryReplicaDataSourceRuleConfiguration("ds", "primary_ds", Collections.emptyList(), null), new RoundRobinPrimaryReplicaLoadBalanceAlgorithm());
    }
    
    @Test
    public void assertGetReplicaDataSourceNamesWithoutDisabledDataSourceNames() {
        assertThat(primaryReplicaDataSourceRule.getReplicaDataSourceNames(), is(Arrays.asList("replica_db_0", "replica_db_1")));
    }
    
    @Test
    public void assertGetReplicaDataSourceNamesWithDisabledDataSourceNames() {
        primaryReplicaDataSourceRule.updateDisabledDataSourceNames("replica_db_0", true);
        assertThat(primaryReplicaDataSourceRule.getReplicaDataSourceNames(), is(Collections.singletonList("replica_db_1")));
    }
    
    @Test
    public void assertUpdateDisabledDataSourceNamesForDisabled() {
        primaryReplicaDataSourceRule.updateDisabledDataSourceNames("replica_db_0", true);
        assertThat(primaryReplicaDataSourceRule.getReplicaDataSourceNames(), is(Collections.singletonList("replica_db_1")));
    }
    
    @Test
    public void assertUpdateDisabledDataSourceNamesForEnabled() {
        primaryReplicaDataSourceRule.updateDisabledDataSourceNames("replica_db_0", true);
        primaryReplicaDataSourceRule.updateDisabledDataSourceNames("replica_db_0", false);
        assertThat(primaryReplicaDataSourceRule.getReplicaDataSourceNames(), is(Arrays.asList("replica_db_0", "replica_db_1")));
    }
    
    @Test
    public void assertGetDataSourceMapper() {
        Map<String, Collection<String>> actual = primaryReplicaDataSourceRule.getDataSourceMapper();
        Map<String, Collection<String>> expected = ImmutableMap.of("test_ms", Arrays.asList("primary_db", "replica_db_0", "replica_db_1"));
        assertThat(actual, is(expected));
    }
}
