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

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.RandomReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.RoundRobinReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.spi.ReadwriteSplittingType;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ReadwriteSplittingDataSourceRuleTest {
    
    private ReadwriteSplittingDataSourceRule readwriteSplittingDataSourceRule;
    
    static {
        ShardingSphereServiceLoader.register(ReadwriteSplittingType.class);
    }
    
    @Before
    public void setUp() {
        readwriteSplittingDataSourceRule = new ReadwriteSplittingDataSourceRule(
                new ReadwriteSplittingDataSourceRuleConfiguration("test_pr", "Static", getProperties("write_ds", "read_ds_0,read_ds_1"), ""), new RandomReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewReadwriteSplittingDataSourceRuleWithoutName() {
        new ReadwriteSplittingDataSourceRule(new ReadwriteSplittingDataSourceRuleConfiguration("", "Static", getProperties("write_ds", "read_ds"), null), new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewReadwriteSplittingDataSourceRuleWithoutWriteDataSourceName() {
        new ReadwriteSplittingDataSourceRule(new ReadwriteSplittingDataSourceRuleConfiguration("ds", "Static", getProperties("", "read_ds"), null),
                new RoundRobinReplicaLoadBalanceAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewReadwriteSplittingDataSourceRuleWithEmptyReadDataSourceName() {
        new ReadwriteSplittingDataSourceRule(new ReadwriteSplittingDataSourceRuleConfiguration("ds", "Static", getProperties("write_ds", ""), ""), new RoundRobinReplicaLoadBalanceAlgorithm());
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
    
    @Test
    public void assertGetWriteDataSource() {
        String writeDataSourceName = readwriteSplittingDataSourceRule.getWriteDataSource();
        assertThat(writeDataSourceName, is("write_ds"));
    }
    
    @Test
    public void assertGetDataSources() {
        Map<String, String> dataSources = readwriteSplittingDataSourceRule.getDataSources(false);
        assertThat(dataSources.get(ExportableConstants.PRIMARY_DATA_SOURCE_NAME), is("write_ds"));
        assertThat(dataSources.get(ExportableConstants.REPLICA_DATA_SOURCE_NAMES), is("read_ds_0,read_ds_1"));
        readwriteSplittingDataSourceRule.updateDisabledDataSourceNames("read_ds_0", true);
        Map<String, String> dataSourcesByRemoveDisabled = readwriteSplittingDataSourceRule.getDataSources(true);
        assertThat(dataSourcesByRemoveDisabled.get(ExportableConstants.PRIMARY_DATA_SOURCE_NAME), is("write_ds"));
        assertThat(dataSourcesByRemoveDisabled.get(ExportableConstants.REPLICA_DATA_SOURCE_NAMES), is("read_ds_1"));
    }
    
    private Properties getProperties(final String writeDataSource, final String readDataSources) {
        Properties result = new Properties();
        result.setProperty("write-data-source-name", writeDataSource);
        result.setProperty("read-data-source-names", readDataSources);
        return result;
    }
}
