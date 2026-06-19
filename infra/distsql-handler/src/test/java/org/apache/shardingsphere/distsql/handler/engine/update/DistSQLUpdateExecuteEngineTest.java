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

package org.apache.shardingsphere.distsql.handler.engine.update;

import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorConnectionContextAware;
import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database.DatabaseRuleOperator;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database.DatabaseRuleOperatorFactory;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutorFactory;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.global.GlobalRuleDefinitionExecutorFactory;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.database.DatabaseRuleDefinitionStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.global.GlobalRuleDefinitionStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.FromDatabaseSQLStatementAttribute;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("unchecked")
class DistSQLUpdateExecuteEngineTest {
    
    @Test
    void assertExecuteUpdateWithGlobalRuleDefinitionStatement() throws SQLException {
        GlobalRuleDefinitionStatement sqlStatement = mock(GlobalRuleDefinitionStatement.class, RETURNS_DEEP_STUBS);
        RuleMetaData globalRuleMetaData = mock(RuleMetaData.class);
        MetaDataManagerPersistService metaDataManagerPersistService = mock(MetaDataManagerPersistService.class);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        @SuppressWarnings("rawtypes")
        GlobalRuleDefinitionExecutor executor = mock(GlobalRuleDefinitionExecutor.class);
        when(sqlStatement.getAttributes().findAttribute(FromDatabaseSQLStatementAttribute.class)).thenReturn(Optional.empty());
        try (MockedStatic<GlobalRuleDefinitionExecutorFactory> mockedStatic = mockStatic(GlobalRuleDefinitionExecutorFactory.class)) {
            when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
            when(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService()).thenReturn(metaDataManagerPersistService);
            mockedStatic.when(() -> GlobalRuleDefinitionExecutorFactory.newInstance(sqlStatement, globalRuleMetaData)).thenReturn(executor);
            when(executor.buildToBeAlteredRuleConfiguration(sqlStatement)).thenReturn(ruleConfig);
            new DistSQLUpdateExecuteEngine(sqlStatement, null, contextManager, null).executeUpdate();
        }
        verify(executor).checkBeforeUpdate(sqlStatement);
        verify(metaDataManagerPersistService).alterGlobalRuleConfiguration(ruleConfig);
    }
    
