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

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.DialectSchemaMetaDataLoader;
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

class OracleSchemaMetaDataLoaderTest {
    
    private static final String ALL_CONSTRAINTS_SQL_WITHOUT_TABLES = "SELECT A.OWNER AS TABLE_SCHEMA, A.TABLE_NAME AS TABLE_NAME, B.COLUMN_NAME AS COLUMN_NAME FROM ALL_CONSTRAINTS A"
            + " INNER JOIN ALL_CONS_COLUMNS B ON A.CONSTRAINT_NAME = B.CONSTRAINT_NAME WHERE CONSTRAINT_TYPE = 'P' AND A.OWNER = 'TEST'";
    
    private static final String ALL_CONSTRAINTS_SQL_WITH_TABLES = "SELECT A.OWNER AS TABLE_SCHEMA, A.TABLE_NAME AS TABLE_NAME, B.COLUMN_NAME AS COLUMN_NAME FROM ALL_CONSTRAINTS A"
            + " INNER JOIN ALL_CONS_COLUMNS B ON A.CONSTRAINT_NAME = B.CONSTRAINT_NAME WHERE CONSTRAINT_TYPE = 'P' AND A.OWNER = 'TEST' AND A.TABLE_NAME IN ('tbl')";
    
    private static final String ALL_INDEXES_SQL = "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, INDEX_NAME FROM ALL_INDEXES WHERE OWNER = ? AND TABLE_NAME IN ('tbl')";
    
    private static final String ALL_TAB_COLUMNS_SQL_CONDITION1 = "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_ID, HIDDEN_COLUMN , IDENTITY_COLUMN, COLLATION"
            + " FROM ALL_TAB_COLS WHERE OWNER = ? AND TABLE_NAME IN ('tbl') ORDER BY COLUMN_ID";
    
    private static final String ALL_TAB_COLUMNS_SQL_CONDITION2 = "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_ID, HIDDEN_COLUMN , IDENTITY_COLUMN"
            + " FROM ALL_TAB_COLS WHERE OWNER = ? AND TABLE_NAME IN ('tbl') ORDER BY COLUMN_ID";
    
    private static final String ALL_TAB_COLUMNS_SQL_CONDITION3 = "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_ID, HIDDEN_COLUMN  FROM ALL_TAB_COLS WHERE OWNER = ?"
            + " AND TABLE_NAME IN ('tbl') ORDER BY COLUMN_ID";
    
    private static final String ALL_TAB_COLUMNS_SQL_CONDITION4 = "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_ID, HIDDEN_COLUMN , IDENTITY_COLUMN, COLLATION"
            + " FROM ALL_TAB_COLS WHERE OWNER = ? AND TABLE_NAME IN ('tbl') ORDER BY COLUMN_ID";
    
    private static final String ALL_TAB_COLUMNS_SQL_CONDITION5 = "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_ID, HIDDEN_COLUMN , IDENTITY_COLUMN FROM ALL_TAB_COLS"
            + " WHERE OWNER = ? AND TABLE_NAME IN ('tbl') ORDER BY COLUMN_ID";
    
    private static final String ALL_TAB_COLUMNS_SQL_CONDITION6 = "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_ID, HIDDEN_COLUMN  FROM ALL_TAB_COLS"
            + " WHERE OWNER = ? AND TABLE_NAME IN ('tbl') ORDER BY COLUMN_ID";
    
    private static final String ALL_TAB_COLUMNS_SQL_CONDITION7 = "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_ID, HIDDEN_COLUMN , IDENTITY_COLUMN, COLLATION"
            + " FROM ALL_TAB_COLS WHERE OWNER = ? AND TABLE_NAME IN ('tbl') ORDER BY COLUMN_ID";
    
