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

package org.apache.shardingsphere.driver.executor.engine.batch.preparedstatement;

import org.apache.shardingsphere.driver.executor.callback.replay.PreparedStatementParametersReplayCallback;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.statement.StatementManager;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DriverExecuteBatchExecutorTest {
    
    private ShardingSphereConnection connection;
    
    private ShardingSphereMetaData metaData;
    
    private ShardingSphereDatabase database;
    
    private QueryContext queryContext;
    
    private StatementOption statementOption;
    
    @BeforeEach
    void setUp() {
        connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(any())).thenReturn(1);
        metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        statementOption = mock(StatementOption.class);
        when(queryContext.getMetaData().getGlobalRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        StorageUnit firstStorageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        StorageUnit secondStorageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(firstStorageUnit.getStorageType().getType()).thenReturn("PostgreSQL");
        when(secondStorageUnit.getStorageType().getType()).thenReturn("openGauss");
        Map<String, StorageUnit> storageUnits = new HashMap<>(2, 1F);
        storageUnits.put("foo_ds", firstStorageUnit);
        storageUnits.put("bar_ds", secondStorageUnit);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
    }
    
    @ParameterizedTest(name = "first route {0}, last route {1}")
    @MethodSource("getRejectedBatches")
    void assertExecuteBatchWithMixedStorageTypesForDAL(final Collection<String> firstDataSourceNames, final Collection<String> lastDataSourceNames) throws SQLException {
        AnalyzeTableStatement sqlStatement = mock(AnalyzeTableStatement.class);
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(sqlStatement);
        ExecutionContext firstContext = createExecutionContext(firstDataSourceNames);
        ExecutionContext lastContext = createExecutionContext(lastDataSourceNames);
        Collection<BatchExecutionUnit> batchExecutionUnits = Arrays.asList(
                new BatchExecutionUnit(new ExecutionUnit("foo_ds", new SQLUnit("ANALYZE foo_table", Collections.emptyList()))),
                new BatchExecutionUnit(new ExecutionUnit("bar_ds", new SQLUnit("ANALYZE foo_table", Collections.emptyList()))));
        PreparedStatementParametersReplayCallback replayCallback = mock(PreparedStatementParametersReplayCallback.class);
        try (
                MockedConstruction<KernelProcessor> ignored = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(), any(), any())).thenReturn(1 == context.getCount() ? firstContext : lastContext));
                MockedConstruction<DriverExecutionPrepareEngine> prepareEngines = mockConstruction(DriverExecutionPrepareEngine.class);
                MockedConstruction<BatchPreparedStatementExecutor> batchExecutors = mockConstruction(BatchPreparedStatementExecutor.class,
                        (mock, context) -> when(mock.getBatchExecutionUnits()).thenReturn(batchExecutionUnits))) {
            DriverExecuteBatchExecutor executor = new DriverExecuteBatchExecutor(connection, metaData, statementOption, mock(StatementManager.class), database);
            executor.addBatch(queryContext, database);
            executor.addBatch(queryContext, database);
            assertThrows(SQLException.class, () -> executor.executeBatch(database, queryContext.getSqlStatementContext(), Collections.emptyList(), statementOption,
                    (statements, params) -> {
                    }, replayCallback, () -> {
                    }));
            verifyNoInteractions(prepareEngines.constructed().get(0), replayCallback);
            verify(batchExecutors.constructed().get(0), never()).init(any());
            verify(batchExecutors.constructed().get(0), never()).executeBatch(any());
        }
    }
    
    private ExecutionContext createExecutionContext(final Collection<String> dataSourceNames) {
        Collection<ExecutionUnit> executionUnits = new ArrayList<>(dataSourceNames.size());
        for (String each : dataSourceNames) {
            executionUnits.add(new ExecutionUnit(each, new SQLUnit("ANALYZE foo_table", Collections.emptyList())));
        }
        return new ExecutionContext(queryContext, executionUnits, mock(RouteContext.class));
    }
    
    private static Stream<Arguments> getRejectedBatches() {
        return Stream.of(
                Arguments.of(Collections.singletonList("foo_ds"), Collections.singletonList("bar_ds")),
                Arguments.of(Arrays.asList("foo_ds", "bar_ds"), Collections.emptyList()),
                Arguments.of(Arrays.asList("foo_ds", "bar_ds"), Arrays.asList("foo_ds", "bar_ds")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getAllowedBatches")
    @SuppressWarnings("unchecked")
    void assertExecuteBatch(final SQLStatement sqlStatement, final Collection<String> dataSourceNames, final boolean homogeneous) throws SQLException {
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(sqlStatement);
        if (homogeneous) {
            when(database.getResourceMetaData().getStorageUnits().get("bar_ds").getStorageType().getType()).thenReturn("PostgreSQL");
        }
        ExecutionContext executionContext = createExecutionContext(dataSourceNames);
        Collection<BatchExecutionUnit> batchExecutionUnits = new ArrayList<>(executionContext.getExecutionUnits().size());
        for (ExecutionUnit each : executionContext.getExecutionUnits()) {
            batchExecutionUnits.add(new BatchExecutionUnit(each));
        }
        try (
                MockedConstruction<KernelProcessor> ignored = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(), any(), any())).thenReturn(executionContext));
                MockedConstruction<DriverExecutionPrepareEngine> prepareEngines = mockConstruction(DriverExecutionPrepareEngine.class,
                        (mock, context) -> doReturn(mock(ExecutionGroupContext.class)).when(mock).prepare(any(), any(), any(), any()));
                MockedConstruction<BatchPreparedStatementExecutor> batchExecutors = mockConstruction(BatchPreparedStatementExecutor.class, (mock, context) -> {
                    when(mock.getBatchExecutionUnits()).thenReturn(batchExecutionUnits);
                    when(mock.executeBatch(any())).thenReturn(new int[]{1});
                })) {
            DriverExecuteBatchExecutor executor = new DriverExecuteBatchExecutor(connection, metaData, statementOption, mock(StatementManager.class), database);
            executor.addBatch(queryContext, database);
            int[] actual = executor.executeBatch(database, queryContext.getSqlStatementContext(), Collections.emptyList(), statementOption,
                    (statements, params) -> {
                    }, (statement, params) -> {
                    }, () -> {
                    });
            assertThat(actual, is(new int[]{1}));
            verify(prepareEngines.constructed().get(0)).prepare(any(), any(), any(), any());
        }
    }
    
    private static Stream<Arguments> getAllowedBatches() {
        return Stream.of(
                Arguments.of(Named.of("single-source DAL", mock(AnalyzeTableStatement.class)), Collections.singletonList("foo_ds"), false),
                Arguments.of(Named.of("homogeneous DAL", mock(AnalyzeTableStatement.class)), Arrays.asList("foo_ds", "bar_ds"), true),
                Arguments.of(Named.of("heterogeneous DML", mock(UpdateStatement.class)), Arrays.asList("foo_ds", "bar_ds"), false));
    }
    
    @Test
    void assertExecuteBatchWithoutEntries() throws SQLException {
        try (
                MockedConstruction<DriverExecutionPrepareEngine> prepareEngines = mockConstruction(DriverExecutionPrepareEngine.class);
                MockedConstruction<BatchPreparedStatementExecutor> batchExecutors = mockConstruction(BatchPreparedStatementExecutor.class)) {
            DriverExecuteBatchExecutor executor = new DriverExecuteBatchExecutor(connection, metaData, statementOption, mock(StatementManager.class), database);
            int[] actual = executor.executeBatch(database, queryContext.getSqlStatementContext(), Collections.emptyList(), statementOption,
                    (statements, params) -> {
                    }, (statement, params) -> {
                    }, () -> {
                    });
            assertThat(actual, is(new int[0]));
            verifyNoInteractions(prepareEngines.constructed().get(0), batchExecutors.constructed().get(0));
        }
    }
}
