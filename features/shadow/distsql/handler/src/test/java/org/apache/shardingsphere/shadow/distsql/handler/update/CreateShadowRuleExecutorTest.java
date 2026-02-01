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
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.statement.CreateShadowRuleStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateShadowRuleExecutorTest {
    
    private final CreateShadowRuleExecutor executor = new CreateShadowRuleExecutor();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ResourceMetaData resourceMetaData;
    
    @Mock
    private ShadowRuleConfiguration currentConfig;
    
    @BeforeEach
    void setUp() {
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getName()).thenReturn("shadow_db");
        when(currentConfig.getDataSources()).thenReturn(Collections.singleton(new ShadowDataSourceConfiguration("initRuleName", "initDs0", "initDs0Shadow")));
        executor.setDatabase(database);
    }
    
    @Test
    void assertExecuteWithDuplicateRuleName() {
        ShadowRuleSegment ruleSegment = new ShadowRuleSegment("ruleName", null, null, null);
        CreateShadowRuleStatement sqlStatement = new CreateShadowRuleStatement(false, Arrays.asList(ruleSegment, ruleSegment));
        sqlStatement.buildAttributes();
        assertThrows(DuplicateRuleException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertExecuteWithDuplicateRuleNameInMetaData() {
        when(currentConfig.getDataSources()).thenReturn(Collections.singleton(new ShadowDataSourceConfiguration("ruleName", "ds", "ds_shadow")));
        ShadowRuleSegment ruleSegment = new ShadowRuleSegment("ruleName", null, null, null);
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        CreateShadowRuleStatement sqlStatement = new CreateShadowRuleStatement(false, Collections.singleton(ruleSegment));
        sqlStatement.buildAttributes();
        assertThrows(DuplicateRuleException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertExecuteWithDuplicateLogicResource() {
        DataSourceMapperRuleAttribute ruleAttribute = mock(DataSourceMapperRuleAttribute.class, RETURNS_DEEP_STUBS);
        when(ruleAttribute.getDataSourceMapper()).thenReturn(Collections.singletonMap("duplicate_ds", Collections.singleton("ds_0")));
        when(database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)).thenReturn(Collections.singleton(ruleAttribute));
        executor.setDatabase(database);
        ShadowRuleSegment ruleSegment = new ShadowRuleSegment("duplicate_ds", null, null, null);
        CreateShadowRuleStatement sqlStatement = new CreateShadowRuleStatement(false, Collections.singleton(ruleSegment));
        sqlStatement.buildAttributes();
        assertThrows(InvalidRuleConfigurationException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertExecuteWithNotExistResource() {
        when(resourceMetaData.getNotExistedDataSources(any())).thenReturn(Arrays.asList("ds0", "ds1"));
        CreateShadowRuleStatement sqlStatement = new CreateShadowRuleStatement(false, Collections.singleton(new ShadowRuleSegment("ruleName", "ds1", null, null)));
        sqlStatement.buildAttributes();
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        assertThrows(MissingRequiredStorageUnitsException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertExecuteDuplicateAlgorithm() {
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("name", PropertiesBuilder.build(new Property("type", "value"))));
        CreateShadowRuleStatement sqlStatement = new CreateShadowRuleStatement(false, Arrays.asList(
                new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))),
                new ShadowRuleSegment("ruleName", "ds1", null, Collections.singletonMap("t_order_1", Collections.singleton(segment)))));
        sqlStatement.buildAttributes();
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        assertThrows(DuplicateRuleException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertExecuteDuplicateAlgorithmWithoutConfiguration() {
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("name", PropertiesBuilder.build(new Property("type", "value"))));
        CreateShadowRuleStatement sqlStatement = new CreateShadowRuleStatement(false, Arrays.asList(
                new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))),
                new ShadowRuleSegment("ruleName1", "ds1", null, Collections.singletonMap("t_order_1", Collections.singleton(segment)))));
        sqlStatement.buildAttributes();
        assertThrows(DuplicateRuleException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertInvalidAlgorithmConfiguration() {
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("type", PropertiesBuilder.build(new Property("type", "value"))));
        CreateShadowRuleStatement sqlStatement = new CreateShadowRuleStatement(false,
                Collections.singleton(new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment)))));
        sqlStatement.buildAttributes();
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        assertThrows(ServiceProviderNotFoundException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertExecuteWithoutProps() {
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("SQL_HINT", null));
        CreateShadowRuleStatement sqlStatement = new CreateShadowRuleStatement(false,
                Collections.singleton(new ShadowRuleSegment("initRuleNameWithoutProps", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment)))));
        sqlStatement.buildAttributes();
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        executor.checkBeforeUpdate(sqlStatement);
    }
    
    @Test
    void assertExecuteWithIfNotExists() {
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("SQL_HINT", PropertiesBuilder.build(new Property("type", "value"))));
        CreateShadowRuleStatement sqlStatement = new CreateShadowRuleStatement(true,
                Collections.singleton(new ShadowRuleSegment("initRuleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment)))));
        sqlStatement.buildAttributes();
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        executor.checkBeforeUpdate(sqlStatement);
    }
}
