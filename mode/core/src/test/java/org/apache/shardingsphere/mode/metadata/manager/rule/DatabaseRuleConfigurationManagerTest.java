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

package org.apache.shardingsphere.mode.metadata.manager.rule;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.PartialRuleUpdateSupported;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.factory.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings({"rawtypes", "unchecked"})
@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseRulesBuilder.class)
class DatabaseRuleConfigurationManagerTest {
    
    private static final String DATABASE_NAME = "foo_db";
    
    @Test
    void assertRefreshWithPartialUpdateAndRebuild() throws SQLException {
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        ShardingSphereRule closableRule = mock(ShardingSphereRule.class, withSettings().extraInterfaces(PartialRuleUpdateSupported.class, AutoCloseable.class));
        when(closableRule.getConfiguration()).thenReturn(ruleConfig);
        PartialRuleUpdateSupported updater = (PartialRuleUpdateSupported) closableRule;
        when(updater.partialUpdate(ruleConfig)).thenReturn(true);
        ShardingSphereRule nonClosableRule = mock(ShardingSphereRule.class);
        when(nonClosableRule.getConfiguration()).thenReturn(mock(RuleConfiguration.class, withSettings().extraInterfaces(Cloneable.class)));
        RuleMetaData ruleMetaData = new RuleMetaData(new LinkedList<>(Arrays.asList(closableRule, nonClosableRule)));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase(DATABASE_NAME)).thenReturn(database);
        ShardingSphereRule rebuiltRule = mock(ShardingSphereRule.class);
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createByAlterRule(eq(DATABASE_NAME), eq(false), any(Collection.class), eq(metaDataContexts))).thenReturn(mock(MetaDataContexts.class)))) {
            when(DatabaseRulesBuilder.build(eq(DATABASE_NAME), any(), any(), eq(ruleConfig), any(), any())).thenReturn(rebuiltRule);
            new DatabaseRuleConfigurationManager(metaDataContexts, mock(ComputeNodeInstanceContext.class), mock(MetaDataPersistFacade.class)).refresh(DATABASE_NAME, ruleConfig, true);
            verify((PartialRuleUpdateSupported) closableRule).updateConfiguration(ruleConfig);
            verify(metaDataContexts).update(any(MetaDataContexts.class));
            assertDoesNotThrow(() -> verify((AutoCloseable) closableRule).close());
        }
    }
    
    @Test
    void assertRefreshWithPartialUpdateSkipMetadata() throws SQLException {
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class, withSettings().extraInterfaces(Serializable.class));
        ShardingSphereRule partialRule = mock(ShardingSphereRule.class, withSettings().extraInterfaces(PartialRuleUpdateSupported.class));
        when(partialRule.getConfiguration()).thenReturn(ruleConfig);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(partialRule)));
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        when(metaDataContexts.getMetaData().getDatabase(DATABASE_NAME)).thenReturn(database);
        new DatabaseRuleConfigurationManager(metaDataContexts, mock(ComputeNodeInstanceContext.class), mock(MetaDataPersistFacade.class)).refresh(DATABASE_NAME, ruleConfig, true);
        verify((PartialRuleUpdateSupported) partialRule).updateConfiguration(ruleConfig);
        verify(metaDataContexts, never()).update(any(MetaDataContexts.class));
    }
    
    @Test
    void assertRefreshWithPartialUpdateWithoutRebuild() throws SQLException {
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class, withSettings().extraInterfaces(Serializable.class));
        ShardingSphereRule partialRule = mock(ShardingSphereRule.class, withSettings().extraInterfaces(PartialRuleUpdateSupported.class));
        when(partialRule.getConfiguration()).thenReturn(ruleConfig);
        PartialRuleUpdateSupported updater = (PartialRuleUpdateSupported) partialRule;
        when(updater.partialUpdate(ruleConfig)).thenReturn(true);
        RuleMetaData ruleMetaData = new RuleMetaData(new LinkedList<>(Collections.singleton(partialRule)));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase(DATABASE_NAME)).thenReturn(database);
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createByAlterRule(eq(DATABASE_NAME), eq(false), any(Collection.class), eq(metaDataContexts))).thenReturn(mock(MetaDataContexts.class)))) {
            new DatabaseRuleConfigurationManager(metaDataContexts, mock(ComputeNodeInstanceContext.class), mock(MetaDataPersistFacade.class)).refresh(DATABASE_NAME, ruleConfig, false);
            verify(metaDataContexts).update(any(MetaDataContexts.class));
            verify(updater).updateConfiguration(ruleConfig);
        }
    }
    
    @Test
    void assertRefreshWithoutExistingRuleAndWithoutRebuild() throws SQLException {
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class, withSettings().extraInterfaces(Serializable.class));
        ShardingSphereRule otherRule = mock(ShardingSphereRule.class);
        when(otherRule.getConfiguration()).thenReturn(mock(RuleConfiguration.class, withSettings().extraInterfaces(Cloneable.class)));
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(otherRule));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase(DATABASE_NAME)).thenReturn(database);
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createByAlterRule(eq(DATABASE_NAME), eq(false), any(Collection.class), eq(metaDataContexts))).thenReturn(mock(MetaDataContexts.class)))) {
            new DatabaseRuleConfigurationManager(metaDataContexts, mock(ComputeNodeInstanceContext.class), mock(MetaDataPersistFacade.class)).refresh(DATABASE_NAME, ruleConfig, false);
            verify(metaDataContexts).update(any(MetaDataContexts.class));
        }
    }
    
    @Test
    void assertRefreshWithExistingNonPartialRule() throws SQLException {
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class, withSettings().extraInterfaces(Serializable.class));
        ShardingSphereRule existingRule = mock(ShardingSphereRule.class);
        when(existingRule.getConfiguration()).thenReturn(ruleConfig);
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(existingRule));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase(DATABASE_NAME)).thenReturn(database);
        ShardingSphereRule rebuiltRule = mock(ShardingSphereRule.class);
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createByAlterRule(eq(DATABASE_NAME), eq(false), any(Collection.class), eq(metaDataContexts))).thenReturn(mock(MetaDataContexts.class)))) {
            when(DatabaseRulesBuilder.build(eq(DATABASE_NAME), any(), any(), eq(ruleConfig), any(), any())).thenReturn(rebuiltRule);
            new DatabaseRuleConfigurationManager(metaDataContexts, mock(ComputeNodeInstanceContext.class), mock(MetaDataPersistFacade.class)).refresh(DATABASE_NAME, ruleConfig, true);
            verify(metaDataContexts).update(any(MetaDataContexts.class));
        }
    }
    
    @Test
    void assertRefreshWrapsException() {
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class, withSettings().extraInterfaces(Serializable.class));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(rule));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase(DATABASE_NAME)).thenReturn(database);
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createByAlterRule(eq(DATABASE_NAME), eq(false), any(Collection.class), eq(metaDataContexts))).thenThrow(new SQLException("failed")))) {
            assertThrows(SQLException.class,
                    () -> new DatabaseRuleConfigurationManager(metaDataContexts, mock(ComputeNodeInstanceContext.class), mock(MetaDataPersistFacade.class)).refresh(DATABASE_NAME, ruleConfig, false));
        }
    }
    
    @Test
    void assertRefreshThrowsWhenCloseOriginalRuleFailed() throws Exception {
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class, withSettings().extraInterfaces(Serializable.class));
        ShardingSphereRule closableRule = mock(ShardingSphereRule.class, withSettings().extraInterfaces(PartialRuleUpdateSupported.class, AutoCloseable.class));
        when(closableRule.getConfiguration()).thenReturn(ruleConfig);
        PartialRuleUpdateSupported updater = (PartialRuleUpdateSupported) closableRule;
        when(updater.partialUpdate(ruleConfig)).thenReturn(true);
        doThrow(Exception.class).when((AutoCloseable) closableRule).close();
        RuleMetaData ruleMetaData = new RuleMetaData(new LinkedList<>(Collections.singleton(closableRule)));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase(DATABASE_NAME)).thenReturn(database);
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createByAlterRule(eq(DATABASE_NAME), eq(false), any(Collection.class), eq(metaDataContexts))).thenReturn(mock(MetaDataContexts.class)))) {
            assertThrows(Exception.class,
                    () -> new DatabaseRuleConfigurationManager(metaDataContexts, mock(ComputeNodeInstanceContext.class), mock(MetaDataPersistFacade.class)).refresh(DATABASE_NAME, ruleConfig, false));
        }
    }
}
