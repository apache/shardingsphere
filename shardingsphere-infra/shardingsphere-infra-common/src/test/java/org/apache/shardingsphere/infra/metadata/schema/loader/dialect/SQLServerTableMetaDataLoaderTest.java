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

package org.apache.shardingsphere.infra.metadata.schema.loader.dialect;

import org.apache.shardingsphere.infra.metadata.schema.loader.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.spi.singleton.SingletonSPIRegistry;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SQLServerTableMetaDataLoaderTest {
    
    private static final Map<String, DialectTableMetaDataLoader> DIALECT_METADATA_LOADER_MAP = SingletonSPIRegistry.getSingletonInstancesMap(
            DialectTableMetaDataLoader.class, DialectTableMetaDataLoader::getDatabaseType);
    
    @Test
    public void assertLoadWithoutTables() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(
                "SELECT obj.name AS TABLE_NAME, col.name AS COLUMN_NAME, t.name AS DATA_TYPE,"
                        + " col.collation_name AS COLLATION_NAME, col.column_id, is_identity AS IS_IDENTITY,"
                        + " (SELECT TOP 1 ind.is_primary_key FROM sys.index_columns ic LEFT JOIN sys.indexes ind ON ic.object_id = ind.object_id"
                        + " AND ic.index_id = ind.index_id AND ind.name LIKE 'PK_%' WHERE ic.object_id = obj.object_id AND ic.column_id = col.column_id) AS IS_PRIMARY_KEY"
                        + " FROM sys.objects obj INNER JOIN sys.columns col ON obj.object_id = col.object_id LEFT JOIN sys.types t ON t.user_type_id = col.user_type_id ORDER BY col.column_id")
                .executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(
                "SELECT a.name AS INDEX_NAME, c.name AS TABLE_NAME FROM sys.indexes a"
                        + " JOIN sys.objects c ON a.object_id = c.object_id WHERE a.index_id NOT IN (0, 255) AND c.name IN ('tbl')")
                .executeQuery()).thenReturn(indexResultSet);
        assertTableMetaDataMap(getTableMetaDataLoader().load(dataSource, Collections.emptyList()));
    }
    
    @Test
    public void assertLoadWithTables() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(
                "SELECT obj.name AS TABLE_NAME, col.name AS COLUMN_NAME, t.name AS DATA_TYPE,"
                        + " col.collation_name AS COLLATION_NAME, col.column_id, is_identity AS IS_IDENTITY,"
                        + " (SELECT TOP 1 ind.is_primary_key FROM sys.index_columns ic LEFT JOIN sys.indexes ind ON ic.object_id = ind.object_id"
                        + " AND ic.index_id = ind.index_id AND ind.name LIKE 'PK_%' WHERE ic.object_id = obj.object_id AND ic.column_id = col.column_id) AS IS_PRIMARY_KEY"
                        + " FROM sys.objects obj INNER JOIN sys.columns col ON obj.object_id = col.object_id LEFT JOIN sys.types t ON t.user_type_id = col.user_type_id"
                        + " WHERE obj.name IN ('tbl') ORDER BY col.column_id")
                .executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(
                "SELECT a.name AS INDEX_NAME, c.name AS TABLE_NAME FROM sys.indexes a"
                        + " JOIN sys.objects c ON a.object_id = c.object_id WHERE a.index_id NOT IN (0, 255) AND c.name IN ('tbl')")
                .executeQuery()).thenReturn(indexResultSet);
        assertTableMetaDataMap(getTableMetaDataLoader().load(dataSource, Collections.singletonList("tbl")));
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
        when(result.getInt("DATA_TYPE")).thenReturn(4, 12);
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
    
    private DialectTableMetaDataLoader getTableMetaDataLoader() {
        DialectTableMetaDataLoader result = DIALECT_METADATA_LOADER_MAP.get("SQLServer");
        if (null != result) {
            return result;
        }
        throw new IllegalStateException("Can not find SQLServerTableMetaDataLoader");
    }
    
    private void assertTableMetaDataMap(final Map<String, TableMetaData> actual) {
        assertThat(actual.size(), is(1));
        TableMetaData actualTableMetaData = actual.get("tbl");
        assertThat(actualTableMetaData.getColumns().size(), is(2));
        List<String> actualColumnNames = new ArrayList<>(actualTableMetaData.getColumns().keySet());
        assertThat(actualTableMetaData.getColumns().get(actualColumnNames.get(0)), is(new ColumnMetaData("id", 4, false, true, true)));
        assertThat(actualTableMetaData.getColumns().get(actualColumnNames.get(1)), is(new ColumnMetaData("name", 12, false, false, false)));
        assertThat(actualTableMetaData.getIndexes().size(), is(1));
        assertThat(actualTableMetaData.getIndexes().get("id"), is(new IndexMetaData("id")));
    }
}
