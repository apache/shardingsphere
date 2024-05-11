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

package org.apache.shardingsphere.infra.executor.sql.execute.engine.jdbc;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JDBCExecutorCallbackTest {
    
    @Mock
    private PreparedStatement preparedStatement;
    
    private Collection<JDBCExecutionUnit> units;
    
    @BeforeEach
    void setUp() {
        units = Collections.singletonList(
                new JDBCExecutionUnit(new ExecutionUnit("ds", new SQLUnit("SELECT now()", Collections.emptyList())), ConnectionMode.CONNECTION_STRICTLY, preparedStatement));
        SQLExecutorExceptionHandler.setExceptionThrown(true);
    }
    
    @Test
    void assertExecuteFailedAndProtocolTypeDifferentWithDatabaseType() throws SQLException {
        Object saneResult = new Object();
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        when(resourceMetaData.getStorageUnits().get("ds").getStorageType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        JDBCExecutorCallback<Object> callback =
                new JDBCExecutorCallback<Object>(TypedSPILoader.getService(DatabaseType.class, "MySQL"), resourceMetaData, mock(SelectStatement.class), true) {
                    
                    @Override
                    protected Object executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
                        throw new SQLException("");
                    }
                    
                    @Override
                    protected Optional<Object> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
                        return Optional.of(saneResult);
                    }
                };
        String processId = new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong()).toString().replace("-", "");
        assertThat(callback.execute(units, true, processId), is(Collections.singletonList(saneResult)));
        assertThat(callback.execute(units, false, processId), is(Collections.emptyList()));
    }
    
    @Test
    void assertExecuteSQLExceptionOccurredAndProtocolTypeSameAsDatabaseType() {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        when(resourceMetaData.getStorageUnits().get("ds").getStorageType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        JDBCExecutorCallback<Object> callback =
                new JDBCExecutorCallback<Object>(TypedSPILoader.getService(DatabaseType.class, "MySQL"), resourceMetaData, mock(SelectStatement.class), true) {
                    
                    @Override
                    protected Object executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
                        throw new SQLException("");
                    }
                    
                    @Override
                    protected Optional<Object> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
                        return Optional.empty();
                    }
                };
        String processId = new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong()).toString().replace("-", "");
        assertThrows(SQLException.class, () -> callback.execute(units, true, processId));
    }
}
