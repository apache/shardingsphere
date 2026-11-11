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

package org.apache.shardingsphere.driver.executor.engine;

import org.apache.shardingsphere.driver.executor.engine.pushdown.jdbc.DriverJDBCPushDownExecuteUpdateExecutor;
import org.apache.shardingsphere.driver.executor.engine.pushdown.raw.DriverRawPushDownExecuteUpdateExecutor;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.attribute.raw.RawExecutionRuleAttribute;
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DriverExecuteUpdateExecutorTest {
    
    private ShardingSphereMetaData metaData;
    
    private DriverJDBCPushDownExecuteUpdateExecutor jdbcPushDownExecutor;
    
    private DriverRawPushDownExecuteUpdateExecutor rawPushDownExecutor;
    
    private DriverExecuteUpdateExecutor executor;
    
    @BeforeEach
    void setUp() {
        metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        jdbcPushDownExecutor = mock(DriverJDBCPushDownExecuteUpdateExecutor.class);
        rawPushDownExecutor = mock(DriverRawPushDownExecuteUpdateExecutor.class);
        executor = new DriverExecuteUpdateExecutor(metaData, jdbcPushDownExecutor, rawPushDownExecutor);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertExecuteUpdateWithMixedStorageTypesForDAL() {
        ShardingSphereDatabase database = createDatabase(true);
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        AnalyzeTableStatement sqlStatement = mock(AnalyzeTableStatement.class);
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(sqlStatement);
        ExecutionContext executionContext = createExecutionContext(queryContext);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        try (
                MockedConstruction<KernelProcessor> ignored = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(), any(), any())).thenReturn(executionContext))) {
            assertThrows(SQLException.class, () -> executor.executeUpdate(database, queryContext, prepareEngine, (sql, statement) -> 0, (statements, params) -> {
            }, () -> {
            }));
            verifyNoInteractions(prepareEngine, jdbcPushDownExecutor, rawPushDownExecutor);
        }
    }
    
    private ShardingSphereDatabase createDatabase(final boolean mixedStorageTypes) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        StorageUnit firstStorageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        StorageUnit secondStorageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(firstStorageUnit.getStorageType().getType()).thenReturn("PostgreSQL");
        when(secondStorageUnit.getStorageType().getType()).thenReturn(mixedStorageTypes ? "openGauss" : "PostgreSQL");
        Map<String, StorageUnit> storageUnits = new HashMap<>(2, 1F);
        storageUnits.put("foo_ds", firstStorageUnit);
        storageUnits.put("bar_ds", secondStorageUnit);
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(result.getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class)).thenReturn(Collections.emptyList());
        return result;
    }
    
    private ExecutionContext createExecutionContext(final QueryContext queryContext) {
        return new ExecutionContext(queryContext, Arrays.asList(
                new ExecutionUnit("foo_ds", new SQLUnit("ANALYZE foo_table", Collections.emptyList())),
                new ExecutionUnit("bar_ds", new SQLUnit("ANALYZE foo_table", Collections.emptyList()))), mock(RouteContext.class));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertExecuteUpdateWithMixedStorageTypesForRawDAL() {
        ShardingSphereDatabase database = createDatabase(true);
        when(database.getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class)).thenReturn(Collections.singletonList(mock(RawExecutionRuleAttribute.class)));
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        AnalyzeTableStatement sqlStatement = mock(AnalyzeTableStatement.class);
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(sqlStatement);
        ExecutionContext executionContext = createExecutionContext(queryContext);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        try (
                MockedConstruction<KernelProcessor> ignored = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(), any(), any())).thenReturn(executionContext))) {
            assertThrows(SQLException.class, () -> executor.executeUpdate(database, queryContext, prepareEngine, (sql, statement) -> 0, (statements, params) -> {
            }, () -> {
            }));
            verifyNoInteractions(prepareEngine, jdbcPushDownExecutor, rawPushDownExecutor);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getAllowedUpdates")
    @SuppressWarnings("unchecked")
    void assertExecuteUpdate(final SQLStatement sqlStatement, final boolean mixedStorageTypes, final boolean raw, final int expected) throws SQLException {
        ShardingSphereDatabase database = createDatabase(mixedStorageTypes);
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(sqlStatement);
        ExecutionContext executionContext = createExecutionContext(queryContext);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        if (raw) {
            when(database.getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class)).thenReturn(Collections.singletonList(mock(RawExecutionRuleAttribute.class)));
            when(rawPushDownExecutor.executeUpdate(database, executionContext)).thenReturn(expected);
        } else {
            when(jdbcPushDownExecutor.executeUpdate(any(), any(), any(), any(), any(), any())).thenReturn(expected);
        }
        try (
                MockedConstruction<KernelProcessor> ignored = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(), any(), any())).thenReturn(executionContext))) {
            int actual = executor.executeUpdate(database, queryContext, prepareEngine, (sql, statement) -> 0, (statements, params) -> {
            }, () -> {
            });
            assertThat(actual, is(expected));
        }
    }
    
    private static Stream<Arguments> getAllowedUpdates() {
        return Stream.of(
                Arguments.of(Named.of("homogeneous DAL with JDBC", mock(AnalyzeTableStatement.class)), false, false, 11),
                Arguments.of(Named.of("heterogeneous DML with JDBC", mock(UpdateStatement.class)), true, false, 11),
                Arguments.of(Named.of("homogeneous DAL with raw executor", mock(AnalyzeTableStatement.class)), false, true, 17),
                Arguments.of(Named.of("heterogeneous DML with raw executor", mock(UpdateStatement.class)), true, true, 17));
    }
}
