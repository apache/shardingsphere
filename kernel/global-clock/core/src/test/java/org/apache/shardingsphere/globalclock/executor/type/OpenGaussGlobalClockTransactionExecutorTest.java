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

package org.apache.shardingsphere.globalclock.executor.type;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.globalclock.executor.GlobalClockTransactionExecutor;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenGaussGlobalClockTransactionExecutorTest {
    
    private GlobalClockTransactionExecutor executor;
    
    @BeforeEach
    void setUp() {
        executor = DatabaseTypedSPILoader.getService(GlobalClockTransactionExecutor.class, TypedSPILoader.getService(DatabaseType.class, "openGauss"));
    }
    
    @Test
    void assertSendSnapshotTimestamp() throws SQLException {
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        executor.sendSnapshotTimestamp(Collections.singleton(connection), 10L);
        verify(statement).execute("SELECT 10 AS SETSNAPSHOTCSN");
    }
    
    @Test
    void assertSendCommitTimestamp() throws SQLException {
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        executor.sendCommitTimestamp(Collections.singleton(connection), 10L);
        verify(statement).execute("SELECT 10 AS SETCOMMITCSN");
    }
}
