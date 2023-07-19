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

package org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.dialect;

import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.DialectSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLServerSchemaMetaDataLoaderTest {
    
    private static final String LOAD_COLUMN_META_DATA_WITHOUT_TABLES_HIGH_VERSION = "SELECT obj.name AS TABLE_NAME, col.name AS COLUMN_NAME, t.name AS DATA_TYPE,"
            + " col.collation_name AS COLLATION_NAME, col.column_id, is_identity AS IS_IDENTITY, is_hidden AS IS_HIDDEN,"
            + " (SELECT TOP 1 ind.is_primary_key FROM sys.index_columns ic LEFT JOIN sys.indexes ind ON ic.object_id = ind.object_id"
            + " AND ic.index_id = ind.index_id AND ind.name LIKE 'PK_%' WHERE ic.object_id = obj.object_id AND ic.column_id = col.column_id) AS IS_PRIMARY_KEY"
            + " FROM sys.objects obj INNER JOIN sys.columns col ON obj.object_id = col.object_id LEFT JOIN sys.types t ON t.user_type_id = col.user_type_id ORDER BY col.column_id";
    
    private static final String LOAD_COLUMN_META_DATA_WITHOUT_TABLES_LOW_VERSION = "SELECT obj.name AS TABLE_NAME, col.name AS COLUMN_NAME, t.name AS DATA_TYPE,"
            + " col.collation_name AS COLLATION_NAME, col.column_id, is_identity AS IS_IDENTITY,"
            + "  (SELECT TOP 1 ind.is_primary_key FROM sys.index_columns ic LEFT JOIN sys.indexes ind ON ic.object_id = ind.object_id"
            + " AND ic.index_id = ind.index_id AND ind.name LIKE 'PK_%' WHERE ic.object_id = obj.object_id AND ic.column_id = col.column_id) AS IS_PRIMARY_KEY"
            + " FROM sys.objects obj INNER JOIN sys.columns col ON obj.object_id = col.object_id LEFT JOIN sys.types t ON t.user_type_id = col.user_type_id ORDER BY col.column_id";
    
    private static final String LOAD_COLUMN_META_DATA_WITH_TABLES_HIGH_VERSION = "SELECT obj.name AS TABLE_NAME, col.name AS COLUMN_NAME, t.name AS DATA_TYPE,"
            + " col.collation_name AS COLLATION_NAME, col.column_id, is_identity AS IS_IDENTITY, is_hidden AS IS_HIDDEN,"
            + " (SELECT TOP 1 ind.is_primary_key FROM sys.index_columns ic LEFT JOIN sys.indexes ind ON ic.object_id = ind.object_id"
            + " AND ic.index_id = ind.index_id AND ind.name LIKE 'PK_%' WHERE ic.object_id = obj.object_id AND ic.column_id = col.column_id) AS IS_PRIMARY_KEY"
            + " FROM sys.objects obj INNER JOIN sys.columns col ON obj.object_id = col.object_id LEFT JOIN sys.types t ON t.user_type_id = col.user_type_id"
            + " WHERE obj.name IN ('tbl') ORDER BY col.column_id";
    
    private static final String LOAD_COLUMN_META_DATA_WITH_TABLES_LOW_VERSION = "SELECT obj.name AS TABLE_NAME, col.name AS COLUMN_NAME, t.name AS DATA_TYPE,"
            + " col.collation_name AS COLLATION_NAME, col.column_id, is_identity AS IS_IDENTITY,"
            + "  (SELECT TOP 1 ind.is_primary_key FROM sys.index_columns ic LEFT JOIN sys.indexes ind ON ic.object_id = ind.object_id"
            + " AND ic.index_id = ind.index_id AND ind.name LIKE 'PK_%' WHERE ic.object_id = obj.object_id AND ic.column_id = col.column_id) AS IS_PRIMARY_KEY"
            + " FROM sys.objects obj INNER JOIN sys.columns col ON obj.object_id = col.object_id LEFT JOIN sys.types t ON t.user_type_id = col.user_type_id"
            + " WHERE obj.name IN ('tbl') ORDER BY col.column_id";
    
    private static final String LOAD_INDEX_META_DATA = "SELECT a.name AS INDEX_NAME, c.name AS TABLE_NAME FROM sys.indexes a"
            + " JOIN sys.objects c ON a.object_id = c.object_id WHERE a.index_id NOT IN (0, 255) AND c.name IN ('tbl')";
    
    @Test
    void assertLoadWithoutTablesWithHighVersion() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(LOAD_COLUMN_META_DATA_WITHOUT_TABLES_HIGH_VERSION)
                .executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(LOAD_INDEX_META_DATA)
                .executeQuery()).thenReturn(indexResultSet);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(15);
        Collection<SchemaMetaData> actual = getDialectTableMetaDataLoader().load(dataSource, Collections.emptyList(), "sharding_db");
        assertTableMetaDataMap(actual);
        TableMetaData actualTableMetaData = actual.iterator().next().getTables().iterator().next();
        Iterator<ColumnMetaData> columnsIterator = actualTableMetaData.getColumns().iterator();
        assertThat(columnsIterator.next(), is(new ColumnMetaData("id", Types.INTEGER, false, true, true, true, false)));
        assertThat(columnsIterator.next(), is(new ColumnMetaData("name", Types.VARCHAR, false, false, false, false, false)));
    }
    
    @Test
    void assertLoadWithoutTablesWithLowVersion() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(LOAD_COLUMN_META_DATA_WITHOUT_TABLES_LOW_VERSION)
                .executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(LOAD_INDEX_META_DATA)
                .executeQuery()).thenReturn(indexResultSet);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(14);
        Collection<SchemaMetaData> actual = getDialectTableMetaDataLoader().load(dataSource, Collections.emptyList(), "sharding_db");
        assertTableMetaDataMap(actual);
        TableMetaData actualTableMetaData = actual.iterator().next().getTables().iterator().next();
        Iterator<ColumnMetaData> columnsIterator = actualTableMetaData.getColumns().iterator();
        assertThat(columnsIterator.next(), is(new ColumnMetaData("id", Types.INTEGER, false, true, true, true, false)));
        assertThat(columnsIterator.next(), is(new ColumnMetaData("name", Types.VARCHAR, false, false, false, true, false)));
    }
    
    @Test
    void assertLoadWithTablesWithHighVersion() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(LOAD_COLUMN_META_DATA_WITH_TABLES_HIGH_VERSION).executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(LOAD_INDEX_META_DATA)
                .executeQuery()).thenReturn(indexResultSet);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(15);
        Collection<SchemaMetaData> actual = getDialectTableMetaDataLoader().load(dataSource, Collections.singletonList("tbl"), "sharding_db");
        assertTableMetaDataMap(actual);
        TableMetaData actualTableMetaData = actual.iterator().next().getTables().iterator().next();
        Iterator<ColumnMetaData> columnsIterator = actualTableMetaData.getColumns().iterator();
        assertThat(columnsIterator.next(), is(new ColumnMetaData("id", Types.INTEGER, false, true, true, true, false)));
        assertThat(columnsIterator.next(), is(new ColumnMetaData("name", Types.VARCHAR, false, false, false, false, false)));
    }
    
    @Test
    void assertLoadWithTablesWithLowVersion() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(LOAD_COLUMN_META_DATA_WITH_TABLES_LOW_VERSION).executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(LOAD_INDEX_META_DATA)
                .executeQuery()).thenReturn(indexResultSet);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(14);
        Collection<SchemaMetaData> actual = getDialectTableMetaDataLoader().load(dataSource, Collections.singletonList("tbl"), "sharding_db");
        assertTableMetaDataMap(actual);
        TableMetaData actualTableMetaData = actual.iterator().next().getTables().iterator().next();
        Iterator<ColumnMetaData> columnsIterator = actualTableMetaData.getColumns().iterator();
        assertThat(columnsIterator.next(), is(new ColumnMetaData("id", Types.INTEGER, false, true, true, true, false)));
        assertThat(columnsIterator.next(), is(new ColumnMetaData("name", Types.VARCHAR, false, false, false, true, false)));
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
        when(result.getString("COLUMN_KEY")).thenReturn("1", "");
        when(result.getString("IS_IDENTITY")).thenReturn("1", "");
        when(result.getString("IS_HIDDEN")).thenReturn("0", "1");
        when(result.getString("COLLATION_NAME")).thenReturn("SQL_Latin1_General_CP1_CS_AS", "utf8");
        return result;
    }
    
    private ResultSet mockIndexMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("INDEX_NAME")).thenReturn("id");
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        return result;
    }
    
    private DialectSchemaMetaDataLoader getDialectTableMetaDataLoader() {
        Optional<DialectSchemaMetaDataLoader> result = DatabaseTypedSPILoader.findService(DialectSchemaMetaDataLoader.class, TypedSPILoader.getService(DatabaseType.class, "SQLServer"));
        assertTrue(result.isPresent());
        return result.get();
    }
    
    private void assertTableMetaDataMap(final Collection<SchemaMetaData> schemaMetaDataList) {
        assertThat(schemaMetaDataList.size(), is(1));
        TableMetaData actualTableMetaData = schemaMetaDataList.iterator().next().getTables().iterator().next();
        assertThat(actualTableMetaData.getColumns().size(), is(2));
        assertThat(actualTableMetaData.getIndexes().size(), is(1));
        Iterator<IndexMetaData> indexesIterator = actualTableMetaData.getIndexes().iterator();
        assertThat(indexesIterator.next(), is(new IndexMetaData("id")));
    }
}
