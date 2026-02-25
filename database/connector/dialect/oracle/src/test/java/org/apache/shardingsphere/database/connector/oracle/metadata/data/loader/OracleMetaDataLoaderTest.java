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

package org.apache.shardingsphere.database.connector.oracle.metadata.data.loader;

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

class OracleMetaDataLoaderTest {
    
    private static final String ALL_CONSTRAINTS_SQL_WITH_TABLES = "SELECT A.OWNER AS TABLE_SCHEMA, A.TABLE_NAME AS TABLE_NAME, B.COLUMN_NAME AS COLUMN_NAME FROM ALL_CONSTRAINTS A"
            + " INNER JOIN ALL_CONS_COLUMNS B ON A.CONSTRAINT_NAME = B.CONSTRAINT_NAME WHERE CONSTRAINT_TYPE = 'P' AND A.OWNER = 'TEST' AND A.TABLE_NAME IN ('tbl')";
    
    private static final String ALL_INDEXES_SQL = "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, INDEX_NAME, UNIQUENESS FROM ALL_INDEXES WHERE OWNER = ? AND TABLE_NAME IN ('tbl')";
    
    private static final String ALL_INDEX_COLUMNS_SQL_WITH_MULTIPLE_INDEXES = "SELECT INDEX_NAME, COLUMN_NAME FROM ALL_IND_COLUMNS WHERE INDEX_OWNER = ? AND INDEX_NAME IN ('id','id_2')";
    
    private static final String ALL_VIEWS_SQL = "SELECT VIEW_NAME FROM ALL_VIEWS WHERE OWNER = ? AND VIEW_NAME IN ('tbl')";
    
    private static final String ALL_TAB_COLUMNS_SQL_WITH_IDENTITY_AND_COLLATION =
            "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, NULLABLE, DATA_TYPE, COLUMN_ID, HIDDEN_COLUMN , IDENTITY_COLUMN, COLLATION"
                    + " FROM ALL_TAB_COLS WHERE OWNER = ? AND TABLE_NAME IN ('tbl') ORDER BY COLUMN_ID";
    
    private static final String ALL_TAB_COLUMNS_SQL_WITH_IDENTITY = "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, NULLABLE, DATA_TYPE, COLUMN_ID, HIDDEN_COLUMN , IDENTITY_COLUMN "
            + "FROM ALL_TAB_COLS WHERE OWNER = ? AND TABLE_NAME IN ('tbl') ORDER BY COLUMN_ID";
    
    private static final String ALL_TAB_COLUMNS_SQL_WITHOUT_IDENTITY_AND_COLLATION =
            "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, NULLABLE, DATA_TYPE, COLUMN_ID, HIDDEN_COLUMN  FROM ALL_TAB_COLS"
                    + " WHERE OWNER = ? AND TABLE_NAME IN ('tbl') ORDER BY COLUMN_ID";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Oracle");
    
    private final DialectMetaDataLoader dialectMetaDataLoader = DatabaseTypedSPILoader.getService(DialectMetaDataLoader.class, databaseType);
    
