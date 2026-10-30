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

package org.apache.shardingsphere.driver.executor.engine.pushdown.jdbc;

import org.apache.shardingsphere.driver.executor.callback.add.StatementAddCallback;
import org.apache.shardingsphere.driver.executor.callback.replay.StatementReplayCallback;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DriverJDBCPushDownExecuteQueryExecutorTest {
    
    @Test
    @SuppressWarnings("unchecked")
    void assertExecuteQuery() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        StorageUnit mysqlStorageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        StorageUnit postgresqlStorageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(mysqlStorageUnit.getStorageType().getType()).thenReturn("MySQL");
        when(postgresqlStorageUnit.getStorageType().getType()).thenReturn("PostgreSQL");
        Map<String, StorageUnit> storageUnits = new HashMap<>(2, 1F);
        storageUnits.put("foo_mysql", mysqlStorageUnit);
        storageUnits.put("foo_postgresql", postgresqlStorageUnit);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        QueryContext queryContext = mock(QueryContext.class);
        ExecutionContext executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
        when(executionContext.getSqlStatementContext().getSqlStatement()).thenReturn(mock(SelectStatement.class));
        when(executionContext.getExecutionUnits()).thenReturn(Arrays.asList(
                new ExecutionUnit("foo_mysql", new SQLUnit("SELECT id FROM foo_table", Collections.emptyList())),
                new ExecutionUnit("foo_postgresql", new SQLUnit("SELECT id FROM foo_table", Collections.emptyList()))));
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        JDBCExecutor backendExecutor = mock(JDBCExecutor.class);
        DriverJDBCPushDownExecuteQueryExecutor executor = new DriverJDBCPushDownExecuteQueryExecutor(connection, metaData, backendExecutor);
        try (
                MockedConstruction<KernelProcessor> ignored = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(), any(), any())).thenReturn(executionContext))) {
            assertThrows(SQLException.class, () -> executor.executeQuery(database, queryContext, prepareEngine, mock(Statement.class), Collections.emptyMap(), mock(StatementAddCallback.class),
                    mock(StatementReplayCallback.class)));
            verifyNoInteractions(prepareEngine, backendExecutor);
        }
    }
}
