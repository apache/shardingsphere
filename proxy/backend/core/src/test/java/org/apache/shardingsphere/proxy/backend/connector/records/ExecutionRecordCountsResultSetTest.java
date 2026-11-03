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

package org.apache.shardingsphere.proxy.backend.connector.records;

import org.apache.shardingsphere.proxy.backend.connector.records.ExecutionRecordCountsResultSet.RecordCountsCapture;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExecutionRecordCountsResultSetTest {
    
    @Test
    void assertCaptureWhenResultSetExhausted() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        RecordCountsCapture capture = mock(RecordCountsCapture.class);
        ResultSet actual = ExecutionRecordCountsResultSet.wrap(resultSet, capture);
        assertTrue(actual.next());
        verify(capture, never()).capture();
        assertFalse(actual.next());
        verify(capture).capture();
    }
    
    @Test
    void assertDelegateMethod() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt(1)).thenReturn(7);
        ResultSet actual = ExecutionRecordCountsResultSet.wrap(resultSet, mock(RecordCountsCapture.class));
        assertThat(actual.getInt(1), is(7));
        actual.close();
        verify(resultSet).close();
    }
    
    @Test
    void assertPropagateSQLExceptionFromResultSet() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        SQLException expected = new SQLException();
        when(resultSet.next()).thenThrow(expected);
        ResultSet actual = ExecutionRecordCountsResultSet.wrap(resultSet, mock(RecordCountsCapture.class));
        assertThat(assertThrows(SQLException.class, actual::next), sameInstance(expected));
    }
    
    @Test
    void assertPropagateSQLExceptionFromCapture() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);
        SQLException expected = new SQLException();
        RecordCountsCapture capture = mock(RecordCountsCapture.class);
        doThrow(expected).when(capture).capture();
        ResultSet actual = ExecutionRecordCountsResultSet.wrap(resultSet, capture);
        assertThat(assertThrows(SQLException.class, actual::next), sameInstance(expected));
    }
}
