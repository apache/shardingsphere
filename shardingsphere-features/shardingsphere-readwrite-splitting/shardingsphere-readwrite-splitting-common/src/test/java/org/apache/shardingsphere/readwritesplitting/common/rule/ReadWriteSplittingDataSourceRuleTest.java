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

package org.apache.shardingsphere.readwritesplitting.common.rule;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadWriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.algorithm.RandomReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.common.algorithm.RoundRobinReplicaLoadBalanceAlgorithm;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ReadWriteSplittingDataSourceRuleTest {
    
    private final ReadWriteSplittingDataSourceRule readWriteSplittingDataSourceRule = new ReadWriteSplittingDataSourceRule(
            new ReadWriteSplittingDataSourceRuleConfiguration("test_pr", "", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), "random"), new RandomReplicaLoadBalanceAlgorithm());
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewReadWriteSplittingDataSourceRuleWithoutName() {
        new ReadWriteSplittingDataSourceRule(new ReadWriteSplittingDataSourceRuleConfiguration("", "", "write_ds", Collections.singletonList("read_ds"), null), 
                new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewReadWriteSplittingDataSourceRuleWithoutPrimaryDataSourceName() {
        new ReadWriteSplittingDataSourceRule(new ReadWriteSplittingDataSourceRuleConfiguration("ds", "", "", Collections.singletonList("read_ds"), null), new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewReadWriteSplittingDataSourceRuleWithNullReadDataSourceName() {
        new ReadWriteSplittingDataSourceRule(new ReadWriteSplittingDataSourceRuleConfiguration("ds", "", "write_ds", null, null), new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewReadWriteSplittingDataSourceRuleWithEmptyReadDataSourceName() {
        new ReadWriteSplittingDataSourceRule(new ReadWriteSplittingDataSourceRuleConfiguration("ds", "", "write_ds", Collections.emptyList(), null), new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test
    public void assertGetReadDataSourceNamesWithoutDisabledDataSourceNames() {
        assertThat(readWriteSplittingDataSourceRule.getReadDataSourceNames(), is(Arrays.asList("read_ds_0", "read_ds_1")));
    }
    
    @Test
    public void assertGetReadDataSourceNamesWithDisabledDataSourceNames() {
        readWriteSplittingDataSourceRule.updateDisabledDataSourceNames("read_ds_0", true);
        assertThat(readWriteSplittingDataSourceRule.getReadDataSourceNames(), is(Collections.singletonList("read_ds_1")));
    }
    
    @Test
    public void assertUpdateDisabledDataSourceNamesForDisabled() {
        readWriteSplittingDataSourceRule.updateDisabledDataSourceNames("read_ds_0", true);
        assertThat(readWriteSplittingDataSourceRule.getReadDataSourceNames(), is(Collections.singletonList("read_ds_1")));
    }
    
    @Test
    public void assertUpdateDisabledDataSourceNamesForEnabled() {
        readWriteSplittingDataSourceRule.updateDisabledDataSourceNames("read_ds_0", true);
        readWriteSplittingDataSourceRule.updateDisabledDataSourceNames("read_ds_0", false);
        assertThat(readWriteSplittingDataSourceRule.getReadDataSourceNames(), is(Arrays.asList("read_ds_0", "read_ds_1")));
    }
    
    @Test
    public void assertGetDataSourceMapper() {
        Map<String, Collection<String>> actual = readWriteSplittingDataSourceRule.getDataSourceMapper();
        Map<String, Collection<String>> expected = ImmutableMap.of("test_pr", Arrays.asList("write_ds", "read_ds_0", "read_ds_1"));
        assertThat(actual, is(expected));
    }
}