    @Test
    void assertExecuteUpdateWithDatabaseRuleDefinitionStatementAndRegisteredExecutor() throws SQLException {
        DatabaseRuleDefinitionStatement sqlStatement = mock(DatabaseRuleDefinitionStatement.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ContextManager contextManager = mock(ContextManager.class);
        @SuppressWarnings("rawtypes")
        DatabaseRuleDefinitionExecutor executor = mock(DatabaseRuleDefinitionExecutor.class);
        DatabaseRuleOperator operator = mock(DatabaseRuleOperator.class);
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        when(sqlStatement.getAttributes().findAttribute(FromDatabaseSQLStatementAttribute.class)).thenReturn(Optional.empty());
        try (
                MockedStatic<DatabaseRuleDefinitionExecutorFactory> executorFactory = mockStatic(DatabaseRuleDefinitionExecutorFactory.class);
                MockedStatic<DatabaseRuleOperatorFactory> operatorFactory = mockStatic(DatabaseRuleOperatorFactory.class)) {
            when(contextManager.getDatabase("foo_db")).thenReturn(database);
            executorFactory.when(() -> DatabaseRuleDefinitionExecutorFactory.findInstance(sqlStatement, database)).thenReturn(Optional.of(executor));
            when(executor.getRuleClass()).thenReturn(ShardingSphereRule.class);
            when(database.getRuleMetaData().findSingleRule(ShardingSphereRule.class)).thenReturn(Optional.of(rule));
            when(rule.getConfiguration()).thenReturn(ruleConfig);
            operatorFactory.when(() -> DatabaseRuleOperatorFactory.newInstance(contextManager, executor)).thenReturn(operator);
            new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", contextManager, null).executeUpdate();
        }
        verify(executor).checkBeforeUpdate(sqlStatement);
        verify(operator).operate(sqlStatement, database, ruleConfig);
    }
    
    @Test
    void assertExecuteUpdateWithDatabaseRuleDefinitionStatementAndAbsentExecutor() throws SQLException {
        DatabaseRuleDefinitionStatement sqlStatement = mock(DatabaseRuleDefinitionStatement.class, RETURNS_DEEP_STUBS);
        ContextManager contextManager = mock(ContextManager.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(sqlStatement.getAttributes().findAttribute(FromDatabaseSQLStatementAttribute.class)).thenReturn(Optional.empty());
        try (MockedStatic<DatabaseRuleDefinitionExecutorFactory> mockedStatic = mockStatic(DatabaseRuleDefinitionExecutorFactory.class)) {
            when(contextManager.getDatabase("foo_db")).thenReturn(database);
            mockedStatic.when(() -> DatabaseRuleDefinitionExecutorFactory.findInstance(sqlStatement, database)).thenReturn(Optional.empty());
            new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", contextManager, null).executeUpdate();
        }
        verify(contextManager).getDatabase("foo_db");
    }
    
    @SuppressWarnings("CastToIncompatibleInterface")
    @Test
    void assertExecuteUpdateWithAdvancedExecutor() throws SQLException {
        DistSQLStatement sqlStatement = mock(DistSQLStatement.class, RETURNS_DEEP_STUBS);
        DistSQLConnectionContext distsqlConnectionContext = mock(DistSQLConnectionContext.class);
        ContextManager contextManager = mock(ContextManager.class);
        @SuppressWarnings("rawtypes")
        AdvancedDistSQLUpdateExecutor executor = mock(AdvancedDistSQLUpdateExecutor.class, withSettings().extraInterfaces(DistSQLExecutorConnectionContextAware.class));
        when(sqlStatement.getAttributes().findAttribute(FromDatabaseSQLStatementAttribute.class)).thenReturn(Optional.empty());
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.findService(AdvancedDistSQLUpdateExecutor.class, sqlStatement.getClass())).thenReturn(Optional.of(executor));
            assertDoesNotThrow(() -> new DistSQLUpdateExecuteEngine(sqlStatement, null, contextManager, distsqlConnectionContext).executeUpdate());
        }
        verify((DistSQLExecutorConnectionContextAware) executor).setConnectionContext(distsqlConnectionContext);
        verify(executor).executeUpdate(sqlStatement, contextManager);
        verify(contextManager, never()).getDatabase(anyString());
    }
    
    @SuppressWarnings({"CastToIncompatibleInterface", "rawtypes"})
    @Test
    void assertExecuteUpdateWithDefaultExecutor() throws SQLException {
        DistSQLStatement sqlStatement = mock(DistSQLStatement.class, RETURNS_DEEP_STUBS);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        DistSQLConnectionContext distsqlConnectionContext = mock(DistSQLConnectionContext.class);
        DistSQLUpdateExecutor executor = mock(DistSQLUpdateExecutor.class, withSettings().extraInterfaces(DistSQLExecutorConnectionContextAware.class));
        when(sqlStatement.getAttributes().findAttribute(FromDatabaseSQLStatementAttribute.class)).thenReturn(Optional.empty());
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.findService(AdvancedDistSQLUpdateExecutor.class, sqlStatement.getClass())).thenReturn(Optional.empty());
            mockedStatic.when(() -> TypedSPILoader.getService(DistSQLUpdateExecutor.class, sqlStatement.getClass())).thenReturn(executor);
            new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", contextManager, distsqlConnectionContext).executeUpdate();
        }
        verify(contextManager).getDatabase("foo_db");
        verify((DistSQLExecutorConnectionContextAware) executor).setConnectionContext(distsqlConnectionContext);
        verify(executor).executeUpdate(sqlStatement, contextManager);
    }
}
