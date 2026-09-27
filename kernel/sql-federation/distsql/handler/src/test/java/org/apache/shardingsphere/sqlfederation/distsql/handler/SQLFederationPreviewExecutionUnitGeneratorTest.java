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

package org.apache.shardingsphere.sqlfederation.distsql.handler;

import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.executor.rul.PreviewExecutionUnitGenerator;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sqlfederation.context.SQLFederationContext;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class SQLFederationPreviewExecutionUnitGeneratorTest {
    
    private final PreviewExecutionUnitGenerator generator = ShardingSphereServiceLoader.getServiceInstances(PreviewExecutionUnitGenerator.class).iterator().next();
    
    @Test
    void assertGenerateWhenFederationNotSelected() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        when(database.getDefaultSchemaName()).thenReturn("foo_schema");
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getTablesContext().getSchemaName()).thenReturn(Optional.empty());
        ContextManager contextManager = mockContextManager();
        DistSQLConnectionContext connectionContext = mockConnectionContext();
        try (
                MockedConstruction<JDBCExecutor> ignoredJDBCExecutor = mockConstruction(JDBCExecutor.class);
                MockedConstruction<SQLFederationEngine> ignoredFederationEngine =
                        mockConstruction(SQLFederationEngine.class, (mock, context) -> when(mock.decide(any(), any())).thenReturn(false))) {
            Optional<Collection<ExecutionUnit>> actual = generator.generate(database, queryContext, contextManager, connectionContext);
            assertFalse(actual.isPresent());
        }
    }
    
    @Test
    void assertGenerateWhenFederationSelected() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getDefaultSchemaName()).thenReturn("foo_schema");
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getTablesContext().getSchemaName()).thenReturn(Optional.empty());
        ContextManager contextManager = mockContextManager();
        DistSQLConnectionContext connectionContext = mockConnectionContext();
        when(connectionContext.getProcessId()).thenReturn("foo_process");
        ExecutionUnit expected = new ExecutionUnit("foo_ds", new SQLUnit("SELECT 1", Collections.emptyList()));
        AtomicReference<String> actualSchemaName = new AtomicReference<>();
        try (
                MockedConstruction<JDBCExecutor> ignoredJDBCExecutor = mockConstruction(JDBCExecutor.class);
                MockedConstruction<DriverExecutionPrepareEngine> ignoredPrepareEngine = mockConstruction(DriverExecutionPrepareEngine.class);
                MockedConstruction<SQLFederationEngine> ignoredFederationEngine = mockConstruction(SQLFederationEngine.class, (mock, context) -> {
                    actualSchemaName.set((String) context.arguments().get(1));
                    when(mock.decide(any(), any())).thenReturn(true);
                    doAnswer(invocation -> {
                        SQLFederationContext federationContext = invocation.getArgument(2);
                        assertThat(federationContext.getProcessId(), is("foo_process"));
                        federationContext.getPreviewExecutionUnits().add(expected);
                        return null;
                    }).when(mock).executeQuery(any(), any(), any());
                })) {
            Optional<Collection<ExecutionUnit>> actual = generator.generate(database, queryContext, contextManager, connectionContext);
            assertTrue(actual.isPresent());
            assertThat(actual.get(), is(Collections.singletonList(expected)));
            assertThat(actualSchemaName.get(), is("foo_schema"));
        }
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getGlobalRuleMetaData()).thenReturn(mock(RuleMetaData.class));
        when(metaData.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(result.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        return result;
    }
    
    private DistSQLConnectionContext mockConnectionContext() {
        DistSQLConnectionContext result = mock(DistSQLConnectionContext.class, RETURNS_DEEP_STUBS);
        when(result.getExecutorEngineSupplier()).thenReturn(() -> mock(ExecutorEngine.class));
        return result;
    }
}
