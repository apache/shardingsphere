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

package org.apache.shardingsphere.readwritesplitting.rule;

import org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.RandomReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ReadwriteSplittingDataSourceRuleTest {
    
    private ReadwriteSplittingDataSourceRule readwriteSplittingDataSourceRule;
    
    @Before
    public void setUp() {
        readwriteSplittingDataSourceRule = new ReadwriteSplittingDataSourceRule(
                new ReadwriteSplittingDataSourceRuleConfiguration("test_pr",
                        new StaticReadwriteSplittingStrategyConfiguration("write_ds", Arrays.asList("read_ds_0", "read_ds_1")), null, null),
                new RandomReadQueryLoadBalanceAlgorithm(), Collections.emptyList());
    }
    
    @Test
    public void assertGetReadDataSourceNamesWithoutDisabledDataSourceNames() {
        assertThat(readwriteSplittingDataSourceRule.getEnabledReplicaDataSources(), is(Arrays.asList("read_ds_0", "read_ds_1")));
    }
    
    @Test
    public void assertGetReadDataSourceNamesWithDisabledDataSourceNames() {
        readwriteSplittingDataSourceRule.updateDisabledDataSourceNames("read_ds_0", true);
        assertThat(readwriteSplittingDataSourceRule.getEnabledReplicaDataSources(), is(Collections.singletonList("read_ds_1")));
    }
    
    @Test
    public void assertUpdateDisabledDataSourceNamesForDisabled() {
        readwriteSplittingDataSourceRule.updateDisabledDataSourceNames("read_ds_0", true);
        assertThat(readwriteSplittingDataSourceRule.getEnabledReplicaDataSources(), is(Collections.singletonList("read_ds_1")));
    }
    
    @Test
    public void assertUpdateDisabledDataSourceNamesForEnabled() {
        readwriteSplittingDataSourceRule.updateDisabledDataSourceNames("read_ds_0", true);
        readwriteSplittingDataSourceRule.updateDisabledDataSourceNames("read_ds_0", false);
        assertThat(readwriteSplittingDataSourceRule.getEnabledReplicaDataSources(), is(Arrays.asList("read_ds_0", "read_ds_1")));
    }
    
    @Test
    public void assertGetWriteDataSource() {
        String writeDataSourceName = readwriteSplittingDataSourceRule.getWriteDataSource();
        assertThat(writeDataSourceName, is("write_ds"));
    }
    
    @Test
    public void assertGetEnabledReplicaDataSources() {
        readwriteSplittingDataSourceRule.updateDisabledDataSourceNames("read_ds_0", true);
        assertThat(readwriteSplittingDataSourceRule.getEnabledReplicaDataSources(), is(Collections.singletonList("read_ds_1")));
    }
}
