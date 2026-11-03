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

package org.apache.shardingsphere.proxy.backend.firebird.connector.records;

import org.apache.shardingsphere.proxy.backend.connector.records.ExecutionRecordCounts;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.SqlCountHolder;
import org.firebirdsql.jdbc.FBStatement;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebirdExecutionRecordCountsTest {
    
    @Test
    void assertCapture() throws ReflectiveOperationException, SQLException {
        FBStatement statement = mock(FBStatement.class);
        FbStatement handle = mock(FbStatement.class);
        when(statement.unwrap(FBStatement.class)).thenReturn(statement);
        when(handle.getSqlCounts()).thenReturn(new SqlCountHolder(2L, 0L, 1L, 3L));
        Plugins.getMemberAccessor().set(FBStatement.class.getDeclaredField("fbStatement"), statement, handle);
        ExecutionRecordCounts actual = new FirebirdExecutionRecordCounts().capture(statement);
        assertThat(actual.getInserted(), is(1L));
        assertThat(actual.getUpdated(), is(2L));
        assertThat(actual.getSelected(), is(3L));
    }
    
    @Test
    void assertCaptureWithSQLException() throws ReflectiveOperationException, SQLException {
        FBStatement statement = mock(FBStatement.class);
        FbStatement handle = mock(FbStatement.class);
        SQLException expected = new SQLException();
        when(statement.unwrap(FBStatement.class)).thenReturn(statement);
        when(handle.getSqlCounts()).thenThrow(expected);
        Plugins.getMemberAccessor().set(FBStatement.class.getDeclaredField("fbStatement"), statement, handle);
        FirebirdExecutionRecordCounts recordCounts = new FirebirdExecutionRecordCounts();
        assertThat(assertThrows(SQLException.class, () -> recordCounts.capture(statement)), sameInstance(expected));
    }
    
    @Test
    void assertGetDatabaseType() {
        assertThat(new FirebirdExecutionRecordCounts().getDatabaseType(), is("Firebird"));
    }
}
