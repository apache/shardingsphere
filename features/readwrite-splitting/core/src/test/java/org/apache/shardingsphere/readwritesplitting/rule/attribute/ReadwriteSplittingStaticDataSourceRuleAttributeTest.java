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

package org.apache.shardingsphere.readwritesplitting.rule.attribute;

import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.readwritesplitting.deliver.QualifiedDataSourceDeletedEvent;
import org.apache.shardingsphere.readwritesplitting.exception.logic.ReadwriteSplittingDataSourceRuleNotFoundException;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereArgumentVerifyMatchers.deepEq;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReadwriteSplittingStaticDataSourceRuleAttributeTest {
    
    @Test
    void assertUpdateStatusWithNotExistedDataSourceGroupRule() {
        ReadwriteSplittingStaticDataSourceRuleAttribute ruleAttribute = new ReadwriteSplittingStaticDataSourceRuleAttribute("foo_db", Collections.emptyMap(), mock(ComputeNodeInstanceContext.class));
        assertThrows(ReadwriteSplittingDataSourceRuleNotFoundException.class, () -> ruleAttribute.updateStatus(new QualifiedDataSource("foo_db.foo_group.foo_ds"), DataSourceState.DISABLED));
    }
    
    @Test
    void assertUpdateStatusWithDisable() {
        ReadwriteSplittingDataSourceGroupRule dataSourceGroupRule = mock(ReadwriteSplittingDataSourceGroupRule.class);
        ReadwriteSplittingStaticDataSourceRuleAttribute ruleAttribute = new ReadwriteSplittingStaticDataSourceRuleAttribute(
                "foo_db", Collections.singletonMap("foo_group", dataSourceGroupRule), mock(ComputeNodeInstanceContext.class));
        ruleAttribute.updateStatus(new QualifiedDataSource("foo_db.foo_group.foo_ds"), DataSourceState.DISABLED);
        verify(dataSourceGroupRule).disableDataSource("foo_ds");
    }
    
    @Test
    void assertUpdateStatusWithEnable() {
        ReadwriteSplittingDataSourceGroupRule dataSourceGroupRule = mock(ReadwriteSplittingDataSourceGroupRule.class);
        ReadwriteSplittingStaticDataSourceRuleAttribute ruleAttribute = new ReadwriteSplittingStaticDataSourceRuleAttribute(
                "foo_db", Collections.singletonMap("foo_group", dataSourceGroupRule), mock(ComputeNodeInstanceContext.class));
        ruleAttribute.updateStatus(new QualifiedDataSource("foo_db.foo_group.foo_ds"), DataSourceState.ENABLED);
        verify(dataSourceGroupRule).enableDataSource("foo_ds");
    }
    
    @Test
    void assertCleanStorageNodeDataSourceWithNotExistedDataSourceGroupRule() {
        ReadwriteSplittingStaticDataSourceRuleAttribute ruleAttribute = new ReadwriteSplittingStaticDataSourceRuleAttribute("foo_db", Collections.emptyMap(), mock(ComputeNodeInstanceContext.class));
        assertThrows(ReadwriteSplittingDataSourceRuleNotFoundException.class, () -> ruleAttribute.cleanStorageNodeDataSource("foo_group"));
    }
    
    @Test
    void assertCleanStorageNodeDataSource() {
        ReadwriteSplittingDataSourceGroupRule dataSourceGroupRule = mock(ReadwriteSplittingDataSourceGroupRule.class, RETURNS_DEEP_STUBS);
        when(dataSourceGroupRule.getName()).thenReturn("foo_group");
        when(dataSourceGroupRule.getReadwriteSplittingGroup().getReadDataSources()).thenReturn(Collections.singletonList("read_ds"));
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        ReadwriteSplittingStaticDataSourceRuleAttribute ruleAttribute = new ReadwriteSplittingStaticDataSourceRuleAttribute(
                "foo_db", Collections.singletonMap("foo_group", dataSourceGroupRule), computeNodeInstanceContext);
        ruleAttribute.cleanStorageNodeDataSource("foo_group");
        verify(computeNodeInstanceContext.getEventBusContext()).post(deepEq(new QualifiedDataSourceDeletedEvent(new QualifiedDataSource("foo_db.foo_group.read_ds"))));
    }
    
    @Test
    void assertCleanStorageNodeDataSources() {
        ReadwriteSplittingDataSourceGroupRule dataSourceGroupRule = mock(ReadwriteSplittingDataSourceGroupRule.class, RETURNS_DEEP_STUBS);
        when(dataSourceGroupRule.getName()).thenReturn("foo_group");
        when(dataSourceGroupRule.getReadwriteSplittingGroup().getReadDataSources()).thenReturn(Collections.singletonList("read_ds"));
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        ReadwriteSplittingStaticDataSourceRuleAttribute ruleAttribute = new ReadwriteSplittingStaticDataSourceRuleAttribute(
                "foo_db", Collections.singletonMap("foo_group", dataSourceGroupRule), computeNodeInstanceContext);
        ruleAttribute.cleanStorageNodeDataSources();
        verify(computeNodeInstanceContext.getEventBusContext()).post(deepEq(new QualifiedDataSourceDeletedEvent(new QualifiedDataSource("foo_db.foo_group.read_ds"))));
    }
}
