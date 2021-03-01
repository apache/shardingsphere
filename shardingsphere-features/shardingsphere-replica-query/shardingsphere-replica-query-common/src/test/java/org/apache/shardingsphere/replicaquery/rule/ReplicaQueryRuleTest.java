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
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.replicaquery.api.config.ReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.rule.ReplicaQueryDataSourceRuleConfiguration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ReplicaQueryRuleTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewWithEmptyDataSourceRule() {
        new ReplicaQueryRule(new ReplicaQueryRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
    }
    
    @Test
    public void assertFindDataSourceRule() {
        Optional<ReplicaQueryDataSourceRule> actual = createReplicaQueryRule().findDataSourceRule("test_pr");
        assertTrue(actual.isPresent());
        assertDataSourceRule(actual.get());
    }
    
    @Test
    public void assertGetSingleDataSourceRule() {
        assertDataSourceRule(createReplicaQueryRule().getSingleDataSourceRule());
    }
    
    private ReplicaQueryRule createReplicaQueryRule() {
        ReplicaQueryDataSourceRuleConfiguration config = 
                new ReplicaQueryDataSourceRuleConfiguration("test_pr", "primary_ds", Arrays.asList("replica_ds_0", "replica_ds_1"), "random");
        return new ReplicaQueryRule(new ReplicaQueryRuleConfiguration(
                Collections.singleton(config), ImmutableMap.of("random", new ShardingSphereAlgorithmConfiguration("RANDOM", new Properties()))));
    }
    
    private void assertDataSourceRule(final ReplicaQueryDataSourceRule actual) {
        assertThat(actual.getName(), is("test_pr"));
        assertThat(actual.getPrimaryDataSourceName(), is("primary_ds"));
        assertThat(actual.getReplicaDataSourceNames(), is(Arrays.asList("replica_ds_0", "replica_ds_1")));
        assertThat(actual.getLoadBalancer().getType(), is("RANDOM"));
    }
    
    @Test
    public void assertUpdateRuleStatusWithNotExistDataSource() {
        ReplicaQueryRule replicaQueryRule = createReplicaQueryRule();
        replicaQueryRule.updateRuleStatus(new DataSourceNameDisabledEvent("replica_db", true));
        assertThat(replicaQueryRule.getSingleDataSourceRule().getReplicaDataSourceNames(), is(Arrays.asList("replica_ds_0", "replica_ds_1")));
    }
    
    @Test
    public void assertUpdateRuleStatus() {
        ReplicaQueryRule replicaQueryRule = createReplicaQueryRule();
        replicaQueryRule.updateRuleStatus(new DataSourceNameDisabledEvent("replica_ds_0", true));
        assertThat(replicaQueryRule.getSingleDataSourceRule().getReplicaDataSourceNames(), is(Collections.singletonList("replica_ds_1")));
    }
    
    @Test
    public void assertUpdateRuleStatusWithEnable() {
        ReplicaQueryRule replicaQueryRule = createReplicaQueryRule();
        replicaQueryRule.updateRuleStatus(new DataSourceNameDisabledEvent("replica_ds_0", true));
        assertThat(replicaQueryRule.getSingleDataSourceRule().getReplicaDataSourceNames(), is(Collections.singletonList("replica_ds_1")));
        replicaQueryRule.updateRuleStatus(new DataSourceNameDisabledEvent("replica_ds_0", false));
        assertThat(replicaQueryRule.getSingleDataSourceRule().getReplicaDataSourceNames(), is(Arrays.asList("replica_ds_0", "replica_ds_1")));
    }
    
    @Test
    public void assertGetDataSourceMapper() {
        ReplicaQueryRule replicaQueryRule = createReplicaQueryRule();
        Map<String, Collection<String>> actual = replicaQueryRule.getDataSourceMapper();
        Map<String, Collection<String>> expected = ImmutableMap.of("test_pr", Arrays.asList("primary_ds", "replica_ds_0", "replica_ds_1"));
        assertThat(actual, is(expected));
    }
}
