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

package org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database.type;

import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleAlterExecutor;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.database.DatabaseRuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationEmptyChecker;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.PersistServiceFacade;
import org.apache.shardingsphere.mode.persist.mode.ModePersistServiceFacade;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(TypedSPILoader.class)
class AlterDatabaseRuleOperatorTest {
    
    @Mock
    private ContextManager contextManager;
    
    @Mock
    private PersistServiceFacade persistServiceFacade;
    
    @Mock
    private ModePersistServiceFacade modePersistServiceFacade;
    
    @Mock
    private MetaDataManagerPersistService metaDataManagerPersistService;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private DatabaseRuleAlterExecutor executor;
    
    private AlterDatabaseRuleOperator ruleOperator;
    
    @BeforeEach
    void setUp() {
        when(contextManager.getPersistServiceFacade()).thenReturn(persistServiceFacade);
        when(persistServiceFacade.getModeFacade()).thenReturn(modePersistServiceFacade);
        when(modePersistServiceFacade.getMetaDataManagerService()).thenReturn(metaDataManagerPersistService);
        ruleOperator = new AlterDatabaseRuleOperator(contextManager, executor);
    }
    
    @Test
    void assertOperate() {
        DatabaseRuleDefinitionStatement sqlStatement = mock(DatabaseRuleDefinitionStatement.class);
        TestDatabaseRuleConfiguration toBeAlteredRuleConfig = new TestDatabaseRuleConfiguration();
        RuleConfiguration decoratedRuleConfig = mock(RuleConfiguration.class);
        TestDatabaseRuleConfiguration toBeDroppedRuleConfig = new TestDatabaseRuleConfiguration();
        DatabaseRuleConfigurationEmptyChecker emptyChecker = mock(DatabaseRuleConfigurationEmptyChecker.class);
        when(executor.buildToBeAlteredRuleConfiguration(sqlStatement)).thenReturn(toBeAlteredRuleConfig);
        when(database.decorateRuleConfiguration(toBeAlteredRuleConfig)).thenReturn(decoratedRuleConfig);
        when(executor.buildToBeDroppedRuleConfiguration(toBeAlteredRuleConfig)).thenReturn(toBeDroppedRuleConfig);
        when((TypedSPI) TypedSPILoader.getService(DatabaseRuleConfigurationEmptyChecker.class, TestDatabaseRuleConfiguration.class)).thenReturn(emptyChecker);
        when(emptyChecker.isEmpty(toBeDroppedRuleConfig)).thenReturn(false);
        RuleConfiguration currentRuleConfig = mock(RuleConfiguration.class);
        ruleOperator.operate(sqlStatement, database, currentRuleConfig);
        InOrder actual = inOrder(metaDataManagerPersistService);
        actual.verify(metaDataManagerPersistService).alterRuleConfiguration(database, decoratedRuleConfig);
        actual.verify(metaDataManagerPersistService).removeRuleConfigurationItem(database, toBeDroppedRuleConfig);
    }
    
    @Test
    void assertOperateWhenNoRuleConfigurationToRemove() {
        DatabaseRuleDefinitionStatement sqlStatement = mock(DatabaseRuleDefinitionStatement.class);
        TestDatabaseRuleConfiguration toBeAlteredRuleConfig = new TestDatabaseRuleConfiguration();
        RuleConfiguration decoratedRuleConfig = mock(RuleConfiguration.class);
        when(executor.buildToBeAlteredRuleConfiguration(sqlStatement)).thenReturn(toBeAlteredRuleConfig);
        when(database.decorateRuleConfiguration(toBeAlteredRuleConfig)).thenReturn(decoratedRuleConfig);
        RuleConfiguration currentRuleConfig = mock(RuleConfiguration.class);
        ruleOperator.operate(sqlStatement, database, currentRuleConfig);
        verify(metaDataManagerPersistService).alterRuleConfiguration(database, decoratedRuleConfig);
        verify(metaDataManagerPersistService, never()).removeRuleConfigurationItem(database, null);
    }
    
    @Test
    void assertOperateWhenRuleConfigurationToRemoveEmpty() {
        DatabaseRuleDefinitionStatement sqlStatement = mock(DatabaseRuleDefinitionStatement.class);
        TestDatabaseRuleConfiguration toBeAlteredRuleConfig = new TestDatabaseRuleConfiguration();
        RuleConfiguration decoratedRuleConfig = mock(RuleConfiguration.class);
        TestDatabaseRuleConfiguration toBeDroppedRuleConfig = new TestDatabaseRuleConfiguration();
        DatabaseRuleConfigurationEmptyChecker emptyChecker = mock(DatabaseRuleConfigurationEmptyChecker.class);
        when(executor.buildToBeAlteredRuleConfiguration(sqlStatement)).thenReturn(toBeAlteredRuleConfig);
        when(database.decorateRuleConfiguration(toBeAlteredRuleConfig)).thenReturn(decoratedRuleConfig);
        when(executor.buildToBeDroppedRuleConfiguration(toBeAlteredRuleConfig)).thenReturn(toBeDroppedRuleConfig);
        when((TypedSPI) TypedSPILoader.getService(DatabaseRuleConfigurationEmptyChecker.class, TestDatabaseRuleConfiguration.class)).thenReturn(emptyChecker);
        when(emptyChecker.isEmpty(toBeDroppedRuleConfig)).thenReturn(true);
        RuleConfiguration currentRuleConfig = mock(RuleConfiguration.class);
        ruleOperator.operate(sqlStatement, database, currentRuleConfig);
        verify(metaDataManagerPersistService).alterRuleConfiguration(database, decoratedRuleConfig);
        verify(metaDataManagerPersistService, never()).removeRuleConfigurationItem(database, toBeDroppedRuleConfig);
    }
    
    private static final class TestDatabaseRuleConfiguration implements DatabaseRuleConfiguration {
    }
}
