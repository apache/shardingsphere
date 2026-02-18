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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class GeneratedValueUtilsTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertGetGeneratedValueArguments")
    void assertGetGeneratedValue(final String name, final String generatedKeysColumnName, final String columnName, final Object generatedKeyValue,
                                 final boolean generatedKeyThrowsSQLException, final Object columnValue, final boolean columnThrowsSQLException,
                                 final Object indexValue, final Object expected, final boolean generatedKeyAccessed, final boolean columnAccessed,
                                 final boolean indexAccessed) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        if (null != generatedKeysColumnName) {
            if (generatedKeyThrowsSQLException) {
                when(resultSet.getObject(generatedKeysColumnName)).thenThrow(SQLException.class);
            } else {
                when(resultSet.getObject(generatedKeysColumnName)).thenReturn(generatedKeyValue);
            }
        }
        if (null != columnName && !columnName.equals(generatedKeysColumnName)) {
            if (columnThrowsSQLException) {
                when(resultSet.getObject(columnName)).thenThrow(SQLException.class);
            } else {
                when(resultSet.getObject(columnName)).thenReturn(columnValue);
            }
        }
        when(resultSet.getObject(1)).thenReturn(indexValue);
        Comparable<?> actual = GeneratedValueUtils.getGeneratedValue(resultSet, generatedKeysColumnName, columnName);
        assertThat(actual, is(expected));
        if (generatedKeyAccessed) {
            verify(resultSet).getObject(generatedKeysColumnName);
        }
        if (columnAccessed) {
            verify(resultSet).getObject(columnName);
        }
        if (indexAccessed) {
            verify(resultSet).getObject(1);
        }
        verifyNoMoreInteractions(resultSet);
    }
    
    private static Stream<Arguments> assertGetGeneratedValueArguments() {
        return Stream.of(
                Arguments.arguments("generated keys column returns value", "generated_key", "id", 1L, false, 2L, false, 3L, 1L, true, false, false),
                Arguments.arguments("generated keys column throws and column name returns value", "generated_key", "id", null, true, 2L, false, 3L, 2L, true, true, false),
                Arguments.arguments("generated keys column is null and column name returns value", null, "id", null, false, 2L, false, 3L, 2L, false, true, false),
                Arguments.arguments("column name equals generated keys column and fallback to index", "id", "id", null, true, null, false, 3L, 3L, true, false, true),
                Arguments.arguments("column name throws and fallback to index", "generated_key", "id", null, true, null, true, 3L, 3L, true, true, true),
                Arguments.arguments("generated keys and column name are null and fallback to index", null, null, null, false, null, false, 3L, 3L, false, false, true));
    }
}
