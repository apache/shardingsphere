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
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationEmptyChecker;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.PartialRuleUpdateSupported;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.factory.MetaDataContextsFactory;
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
import static org.mockito.ArgumentMatchers.argThat;
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
@StaticMockSettings(TypedSPILoader.class)
class DatabaseRuleConfigurationManagerTest {
    
    private static final String DATABASE_NAME = "foo_db";
    
    @Test
    void assertRefreshWithPartialUpdateAndRebuild() throws SQLException {
        DatabaseRuleConfiguration ruleConfig = mockDatabaseRuleConfiguration(false);
        ShardingSphereRule closableRule = mock(ShardingSphereRule.class, withSettings().extraInterfaces(PartialRuleUpdateSupported.class, AutoCloseable.class));
        when(closableRule.getConfiguration()).thenReturn(ruleConfig);
        PartialRuleUpdateSupported updater = (PartialRuleUpdateSupported) closableRule;
        when(updater.partialUpdate(ruleConfig)).thenReturn(true);
        ShardingSphereRule nonClosableRule = mock(ShardingSphereRule.class);
        RuleConfiguration otherRuleConfig = mock(RuleConfiguration.class, withSettings().extraInterfaces(Cloneable.class));
        when(nonClosableRule.getConfiguration()).thenReturn(otherRuleConfig);
        RuleMetaData ruleMetaData = new RuleMetaData(new LinkedList<>(Arrays.asList(closableRule, nonClosableRule)));
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase(DATABASE_NAME).getRuleMetaData()).thenReturn(ruleMetaData);
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createByAlterRule(eq(DATABASE_NAME), eq(false), any(Collection.class), eq(metaDataContexts))).thenReturn(mock(MetaDataContexts.class)))) {
            new DatabaseRuleConfigurationManager(metaDataContexts, mock(), mock()).refresh(DATABASE_NAME, ruleConfig);
            verify(ignored.constructed().iterator().next()).createByAlterRule(eq(DATABASE_NAME), eq(false),
                    argThat(actual -> 2 == actual.size() && actual.contains(ruleConfig) && actual.contains(otherRuleConfig)), eq(metaDataContexts));
            verify((PartialRuleUpdateSupported) closableRule).updateConfiguration(ruleConfig);
            verify(metaDataContexts).update(any(MetaDataContexts.class));
            assertDoesNotThrow(() -> verify((AutoCloseable) closableRule).close());
        }
    }
    
    @Test
    void assertRefreshWithPartialUpdateSkipMetadata() throws SQLException {
        DatabaseRuleConfiguration ruleConfig = mockDatabaseRuleConfiguration(false);
        ShardingSphereRule partialRule = mock(ShardingSphereRule.class, withSettings().extraInterfaces(PartialRuleUpdateSupported.class));
        when(partialRule.getConfiguration()).thenReturn(ruleConfig);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase(DATABASE_NAME).getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(partialRule)));
        new DatabaseRuleConfigurationManager(metaDataContexts, mock(), mock()).refresh(DATABASE_NAME, ruleConfig);
        verify((PartialRuleUpdateSupported) partialRule).updateConfiguration(ruleConfig);
        verify(metaDataContexts, never()).update(any(MetaDataContexts.class));
    }
    
    @Test
    void assertRefreshWithEmptyRuleConfigurationAndPartialRule() throws SQLException {
        DatabaseRuleConfiguration ruleConfig = mockDatabaseRuleConfiguration(true);
        ShardingSphereRule partialRule = mock(ShardingSphereRule.class, withSettings().extraInterfaces(PartialRuleUpdateSupported.class));
        when(partialRule.getConfiguration()).thenReturn(ruleConfig);
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(partialRule));
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase(DATABASE_NAME).getRuleMetaData()).thenReturn(ruleMetaData);
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createByAlterRule(eq(DATABASE_NAME), eq(false), any(Collection.class), eq(metaDataContexts))).thenReturn(mock(MetaDataContexts.class)))) {
            new DatabaseRuleConfigurationManager(metaDataContexts, mock(), mock()).refresh(DATABASE_NAME, ruleConfig);
            verify(ignored.constructed().iterator().next()).createByAlterRule(eq(DATABASE_NAME), eq(false), argThat(Collection::isEmpty), eq(metaDataContexts));
            verify((PartialRuleUpdateSupported) partialRule, never()).partialUpdate(ruleConfig);
            verify((PartialRuleUpdateSupported) partialRule, never()).updateConfiguration(ruleConfig);
            verify(metaDataContexts).update(any(MetaDataContexts.class));
        }
    }
    
    @Test
    void assertRefreshWithPartialUpdateNeedRefreshMetadata() throws SQLException {
        DatabaseRuleConfiguration ruleConfig = mockDatabaseRuleConfiguration(false);
        ShardingSphereRule partialRule = mock(ShardingSphereRule.class, withSettings().extraInterfaces(PartialRuleUpdateSupported.class));
        when(partialRule.getConfiguration()).thenReturn(ruleConfig);
        PartialRuleUpdateSupported updater = (PartialRuleUpdateSupported) partialRule;
        when(updater.partialUpdate(ruleConfig)).thenReturn(true);
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(partialRule));
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase(DATABASE_NAME).getRuleMetaData()).thenReturn(ruleMetaData);
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createByAlterRule(eq(DATABASE_NAME), eq(false), any(Collection.class), eq(metaDataContexts))).thenReturn(mock(MetaDataContexts.class)))) {
            new DatabaseRuleConfigurationManager(metaDataContexts, mock(), mock()).refresh(DATABASE_NAME, ruleConfig);
            verify(ignored.constructed().iterator().next()).createByAlterRule(eq(DATABASE_NAME), eq(false),
                    argThat(actual -> 1 == actual.size() && actual.contains(ruleConfig)), eq(metaDataContexts));
            verify(metaDataContexts).update(any(MetaDataContexts.class));
            verify(updater).updateConfiguration(ruleConfig);
        }
    }
    
    @Test
    void assertRefreshWithoutExistingRule() throws SQLException {
        DatabaseRuleConfiguration ruleConfig = mockDatabaseRuleConfiguration(false);
        ShardingSphereRule otherRule = mock(ShardingSphereRule.class);
        RuleConfiguration otherRuleConfig = mock(RuleConfiguration.class, withSettings().extraInterfaces(Cloneable.class));
        when(otherRule.getConfiguration()).thenReturn(otherRuleConfig);
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(otherRule));
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase(DATABASE_NAME).getRuleMetaData()).thenReturn(ruleMetaData);
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createByAlterRule(eq(DATABASE_NAME), eq(false), any(Collection.class), eq(metaDataContexts))).thenReturn(mock(MetaDataContexts.class)))) {
            new DatabaseRuleConfigurationManager(metaDataContexts, mock(), mock()).refresh(DATABASE_NAME, ruleConfig);
            verify(ignored.constructed().iterator().next()).createByAlterRule(eq(DATABASE_NAME), eq(false),
                    argThat(actual -> 2 == actual.size() && actual.contains(ruleConfig) && actual.contains(otherRuleConfig)), eq(metaDataContexts));
            verify(metaDataContexts).update(any(MetaDataContexts.class));
        }
    }
    
    @Test
    void assertRefreshWithExistingNonPartialRule() throws SQLException {
        DatabaseRuleConfiguration ruleConfig = mockDatabaseRuleConfiguration(false);
        ShardingSphereRule existingRule = mock(ShardingSphereRule.class);
        when(existingRule.getConfiguration()).thenReturn(ruleConfig);
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(existingRule));
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase(DATABASE_NAME).getRuleMetaData()).thenReturn(ruleMetaData);
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createByAlterRule(eq(DATABASE_NAME), eq(false), any(Collection.class), eq(metaDataContexts))).thenReturn(mock(MetaDataContexts.class)))) {
            new DatabaseRuleConfigurationManager(metaDataContexts, mock(), mock()).refresh(DATABASE_NAME, ruleConfig);
            verify(ignored.constructed().iterator().next()).createByAlterRule(eq(DATABASE_NAME), eq(false),
                    argThat(actual -> 1 == actual.size() && actual.contains(ruleConfig)), eq(metaDataContexts));
            verify(metaDataContexts).update(any(MetaDataContexts.class));
        }
    }
    
    @Test
    void assertRefreshWrapsException() {
        DatabaseRuleConfiguration ruleConfig = mockDatabaseRuleConfiguration(false);
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(rule));
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase(DATABASE_NAME).getRuleMetaData()).thenReturn(ruleMetaData);
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createByAlterRule(eq(DATABASE_NAME), eq(false), any(Collection.class), eq(metaDataContexts))).thenThrow(SQLException.class))) {
            assertThrows(SQLException.class, () -> new DatabaseRuleConfigurationManager(metaDataContexts, mock(), mock()).refresh(DATABASE_NAME, ruleConfig));
        }
    }
    
    @Test
    void assertRefreshThrowsWhenCloseOriginalRuleFailed() throws Exception {
        DatabaseRuleConfiguration ruleConfig = mockDatabaseRuleConfiguration(false);
        ShardingSphereRule closableRule = mock(ShardingSphereRule.class, withSettings().extraInterfaces(PartialRuleUpdateSupported.class, AutoCloseable.class));
        when(closableRule.getConfiguration()).thenReturn(ruleConfig);
        PartialRuleUpdateSupported updater = (PartialRuleUpdateSupported) closableRule;
        when(updater.partialUpdate(ruleConfig)).thenReturn(true);
        doThrow(Exception.class).when((AutoCloseable) closableRule).close();
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(closableRule));
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase(DATABASE_NAME).getRuleMetaData()).thenReturn(ruleMetaData);
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createByAlterRule(eq(DATABASE_NAME), eq(false), any(Collection.class), eq(metaDataContexts))).thenReturn(mock(MetaDataContexts.class)))) {
            assertThrows(Exception.class, () -> new DatabaseRuleConfigurationManager(metaDataContexts, mock(), mock()).refresh(DATABASE_NAME, ruleConfig));
        }
    }
    
    private DatabaseRuleConfiguration mockDatabaseRuleConfiguration(final boolean isEmpty) {
        DatabaseRuleConfiguration result = mock(DatabaseRuleConfiguration.class, withSettings().extraInterfaces(Serializable.class));
        DatabaseRuleConfigurationEmptyChecker checker = mock(DatabaseRuleConfigurationEmptyChecker.class);
        when(checker.isEmpty(result)).thenReturn(isEmpty);
        when((TypedSPI) TypedSPILoader.getService(DatabaseRuleConfigurationEmptyChecker.class, result.getClass())).thenReturn(checker);
        return result;
    }
}
