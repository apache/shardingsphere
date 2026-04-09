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

package org.apache.shardingsphere.distsql.handler.aware;

import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.rule.global.ShowGlobalRulesStatement;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.aware.ConnectionContextAware;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DistSQLExecutorAwareSetterTest {
    
    @Test
    void assertSetWithUnawareExecutor() {
        ContextManager contextManager = mock(ContextManager.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        DistSQLConnectionContext distSQLConnectionContext = mock(DistSQLConnectionContext.class);
        DistSQLStatement sqlStatement = mock(DistSQLStatement.class);
        new DistSQLExecutorAwareSetter(new Object()).set(contextManager, database, distSQLConnectionContext, sqlStatement);
        verifyNoInteractions(contextManager, database, distSQLConnectionContext, sqlStatement);
    }
    
    @Test
    void assertSetWithDatabaseAwareExecutorAndNullDatabase() {
        DistSQLExecutorDatabaseAware executor = mock(DistSQLExecutorDatabaseAware.class);
        DistSQLExecutorAwareSetter setter = new DistSQLExecutorAwareSetter(executor);
        assertThrows(NoDatabaseSelectedException.class, () -> setter.set(mock(ContextManager.class), null, mock(DistSQLConnectionContext.class), mock(DistSQLStatement.class)));
        verifyNoInteractions(executor);
    }
    
    @Test
    void assertSetWithDatabaseAwareExecutor() {
        DistSQLExecutorDatabaseAware executor = mock(DistSQLExecutorDatabaseAware.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        new DistSQLExecutorAwareSetter(executor).set(mock(ContextManager.class), database, mock(DistSQLConnectionContext.class), mock(DistSQLStatement.class));
        verify(executor).setDatabase(database);
    }
    
    @Test
    void assertSetWithRuleAwareExecutorAndNullDatabase() {
        DistSQLExecutorRuleAware<ShardingSphereRule> executor = mockRuleAwareExecutor();
        DistSQLExecutorAwareSetter setter = new DistSQLExecutorAwareSetter(executor);
        assertThrows(NoDatabaseSelectedException.class, () -> setter.set(mock(ContextManager.class), null, mock(DistSQLConnectionContext.class), mock(DistSQLStatement.class)));
        verifyNoInteractions(executor);
    }
    
    @Test
    void assertSetWithRuleAwareExecutorUsingGlobalRule() {
        DistSQLExecutorRuleAware<ShardingSphereRule> executor = mockRuleAwareExecutor();
        ShardingSphereRule expectedRule = mock(ShardingSphereRule.class);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findSingleRule(ShardingSphereRule.class)).thenReturn(Optional.of(expectedRule));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        new DistSQLExecutorAwareSetter(executor).set(contextManager, database, mock(DistSQLConnectionContext.class), mock(DistSQLStatement.class));
        verify(executor).setRule(expectedRule);
        verify(database, never()).getRuleMetaData();
    }
    
    @Test
    void assertSetWithRuleAwareExecutorUsingDatabaseRule() {
        DistSQLExecutorRuleAware<ShardingSphereRule> executor = mockRuleAwareExecutor();
        ShardingSphereRule expectedRule = mock(ShardingSphereRule.class);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findSingleRule(ShardingSphereRule.class)).thenReturn(Optional.empty());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().findSingleRule(ShardingSphereRule.class)).thenReturn(Optional.of(expectedRule));
        new DistSQLExecutorAwareSetter(executor).set(contextManager, database, mock(DistSQLConnectionContext.class), mock(DistSQLStatement.class));
        verify(executor).setRule(expectedRule);
    }
    
    @Test
    void assertSetWithRuleAwareExecutorAndMissingRule() {
        DistSQLExecutorRuleAware<ShardingSphereRule> executor = mockRuleAwareExecutor();
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findSingleRule(ShardingSphereRule.class)).thenReturn(Optional.empty());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().findSingleRule(ShardingSphereRule.class)).thenReturn(Optional.empty());
        UnsupportedSQLOperationException actual = assertThrows(UnsupportedSQLOperationException.class,
                () -> new DistSQLExecutorAwareSetter(executor).set(contextManager, database, mock(DistSQLConnectionContext.class), mock(DistSQLStatement.class)));
        assertThat(actual.getMessage(), is("Unsupported SQL operation: The current database has no `interface org.apache.shardingsphere.infra.rule.ShardingSphereRule` rules."));
        verify(executor, never()).setRule(any());
    }
    
    @Test
    void assertSetWithRuleAwareExecutorAndShowGlobalRulesStatement() {
        DistSQLExecutorRuleAware<ShardingSphereRule> executor = mockRuleAwareExecutor();
        ShardingSphereRule expectedRule = mock(ShardingSphereRule.class);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findSingleRule(ShardingSphereRule.class)).thenReturn(Optional.of(expectedRule));
        new DistSQLExecutorAwareSetter(executor).set(contextManager, null, mock(DistSQLConnectionContext.class), mock(ShowGlobalRulesStatement.class));
        verify(executor).setRule(expectedRule);
    }
    
    @Test
    void assertSetWithGlobalRuleDefinitionExecutor() {
        GlobalRuleDefinitionExecutor<?, ShardingSphereRule> executor = mockGlobalRuleDefinitionExecutor();
        ShardingSphereRule expectedRule = mock(ShardingSphereRule.class);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findSingleRule(ShardingSphereRule.class)).thenReturn(Optional.of(expectedRule));
        new DistSQLExecutorAwareSetter(executor).set(contextManager, null, mock(DistSQLConnectionContext.class), mock(DistSQLStatement.class));
        verify(executor).setRule(expectedRule);
    }
    
    @Test
    void assertSetWithDistSQLConnectionContextAwareExecutor() {
        DistSQLExecutorConnectionContextAware executor = mock(DistSQLExecutorConnectionContextAware.class);
        DistSQLConnectionContext distSQLConnectionContext = mock(DistSQLConnectionContext.class);
        new DistSQLExecutorAwareSetter(executor).set(mock(ContextManager.class), mock(ShardingSphereDatabase.class), distSQLConnectionContext, mock(DistSQLStatement.class));
        verify(executor).setConnectionContext(distSQLConnectionContext);
    }
    
    @Test
    void assertSetWithConnectionContextAwareExecutor() {
        ConnectionContextAware executor = mock(ConnectionContextAware.class);
        ConnectionContext expectedConnectionContext = mock(ConnectionContext.class);
        DistSQLConnectionContext distSQLConnectionContext = mock(DistSQLConnectionContext.class);
        QueryContext queryContext = mock(QueryContext.class);
        when(distSQLConnectionContext.getQueryContext()).thenReturn(queryContext);
        when(queryContext.getConnectionContext()).thenReturn(expectedConnectionContext);
        new DistSQLExecutorAwareSetter(executor).set(mock(ContextManager.class), mock(ShardingSphereDatabase.class), distSQLConnectionContext, mock(DistSQLStatement.class));
        verify(executor).setConnectionContext(expectedConnectionContext);
    }
    
    @SuppressWarnings("unchecked")
    private DistSQLExecutorRuleAware<ShardingSphereRule> mockRuleAwareExecutor() {
        DistSQLExecutorRuleAware<ShardingSphereRule> result = mock(DistSQLExecutorRuleAware.class);
        when(result.getRuleClass()).thenReturn(ShardingSphereRule.class);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private GlobalRuleDefinitionExecutor<?, ShardingSphereRule> mockGlobalRuleDefinitionExecutor() {
        GlobalRuleDefinitionExecutor<?, ShardingSphereRule> result = mock(GlobalRuleDefinitionExecutor.class);
        when(result.getRuleClass()).thenReturn(ShardingSphereRule.class);
        return result;
    }
}
