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

import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.schema.loader.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.loader.spi.DialectTableMetaDataLoaderFactory;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class H2TableMetaDataLoaderTest {
    
    @Test
    public void assertLoadWithoutTables() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(
                "SELECT TABLE_CATALOG, TABLE_NAME, COLUMN_NAME, DATA_TYPE, TYPE_NAME, ORDINAL_POSITION FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? "
                        + "ORDER BY ORDINAL_POSITION").executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(
                "SELECT TABLE_CATALOG, TABLE_NAME, INDEX_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? AND TABLE_NAME IN ('tbl')")
                .executeQuery())
                .thenReturn(indexResultSet);
        ResultSet primaryKeys = mockPrimaryKeysMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(
                "SELECT TABLE_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? AND PRIMARY_KEY = TRUE").executeQuery()).thenReturn(primaryKeys);
        ResultSet generatedInfo = mockGeneratedInfoResultSet();
        when(dataSource.getConnection().prepareStatement(
                "SELECT C.TABLE_NAME TABLE_NAME, C.COLUMN_NAME COLUMN_NAME, COALESCE(S.IS_GENERATED, FALSE) IS_GENERATED FROM INFORMATION_SCHEMA.COLUMNS C RIGHT JOIN"
                        + " INFORMATION_SCHEMA.SEQUENCES S ON C.SEQUENCE_NAME=S.SEQUENCE_NAME WHERE C.TABLE_CATALOG=? AND C.TABLE_SCHEMA=?").executeQuery()).thenReturn(generatedInfo);
        assertTableMetaDataMap(getDialectTableMetaDataLoader().load(dataSource, Collections.emptyList()));
    }
    
    @Test
    public void assertLoadWithTables() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(
                "SELECT TABLE_CATALOG, TABLE_NAME, COLUMN_NAME, DATA_TYPE, TYPE_NAME, ORDINAL_POSITION FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? "
                        + "AND TABLE_NAME IN ('tbl') ORDER BY ORDINAL_POSITION")
                .executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(
                "SELECT TABLE_CATALOG, TABLE_NAME, INDEX_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? AND TABLE_NAME IN ('tbl')")
                .executeQuery()).thenReturn(indexResultSet);
        ResultSet primaryKeys = mockPrimaryKeysMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(
                "SELECT TABLE_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? AND PRIMARY_KEY = TRUE AND TABLE_NAME IN ('tbl')")
                .executeQuery())
                .thenReturn(primaryKeys);
        ResultSet generatedInfo = mockGeneratedInfoResultSet();
        when(dataSource.getConnection().prepareStatement(
                "SELECT C.TABLE_NAME TABLE_NAME, C.COLUMN_NAME COLUMN_NAME, COALESCE(S.IS_GENERATED, FALSE) IS_GENERATED FROM INFORMATION_SCHEMA.COLUMNS C"
                        + " RIGHT JOIN INFORMATION_SCHEMA.SEQUENCES S ON C.SEQUENCE_NAME=S.SEQUENCE_NAME WHERE C.TABLE_CATALOG=? AND C.TABLE_SCHEMA=? AND TABLE_NAME IN ('tbl')")
                .executeQuery())
                .thenReturn(generatedInfo);
        assertTableMetaDataMap(getDialectTableMetaDataLoader().load(dataSource, Collections.singletonList("tbl")));
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
        when(result.getString("TYPE_NAME")).thenReturn("int", "varchar");
        when(result.getString("COLUMN_KEY")).thenReturn("PRI", "");
        when(result.getString("EXTRA")).thenReturn("auto_increment", "");
        when(result.getString("COLLATION_NAME")).thenReturn("utf8_general_ci", "utf8");
        return result;
    }
    
    private ResultSet mockPrimaryKeysMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        when(result.getString("COLUMN_NAME")).thenReturn("id");
        return result;
    }
    
    private ResultSet mockGeneratedInfoResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        when(result.getString("COLUMN_NAME")).thenReturn("id");
        when(result.getBoolean("IS_GENERATED")).thenReturn(false);
        return result;
    }
    
    private ResultSet mockIndexMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("INDEX_NAME")).thenReturn("id");
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        return result;
    }
    
    private DialectTableMetaDataLoader getDialectTableMetaDataLoader() {
        Optional<DialectTableMetaDataLoader> result = DialectTableMetaDataLoaderFactory.newInstance(DatabaseTypeRegistry.getActualDatabaseType("H2"));
        assertTrue(result.isPresent());
        return result.get();
    }
    
    private void assertTableMetaDataMap(final Map<String, TableMetaData> actual) {
        assertThat(actual.size(), is(1));
        TableMetaData actualTableMetaData = actual.get("tbl");
        assertThat(actualTableMetaData.getColumns().size(), is(2));
        List<String> actualColumnNames = new ArrayList<>(actualTableMetaData.getColumns().keySet());
        assertThat(actualTableMetaData.getColumns().get(actualColumnNames.get(0)), is(new ColumnMetaData("id", 4, true, false, true)));
        assertThat(actualTableMetaData.getColumns().get(actualColumnNames.get(1)), is(new ColumnMetaData("name", 12, false, false, true)));
        assertThat(actualTableMetaData.getIndexes().size(), is(1));
        assertThat(actualTableMetaData.getIndexes().get("id"), is(new IndexMetaData("id")));
    }
}
