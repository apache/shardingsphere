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

package org.apache.shardingsphere.database.connector.firebird.metadata.data.loader;

import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdBlobColumnLoaderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    @Mock
    private ResultSet resultSet;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private Connection connection;
    
    @Mock
    private DataSource dataSource;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("loadWithBlobColumnArguments")
    void assertLoadWithBlobColumn(final String name, final String columnName, final Object subType, final Integer expectedSubType) throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("COLUMN_NAME")).thenReturn(columnName);
        when(resultSet.getObject("SUB_TYPE")).thenReturn(subType);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(dataSource.getConnection()).thenReturn(connection);
        MetaDataLoaderMaterial material = new MetaDataLoaderMaterial(Collections.singleton("foo_tbl"), "foo_ds", dataSource, databaseType, "schema");
        Map<String, Map<String, Integer>> actual = new FirebirdBlobColumnLoader(material).load();
        assertThat(actual, hasKey("foo_tbl"));
        Map<String, Integer> actualTableColumns = actual.get("foo_tbl");
        assertThat(actualTableColumns.size(), is(1));
        assertThat(actualTableColumns.get("BLOB_COL"), is(expectedSubType));
        verify(preparedStatement).setString(1, "FOO_TBL");
    }
    
    @Test
    void assertLoadWithInvalidColumnNames() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("COLUMN_NAME")).thenReturn(null, "   ");
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(dataSource.getConnection()).thenReturn(connection);
        MetaDataLoaderMaterial material = new MetaDataLoaderMaterial(Collections.singleton("foo_tbl"), "foo_ds", dataSource, databaseType, "schema");
        Map<String, Map<String, Integer>> actual = new FirebirdBlobColumnLoader(material).load();
        assertThat(actual, hasKey("foo_tbl"));
        assertTrue(actual.get("foo_tbl").isEmpty());
        verify(preparedStatement).setString(1, "FOO_TBL");
    }
    
    @Test
    void assertLoadWithNoTables() throws SQLException {
        MetaDataLoaderMaterial material = new MetaDataLoaderMaterial(Collections.emptyList(), "foo_ds", dataSource, databaseType, "schema");
        assertTrue(new FirebirdBlobColumnLoader(material).load().isEmpty());
    }
    
    private static Stream<Arguments> loadWithBlobColumnArguments() {
        return Stream.of(
                Arguments.of("trimmed column with integer subtype", " blob_col ", 2, 2),
                Arguments.of("upper case column with null subtype", "BLOB_COL", null, null),
                Arguments.of("lower case column with negative subtype", "blob_col", -1, -1));
    }
}
