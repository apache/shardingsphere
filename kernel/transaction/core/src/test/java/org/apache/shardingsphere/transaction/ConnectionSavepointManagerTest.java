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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ConnectionSavepointManagerTest {
    
    private static final String SAVE_POINT = "SavePoint";
    
    @Mock
    private Connection connection;
    
    @Mock
    private Savepoint savepoint;
    
    @InjectMocks
    private ConnectionSavepointManager connectionSavepointManager;
    
    @Before
    public void setup() throws SQLException {
        MockitoAnnotations.initMocks(this);
        when(connection.setSavepoint(SAVE_POINT)).thenReturn(savepoint);
    }
    
    @Test
    public void testSetSavepoint() throws SQLException {
        connectionSavepointManager.setSavepoint(connection, SAVE_POINT);
        verify(connection, times(1)).setSavepoint(SAVE_POINT);
    }
    
    @Test
    public void testRollbackToSavepoint() throws SQLException {
        connectionSavepointManager.setSavepoint(connection, SAVE_POINT);
        connectionSavepointManager.rollbackToSavepoint(connection, SAVE_POINT);
        verify(connection, times(1)).rollback(savepoint);
    }
    
    @Test
    public void testSaveReleaseSavingPoint() throws SQLException {
        connectionSavepointManager.setSavepoint(connection, SAVE_POINT);
        connectionSavepointManager.releaseSavepoint(connection, SAVE_POINT);
        verify(connection, times(1)).releaseSavepoint(savepoint);
    }
}
