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

package org.apache.shardingsphere.database.connector.core.metadata.data.loader.type;

import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IndexMetaDataLoaderTest {
    
    @Test
    void assertLoadWhenGetIndexInfoThrowsOracleViewNotAppropriateVendorCode() throws SQLException {
        assertTrue(IndexMetaDataLoader.load(mockConnectionWithFailure(new SQLException("vendor", "42000", 1702)), "tbl").isEmpty());
    }
    
    @Test
    void assertLoadWhenGetIndexInfoThrowsSQLException() {
        assertThrows(SQLException.class, () -> IndexMetaDataLoader.load(mockConnectionWithFailure(new SQLException("vendor", "42000", 1703)), "tbl"));
    }
    
    private Connection mockConnectionWithFailure(final SQLException exception) throws SQLException {
        Connection result = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(result.getCatalog()).thenReturn("catalog");
        when(result.getMetaData().getIndexInfo("catalog", null, "tbl", false, false)).thenThrow(exception);
        return result;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getLoadCases")
    void assertLoad(final String name,
                    final ResultSet resultSet, final int expectedSize, final String expectedName, final Collection<String> expectedColumns, final boolean expectedUnique) throws SQLException {
        Collection<IndexMetaData> actual = IndexMetaDataLoader.load(mockConnection(resultSet), "tbl");
        assertThat(actual.size(), is(expectedSize));
        if (0 == expectedSize) {
            return;
        }
        IndexMetaData actualIndexMetaData = actual.iterator().next();
        assertThat(actualIndexMetaData.getName(), is(expectedName));
        assertThat(actualIndexMetaData.getColumns(), is(expectedColumns));
        assertThat(actualIndexMetaData.isUnique(), is(expectedUnique));
    }
    
    private static Stream<Arguments> getLoadCases() throws SQLException {
        return Stream.of(
                Arguments.of("skipNullIndexName", mockResultSet(new String[]{null}, new String[]{"foo_column"}, new boolean[]{false}),
                        0, "", Collections.emptyList(), false),
                Arguments.of("loadUniqueIndex", mockResultSet(new String[]{"foo_index"}, new String[]{"foo_column"}, new boolean[]{false}),
                        1, "foo_index", Collections.singletonList("foo_column"), true),
                Arguments.of("loadNonUniqueIndexWithMultipleColumns", mockResultSet(new String[]{"foo_index", "foo_index"},
                        new String[]{"foo_column", "bar_column"}, new boolean[]{true, true}), 1, "foo_index", Arrays.asList("foo_column", "bar_column"), false));
    }
    
    private static ResultSet mockResultSet(final String[] indexNames, final String[] columnNames, final boolean[] nonUniqueFlags) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        AtomicInteger rowCursor = new AtomicInteger(-1);
        when(result.next()).thenAnswer(invocation -> rowCursor.incrementAndGet() < indexNames.length);
        when(result.getString("INDEX_NAME")).thenAnswer(invocation -> indexNames[rowCursor.get()]);
        when(result.getString("COLUMN_NAME")).thenAnswer(invocation -> columnNames[rowCursor.get()]);
        when(result.getBoolean("NON_UNIQUE")).thenAnswer(invocation -> nonUniqueFlags[rowCursor.get()]);
        return result;
    }
    
    private static Connection mockConnection(final ResultSet resultSet) throws SQLException {
        Connection result = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(result.getCatalog()).thenReturn("catalog");
        when(result.getMetaData().getIndexInfo("catalog", null, "tbl", false, false)).thenReturn(resultSet);
        return result;
    }
}
