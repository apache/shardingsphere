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

package org.apache.shardingsphere.sqlfederation.executor.enumerable.enumerator.jdbc;

import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JDBCDataRowEnumeratorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("timeArguments")
    void assertEnumerateTimeRow(final String name, final Object value, final Time expected) throws SQLException {
        MergedResult queryResult = mock(MergedResult.class);
        QueryResultMetaData metaData = mock(QueryResultMetaData.class);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnType(1)).thenReturn(Types.TIME);
        when(metaData.getColumnType(2)).thenReturn(Types.INTEGER);
        when(queryResult.next()).thenReturn(true);
        when(queryResult.getValue(1, Time.class)).thenReturn(value);
        when(queryResult.getValue(2, Object.class)).thenReturn(48);
        JDBCDataRowEnumerator enumerator = new JDBCDataRowEnumerator(queryResult, metaData, Collections.emptyList());
        assertTrue(enumerator.moveNext());
        Object actual = enumerator.current();
        assertThat(actual, isA(Object[].class));
        Object[] actualValues = (Object[]) actual;
        assertThat(actualValues[0], isA(Time.class));
        assertThat(actualValues[0], is(expected));
        assertThat(actualValues[1], is(48));
        verify(queryResult).getValue(1, Time.class);
        verify(queryResult).getValue(2, Object.class);
        verify(metaData).getColumnType(1);
        verify(metaData).getColumnType(2);
    }
    
    private static Stream<Arguments> timeArguments() {
        return Stream.of(
                Arguments.of("memory-microseconds", LocalTime.of(12, 34, 56, 123456000), new Time(Time.valueOf("12:34:56").getTime() + 123)),
                Arguments.of("memory-midnight", LocalTime.MIDNIGHT, Time.valueOf("00:00:00")),
                Arguments.of("memory-whole-second", LocalTime.of(12, 34, 56), Time.valueOf("12:34:56")),
                Arguments.of("legacy-time", new Time(Time.valueOf("12:34:56").getTime() + 123), new Time(Time.valueOf("12:34:56").getTime() + 123)));
    }
    
    @Test
    void assertEnumerateNullTimeRow() throws SQLException {
        MergedResult queryResult = mock(MergedResult.class);
        QueryResultMetaData metaData = mock(QueryResultMetaData.class);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnType(1)).thenReturn(Types.TIME);
        when(queryResult.next()).thenReturn(true);
        JDBCDataRowEnumerator enumerator = new JDBCDataRowEnumerator(queryResult, metaData, Collections.emptyList());
        assertTrue(enumerator.moveNext());
        assertNull(enumerator.current());
        verify(queryResult).getValue(1, Time.class);
    }
    
    @Test
    void assertEnumerateNonTimeLocalTimeRow() throws SQLException {
        MergedResult queryResult = mock(MergedResult.class);
        QueryResultMetaData metaData = mock(QueryResultMetaData.class);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnType(1)).thenReturn(Types.JAVA_OBJECT);
        when(queryResult.next()).thenReturn(true);
        LocalTime expected = LocalTime.of(12, 34, 56, 123456000);
        when(queryResult.getValue(1, Object.class)).thenReturn(expected);
        JDBCDataRowEnumerator enumerator = new JDBCDataRowEnumerator(queryResult, metaData, Collections.emptyList());
        assertTrue(enumerator.moveNext());
        assertThat(enumerator.current(), sameInstance(expected));
    }
    
    @Test
    void assertEnumerateRowsBuildsRows() throws SQLException {
        MergedResult firstQueryResult = mock(MergedResult.class);
        LocalDateTime expectedDateTime = LocalDateTime.of(2024, 1, 1, 1, 0);
        when(firstQueryResult.next()).thenReturn(true, false);
        when(firstQueryResult.getValue(1, Object.class)).thenReturn(expectedDateTime);
        when(firstQueryResult.getValue(2, Object.class)).thenReturn("bar_value");
        QueryResultMetaData firstMetaData = mock(QueryResultMetaData.class);
        when(firstMetaData.getColumnCount()).thenReturn(2);
        Statement firstStatement = mock(Statement.class);
        Statement secondStatement = mock(Statement.class);
        JDBCDataRowEnumerator enumerator = new JDBCDataRowEnumerator(firstQueryResult, firstMetaData, Arrays.asList(firstStatement, secondStatement));
        assertTrue(enumerator.moveNext());
        Object actualRow = enumerator.current();
        assertThat(actualRow, isA(Object[].class));
        Object[] actualRowValues = (Object[]) actualRow;
        assertThat(actualRowValues.length, is(2));
        assertThat(actualRowValues[0], isA(Timestamp.class));
        assertThat(actualRowValues[0], is(Timestamp.valueOf(expectedDateTime)));
        assertThat(actualRowValues[1], is("bar_value"));
        assertFalse(enumerator.moveNext());
        enumerator.reset();
        enumerator.close();
        verify(firstStatement).close();
        verify(secondStatement).close();
        assertNull(enumerator.current());
    }
    
    @Test
    void assertEnumerateRowsBuildsRowsWithSingleColumn() throws SQLException {
        MergedResult singleColumnResult = mock(MergedResult.class);
        QueryResultMetaData singleColumnMetaData = mock(QueryResultMetaData.class);
        when(singleColumnMetaData.getColumnCount()).thenReturn(1);
        when(singleColumnResult.next()).thenReturn(true);
        when(singleColumnResult.getValue(1, Object.class)).thenReturn(10);
        JDBCDataRowEnumerator enumerator = new JDBCDataRowEnumerator(singleColumnResult, singleColumnMetaData, Collections.emptyList());
        assertTrue(enumerator.moveNext());
        assertThat(enumerator.current(), is(10));
        enumerator.close();
        assertNull(enumerator.current());
    }
    
    @Test
    void assertCloseWrapsSQLException() throws SQLException {
        Statement statement = mock(Statement.class);
        doThrow(SQLException.class).when(statement).close();
        JDBCDataRowEnumerator enumerator = new JDBCDataRowEnumerator(mock(MergedResult.class), mock(QueryResultMetaData.class), Collections.singleton(statement));
        SQLWrapperException actualException = assertThrows(SQLWrapperException.class, enumerator::close);
        assertThat(actualException.getCause(), isA(SQLException.class));
    }
    
    @Test
    void assertCloseClosesRemainingStatementsAfterFailure() throws SQLException {
        Statement failedStatement = mock(Statement.class);
        SQLException expectedException = new SQLException("failed to close");
        doThrow(expectedException).when(failedStatement).close();
        Statement secondStatement = mock(Statement.class);
        Statement thirdStatement = mock(Statement.class);
        JDBCDataRowEnumerator enumerator = new JDBCDataRowEnumerator(
                mock(MergedResult.class), mock(QueryResultMetaData.class), Arrays.asList(failedStatement, secondStatement, thirdStatement));
        SQLWrapperException actualException = assertThrows(SQLWrapperException.class, enumerator::close);
        assertThat(actualException.getCause(), is(expectedException));
        verify(secondStatement).close();
        verify(thirdStatement).close();
        assertNull(enumerator.current());
    }
    
    @Test
    void assertCloseContinuesWhenTwoStatementsThrowTheSameException() throws SQLException {
        SQLException sharedException = new SQLException("shared failure");
        Statement firstStatement = mock(Statement.class);
        doThrow(sharedException).when(firstStatement).close();
        Statement secondStatement = mock(Statement.class);
        doThrow(sharedException).when(secondStatement).close();
        Statement thirdStatement = mock(Statement.class);
        JDBCDataRowEnumerator enumerator = new JDBCDataRowEnumerator(
                mock(MergedResult.class), mock(QueryResultMetaData.class), Arrays.asList(firstStatement, secondStatement, thirdStatement));
        SQLWrapperException actualException = assertThrows(SQLWrapperException.class, enumerator::close);
        assertThat(actualException.getCause(), is(sharedException));
        verify(thirdStatement).close();
        assertNull(enumerator.current());
    }
    
    @Test
    void assertCloseSuppressesLaterFailures() throws SQLException {
        Statement firstStatement = mock(Statement.class);
        SQLException firstException = new SQLException("first");
        doThrow(firstException).when(firstStatement).close();
        Statement secondStatement = mock(Statement.class);
        SQLException secondException = new SQLException("second");
        doThrow(secondException).when(secondStatement).close();
        JDBCDataRowEnumerator enumerator = new JDBCDataRowEnumerator(
                mock(MergedResult.class), mock(QueryResultMetaData.class), Arrays.asList(firstStatement, secondStatement));
        SQLWrapperException actualException = assertThrows(SQLWrapperException.class, enumerator::close);
        assertThat(actualException.getCause(), is(firstException));
        assertThat(actualException.getCause().getSuppressed().length, is(1));
        assertThat(actualException.getCause().getSuppressed()[0], is(secondException));
    }
}
