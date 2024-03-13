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

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datasource.StaticDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.mode.event.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.event.storage.StorageNodeDataSourceChangedEvent;
import org.apache.shardingsphere.mode.event.storage.StorageNodeRole;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ReadwriteSplittingRuleTest {
    
    @Test
    void assertFindDataSourceRule() {
        Optional<ReadwriteSplittingDataSourceRule> actual = createReadwriteSplittingRule().findDataSourceRule("readwrite");
        assertTrue(actual.isPresent());
        assertDataSourceRule(actual.get());
    }
    
    @Test
    void assertGetSingleDataSourceRule() {
        assertDataSourceRule(createReadwriteSplittingRule().getSingleDataSourceRule());
    }
    
    private ReadwriteSplittingRule createReadwriteSplittingRule() {
        ReadwriteSplittingDataSourceRuleConfiguration config =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), "random");
        return new ReadwriteSplittingRule("logic_db", new ReadwriteSplittingRuleConfiguration(
                Collections.singleton(config), Collections.singletonMap("random", new AlgorithmConfiguration("RANDOM", new Properties()))), mock(InstanceContext.class));
    }
    
    private void assertDataSourceRule(final ReadwriteSplittingDataSourceRule actual) {
        assertThat(actual.getName(), is("readwrite"));
        assertThat(actual.getReadwriteSplittingGroup().getWriteDataSource(), is("write_ds"));
        assertThat(actual.getReadwriteSplittingGroup().getReadDataSources(), is(Arrays.asList("read_ds_0", "read_ds_1")));
        assertThat(actual.getLoadBalancer().getType(), is("RANDOM"));
    }
    
    @Test
    void assertUpdateRuleStatusWithNotExistDataSource() {
        ReadwriteSplittingRule readwriteSplittingRule = createReadwriteSplittingRule();
        readwriteSplittingRule.getAttributes().getAttribute(StaticDataSourceRuleAttribute.class).updateStatus(new StorageNodeDataSourceChangedEvent(
                new QualifiedDatabase("readwrite_splitting_db.readwrite.read_ds"), new StorageNodeDataSource(StorageNodeRole.MEMBER, DataSourceState.DISABLED)));
        assertThat(readwriteSplittingRule.getSingleDataSourceRule().getDisabledDataSourceNames(), is(Collections.singleton("read_ds")));
    }
    
    @Test
    void assertUpdateRuleStatus() {
        ReadwriteSplittingRule readwriteSplittingRule = createReadwriteSplittingRule();
        readwriteSplittingRule.getAttributes().getAttribute(StaticDataSourceRuleAttribute.class).updateStatus(new StorageNodeDataSourceChangedEvent(
                new QualifiedDatabase("readwrite_splitting_db.readwrite.read_ds_0"), new StorageNodeDataSource(StorageNodeRole.MEMBER, DataSourceState.DISABLED)));
        assertThat(readwriteSplittingRule.getSingleDataSourceRule().getDisabledDataSourceNames(), is(Collections.singleton("read_ds_0")));
    }
    
    @Test
    void assertUpdateRuleStatusWithEnable() {
        ReadwriteSplittingRule readwriteSplittingRule = createReadwriteSplittingRule();
        readwriteSplittingRule.getAttributes().getAttribute(StaticDataSourceRuleAttribute.class).updateStatus(new StorageNodeDataSourceChangedEvent(
                new QualifiedDatabase("readwrite_splitting_db.readwrite.read_ds_0"), new StorageNodeDataSource(StorageNodeRole.MEMBER, DataSourceState.DISABLED)));
        assertThat(readwriteSplittingRule.getSingleDataSourceRule().getDisabledDataSourceNames(), is(Collections.singleton("read_ds_0")));
        readwriteSplittingRule.getAttributes().getAttribute(StaticDataSourceRuleAttribute.class).updateStatus(new StorageNodeDataSourceChangedEvent(
                new QualifiedDatabase("readwrite_splitting_db.readwrite.read_ds_0"), new StorageNodeDataSource(StorageNodeRole.MEMBER, DataSourceState.ENABLED)));
        assertThat(readwriteSplittingRule.getSingleDataSourceRule().getDisabledDataSourceNames(), is(Collections.emptySet()));
    }
    
    @Test
    void assertCreateReadwriteSplittingRuleWithRowValueExpressionImpl() {
        ReadwriteSplittingDataSourceRuleConfiguration config = new ReadwriteSplittingDataSourceRuleConfiguration(
                "<GROOVY>${['readwrite']}_ds",
                "<GROOVY>${['write']}_ds",
                Arrays.asList("<GROOVY>read_ds_${['0']}", "read_ds_${['1']}", "read_ds_2", "<LITERAL>read_ds_3"),
                "random");
        ReadwriteSplittingRule readwriteSplittingRule = new ReadwriteSplittingRule("logic_db", new ReadwriteSplittingRuleConfiguration(
                Collections.singleton(config), Collections.singletonMap("random", new AlgorithmConfiguration("RANDOM", new Properties()))), mock(InstanceContext.class));
        Optional<ReadwriteSplittingDataSourceRule> actual = readwriteSplittingRule.findDataSourceRule("readwrite_ds");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("readwrite_ds"));
        assertThat(actual.get().getReadwriteSplittingGroup().getWriteDataSource(), is("write_ds"));
        assertThat(actual.get().getReadwriteSplittingGroup().getReadDataSources(), is(Arrays.asList("read_ds_0", "read_ds_1", "read_ds_2", "read_ds_3")));
        assertThat(actual.get().getLoadBalancer().getType(), is("RANDOM"));
    }
}
