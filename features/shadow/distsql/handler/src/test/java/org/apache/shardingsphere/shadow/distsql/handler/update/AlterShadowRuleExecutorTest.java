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

package org.apache.shardingsphere.shadow.distsql.handler.update;

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.exception.InUsedAlgorithmException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.statement.AlterShadowRuleStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AlterShadowRuleExecutorTest {
    
    private final AlterShadowRuleExecutor executor = new AlterShadowRuleExecutor();
    
    @Mock
    private ResourceMetaData resourceMetaData;
    
    @Mock
    private ShadowRuleConfiguration currentConfig;
    
    @BeforeEach
    void setUp() {
        Collection<ShadowDataSourceConfiguration> shadowDataSource = new LinkedList<>();
        shadowDataSource.add(new ShadowDataSourceConfiguration("initRuleName1", "ds1", "ds_shadow1"));
        shadowDataSource.add(new ShadowDataSourceConfiguration("initRuleName2", "ds2", "ds_shadow2"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(currentConfig.getDataSources()).thenReturn(shadowDataSource);
        executor.setDatabase(database);
    }
    
    @Test
    void assertExecuteWithDuplicateRuleName() {
        ShadowRuleSegment ruleSegment = new ShadowRuleSegment("ruleName", null, null, null);
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        assertThrows(DuplicateRuleException.class, () -> executor.checkBeforeUpdate(new AlterShadowRuleStatement(Arrays.asList(ruleSegment, ruleSegment))));
    }
    
    @Test
    void assertExecuteWithRuleNameNotInMetaData() {
        ShadowRuleSegment ruleSegment = new ShadowRuleSegment("ruleName", null, null, null);
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(new AlterShadowRuleStatement(Collections.singleton(ruleSegment))));
    }
    
    @Test
    void assertExecuteWithNotExistResource() {
        List<String> dataSources = Arrays.asList("ds", "ds0");
        when(resourceMetaData.getNotExistedDataSources(any())).thenReturn(dataSources);
        AlterShadowRuleStatement sqlStatement = new AlterShadowRuleStatement(Collections.singleton(new ShadowRuleSegment("initRuleName1", "ds3", null, null)));
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        assertThrows(MissingRequiredStorageUnitsException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertExecuteDuplicateAlgorithm() {
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("name", PropertiesBuilder.build(new Property("type", "value"))));
        AlterShadowRuleStatement sqlStatement = new AlterShadowRuleStatement(Arrays.asList(
                new ShadowRuleSegment("initRuleName1", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))),
                new ShadowRuleSegment("initRuleName2", "ds1", null, Collections.singletonMap("t_order_1", Collections.singleton(segment)))));
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        assertThrows(InUsedAlgorithmException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertExecuteDuplicateAlgorithmWithoutConfiguration() {
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("name", PropertiesBuilder.build(new Property("type", "value"))));
        AlterShadowRuleStatement sqlStatement = new AlterShadowRuleStatement(Arrays.asList(
                new ShadowRuleSegment("initRuleName1", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))),
                new ShadowRuleSegment("initRuleName2", "ds1", null, Collections.singletonMap("t_order_1", Collections.singleton(segment)))));
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        assertThrows(InUsedAlgorithmException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertExecuteSuccess() {
        Properties props = PropertiesBuilder.build(new Property("type", "value"));
        ShadowAlgorithmSegment segment1 = new ShadowAlgorithmSegment("algorithmName1", new AlgorithmSegment("SQL_HINT", props));
        ShadowAlgorithmSegment segment2 = new ShadowAlgorithmSegment("algorithmName2", new AlgorithmSegment("SQL_HINT", props));
        AlterShadowRuleStatement sqlStatement = new AlterShadowRuleStatement(Arrays.asList(
                new ShadowRuleSegment("initRuleName1", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment1))),
                new ShadowRuleSegment("initRuleName2", "ds1", null, Collections.singletonMap("t_order_1", Collections.singleton(segment2)))));
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        executor.checkBeforeUpdate(sqlStatement);
    }
    
    @Test
    void assertExecuteSuccessWithoutProps() {
        ShadowAlgorithmSegment segment1 = new ShadowAlgorithmSegment("algorithmName1", new AlgorithmSegment("SQL_HINT", null));
        ShadowAlgorithmSegment segment2 = new ShadowAlgorithmSegment("algorithmName2", new AlgorithmSegment("SQL_HINT", null));
        AlterShadowRuleStatement sqlStatement = new AlterShadowRuleStatement(Arrays.asList(
                new ShadowRuleSegment("initRuleName1", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment1))),
                new ShadowRuleSegment("initRuleName2", "ds1", null, Collections.singletonMap("t_order_1", Collections.singleton(segment2)))));
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        executor.checkBeforeUpdate(sqlStatement);
    }
}
