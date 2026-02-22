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

package org.apache.shardingsphere.database.connector.sql92.sqlserver.metadata.data.loader;

import org.apache.shardingsphere.database.connector.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.datatype.DataTypeRegistry;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLServerMetaDataLoaderTest {
    
    private static final String LOAD_COLUMN_META_DATA_WITH_TABLES_HIGH_VERSION = "SELECT obj.name AS TABLE_NAME, col.name AS COLUMN_NAME, t.name AS DATA_TYPE,"
            + " col.collation_name AS COLLATION_NAME, col.column_id, is_identity AS IS_IDENTITY, col.is_nullable AS IS_NULLABLE, is_hidden AS IS_HIDDEN,"
            + " (SELECT TOP 1 ind.is_primary_key FROM sys.index_columns ic LEFT JOIN sys.indexes ind ON ic.object_id = ind.object_id"
            + " AND ic.index_id = ind.index_id AND ind.name LIKE 'PK_%' WHERE ic.object_id = obj.object_id AND ic.column_id = col.column_id) AS IS_PRIMARY_KEY"
            + " FROM sys.objects obj INNER JOIN sys.columns col ON obj.object_id = col.object_id LEFT JOIN sys.types t ON t.user_type_id = col.user_type_id"
            + " WHERE obj.name IN ('tbl') ORDER BY col.column_id";
    
    private static final String LOAD_COLUMN_META_DATA_WITH_TABLES_LOW_VERSION = "SELECT obj.name AS TABLE_NAME, col.name AS COLUMN_NAME, t.name AS DATA_TYPE,"
            + " col.collation_name AS COLLATION_NAME, col.column_id, is_identity AS IS_IDENTITY, col.is_nullable AS IS_NULLABLE,"
            + "  (SELECT TOP 1 ind.is_primary_key FROM sys.index_columns ic LEFT JOIN sys.indexes ind ON ic.object_id = ind.object_id"
            + " AND ic.index_id = ind.index_id AND ind.name LIKE 'PK_%' WHERE ic.object_id = obj.object_id AND ic.column_id = col.column_id) AS IS_PRIMARY_KEY"
            + " FROM sys.objects obj INNER JOIN sys.columns col ON obj.object_id = col.object_id LEFT JOIN sys.types t ON t.user_type_id = col.user_type_id"
            + " WHERE obj.name IN ('tbl') ORDER BY col.column_id";
    
    private static final String LOAD_INDEX_META_DATA = "SELECT idx.name AS INDEX_NAME, obj.name AS TABLE_NAME, col.name AS COLUMN_NAME,"
            + " idx.is_unique AS IS_UNIQUE FROM sys.indexes idx"
            + " LEFT JOIN sys.objects obj ON idx.object_id = obj.object_id"
            + " LEFT JOIN sys.columns col ON obj.object_id = col.object_id"
            + " WHERE idx.index_id NOT IN (0, 255) AND obj.name IN ('tbl') ORDER BY idx.index_id";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "SQLServer");
    
    private final DialectMetaDataLoader dialectMetaDataLoader = DatabaseTypedSPILoader.getService(DialectMetaDataLoader.class, databaseType);
    
    @SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed", "resource"})
    @Test
    void assertLoadWithEmptyColumnMetaData() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet tableMetaDataResultSet = mock(ResultSet.class);
        when(tableMetaDataResultSet.next()).thenReturn(false);
        when(dataSource.getConnection().prepareStatement(LOAD_COLUMN_META_DATA_WITH_TABLES_LOW_VERSION).executeQuery()).thenReturn(tableMetaDataResultSet);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(14);
        DataTypeRegistry.load(dataSource, "SQLServer");
        Collection<SchemaMetaData> actual = dialectMetaDataLoader.load(new MetaDataLoaderMaterial(Collections.singletonList("tbl"), "foo_ds", dataSource, databaseType, "sharding_db"));
        assertThat(actual.size(), is(1));
        assertTrue(actual.iterator().next().getTables().isEmpty());
    }
    
    @SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed", "resource"})
    @ParameterizedTest(name = "{0}")
    @MethodSource("loadArguments")
    void assertLoad(final String name, final String tableMetaDataSQL, final int majorVersion, final boolean includeCompositeIndex,
                    final boolean expectedSecondColumnVisible, final Collection<String> expectedIndexColumns) throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet tableMetaDataResultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(tableMetaDataSQL).executeQuery()).thenReturn(tableMetaDataResultSet);
        ResultSet indexMetaDataResultSet = includeCompositeIndex ? mockCompositeIndexMetaDataResultSet() : mockSimpleIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(LOAD_INDEX_META_DATA).executeQuery()).thenReturn(indexMetaDataResultSet);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(majorVersion);
        DataTypeRegistry.load(dataSource, "SQLServer");
        Collection<SchemaMetaData> actual = dialectMetaDataLoader.load(
                new MetaDataLoaderMaterial(Collections.singletonList("tbl"), "foo_ds", dataSource, databaseType, "sharding_db"));
        assertTableMetaData(actual, expectedSecondColumnVisible, expectedIndexColumns);
    }
    
    private DataSource mockDataSource() throws SQLException {
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        ResultSet typeInfoResultSet = mockTypeInfoResultSet();
        when(result.getConnection().getMetaData().getTypeInfo()).thenReturn(typeInfoResultSet);
        return result;
    }
    
    private ResultSet mockTypeInfoResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("TYPE_NAME")).thenReturn("int", "varchar");
        when(result.getInt("DATA_TYPE")).thenReturn(Types.INTEGER, Types.VARCHAR);
        return result;
    }
    
    private ResultSet mockTableMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        when(result.getString("COLUMN_NAME")).thenReturn("id", "name");
        when(result.getString("DATA_TYPE")).thenReturn("int", "varchar");
        when(result.getString("IS_PRIMARY_KEY")).thenReturn("1", "");
        when(result.getString("IS_IDENTITY")).thenReturn("1", "");
        when(result.getString("IS_HIDDEN")).thenReturn("0", "1");
        when(result.getString("COLLATION_NAME")).thenReturn("SQL_Latin1_General_CP1_CS_AS", (String) null);
        when(result.getString("IS_NULLABLE")).thenReturn("0", "1");
        return result;
    }
    
    private ResultSet mockSimpleIndexMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("INDEX_NAME")).thenReturn("id");
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        when(result.getString("COLUMN_NAME")).thenReturn("id");
        when(result.getString("IS_UNIQUE")).thenReturn("1");
        return result;
    }
    
    private ResultSet mockCompositeIndexMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("INDEX_NAME")).thenReturn("id", "id");
        when(result.getString("TABLE_NAME")).thenReturn("tbl", "tbl");
        when(result.getString("COLUMN_NAME")).thenReturn("id", "name");
        when(result.getString("IS_UNIQUE")).thenReturn("1", "1");
        return result;
    }
    
    private void assertTableMetaData(final Collection<SchemaMetaData> schemaMetaDataList, final boolean expectedSecondColumnVisible, final Collection<String> expectedIndexColumns) {
        assertThat(schemaMetaDataList.size(), is(1));
        Collection<TableMetaData> tables = schemaMetaDataList.iterator().next().getTables();
        assertThat(tables.size(), is(1));
        TableMetaData actualTableMetaData = tables.iterator().next();
        assertThat(actualTableMetaData.getName(), is("tbl"));
        assertThat(actualTableMetaData.getColumns().size(), is(2));
        assertThat(actualTableMetaData.getIndexes().size(), is(1));
        List<ColumnMetaData> columnsIterator = new ArrayList<>(actualTableMetaData.getColumns());
        assertColumnMetaData(columnsIterator.get(0), new ColumnMetaData("id", Types.INTEGER, true, true, true, true, false, false));
        assertColumnMetaData(columnsIterator.get(1), new ColumnMetaData("name", Types.VARCHAR, false, false, false, expectedSecondColumnVisible, false, true));
        List<IndexMetaData> indexes = new ArrayList<>(actualTableMetaData.getIndexes());
        IndexMetaData expectedIndexMetaData = new IndexMetaData("id", expectedIndexColumns);
        expectedIndexMetaData.setUnique(true);
        assertIndexMetaData(indexes.get(0), expectedIndexMetaData);
    }
    
    private void assertColumnMetaData(final ColumnMetaData actual, final ColumnMetaData expected) {
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getDataType(), is(expected.getDataType()));
        assertThat(actual.isPrimaryKey(), is(expected.isPrimaryKey()));
        assertThat(actual.isGenerated(), is(expected.isGenerated()));
        assertThat(actual.isCaseSensitive(), is(expected.isCaseSensitive()));
        assertThat(actual.isVisible(), is(expected.isVisible()));
        assertThat(actual.isUnsigned(), is(expected.isUnsigned()));
        assertThat(actual.isNullable(), is(expected.isNullable()));
    }
    
    private void assertIndexMetaData(final IndexMetaData actual, final IndexMetaData expected) {
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getColumns(), is(expected.getColumns()));
        assertThat(actual.isUnique(), is(expected.isUnique()));
    }
    
    private static Stream<Arguments> loadArguments() {
        return Stream.of(
                Arguments.of("load with hidden column on high version", LOAD_COLUMN_META_DATA_WITH_TABLES_HIGH_VERSION, 15, false, false, Collections.singletonList("id")),
                Arguments.of("load with hidden column ignored on low version", LOAD_COLUMN_META_DATA_WITH_TABLES_LOW_VERSION, 14, false, true, Collections.singletonList("id")),
                Arguments.of("load with composite index", LOAD_COLUMN_META_DATA_WITH_TABLES_HIGH_VERSION, 15, true, false, Arrays.asList("id", "name")));
    }
}
