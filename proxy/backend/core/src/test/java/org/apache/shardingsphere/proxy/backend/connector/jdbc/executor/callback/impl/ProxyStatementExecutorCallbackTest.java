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

package org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.callback.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProxyStatementExecutorCallbackTest {
    
    @Mock
    private Statement statement;
    
    @Test
    void assertExecuteWithGeneratedKeys() throws SQLException {
        String sql = "SELECT 1";
        when(statement.execute(sql, Statement.RETURN_GENERATED_KEYS)).thenReturn(true);
        ProxyStatementExecutorCallback callback = new ProxyStatementExecutorCallback(mock(), mock(), mock(), mock(), true, true, false);
        assertTrue(callback.execute(sql, statement, true));
        verify(statement).execute(sql, Statement.RETURN_GENERATED_KEYS);
    }
    
    @Test
    void assertExecuteWithoutGeneratedKeys() throws SQLException {
        String sql = "SELECT 1";
        ProxyStatementExecutorCallback callback = new ProxyStatementExecutorCallback(mock(), mock(), mock(), mock(), false, false, false);
        assertFalse(callback.execute(sql, statement, false));
        verify(statement).execute(sql, Statement.NO_GENERATED_KEYS);
    }
    
    @Test
    void assertExecuteFallbackWhenFeatureNotSupported() throws SQLException {
        String sql = "SELECT 1";
        when(statement.execute(sql, Statement.RETURN_GENERATED_KEYS)).thenThrow(new SQLFeatureNotSupportedException());
        when(statement.execute(sql)).thenReturn(true);
        ProxyStatementExecutorCallback callback = new ProxyStatementExecutorCallback(mock(), mock(), mock(), mock(), true, false, false);
        assertTrue(callback.execute(sql, statement, true));
        verify(statement).execute(sql, Statement.RETURN_GENERATED_KEYS);
        verify(statement).execute(sql);
    }
}