    @SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed", "resource"})
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertLoadArguments")
    void assertLoad(final String name, final int majorVersion, final int minorVersion, final boolean withPrimaryKey, final boolean withNullValue) throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet tableMetaDataResultSet = withNullValue ? mockTableMetaDataResultSetWithNullValue() : mockTableMetaDataResultSet();
        ResultSet indexMetaDataResultSet = mockIndexMetaDataResultSet();
        ResultSet primaryKeysResultSet = withPrimaryKey ? mockPrimaryKeysMetaDataResultSet() : mock(ResultSet.class);
        when(dataSource.getConnection().prepareStatement(getTableMetaDataSQL(majorVersion, minorVersion)).executeQuery()).thenReturn(tableMetaDataResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_INDEXES_SQL).executeQuery()).thenReturn(indexMetaDataResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_CONSTRAINTS_SQL_WITH_TABLES).executeQuery()).thenReturn(primaryKeysResultSet);
        when(dataSource.getConnection().getMetaData().getUserName()).thenReturn("TEST");
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(majorVersion);
        when(dataSource.getConnection().getMetaData().getDatabaseMinorVersion()).thenReturn(minorVersion);
        TableMetaData actualTableMetaData = assertAndGetSingleTableMetaData(loadMetaData(dataSource));
        assertThat(actualTableMetaData.getIndexes().size(), is(1));
        List<ColumnMetaData> columnMetaDataList = new ArrayList<>(actualTableMetaData.getColumns());
        assertColumnMetaData(columnMetaDataList.get(0), getExpectedFirstColumnMetaData(majorVersion, minorVersion, withPrimaryKey));
        assertColumnMetaData(columnMetaDataList.get(1), new ColumnMetaData("name", Types.VARCHAR, false, false, false, false, false, true));
        assertColumnMetaData(columnMetaDataList.get(2), withNullValue
                ? new ColumnMetaData("address", Types.VARCHAR, false, false, false, false, false, true)
                : new ColumnMetaData("creation_time", Types.TIMESTAMP, false, false, false, true, false, true));
    }
    
    @SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed", "resource"})
    @Test
    void assertLoadWithViewAndMultipleIndexes() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet tableMetaDataResultSet = mockTableMetaDataResultSet();
        ResultSet indexMetaDataResultSet = mockIndexMetaDataResultSetWithMultipleIndexes();
        ResultSet indexColumnMetaDataResultSet = mockIndexColumnMetaDataResultSetWithMultipleIndexes();
        ResultSet viewMetaDataResultSet = mockViewMetaDataResultSet();
        ResultSet primaryKeysResultSet = mockPrimaryKeysMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_TAB_COLUMNS_SQL_WITH_IDENTITY_AND_COLLATION).executeQuery()).thenReturn(tableMetaDataResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_INDEXES_SQL).executeQuery()).thenReturn(indexMetaDataResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_INDEX_COLUMNS_SQL_WITH_MULTIPLE_INDEXES).executeQuery()).thenReturn(indexColumnMetaDataResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_VIEWS_SQL).executeQuery()).thenReturn(viewMetaDataResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_CONSTRAINTS_SQL_WITH_TABLES).executeQuery()).thenReturn(primaryKeysResultSet);
        when(dataSource.getConnection().getMetaData().getUserName()).thenReturn("TEST");
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(12);
        when(dataSource.getConnection().getMetaData().getDatabaseMinorVersion()).thenReturn(2);
        TableMetaData actualTableMetaData = assertAndGetSingleTableMetaData(loadMetaData(dataSource));
        assertThat(actualTableMetaData.getIndexes().size(), is(2));
        List<IndexMetaData> actualIndexes = new ArrayList<>(actualTableMetaData.getIndexes());
        assertIndexMetaData(actualIndexes.get(0), new IndexMetaData("id"), true, Collections.singletonList("id"));
        assertIndexMetaData(actualIndexes.get(1), new IndexMetaData("id_2"), false, Collections.singletonList("name"));
    }
    
    @SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed", "resource"})
    @Test
    void assertLoadWithoutIndexes() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet tableMetaDataResultSet = mockTableMetaDataResultSet();
        ResultSet primaryKeysResultSet = mockPrimaryKeysMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_TAB_COLUMNS_SQL_WITH_IDENTITY_AND_COLLATION).executeQuery()).thenReturn(tableMetaDataResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_CONSTRAINTS_SQL_WITH_TABLES).executeQuery()).thenReturn(primaryKeysResultSet);
        when(dataSource.getConnection().getMetaData().getUserName()).thenReturn("TEST");
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(12);
        when(dataSource.getConnection().getMetaData().getDatabaseMinorVersion()).thenReturn(2);
        assertTrue(assertAndGetSingleTableMetaData(loadMetaData(dataSource)).getIndexes().isEmpty());
    }
    
    @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
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
        when(result.getString("NULLABLE")).thenReturn("N", "Y", "Y");
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
        when(result.getString("NULLABLE")).thenReturn("N", "Y", "Y");
        return result;
    }
    
    private ResultSet mockIndexMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("INDEX_NAME")).thenReturn("id");
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        when(result.getString("UNIQUENESS")).thenReturn("UNIQUE");
        return result;
    }
    
    private ResultSet mockIndexMetaDataResultSetWithMultipleIndexes() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("INDEX_NAME")).thenReturn("id", "id_2");
        when(result.getString("TABLE_NAME")).thenReturn("tbl", "tbl");
        when(result.getString("UNIQUENESS")).thenReturn("UNIQUE", "NONUNIQUE");
        return result;
    }
    
    private ResultSet mockIndexColumnMetaDataResultSetWithMultipleIndexes() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("INDEX_NAME")).thenReturn("id", "id_2");
        when(result.getString("COLUMN_NAME")).thenReturn("id", "name");
        return result;
    }
    
    private ResultSet mockViewMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString(1)).thenReturn("tbl");
        return result;
    }
    
    private ResultSet mockPrimaryKeysMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        when(result.getString("COLUMN_NAME")).thenReturn("id");
        return result;
    }
    
    private String getTableMetaDataSQL(final int majorVersion, final int minorVersion) {
        if (majorVersion >= 12 && minorVersion >= 2) {
            return ALL_TAB_COLUMNS_SQL_WITH_IDENTITY_AND_COLLATION;
        }
        if (majorVersion >= 12 && minorVersion == 1) {
            return ALL_TAB_COLUMNS_SQL_WITH_IDENTITY;
        }
        return ALL_TAB_COLUMNS_SQL_WITHOUT_IDENTITY_AND_COLLATION;
    }
    
    private ColumnMetaData getExpectedFirstColumnMetaData(final int majorVersion, final int minorVersion, final boolean withPrimaryKey) {
        boolean generated = majorVersion >= 12 && minorVersion >= 1;
        boolean caseSensitive = majorVersion >= 12 && minorVersion >= 2;
        return new ColumnMetaData("id", Types.INTEGER, withPrimaryKey, generated, caseSensitive, true, false, false);
    }
    
    private Collection<SchemaMetaData> loadMetaData(final DataSource dataSource) throws SQLException {
        DataTypeRegistry.load(dataSource, "Oracle");
        return dialectMetaDataLoader.load(new MetaDataLoaderMaterial(Collections.singleton("tbl"), "foo_ds", dataSource, databaseType, "sharding_db"));
    }
    
    private TableMetaData assertAndGetSingleTableMetaData(final Collection<SchemaMetaData> schemaMetaDataList) {
        assertThat(schemaMetaDataList.size(), is(1));
        SchemaMetaData actualSchemaMetaData = schemaMetaDataList.iterator().next();
        assertThat(actualSchemaMetaData.getTables().size(), is(1));
        return actualSchemaMetaData.getTables().iterator().next();
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
    
    private void assertIndexMetaData(final IndexMetaData actual, final IndexMetaData expected, final boolean expectedUnique, final Collection<String> expectedColumns) {
        expected.setUnique(expectedUnique);
        expected.setColumns(expectedColumns);
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getColumns(), is(expected.getColumns()));
        assertThat(actual.isUnique(), is(expected.isUnique()));
    }
    
    private static Stream<Arguments> assertLoadArguments() {
        return Stream.of(
                Arguments.of("major12Minor2WithoutPrimaryKey", 12, 2, false, false),
                Arguments.of("major12Minor1WithoutPrimaryKey", 12, 1, false, false),
                Arguments.of("major11Minor2WithoutPrimaryKey", 11, 2, false, false),
                Arguments.of("major12Minor0WithoutPrimaryKey", 12, 0, false, false),
                Arguments.of("major12Minor2WithPrimaryKey", 12, 2, true, false),
                Arguments.of("major12Minor1WithPrimaryKey", 12, 1, true, false),
                Arguments.of("major11Minor2WithPrimaryKey", 11, 2, true, false),
                Arguments.of("major12Minor2WithPrimaryKeyAndNullCollation", 12, 2, true, true));
    }
}
