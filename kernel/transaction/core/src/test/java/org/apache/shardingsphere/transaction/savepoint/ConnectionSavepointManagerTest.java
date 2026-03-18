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

package org.apache.shardingsphere.transaction.savepoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConnectionSavepointManagerTest {
    
    private static final String SAVE_POINT = "foo_savepoint";
    
    @Mock
    private Connection connection;
    
    @Mock
    private Savepoint savepoint;
    
    @BeforeEach
    void setup() throws SQLException {
        when(connection.setSavepoint(SAVE_POINT)).thenReturn(savepoint);
    }
    
    @Test
    void assertSetSavepoint() throws SQLException {
        ConnectionSavepointManager.getInstance().setSavepoint(connection, SAVE_POINT);
        verify(connection).setSavepoint(SAVE_POINT);
    }
    
    @Test
    void assertRollbackToSavepoint() throws SQLException {
        ConnectionSavepointManager.getInstance().setSavepoint(connection, SAVE_POINT);
        ConnectionSavepointManager.getInstance().rollbackToSavepoint(connection, SAVE_POINT);
        verify(connection).rollback(savepoint);
    }
    
    @Test
    void assertRollbackWithoutSavepoint() throws SQLException {
        ConnectionSavepointManager.getInstance().rollbackToSavepoint(connection, SAVE_POINT);
        verify(connection, never()).rollback(savepoint);
    }
    
    @Test
    void assertSaveReleaseSavingPoint() throws SQLException {
        ConnectionSavepointManager.getInstance().setSavepoint(connection, SAVE_POINT);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:postgresql://127.0.0.1:5432/foo_ds");
        ConnectionSavepointManager.getInstance().releaseSavepoint(connection, SAVE_POINT);
        verify(connection).releaseSavepoint(savepoint);
    }
    
    @Test
    void assertSaveReleaseSavingPointOfMySQL() throws SQLException {
        ConnectionSavepointManager.getInstance().setSavepoint(connection, SAVE_POINT);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mysql://127.0.0.1:3306/foo_ds");
        Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        ConnectionSavepointManager.getInstance().releaseSavepoint(connection, SAVE_POINT);
        verify(statement).execute("RELEASE SAVEPOINT foo_savepoint");
    }
    
    @Test
    void assertTransactionFinished() throws SQLException {
        ConnectionSavepointManager.getInstance().setSavepoint(connection, SAVE_POINT);
        ConnectionSavepointManager.getInstance().transactionFinished(connection);
        ConnectionSavepointManager.getInstance().releaseSavepoint(connection, SAVE_POINT);
        verify(connection, never()).releaseSavepoint(any(Savepoint.class));
    }
}
