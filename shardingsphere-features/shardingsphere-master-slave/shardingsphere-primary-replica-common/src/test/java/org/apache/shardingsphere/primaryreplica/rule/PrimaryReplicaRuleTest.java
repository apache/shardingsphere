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
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.primaryreplica.api.config.PrimaryReplicaRuleConfiguration;
import org.apache.shardingsphere.primaryreplica.api.config.rule.PrimaryReplicaDataSourceRuleConfiguration;
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

public final class PrimaryReplicaRuleTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewWithEmptyDataSourceRule() {
        new PrimaryReplicaRule(new PrimaryReplicaRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
    }
    
    @Test
    public void assertFindDataSourceRule() {
        Optional<PrimaryReplicaDataSourceRule> actual = createPrimaryReplicaRule().findDataSourceRule("test_ms");
        assertTrue(actual.isPresent());
        assertDataSourceRule(actual.get());
    }
    
    @Test
    public void assertGetSingleDataSourceRule() {
        assertDataSourceRule(createPrimaryReplicaRule().getSingleDataSourceRule());
    }
    
    private PrimaryReplicaRule createPrimaryReplicaRule() {
        PrimaryReplicaDataSourceRuleConfiguration configuration = new PrimaryReplicaDataSourceRuleConfiguration("test_ms", "primary_db", Arrays.asList("replica_db_0", "replica_db_1"), "random");
        return new PrimaryReplicaRule(new PrimaryReplicaRuleConfiguration(
                Collections.singleton(configuration), ImmutableMap.of("random", new ShardingSphereAlgorithmConfiguration("RANDOM", new Properties()))));
    }
    
    private void assertDataSourceRule(final PrimaryReplicaDataSourceRule actual) {
        assertThat(actual.getName(), is("test_ms"));
        assertThat(actual.getPrimaryDataSourceName(), is("primary_db"));
        assertThat(actual.getReplicaDataSourceNames(), is(Arrays.asList("replica_db_0", "replica_db_1")));
        assertThat(actual.getLoadBalancer().getType(), is("RANDOM"));
    }
    
    @Test
    public void assertUpdateRuleStatusWithNotExistDataSource() {
        PrimaryReplicaRule primaryReplicaRule = createPrimaryReplicaRule();
        primaryReplicaRule.updateRuleStatus(new DataSourceNameDisabledEvent("replica_db", true));
        assertThat(primaryReplicaRule.getSingleDataSourceRule().getReplicaDataSourceNames(), is(Arrays.asList("replica_db_0", "replica_db_1")));
    }
    
    @Test
    public void assertUpdateRuleStatus() {
        PrimaryReplicaRule primaryReplicaRule = createPrimaryReplicaRule();
        primaryReplicaRule.updateRuleStatus(new DataSourceNameDisabledEvent("replica_db_0", true));
        assertThat(primaryReplicaRule.getSingleDataSourceRule().getReplicaDataSourceNames(), is(Collections.singletonList("replica_db_1")));
    }
    
    @Test
    public void assertUpdateRuleStatusWithEnable() {
        PrimaryReplicaRule primaryReplicaRule = createPrimaryReplicaRule();
        primaryReplicaRule.updateRuleStatus(new DataSourceNameDisabledEvent("replica_db_0", true));
        assertThat(primaryReplicaRule.getSingleDataSourceRule().getReplicaDataSourceNames(), is(Collections.singletonList("replica_db_1")));
        primaryReplicaRule.updateRuleStatus(new DataSourceNameDisabledEvent("replica_db_0", false));
        assertThat(primaryReplicaRule.getSingleDataSourceRule().getReplicaDataSourceNames(), is(Arrays.asList("replica_db_0", "replica_db_1")));
    }
    
    @Test
    public void assertGetDataSourceMapper() {
        PrimaryReplicaRule primaryReplicaRule = createPrimaryReplicaRule();
        Map<String, Collection<String>> actual = primaryReplicaRule.getDataSourceMapper();
        Map<String, Collection<String>> expected = ImmutableMap.of("test_ms", Arrays.asList("primary_db", "replica_db_0", "replica_db_1"));
        assertThat(actual, is(expected));
    }
}
