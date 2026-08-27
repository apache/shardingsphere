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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OracleMetaDataLoaderTest {
    
    private static final String NO_COLLATION = "";
    
    private static final String ALL_PRIMARY_KEY_CONSTRAINTS_SQL =
            "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, CONSTRAINT_NAME FROM ALL_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'P' AND OWNER = ? AND TABLE_NAME IN ('tbl')";
    
    private static final String ALL_PRIMARY_KEY_COLUMNS_SQL =
            "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME FROM ALL_CONS_COLUMNS WHERE OWNER = ? AND TABLE_NAME IN ('tbl') AND CONSTRAINT_NAME IN (?)";
    
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
    void assertLoad(final String name, final int majorVersion, final int minorVersion,
                    final boolean withPrimaryKey, final String collation, final boolean expectedCaseSensitive) throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet tableMetaDataResultSet = mockTableMetaDataResultSet(collation);
        ResultSet indexMetaDataResultSet = mockIndexMetaDataResultSet();
        ResultSet primaryKeyConstraintsResultSet = withPrimaryKey ? mockPrimaryKeyConstraintsMetaDataResultSet() : mock(ResultSet.class);
        ResultSet primaryKeyColumnsResultSet = mockPrimaryKeyColumnsMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(getTableMetaDataSQL(majorVersion, minorVersion)).executeQuery()).thenReturn(tableMetaDataResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_INDEXES_SQL).executeQuery()).thenReturn(indexMetaDataResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_PRIMARY_KEY_CONSTRAINTS_SQL).executeQuery()).thenReturn(primaryKeyConstraintsResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_PRIMARY_KEY_COLUMNS_SQL).executeQuery()).thenReturn(primaryKeyColumnsResultSet);
        when(dataSource.getConnection().getMetaData().getUserName()).thenReturn("TEST");
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(majorVersion);
        when(dataSource.getConnection().getMetaData().getDatabaseMinorVersion()).thenReturn(minorVersion);
        TableMetaData actualTableMetaData = assertAndGetSingleTableMetaData(loadMetaData(dataSource));
        assertThat(actualTableMetaData.getIndexes().size(), is(1));
        List<ColumnMetaData> columnMetaDataList = new ArrayList<>(actualTableMetaData.getColumns());
        assertColumnMetaData(columnMetaDataList.get(0), getExpectedFirstColumnMetaData(majorVersion, minorVersion, withPrimaryKey));
        assertColumnMetaData(columnMetaDataList.get(1), new ColumnMetaData("name", Types.VARCHAR, false, false, expectedCaseSensitive, false, false, true));
        assertColumnMetaData(columnMetaDataList.get(2), new ColumnMetaData("creation_time", Types.TIMESTAMP, false, false, false, true, false, true));
    }
    
    @SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed", "resource"})
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertLoadPrimaryKeysArguments")
    void assertLoadPrimaryKeys(final String name, final Collection<String> tableNames, final Collection<String> constraintNames,
                               final Collection<String> primaryKeyTableNames, final Collection<String> primaryKeyColumnNames) throws SQLException {
        DataSource dataSource = mockDataSource();
        Connection connection = dataSource.getConnection();
        ResultSet tableMetaDataResultSet = mockMultipleTableMetaDataResultSet(tableNames);
        ResultSet primaryKeyConstraintsResultSet = mockPrimaryKeyConstraintRows(constraintNames);
        ResultSet primaryKeyColumnsResultSet = mockPrimaryKeyColumnRows(primaryKeyTableNames, primaryKeyColumnNames);
        PreparedStatement primaryKeyColumnsStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(getMultipleTableMetaDataSQL(tableNames)).executeQuery()).thenReturn(tableMetaDataResultSet);
        when(connection.prepareStatement(getPrimaryKeyConstraintsSQL(tableNames)).executeQuery()).thenReturn(primaryKeyConstraintsResultSet);
        when(connection.prepareStatement(getPrimaryKeyColumnsSQL(tableNames, constraintNames))).thenReturn(primaryKeyColumnsStatement);
        when(primaryKeyColumnsStatement.executeQuery()).thenReturn(primaryKeyColumnsResultSet);
        when(connection.getMetaData().getUserName()).thenReturn("TEST");
        when(connection.getMetaData().getDatabaseMajorVersion()).thenReturn(12);
        when(connection.getMetaData().getDatabaseMinorVersion()).thenReturn(2);
        clearInvocations(connection, primaryKeyColumnsStatement);
        Collection<SchemaMetaData> actualSchemaMetaData = loadMetaData(dataSource, tableNames);
        assertPrimaryKeys(actualSchemaMetaData.iterator().next().getTables(), primaryKeyTableNames, primaryKeyColumnNames);
        verify(connection).prepareStatement(getPrimaryKeyConstraintsSQL(tableNames));
        verify(connection).prepareStatement(getPrimaryKeyColumnsSQL(tableNames, constraintNames));
        verify(primaryKeyColumnsStatement).setString(1, "TEST");
        int parameterIndex = 2;
        for (String each : constraintNames) {
            verify(primaryKeyColumnsStatement).setString(parameterIndex++, each);
        }
    }
    
    @SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed", "resource"})
    @Test
    void assertLoadWithoutPrimaryKeyColumnsQuery() throws SQLException {
        DataSource dataSource = mockDataSource();
        Connection connection = dataSource.getConnection();
        ResultSet tableMetaDataResultSet = mockTableMetaDataResultSet("BINARY");
        when(connection.prepareStatement(ALL_TAB_COLUMNS_SQL_WITH_IDENTITY_AND_COLLATION).executeQuery()).thenReturn(tableMetaDataResultSet);
        when(connection.prepareStatement(ALL_PRIMARY_KEY_CONSTRAINTS_SQL).executeQuery()).thenReturn(mock(ResultSet.class));
        when(connection.getMetaData().getUserName()).thenReturn("TEST");
        when(connection.getMetaData().getDatabaseMajorVersion()).thenReturn(12);
        when(connection.getMetaData().getDatabaseMinorVersion()).thenReturn(2);
        TableMetaData actualTableMetaData = assertAndGetSingleTableMetaData(loadMetaData(dataSource));
        for (ColumnMetaData each : actualTableMetaData.getColumns()) {
            assertFalse(each.isPrimaryKey());
        }
        verify(connection, never()).prepareStatement(ALL_PRIMARY_KEY_COLUMNS_SQL);
    }
    
    @SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed", "resource"})
    @Test
    void assertLoadWithViewAndMultipleIndexes() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet tableMetaDataResultSet = mockTableMetaDataResultSet("BINARY");
        ResultSet indexMetaDataResultSet = mockIndexMetaDataResultSetWithMultipleIndexes();
        ResultSet indexColumnMetaDataResultSet = mockIndexColumnMetaDataResultSetWithMultipleIndexes();
        ResultSet viewMetaDataResultSet = mockViewMetaDataResultSet();
        ResultSet primaryKeyConstraintsResultSet = mockPrimaryKeyConstraintsMetaDataResultSet();
        ResultSet primaryKeyColumnsResultSet = mockPrimaryKeyColumnsMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_TAB_COLUMNS_SQL_WITH_IDENTITY_AND_COLLATION).executeQuery()).thenReturn(tableMetaDataResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_INDEXES_SQL).executeQuery()).thenReturn(indexMetaDataResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_INDEX_COLUMNS_SQL_WITH_MULTIPLE_INDEXES).executeQuery()).thenReturn(indexColumnMetaDataResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_VIEWS_SQL).executeQuery()).thenReturn(viewMetaDataResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_PRIMARY_KEY_CONSTRAINTS_SQL).executeQuery()).thenReturn(primaryKeyConstraintsResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_PRIMARY_KEY_COLUMNS_SQL).executeQuery()).thenReturn(primaryKeyColumnsResultSet);
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
        ResultSet tableMetaDataResultSet = mockTableMetaDataResultSet("BINARY");
        ResultSet primaryKeyConstraintsResultSet = mockPrimaryKeyConstraintsMetaDataResultSet();
        ResultSet primaryKeyColumnsResultSet = mockPrimaryKeyColumnsMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ALL_TAB_COLUMNS_SQL_WITH_IDENTITY_AND_COLLATION).executeQuery()).thenReturn(tableMetaDataResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_PRIMARY_KEY_CONSTRAINTS_SQL).executeQuery()).thenReturn(primaryKeyConstraintsResultSet);
        when(dataSource.getConnection().prepareStatement(ALL_PRIMARY_KEY_COLUMNS_SQL).executeQuery()).thenReturn(primaryKeyColumnsResultSet);
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
    
    private ResultSet mockTableMetaDataResultSet(final String collation) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        when(result.getString("COLUMN_NAME")).thenReturn("id", "name", "creation_time");
        when(result.getString("DATA_TYPE")).thenReturn("int", "varchar", "TIMESTAMP(6)");
        when(result.getString("HIDDEN_COLUMN")).thenReturn("NO", "YES", "NO");
        when(result.getString("IDENTITY_COLUMN")).thenReturn("YES", "NO", "NO");
        when(result.getString("COLLATION")).thenReturn(null, collation.isEmpty() ? null : collation, null);
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
    
    private ResultSet mockPrimaryKeyConstraintsMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("CONSTRAINT_NAME")).thenReturn("PK'O");
        return result;
    }
    
    private ResultSet mockPrimaryKeyColumnsMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        when(result.getString("COLUMN_NAME")).thenReturn("id");
        return result;
    }
    
    private ResultSet mockPrimaryKeyConstraintRows(final Collection<String> constraintNames) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        List<String> constraints = new ArrayList<>(constraintNames);
        AtomicInteger rowIndex = new AtomicInteger(-1);
        when(result.next()).thenAnswer(ignored -> rowIndex.incrementAndGet() < constraints.size());
        when(result.getString("CONSTRAINT_NAME")).thenAnswer(ignored -> constraints.get(rowIndex.get()));
        return result;
    }
    
    private ResultSet mockPrimaryKeyColumnRows(final Collection<String> tableNames, final Collection<String> columnNames) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        List<String> tables = new ArrayList<>(tableNames);
        List<String> columns = new ArrayList<>(columnNames);
        AtomicInteger rowIndex = new AtomicInteger(-1);
        when(result.next()).thenAnswer(ignored -> rowIndex.incrementAndGet() < columns.size());
        when(result.getString("TABLE_NAME")).thenAnswer(ignored -> tables.get(rowIndex.get()));
        when(result.getString("COLUMN_NAME")).thenAnswer(ignored -> columns.get(rowIndex.get()));
        return result;
    }
    
    private ResultSet mockMultipleTableMetaDataResultSet(final Collection<String> tableNames) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        Collection<String> tables = new LinkedList<>();
        Collection<String> columns = new LinkedList<>();
        for (String each : tableNames) {
            tables.addAll(Arrays.asList(each, each, each));
            columns.addAll(Arrays.asList("id", "tenant_id", "name"));
        }
        List<String> tableNamesByRow = new ArrayList<>(tables);
        List<String> columnNamesByRow = new ArrayList<>(columns);
        AtomicInteger rowIndex = new AtomicInteger(-1);
        when(result.next()).thenAnswer(ignored -> rowIndex.incrementAndGet() < columnNamesByRow.size());
        when(result.getString("TABLE_NAME")).thenAnswer(ignored -> tableNamesByRow.get(rowIndex.get()));
        when(result.getString("COLUMN_NAME")).thenAnswer(ignored -> columnNamesByRow.get(rowIndex.get()));
        when(result.getString("DATA_TYPE")).thenReturn("int");
        when(result.getString("HIDDEN_COLUMN")).thenReturn("NO");
        when(result.getString("IDENTITY_COLUMN")).thenReturn("NO");
        when(result.getString("COLLATION")).thenReturn("BINARY");
        when(result.getString("NULLABLE")).thenReturn("Y");
        return result;
    }
    
    private String getMultipleTableMetaDataSQL(final Collection<String> tableNames) {
        return "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, NULLABLE, DATA_TYPE, COLUMN_ID, HIDDEN_COLUMN , IDENTITY_COLUMN, COLLATION"
                + " FROM ALL_TAB_COLS WHERE OWNER = ? AND TABLE_NAME IN (" + quote(tableNames) + ") ORDER BY COLUMN_ID";
    }
    
    private String getPrimaryKeyConstraintsSQL(final Collection<String> tableNames) {
        return "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, CONSTRAINT_NAME FROM ALL_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'P' AND OWNER = ? AND TABLE_NAME IN ("
                + quote(tableNames) + ")";
    }
    
    private String getPrimaryKeyColumnsSQL(final Collection<String> tableNames, final Collection<String> constraintNames) {
        return "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME FROM ALL_CONS_COLUMNS WHERE OWNER = ? AND TABLE_NAME IN (" + quote(tableNames)
                + ") AND CONSTRAINT_NAME IN (" + String.join(",", Collections.nCopies(constraintNames.size(), "?")) + ")";
    }
    
    private String quote(final Collection<String> values) {
        return values.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(","));
    }
    
    private String getTableMetaDataSQL(final int majorVersion, final int minorVersion) {
        if (majorVersion > 12 || majorVersion == 12 && minorVersion >= 2) {
            return ALL_TAB_COLUMNS_SQL_WITH_IDENTITY_AND_COLLATION;
        }
        if (majorVersion == 12 && minorVersion == 1) {
            return ALL_TAB_COLUMNS_SQL_WITH_IDENTITY;
        }
        return ALL_TAB_COLUMNS_SQL_WITHOUT_IDENTITY_AND_COLLATION;
    }
    
    private ColumnMetaData getExpectedFirstColumnMetaData(final int majorVersion, final int minorVersion, final boolean withPrimaryKey) {
        boolean generated = majorVersion > 12 || majorVersion == 12 && minorVersion >= 1;
        return new ColumnMetaData("id", Types.NUMERIC, withPrimaryKey, generated, false, true, false, false);
    }
    
    private Collection<SchemaMetaData> loadMetaData(final DataSource dataSource) throws SQLException {
        return loadMetaData(dataSource, Collections.singleton("tbl"));
    }
    
    private Collection<SchemaMetaData> loadMetaData(final DataSource dataSource, final Collection<String> tableNames) throws SQLException {
        DataTypeRegistry.load(dataSource, "Oracle");
        return dialectMetaDataLoader.load(new MetaDataLoaderMaterial(tableNames, "foo_ds", dataSource, databaseType, "sharding_db"));
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
    
    private void assertPrimaryKeys(final Collection<TableMetaData> actualTables, final Collection<String> primaryKeyTableNames,
                                   final Collection<String> primaryKeyColumnNames) {
        List<String> tables = new ArrayList<>(primaryKeyTableNames);
        List<String> columns = new ArrayList<>(primaryKeyColumnNames);
        Collection<String> expectedPrimaryKeys = new LinkedList<>();
        for (int i = 0; i < columns.size(); i++) {
            expectedPrimaryKeys.add(tables.get(i) + "." + columns.get(i));
        }
        for (TableMetaData each : actualTables) {
            for (ColumnMetaData column : each.getColumns()) {
                boolean expectedPrimaryKey = expectedPrimaryKeys.contains(each.getName() + "." + column.getName());
                assertThat(column.isPrimaryKey(), is(expectedPrimaryKey));
            }
        }
    }
    
    private static Stream<Arguments> assertLoadArguments() {
        return Stream.of(
                Arguments.of("major12Minor2WithoutPrimaryKey", 12, 2, false, "BINARY", true),
                Arguments.of("major12Minor1WithoutPrimaryKey", 12, 1, false, NO_COLLATION, true),
                Arguments.of("major11Minor2WithoutPrimaryKey", 11, 2, false, NO_COLLATION, true),
                Arguments.of("major12Minor0WithoutPrimaryKey", 12, 0, false, NO_COLLATION, true),
                Arguments.of("major12Minor2WithPrimaryKey", 12, 2, true, "BINARY", true),
                Arguments.of("major12Minor1WithPrimaryKey", 12, 1, true, NO_COLLATION, true),
                Arguments.of("major11Minor2WithPrimaryKey", 11, 2, true, NO_COLLATION, true),
                Arguments.of("major19Minor0WithPrimaryKey", 19, 0, true, "BINARY", true),
                Arguments.of("major12Minor2WithNullCollation", 12, 2, true, NO_COLLATION, false),
                Arguments.of("major12Minor2WithNamedCollation", 12, 2, true, "FRENCH", true),
                Arguments.of("major12Minor2WithCaseInsensitiveCollation", 12, 2, true, "BINARY_CI", false),
                Arguments.of("major12Minor2WithAccentInsensitiveCollation", 12, 2, true, "BINARY_AI", false),
                Arguments.of("major12Minor2WithUCAPrimaryStrength", 12, 2, true, "UCA0700_DUCET_S1", false),
                Arguments.of("major12Minor2WithUCASecondaryStrength", 12, 2, true, "UCA0700_DUCET_S2", false),
                Arguments.of("major12Minor2WithUCATertiaryStrength", 12, 2, true, "UCA0700_DUCET_S3", true),
                Arguments.of("major12Minor2WithUCAQuaternaryStrength", 12, 2, true, "UCA0700_DUCET_S4", true),
                Arguments.of("major12Minor2UsingNLSSortCaseSensitive", 12, 2, true, "USING_NLS_SORT_CS", true),
                Arguments.of("major12Minor2UsingNLSSortCaseInsensitive", 12, 2, true, "USING_NLS_SORT_CI", false),
                Arguments.of("major12Minor2UsingNLSSortAccentInsensitive", 12, 2, true, "USING_NLS_SORT_AI", false),
                Arguments.of("major12Minor2UsingNLSComp", 12, 2, true, "USING_NLS_COMP", false),
                Arguments.of("major12Minor2UsingNLSSort", 12, 2, true, "USING_NLS_SORT", false));
    }
    
    private static Stream<Arguments> assertLoadPrimaryKeysArguments() {
        return Stream.of(
                Arguments.of("quotedPrimaryKey", Collections.singletonList("tbl"), Collections.singletonList("PK'O"), Collections.singletonList("tbl"), Collections.singletonList("id")),
                Arguments.of("compositePrimaryKey", Collections.singletonList("tbl"), Collections.singletonList("foo_pk"), Arrays.asList("tbl", "tbl"), Arrays.asList("id", "tenant_id")),
                Arguments.of("differentPrimaryKeys", Arrays.asList("foo_tbl", "bar_tbl"), Arrays.asList("foo_pk", "bar_pk"),
                        Arrays.asList("foo_tbl", "bar_tbl"), Arrays.asList("id", "tenant_id")),
                Arguments.of("mixedPrimaryKeys", Arrays.asList("foo_tbl", "bar_tbl"), Collections.singletonList("foo_pk"), Collections.singletonList("foo_tbl"), Collections.singletonList("id")),
                Arguments.of("missingPrimaryKeyColumns", Collections.singletonList("tbl"), Collections.singletonList("foo_pk"), Collections.emptyList(), Collections.emptyList()));
    }
}
