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
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.algorithm.RandomReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.common.algorithm.RoundRobinReplicaLoadBalanceAlgorithm;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ReadwriteSplittingDataSourceRuleTest {
    
    private final ReadwriteSplittingDataSourceRule readwriteSplittingDataSourceRule = new ReadwriteSplittingDataSourceRule(
            new ReadwriteSplittingDataSourceRuleConfiguration("test_pr", "", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), "random"), new RandomReplicaLoadBalanceAlgorithm());
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewReadwriteSplittingDataSourceRuleWithoutName() {
        new ReadwriteSplittingDataSourceRule(new ReadwriteSplittingDataSourceRuleConfiguration("", "", "write_ds", Collections.singletonList("read_ds"), null), 
                new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewReadwriteSplittingDataSourceRuleWithoutPrimaryDataSourceName() {
        new ReadwriteSplittingDataSourceRule(new ReadwriteSplittingDataSourceRuleConfiguration("ds", "", "", Collections.singletonList("read_ds"), null), new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewReadwriteSplittingDataSourceRuleWithNullReadDataSourceName() {
        new ReadwriteSplittingDataSourceRule(new ReadwriteSplittingDataSourceRuleConfiguration("ds", "", "write_ds", null, null), new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewReadwriteSplittingDataSourceRuleWithEmptyReadDataSourceName() {
        new ReadwriteSplittingDataSourceRule(new ReadwriteSplittingDataSourceRuleConfiguration("ds", "", "write_ds", Collections.emptyList(), null), new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test
    public void assertGetReadDataSourceNamesWithoutDisabledDataSourceNames() {
        assertThat(readwriteSplittingDataSourceRule.getReadDataSourceNames(), is(Arrays.asList("read_ds_0", "read_ds_1")));
    }
    
    @Test
    public void assertGetReadDataSourceNamesWithDisabledDataSourceNames() {
        readwriteSplittingDataSourceRule.updateDisabledDataSourceNames("read_ds_0", true);
        assertThat(readwriteSplittingDataSourceRule.getReadDataSourceNames(), is(Collections.singletonList("read_ds_1")));
    }
    
    @Test
    public void assertUpdateDisabledDataSourceNamesForDisabled() {
        readwriteSplittingDataSourceRule.updateDisabledDataSourceNames("read_ds_0", true);
        assertThat(readwriteSplittingDataSourceRule.getReadDataSourceNames(), is(Collections.singletonList("read_ds_1")));
    }
    
    @Test
    public void assertUpdateDisabledDataSourceNamesForEnabled() {
        readwriteSplittingDataSourceRule.updateDisabledDataSourceNames("read_ds_0", true);
        readwriteSplittingDataSourceRule.updateDisabledDataSourceNames("read_ds_0", false);
        assertThat(readwriteSplittingDataSourceRule.getReadDataSourceNames(), is(Arrays.asList("read_ds_0", "read_ds_1")));
    }
    
    @Test
    public void assertGetDataSourceMapper() {
        Map<String, Collection<String>> actual = readwriteSplittingDataSourceRule.getDataSourceMapper();
        Map<String, Collection<String>> expected = ImmutableMap.of("test_pr", Arrays.asList("write_ds", "read_ds_0", "read_ds_1"));
        assertThat(actual, is(expected));
    }
}
