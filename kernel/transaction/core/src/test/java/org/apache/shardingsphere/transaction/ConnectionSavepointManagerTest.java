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

package org.apache.shardingsphere.transaction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ConnectionSavepointManagerTest {
    
    private static final String SAVE_POINT = "SavePoint";
    
    @Mock
    private Connection connection;
    
    @Mock
    private Savepoint savepoint;
    
    @Before
    public void setup() throws SQLException {
        when(connection.setSavepoint(SAVE_POINT)).thenReturn(savepoint);
    }
    
    @Test
    public void assertSetSavepoint() throws SQLException {
        ConnectionSavepointManager.getInstance().setSavepoint(connection, SAVE_POINT);
        verify(connection).setSavepoint(SAVE_POINT);
    }
    
    @Test
    public void assertRollbackToSavepoint() throws SQLException {
        ConnectionSavepointManager.getInstance().setSavepoint(connection, SAVE_POINT);
        ConnectionSavepointManager.getInstance().rollbackToSavepoint(connection, SAVE_POINT);
        verify(connection).rollback(savepoint);
    }
    
    @Test
    public void assertSaveReleaseSavingPoint() throws SQLException {
        ConnectionSavepointManager.getInstance().setSavepoint(connection, SAVE_POINT);
        ConnectionSavepointManager.getInstance().releaseSavepoint(connection, SAVE_POINT);
        verify(connection).releaseSavepoint(savepoint);
    }
    
    @Test
    public void assertTransactionFinished() throws SQLException {
        ConnectionSavepointManager.getInstance().setSavepoint(connection, SAVE_POINT);
        ConnectionSavepointManager.getInstance().transactionFinished(connection);
        ConnectionSavepointManager.getInstance().releaseSavepoint(connection, SAVE_POINT);
        verify(connection, never()).releaseSavepoint(any(Savepoint.class));
    }
}
