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

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JDBCDataRowEnumeratorTest {
    
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
        assertThat(actualRow, instanceOf(Object[].class));
        Object[] actualRowValues = (Object[]) actualRow;
        assertThat(actualRowValues.length, is(2));
        assertThat(actualRowValues[0], instanceOf(Timestamp.class));
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
}
