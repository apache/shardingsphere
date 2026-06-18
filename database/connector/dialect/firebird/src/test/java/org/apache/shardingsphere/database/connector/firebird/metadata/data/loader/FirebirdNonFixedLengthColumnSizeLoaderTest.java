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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdNonFixedLengthColumnSizeLoaderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private DatabaseMetaData databaseMetaData;
    
    @Mock
    private ResultSet columnsResultSet;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("recordedColumnSizeArguments")
    void assertLoadWithColumnMetadata(final String name, final int dataType, final String columnName, final int columnSize, final Map<String, Integer> expected) throws SQLException {
        mockLoadPrerequisites();
        when(columnsResultSet.next()).thenReturn(true, false);
        when(columnsResultSet.getString("TABLE_NAME")).thenReturn("FOO_TBL");
        when(columnsResultSet.getInt("DATA_TYPE")).thenReturn(dataType);
        when(columnsResultSet.getString("COLUMN_NAME")).thenReturn(columnName);
        when(columnsResultSet.getInt("COLUMN_SIZE")).thenReturn(columnSize);
        MetaDataLoaderMaterial material = new MetaDataLoaderMaterial(Collections.singleton("foo_tbl"), "logic_ds", dataSource, databaseType, "schema");
        Map<String, Map<String, Integer>> actual = new FirebirdNonFixedLengthColumnSizeLoader(material).load();
        assertThat(actual, hasKey("foo_tbl"));
        assertThat(actual.get("foo_tbl"), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("emptyColumnMetadataArguments")
    void assertLoadWithEmptyColumnMetadata(final String name, final String tableName, final Integer dataType, final boolean stubColumnName, final String columnName,
                                           final boolean stubColumnSize, final Integer columnSize, final boolean stubWasNull, final Boolean wasNull) throws SQLException {
        mockLoadPrerequisites();
        when(columnsResultSet.next()).thenReturn(true, false);
        when(columnsResultSet.getString("TABLE_NAME")).thenReturn(tableName);
        if (null != dataType) {
            when(columnsResultSet.getInt("DATA_TYPE")).thenReturn(dataType);
        }
        if (stubColumnName) {
            when(columnsResultSet.getString("COLUMN_NAME")).thenReturn(columnName);
        }
        if (stubColumnSize) {
            when(columnsResultSet.getInt("COLUMN_SIZE")).thenReturn(columnSize);
        }
        if (stubWasNull) {
            when(columnsResultSet.wasNull()).thenReturn(wasNull);
        }
        MetaDataLoaderMaterial material = new MetaDataLoaderMaterial(Collections.singleton("foo_tbl"), "logic_ds", dataSource, databaseType, "schema");
        Map<String, Map<String, Integer>> actual = new FirebirdNonFixedLengthColumnSizeLoader(material).load();
        assertThat(actual, hasKey("foo_tbl"));
        assertTrue(actual.get("foo_tbl").isEmpty());
    }
    
    @Test
    void assertLoadWithNoTables() throws SQLException {
        MetaDataLoaderMaterial material = new MetaDataLoaderMaterial(Collections.emptyList(), "logic_ds", dataSource, databaseType, "schema");
        assertTrue(new FirebirdNonFixedLengthColumnSizeLoader(material).load().isEmpty());
    }
    
    private void mockLoadPrerequisites() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getCatalog()).thenReturn("catalog");
        when(connection.getSchema()).thenReturn("schema");
        when(databaseMetaData.getColumns("catalog", "schema", "FOO_TBL", "%")).thenReturn(columnsResultSet);
    }
    
    private static Stream<Arguments> recordedColumnSizeArguments() {
        return Stream.of(
                Arguments.of("records char column size", Types.CHAR, "char_col", 128, Collections.singletonMap("CHAR_COL", 128)),
                Arguments.of("records varchar column size", Types.VARCHAR, "varchar_col", 96, Collections.singletonMap("VARCHAR_COL", 96)),
                Arguments.of("records varbinary column size", Types.VARBINARY, "varbinary_col", 64, Collections.singletonMap("VARBINARY_COL", 64)));
    }
    
    private static Stream<Arguments> emptyColumnMetadataArguments() {
        return Stream.of(
                Arguments.of("skips mismatched table metadata", "BAR_TBL", null, false, null, false, null, false, null),
                Arguments.of("skips fixed length type", "FOO_TBL", Types.BIGINT, false, null, false, null, false, null),
                Arguments.of("skips null column name", "FOO_TBL", Types.CHAR, true, null, false, null, false, null),
                Arguments.of("skips null column size", "FOO_TBL", Types.CHAR, true, "char_col", true, 128, true, true),
                Arguments.of("skips non-positive column size", "FOO_TBL", Types.CHAR, true, "char_col", true, 0, true, false));
    }
}