    @Test
    void assertLoadCondition1() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_TAB_COLUMNS_SQL_CONDITION1).executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_INDEXES_SQL).executeQuery()).thenReturn(indexResultSet);
        ResultSet primaryKeys = mockPrimaryKeysMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_CONSTRAINTS_SQL_WITHOUT_TABLES).executeQuery()).thenReturn(primaryKeys);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(12);
        when(dataSource.getConnection().getMetaData().getDatabaseMinorVersion()).thenReturn(2);
        Collection<SchemaMetaData> actual = getDialectTableMetaDataLoader().load(dataSource, Collections.singleton("tbl"), "sharding_db");
        assertTableMetaDataMap(actual);
        TableMetaData actualTableMetaData = actual.iterator().next().getTables().iterator().next();
        Iterator<ColumnMetaData> columnsIterator = actualTableMetaData.getColumns().iterator();
        assertThat(columnsIterator.next(), is(new ColumnMetaData("id", Types.INTEGER, false, true, true, true, false)));
        assertThat(columnsIterator.next(), is(new ColumnMetaData("name", Types.VARCHAR, false, false, false, false, false)));
    }
    
    @Test
    void assertLoadCondition2() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_TAB_COLUMNS_SQL_CONDITION2).executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_INDEXES_SQL).executeQuery()).thenReturn(indexResultSet);
        ResultSet primaryKeys = mockPrimaryKeysMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_CONSTRAINTS_SQL_WITHOUT_TABLES).executeQuery()).thenReturn(primaryKeys);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(12);
        when(dataSource.getConnection().getMetaData().getDatabaseMinorVersion()).thenReturn(1);
        Collection<SchemaMetaData> actual = getDialectTableMetaDataLoader().load(dataSource, Collections.singleton("tbl"), "sharding_db");
        assertTableMetaDataMap(actual);
        TableMetaData actualTableMetaData = actual.iterator().next().getTables().iterator().next();
        Iterator<ColumnMetaData> columnsIterator = actualTableMetaData.getColumns().iterator();
        assertThat(columnsIterator.next(), is(new ColumnMetaData("id", Types.INTEGER, false, true, false, true, false)));
        assertThat(columnsIterator.next(), is(new ColumnMetaData("name", Types.VARCHAR, false, false, false, false, false)));
    }
    
    @Test
    void assertLoadCondition3() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_TAB_COLUMNS_SQL_CONDITION3).executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_INDEXES_SQL).executeQuery()).thenReturn(indexResultSet);
        ResultSet primaryKeys = mockPrimaryKeysMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_CONSTRAINTS_SQL_WITHOUT_TABLES).executeQuery()).thenReturn(primaryKeys);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(11);
        when(dataSource.getConnection().getMetaData().getDatabaseMinorVersion()).thenReturn(2);
        Collection<SchemaMetaData> actual = getDialectTableMetaDataLoader().load(dataSource, Collections.singleton("tbl"), "sharding_db");
        assertTableMetaDataMap(actual);
        TableMetaData actualTableMetaData = actual.iterator().next().getTables().iterator().next();
        Iterator<ColumnMetaData> columnsIterator = actualTableMetaData.getColumns().iterator();
        assertThat(columnsIterator.next(), is(new ColumnMetaData("id", Types.INTEGER, false, false, false, true, false)));
        assertThat(columnsIterator.next(), is(new ColumnMetaData("name", Types.VARCHAR, false, false, false, false, false)));
    }
    
    @Test
    void assertLoadCondition4() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_TAB_COLUMNS_SQL_CONDITION4).executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_INDEXES_SQL).executeQuery()).thenReturn(indexResultSet);
        when(dataSource.getConnection().getMetaData().getUserName()).thenReturn("TEST");
        ResultSet primaryKeys = mockPrimaryKeysMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_CONSTRAINTS_SQL_WITH_TABLES).executeQuery()).thenReturn(primaryKeys);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(12);
        when(dataSource.getConnection().getMetaData().getDatabaseMinorVersion()).thenReturn(2);
        Collection<SchemaMetaData> actual = getDialectTableMetaDataLoader().load(dataSource, Collections.singleton("tbl"), "sharding_db");
        assertTableMetaDataMap(actual);
        TableMetaData actualTableMetaData = actual.iterator().next().getTables().iterator().next();
        Iterator<ColumnMetaData> columnsIterator = actualTableMetaData.getColumns().iterator();
        assertThat(columnsIterator.next(), is(new ColumnMetaData("id", Types.INTEGER, true, true, true, true, false)));
        assertThat(columnsIterator.next(), is(new ColumnMetaData("name", Types.VARCHAR, false, false, false, false, false)));
    }
    
    @Test
    void assertLoadCondition5() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_TAB_COLUMNS_SQL_CONDITION5).executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_INDEXES_SQL).executeQuery()).thenReturn(indexResultSet);
        when(dataSource.getConnection().getMetaData().getUserName()).thenReturn("TEST");
        ResultSet primaryKeys = mockPrimaryKeysMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_CONSTRAINTS_SQL_WITH_TABLES).executeQuery()).thenReturn(primaryKeys);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(12);
        when(dataSource.getConnection().getMetaData().getDatabaseMinorVersion()).thenReturn(1);
        Collection<SchemaMetaData> actual = getDialectTableMetaDataLoader().load(dataSource, Collections.singleton("tbl"), "sharding_db");
        assertTableMetaDataMap(actual);
        TableMetaData actualTableMetaData = actual.iterator().next().getTables().iterator().next();
        Iterator<ColumnMetaData> columnsIterator = actualTableMetaData.getColumns().iterator();
        assertThat(columnsIterator.next(), is(new ColumnMetaData("id", Types.INTEGER, true, true, false, true, false)));
        assertThat(columnsIterator.next(), is(new ColumnMetaData("name", Types.VARCHAR, false, false, false, false, false)));
    }
    
    @Test
    void assertLoadCondition6() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_TAB_COLUMNS_SQL_CONDITION6).executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_INDEXES_SQL).executeQuery()).thenReturn(indexResultSet);
        when(dataSource.getConnection().getMetaData().getUserName()).thenReturn("TEST");
        ResultSet primaryKeys = mockPrimaryKeysMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_CONSTRAINTS_SQL_WITH_TABLES).executeQuery()).thenReturn(primaryKeys);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(11);
        when(dataSource.getConnection().getMetaData().getDatabaseMinorVersion()).thenReturn(2);
        Collection<SchemaMetaData> actual = getDialectTableMetaDataLoader().load(dataSource, Collections.singleton("tbl"), "sharding_db");
        assertTableMetaDataMap(actual);
        TableMetaData actualTableMetaData = actual.iterator().next().getTables().iterator().next();
        Iterator<ColumnMetaData> columnsIterator = actualTableMetaData.getColumns().iterator();
        assertThat(columnsIterator.next(), is(new ColumnMetaData("id", Types.INTEGER, true, false, false, true, false)));
        assertThat(columnsIterator.next(), is(new ColumnMetaData("name", Types.VARCHAR, false, false, false, false, false)));
    }
    
    @Test
    void assertLoadCondition7() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSetWithNullValue();
        when(dataSource.getConnection().prepareStatement(ALL_TAB_COLUMNS_SQL_CONDITION7).executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_INDEXES_SQL).executeQuery()).thenReturn(indexResultSet);
        when(dataSource.getConnection().getMetaData().getUserName()).thenReturn("TEST");
        ResultSet primaryKeys = mockPrimaryKeysMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_CONSTRAINTS_SQL_WITH_TABLES).executeQuery()).thenReturn(primaryKeys);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(12);
        when(dataSource.getConnection().getMetaData().getDatabaseMinorVersion()).thenReturn(2);
        Collection<SchemaMetaData> actual = getDialectTableMetaDataLoader().load(dataSource, Collections.singleton("tbl"), "sharding_db");
        assertTableMetaDataMap(actual);
        TableMetaData actualTableMetaData = actual.iterator().next().getTables().iterator().next();
        Iterator<ColumnMetaData> columnsIterator = actualTableMetaData.getColumns().iterator();
        assertThat(columnsIterator.next(), is(new ColumnMetaData("id", Types.INTEGER, true, true, true, true, false)));
        assertThat(columnsIterator.next(), is(new ColumnMetaData("name", Types.VARCHAR, false, false, false, false, false)));
        assertThat(columnsIterator.next(), is(new ColumnMetaData("address", Types.VARCHAR, false, false, false, false, false)));
    }
    
    private DataSource mockDataSource() throws SQLException {
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        ResultSet typeInfoResultSet = mockTypeInfoResultSet();
        when(result.getConnection().getMetaData().getTypeInfo()).thenReturn(typeInfoResultSet);
        return result;
    }
    
    private ResultSet mockTypeInfoResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, false);
        when(result.getString("TYPE_NAME")).thenReturn("int", "varchar", "TIMESTAMP");
        when(result.getInt("DATA_TYPE")).thenReturn(Types.INTEGER, Types.VARCHAR, Types.TIMESTAMP);
        return result;
    }
    
    private ResultSet mockTableMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        when(result.getString("COLUMN_NAME")).thenReturn("id", "name", "creation_time");
        when(result.getString("DATA_TYPE")).thenReturn("int", "varchar", "TIMESTAMP(6)");
        when(result.getString("HIDDEN_COLUMN")).thenReturn("NO", "YES", "NO");
        when(result.getString("IDENTITY_COLUMN")).thenReturn("YES", "NO", "NO");
        when(result.getString("COLLATION")).thenReturn("BINARY_CS", "BINARY_CI", "BINARY_CI");
        return result;
    }
    
    private ResultSet mockTableMetaDataResultSetWithNullValue() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        when(result.getString("COLUMN_NAME")).thenReturn("id", "name", "address");
        when(result.getString("DATA_TYPE")).thenReturn("int", "varchar", "varchar");
        when(result.getString("HIDDEN_COLUMN")).thenReturn("NO", "YES", "YES");
        when(result.getString("IDENTITY_COLUMN")).thenReturn("YES", "NO", "NO");
        when(result.getString("COLLATION")).thenReturn("BINARY_CS", "BINARY_CI", null);
        return result;
    }
    
    private ResultSet mockIndexMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("INDEX_NAME")).thenReturn("id");
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        return result;
    }
    
    private ResultSet mockPrimaryKeysMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        when(result.getString("COLUMN_NAME")).thenReturn("id");
        return result;
    }
    
    private DialectSchemaMetaDataLoader getDialectTableMetaDataLoader() {
        Optional<DialectSchemaMetaDataLoader> result = TypedSPILoader.findService(
                DialectSchemaMetaDataLoader.class, TypedSPILoader.getService(DatabaseType.class, "Oracle").getType());
        assertTrue(result.isPresent());
        return result.get();
    }
    
    private void assertTableMetaDataMap(final Collection<SchemaMetaData> schemaMetaDataList) {
        assertThat(schemaMetaDataList.size(), is(1));
        TableMetaData actualTableMetaData = schemaMetaDataList.iterator().next().getTables().iterator().next();
        assertThat(actualTableMetaData.getColumns().size(), is(3));
        assertThat(actualTableMetaData.getIndexes().size(), is(1));
        Iterator<IndexMetaData> indexesIterator = actualTableMetaData.getIndexes().iterator();
        assertThat(indexesIterator.next(), is(new IndexMetaData("id")));
    }
}
