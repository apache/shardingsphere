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

package org.apache.shardingsphere.shadow.distsql.update;

import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.update.CreateShadowRuleStatementUpdater;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowRuleStatement;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public final class CreateShadowRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereResourceMetaData resourceMetaData;
    
    @Mock
    private ShadowRuleConfiguration currentConfig;
    
    private final CreateShadowRuleStatementUpdater updater = new CreateShadowRuleStatementUpdater();
    
    @BeforeEach
    public void before() {
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getName()).thenReturn("aa");
        when(currentConfig.getDataSources()).thenReturn(Collections.singleton(new ShadowDataSourceConfiguration("initRuleName", "initDs0", "initDs0Shadow")));
    }
    
    @Test
    public void assertExecuteWithDuplicateRuleName() {
        ShadowRuleSegment ruleSegment = new ShadowRuleSegment("ruleName", null, null, null);
        assertThrows(DuplicateRuleException.class, () -> updater.checkSQLStatement(database, new CreateShadowRuleStatement(false, Arrays.asList(ruleSegment, ruleSegment)), null));
    }
    
    @Test
    public void assertExecuteWithDuplicateRuleNameInMetaData() {
        when(currentConfig.getDataSources()).thenReturn(Collections.singletonList(new ShadowDataSourceConfiguration("ruleName", "ds", "ds_shadow")));
        ShadowRuleSegment ruleSegment = new ShadowRuleSegment("ruleName", null, null, null);
        assertThrows(DuplicateRuleException.class, () -> updater.checkSQLStatement(database, new CreateShadowRuleStatement(false, Collections.singleton(ruleSegment)), currentConfig));
    }
    
    @Test
    public void assertExecuteWithNotExistResource() {
        when(resourceMetaData.getNotExistedDataSources(any())).thenReturn(Arrays.asList("ds0", "ds1"));
        CreateShadowRuleStatement sqlStatement = new CreateShadowRuleStatement(false, Collections.singleton(new ShadowRuleSegment("ruleName", "ds1", null, null)));
        assertThrows(MissingRequiredStorageUnitsException.class, () -> updater.checkSQLStatement(database, sqlStatement, currentConfig));
    }
    
    @Test
    public void assertExecuteDuplicateAlgorithm() {
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("name", PropertiesBuilder.build(new Property("type", "value"))));
        CreateShadowRuleStatement sqlStatement = new CreateShadowRuleStatement(false, Arrays.asList(
                new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))),
                new ShadowRuleSegment("ruleName", "ds1", null, Collections.singletonMap("t_order_1", Collections.singletonList(segment)))));
        assertThrows(DuplicateRuleException.class, () -> updater.checkSQLStatement(database, sqlStatement, currentConfig));
    }
    
    @Test
    public void assertExecuteDuplicateAlgorithmWithoutConfiguration() {
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("name", PropertiesBuilder.build(new Property("type", "value"))));
        CreateShadowRuleStatement sqlStatement = new CreateShadowRuleStatement(false, Arrays.asList(
                new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment))),
                new ShadowRuleSegment("ruleName1", "ds1", null, Collections.singletonMap("t_order_1", Collections.singletonList(segment)))));
        assertThrows(DuplicateRuleException.class, () -> updater.checkSQLStatement(database, sqlStatement, null));
    }
    
    @Test
    public void assertInvalidAlgorithmConfiguration() {
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("type", PropertiesBuilder.build(new Property("type", "value"))));
        CreateShadowRuleStatement sqlStatement = new CreateShadowRuleStatement(false,
                Collections.singleton(new ShadowRuleSegment("ruleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment)))));
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> updater.checkSQLStatement(database, sqlStatement, currentConfig));
    }
    
    @Test
    public void assertExecuteWithIfNotExists() {
        ShadowAlgorithmSegment segment = new ShadowAlgorithmSegment("algorithmName", new AlgorithmSegment("SQL_HINT", PropertiesBuilder.build(new Property("type", "value"))));
        CreateShadowRuleStatement sqlStatement = new CreateShadowRuleStatement(true,
                Collections.singleton(new ShadowRuleSegment("initRuleName", "ds", null, Collections.singletonMap("t_order", Collections.singleton(segment)))));
        updater.checkSQLStatement(database, sqlStatement, currentConfig);
    }
}
