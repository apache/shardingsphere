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

package org.apache.shardingsphere.driver.jdbc.core.statement;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class GeneratedValueUtilsTest {
    
    @Test
    void assertGetGeneratedValueByGeneratedKeysColumn() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("generated_key")).thenReturn(1L);
        Comparable<?> actual = GeneratedValueUtils.getGeneratedValue(resultSet, "generated_key", "id");
        assertThat(actual, is(1L));
        verify(resultSet).getObject("generated_key");
        verifyNoMoreInteractions(resultSet);
    }
    
    @Test
    void assertGetGeneratedValueByColumnNameWhenGeneratedKeysColumnThrows() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("generated_key")).thenThrow(SQLException.class);
        when(resultSet.getObject("id")).thenReturn(2L);
        Comparable<?> actual = GeneratedValueUtils.getGeneratedValue(resultSet, "generated_key", "id");
        assertThat(actual, is(2L));
        verify(resultSet).getObject("generated_key");
        verify(resultSet).getObject("id");
        verifyNoMoreInteractions(resultSet);
    }
    
    @Test
    void assertGetGeneratedValueByColumnNameWhenGeneratedKeysColumnIsNull() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("id")).thenReturn(2L);
        Comparable<?> actual = GeneratedValueUtils.getGeneratedValue(resultSet, null, "id");
        assertThat(actual, is(2L));
        verify(resultSet).getObject("id");
        verifyNoMoreInteractions(resultSet);
    }
    
    @Test
    void assertGetGeneratedValueByColumnIndexWhenColumnNameEqualsGeneratedKeysColumn() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("id")).thenThrow(SQLException.class);
        when(resultSet.getObject(1)).thenReturn(3L);
        Comparable<?> actual = GeneratedValueUtils.getGeneratedValue(resultSet, "id", "id");
        assertThat(actual, is(3L));
        verify(resultSet).getObject("id");
        verify(resultSet).getObject(1);
        verifyNoMoreInteractions(resultSet);
    }
    
    @Test
    void assertGetGeneratedValueByColumnIndexWhenColumnNameThrows() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("generated_key")).thenThrow(SQLException.class);
        when(resultSet.getObject("id")).thenThrow(SQLException.class);
        when(resultSet.getObject(1)).thenReturn(3L);
        Comparable<?> actual = GeneratedValueUtils.getGeneratedValue(resultSet, "generated_key", "id");
        assertThat(actual, is(3L));
        verify(resultSet).getObject("generated_key");
        verify(resultSet).getObject("id");
        verify(resultSet).getObject(1);
        verifyNoMoreInteractions(resultSet);
    }
    
    @Test
    void assertGetGeneratedValueByColumnIndexWhenGeneratedKeysAndColumnNameAreNull() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject(1)).thenReturn(3L);
        Comparable<?> actual = GeneratedValueUtils.getGeneratedValue(resultSet, null, null);
        assertThat(actual, is(3L));
        verify(resultSet).getObject(1);
        verifyNoMoreInteractions(resultSet);
    }
    
    @Test
    void assertGetGeneratedValueWithSQLException() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("generated_key")).thenThrow(SQLException.class);
        when(resultSet.getObject("id")).thenThrow(SQLException.class);
        when(resultSet.getObject(1)).thenThrow(new SQLException("index not found"));
        SQLException actual = assertThrows(SQLException.class, () -> GeneratedValueUtils.getGeneratedValue(resultSet, "generated_key", "id"));
        assertThat(actual.getMessage(), is("index not found"));
        verify(resultSet).getObject("generated_key");
        verify(resultSet).getObject("id");
        verify(resultSet).getObject(1);
        verifyNoMoreInteractions(resultSet);
    }
}
