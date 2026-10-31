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

package org.apache.shardingsphere.database.connector.postgresql.metadata.data.loader;

import org.apache.shardingsphere.database.connector.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ConstraintMetaData;
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
import org.mockito.ArgumentCaptor;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostgreSQLMetaDataLoaderTest {
    
    private static final String BASIC_TABLE_META_DATA_SQL = "SELECT table_name, column_name, ordinal_position, data_type, udt_name, column_default, table_schema, is_nullable, is_identity"
            + " FROM information_schema.columns WHERE table_schema IN ('public')";
    
    private static final String TABLE_META_DATA_SQL_WITHOUT_TABLES = BASIC_TABLE_META_DATA_SQL + " ORDER BY ordinal_position";
    
    private static final String TABLE_META_DATA_SQL_WITH_TABLES = BASIC_TABLE_META_DATA_SQL + " AND table_name IN ('tbl') ORDER BY ordinal_position";
    
    private static final String PRIMARY_KEY_META_DATA_SQL = "SELECT tc.table_name, kc.column_name, kc.table_schema FROM information_schema.table_constraints tc"
            + " JOIN information_schema.key_column_usage kc ON kc.table_schema = tc.table_schema AND kc.table_name = tc.table_name AND kc.constraint_name = tc.constraint_name"
            + " WHERE tc.constraint_type = 'PRIMARY KEY' AND kc.ordinal_position IS NOT NULL AND kc.table_schema IN ('public')";
    
    private static final String BASIC_INDEX_META_DATA_SQL = "SELECT tablename, indexname, schemaname FROM pg_indexes WHERE schemaname IN ('public')";
    
    private static final String ADVANCE_INDEX_META_DATA_SQL =
            "SELECT idx.relname as index_name, insp.nspname as index_schema, tbl.relname as table_name, att.attname AS column_name, pgi.indisunique as is_unique"
                    + " FROM pg_index pgi JOIN pg_class idx ON idx.oid = pgi.indexrelid JOIN pg_namespace insp ON insp.oid = idx.relnamespace JOIN pg_class tbl ON tbl.oid = pgi.indrelid"
                    + " JOIN pg_namespace tnsp ON tnsp.oid = tbl.relnamespace JOIN generate_subscripts(pgi.indkey, 1) AS index_position(position) ON TRUE"
                    + " JOIN pg_attribute att ON att.attrelid = tbl.oid AND att.attnum = pgi.indkey[index_position.position] WHERE tnsp.nspname IN ('public')"
                    + " ORDER BY insp.nspname, idx.relname, index_position.position";
    
    private static final String BASIC_CONSTRAINT_META_DATA_SQL =
            "SELECT tnsp.nspname AS table_schema, tbl.relname AS table_name, pgc.conname AS constraint_name, ref.relname AS refer_table_name"
                    + " FROM pg_constraint pgc JOIN pg_class tbl ON tbl.oid = pgc.conrelid JOIN pg_namespace tnsp ON tnsp.oid = tbl.relnamespace"
                    + " JOIN pg_class ref ON ref.oid = pgc.confrelid WHERE pgc.contype = 'f' AND tnsp.nspname IN ('public')";
    
    private static final String LOAD_ALL_ROLE_TABLE_GRANTS_SQL = "SELECT table_schema, table_name FROM information_schema.role_table_grants";
    
    private static final String LOAD_FILTERED_ROLE_TABLE_GRANTS_SQL = LOAD_ALL_ROLE_TABLE_GRANTS_SQL + " WHERE table_name IN ('tbl')";
    
    private static final String VIEW_META_DATA_SQL_WITHOUT_TABLES = "SELECT table_schema, table_name FROM information_schema.views WHERE table_schema IN ('public') and table_name IN ()";
    
    private static final String VIEW_META_DATA_SQL_WITH_TABLES = "SELECT table_schema, table_name FROM information_schema.views WHERE table_schema IN ('public') and table_name IN ('tbl')";
    
    private static final String ZERO_COLUMN_TABLE_META_DATA_SQL = "SELECT n.nspname AS table_schema, c.relname AS table_name FROM pg_catalog.pg_class c"
            + " JOIN pg_catalog.pg_namespace n ON c.relnamespace = n.oid WHERE (n.nspname, c.relname) IN (SELECT * FROM unnest(?::text[], ?::text[]))"
            + " AND NOT EXISTS (SELECT 1 FROM pg_catalog.pg_attribute a WHERE a.attrelid = c.oid AND a.attnum > 0 AND NOT a.attisdropped)";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final DialectMetaDataLoader metaDataLoader = DatabaseTypedSPILoader.getService(DialectMetaDataLoader.class, databaseType);
    
    @SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed", "resource"})
    @ParameterizedTest(name = "{0}")
    @MethodSource("loadArguments")
    void assertLoad(final String name, final Collection<String> actualTableNames, final String tableMetaDataSQL, final String roleTableGrantsSQL, final String grantSchemaName,
                    final String viewMetaDataSQL, final boolean hasView, final String idColumnDefault, final String idColumnIdentity, final String secondColumnDefault) throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet schemaResultSet = mockSchemaMetaDataResultSet();
        when(dataSource.getConnection().getMetaData().getSchemas()).thenReturn(schemaResultSet);
        ResultSet tableResultSet = mockTableMetaDataResultSet(idColumnDefault, idColumnIdentity, secondColumnDefault);
        when(dataSource.getConnection().prepareStatement(tableMetaDataSQL).executeQuery()).thenReturn(tableResultSet);
        ResultSet primaryKeyResultSet = mockPrimaryKeyMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(PRIMARY_KEY_META_DATA_SQL).executeQuery()).thenReturn(primaryKeyResultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(BASIC_INDEX_META_DATA_SQL).executeQuery()).thenReturn(indexResultSet);
        ResultSet advanceIndexResultSet = mockAdvanceIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(ADVANCE_INDEX_META_DATA_SQL).executeQuery()).thenReturn(advanceIndexResultSet);
        ResultSet constraintResultSet = mockConstraintMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(BASIC_CONSTRAINT_META_DATA_SQL).executeQuery()).thenReturn(constraintResultSet);
        ResultSet roleTableGrantsResultSet = mockRoleTableGrantsResultSet(grantSchemaName);
        when(dataSource.getConnection().prepareStatement(roleTableGrantsSQL).executeQuery()).thenReturn(roleTableGrantsResultSet);
        ResultSet viewResultSet = mockViewMetaDataResultSet(hasView);
        when(dataSource.getConnection().prepareStatement(viewMetaDataSQL).executeQuery()).thenReturn(viewResultSet);
        DataTypeRegistry.load(dataSource, "PostgreSQL");
        clearInvocations(dataSource.getConnection());
        assertTableMetaDataMap(metaDataLoader.load(new MetaDataLoaderMaterial(actualTableNames, "foo_ds", dataSource, databaseType, "sharding_db")));
        verify(dataSource.getConnection(), times(7)).prepareStatement(anyString());
    }
    
    @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
    private DataSource mockDataSource() throws SQLException {
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        ResultSet typeInfoResultSet = mockTypeInfoResultSet();
        when(result.getConnection().getMetaData().getTypeInfo()).thenReturn(typeInfoResultSet);
        return result;
    }
    
    private ResultSet mockSchemaMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("TABLE_SCHEM")).thenReturn("public");
        return result;
    }
    
    private ResultSet mockTypeInfoResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("TYPE_NAME")).thenReturn("int4", "varchar");
        when(result.getInt("DATA_TYPE")).thenReturn(Types.INTEGER, Types.VARCHAR);
        return result;
    }
    
    private ResultSet mockTableMetaDataResultSet(final String idColumnDefault, final String idColumnIdentity, final String secondColumnDefault) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, false);
        when(result.getString("table_name")).thenReturn("ignored_tbl", "tbl", "tbl");
        when(result.getString("column_name")).thenReturn("id", "name");
        when(result.getInt("ordinal_position")).thenReturn(1, 2, 3);
        when(result.getString("data_type")).thenReturn("integer", "character varying");
        when(result.getString("udt_name")).thenReturn("int4", "varchar");
        when(result.getString("column_default")).thenReturn(idColumnDefault, secondColumnDefault);
        when(result.getString("table_schema")).thenReturn("public", "public");
        when(result.getString("is_nullable")).thenReturn("NO", "YES");
        when(result.getString("is_identity")).thenReturn(idColumnIdentity, "NO");
        return result;
    }
    
    private ResultSet mockPrimaryKeyMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("table_name")).thenReturn("tbl");
        when(result.getString("column_name")).thenReturn("id");
        when(result.getString("table_schema")).thenReturn("public");
        return result;
    }
    
    private ResultSet mockIndexMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("tablename")).thenReturn("tbl");
        when(result.getString("indexname")).thenReturn("id");
        when(result.getString("schemaname")).thenReturn("public");
        return result;
    }
    
    private ResultSet mockAdvanceIndexMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, true, false);
        when(result.getString("table_name")).thenReturn("tbl", "tbl", "tbl", "tbl");
        when(result.getString("column_name")).thenReturn("id", "id", "name", "id");
        when(result.getString("index_name")).thenReturn("missing_index", "not_matched", "id", "id");
        when(result.getString("index_schema")).thenReturn("ignored_schema", "public", "public", "public");
        when(result.getBoolean("is_unique")).thenReturn(false, true, true, true);
        return result;
    }
    
    private ResultSet mockConstraintMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("table_schema")).thenReturn("public");
        when(result.getString("table_name")).thenReturn("tbl");
        when(result.getString("constraint_name")).thenReturn("tbl_con");
        when(result.getString("refer_table_name")).thenReturn("refer_tbl");
        return result;
    }
    
    private ResultSet mockRoleTableGrantsResultSet(final String schemaName) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("table_schema")).thenReturn(schemaName);
        when(result.getString("table_name")).thenReturn("tbl");
        return result;
    }
    
    private ResultSet mockViewMetaDataResultSet(final boolean hasView) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(hasView, false);
        when(result.getString("table_schema")).thenReturn("public");
        when(result.getString("table_name")).thenReturn("tbl");
        return result;
    }
    
    private void assertTableMetaDataMap(final Collection<SchemaMetaData> schemaMetaDataList) {
        assertThat(schemaMetaDataList.size(), is(1));
        TableMetaData actualTableMetaData = schemaMetaDataList.iterator().next().getTables().iterator().next();
        assertThat(actualTableMetaData.getColumns().size(), is(2));
        Iterator<ColumnMetaData> columnsIterator = actualTableMetaData.getColumns().iterator();
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("id", Types.INTEGER, true, true, true, true, false, false));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("name", Types.VARCHAR, false, false, true, true, false, true));
        assertThat(actualTableMetaData.getIndexes().size(), is(1));
        Iterator<IndexMetaData> indexesIterator = actualTableMetaData.getIndexes().iterator();
        IndexMetaData indexMetaData = new IndexMetaData("id", Arrays.asList("name", "id"));
        indexMetaData.setUnique(true);
        assertIndexMetaData(indexesIterator.next(), indexMetaData);
        assertThat(actualTableMetaData.getConstraints().size(), is(1));
        Iterator<ConstraintMetaData> constrainsMetaDataList = actualTableMetaData.getConstraints().iterator();
        assertConstraintMetaData(constrainsMetaDataList.next(), new ConstraintMetaData("tbl_con", "refer_tbl"));
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
    
    private void assertConstraintMetaData(final ConstraintMetaData actual, final ConstraintMetaData expected) {
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getReferencedTableName(), is(expected.getReferencedTableName()));
    }
    
    private static Stream<Arguments> loadArguments() {
        return Stream.of(
                Arguments.of("without tables", Collections.emptyList(), TABLE_META_DATA_SQL_WITHOUT_TABLES, LOAD_ALL_ROLE_TABLE_GRANTS_SQL, "public", VIEW_META_DATA_SQL_WITHOUT_TABLES,
                        false, "nextval('id_seq'::regclass)", "NO", null),
                Arguments.of("with identity and view table", Collections.singletonList("tbl"), TABLE_META_DATA_SQL_WITH_TABLES, LOAD_FILTERED_ROLE_TABLE_GRANTS_SQL,
                        "public", VIEW_META_DATA_SQL_WITH_TABLES, true, null, "YES", null),
                Arguments.of("with non generated default", Collections.singletonList("tbl"), TABLE_META_DATA_SQL_WITH_TABLES, LOAD_FILTERED_ROLE_TABLE_GRANTS_SQL,
                        "public", VIEW_META_DATA_SQL_WITH_TABLES, false, "nextval('id_seq'::regclass)", "NO", "'anonymous'"),
                Arguments.of("readable columns with grant in another schema", Collections.singletonList("tbl"), TABLE_META_DATA_SQL_WITH_TABLES, LOAD_FILTERED_ROLE_TABLE_GRANTS_SQL,
                        "ignored_schema", VIEW_META_DATA_SQL_WITH_TABLES, false, "nextval('id_seq'::regclass)", "NO", null));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("zeroColumnTableArguments")
    void assertLoadZeroColumnTable(final String name, final String tableName, final Collection<String> actualTableNames, final String roleTableGrantsSQL) throws SQLException {
        DataSource dataSource = mockGrantedTableDataSource("public", tableName, roleTableGrantsSQL);
        PreparedStatement statement = mock(PreparedStatement.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().prepareStatement(ZERO_COLUMN_TABLE_META_DATA_SQL)).thenReturn(statement);
        when(statement.executeQuery().next()).thenReturn(true, false);
        when(statement.executeQuery().getString("table_schema")).thenReturn("public");
        when(statement.executeQuery().getString("table_name")).thenReturn(tableName);
        clearInvocations(dataSource.getConnection(), statement);
        Collection<SchemaMetaData> actual = metaDataLoader.load(new MetaDataLoaderMaterial(actualTableNames, "foo_ds", dataSource, databaseType, "public"));
        assertThat(actual.size(), is(1));
        SchemaMetaData actualSchema = actual.iterator().next();
        assertThat(actualSchema.getName(), is("public"));
        assertThat(actualSchema.getTables().size(), is(1));
        TableMetaData actualTable = actualSchema.getTables().iterator().next();
        assertThat(actualTable.getName(), is(tableName));
        assertTrue(actualTable.getColumns().isEmpty());
        assertTrue(actualTable.getIndexes().isEmpty());
        assertTrue(actualTable.getConstraints().isEmpty());
        verify(statement).setObject(1, new String[]{"public"});
        verify(statement).setObject(2, new String[]{tableName});
        verify(statement).executeQuery();
        verify(dataSource.getConnection(), times(8)).prepareStatement(anyString());
    }
    
    private DataSource mockGrantedTableDataSource(final String schemaName, final String tableName, final String roleTableGrantsSQL) throws SQLException {
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        ResultSet schemaResultSet = mockSchemaMetaDataResultSet();
        when(result.getConnection().getMetaData().getSchemas()).thenReturn(schemaResultSet);
        ResultSet roleTableGrants = mock(ResultSet.class);
        when(roleTableGrants.next()).thenReturn(true, false);
        when(roleTableGrants.getString("table_schema")).thenReturn(schemaName);
        when(roleTableGrants.getString("table_name")).thenReturn(tableName);
        when(result.getConnection().prepareStatement(roleTableGrantsSQL).executeQuery()).thenReturn(roleTableGrants);
        return result;
    }
    
    private static Stream<Arguments> zeroColumnTableArguments() {
        return Stream.of(
                Arguments.of("pg prefix", "pg_mytable", Collections.emptyList(), LOAD_ALL_ROLE_TABLE_GRANTS_SQL),
                Arguments.of("ordinary name", "foo_zero", Collections.emptyList(), LOAD_ALL_ROLE_TABLE_GRANTS_SQL),
                Arguments.of("requested table", "tbl", Collections.singletonList("tbl"), LOAD_FILTERED_ROLE_TABLE_GRANTS_SQL));
    }
    
    @Test
    void assertLoadUnverifiedZeroColumnCandidate() throws SQLException {
        DataSource dataSource = mockGrantedTableDataSource("public", "tbl", LOAD_FILTERED_ROLE_TABLE_GRANTS_SQL);
        PreparedStatement statement = mock(PreparedStatement.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().prepareStatement(ZERO_COLUMN_TABLE_META_DATA_SQL)).thenReturn(statement);
        clearInvocations(dataSource.getConnection(), statement);
        Collection<SchemaMetaData> actual = metaDataLoader.load(new MetaDataLoaderMaterial(Collections.singletonList("tbl"), "foo_ds", dataSource, databaseType, "public"));
        assertThat(actual.size(), is(1));
        assertTrue(actual.iterator().next().getTables().isEmpty());
        verify(statement).executeQuery();
        verify(dataSource.getConnection(), times(8)).prepareStatement(anyString());
    }
    
    @Test
    void assertLoadGrantOutsideSchemaScope() throws SQLException {
        DataSource dataSource = mockGrantedTableDataSource("ignored_schema", "tbl", LOAD_FILTERED_ROLE_TABLE_GRANTS_SQL);
        clearInvocations(dataSource.getConnection());
        Collection<SchemaMetaData> actual = metaDataLoader.load(new MetaDataLoaderMaterial(Collections.singletonList("tbl"), "foo_ds", dataSource, databaseType, "public"));
        assertThat(actual.size(), is(1));
        assertTrue(actual.iterator().next().getTables().isEmpty());
        verify(dataSource.getConnection(), times(7)).prepareStatement(anyString());
    }
    
    @Test
    void assertLoadWithoutGrantedTables() throws SQLException {
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        ResultSet schemaResultSet = mockSchemaMetaDataResultSet();
        when(dataSource.getConnection().getMetaData().getSchemas()).thenReturn(schemaResultSet);
        clearInvocations(dataSource.getConnection());
        Collection<SchemaMetaData> actual = metaDataLoader.load(new MetaDataLoaderMaterial(Collections.singletonList("tbl"), "foo_ds", dataSource, databaseType, "public"));
        assertThat(actual.size(), is(1));
        assertTrue(actual.iterator().next().getTables().isEmpty());
        verify(dataSource.getConnection(), times(7)).prepareStatement(anyString());
    }
    
    @Test
    void assertLoadZeroColumnCandidatesInOneBatch() throws SQLException {
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        ResultSet schemaResultSet = mockSchemaMetaDataResultSet();
        when(dataSource.getConnection().getMetaData().getSchemas()).thenReturn(schemaResultSet);
        ResultSet roleTableGrants = mock(ResultSet.class);
        when(roleTableGrants.next()).thenReturn(true, true, true, false);
        when(roleTableGrants.getString("table_schema")).thenReturn("public");
        when(roleTableGrants.getString("table_name")).thenReturn("pg_mytable", "pg_mytable", "foo_zero");
        when(dataSource.getConnection().prepareStatement(LOAD_ALL_ROLE_TABLE_GRANTS_SQL).executeQuery()).thenReturn(roleTableGrants);
        PreparedStatement statement = mock(PreparedStatement.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().prepareStatement(ZERO_COLUMN_TABLE_META_DATA_SQL)).thenReturn(statement);
        when(statement.executeQuery().next()).thenReturn(true, true, false);
        when(statement.executeQuery().getString("table_schema")).thenReturn("public");
        when(statement.executeQuery().getString("table_name")).thenReturn("foo_zero", "pg_mytable");
        clearInvocations(dataSource.getConnection(), statement);
        Collection<SchemaMetaData> actual = metaDataLoader.load(new MetaDataLoaderMaterial(Collections.emptyList(), "foo_ds", dataSource, databaseType, "public"));
        assertThat(actual.size(), is(1));
        Collection<TableMetaData> actualTables = actual.iterator().next().getTables();
        assertThat(actualTables.size(), is(2));
        Iterator<TableMetaData> actualTablesIterator = actualTables.iterator();
        assertThat(actualTablesIterator.next().getName(), is("pg_mytable"));
        assertThat(actualTablesIterator.next().getName(), is("foo_zero"));
        for (TableMetaData each : actualTables) {
            assertTrue(each.getColumns().isEmpty());
        }
        verify(statement).setObject(1, new String[]{"public", "public"});
        verify(statement).setObject(2, new String[]{"pg_mytable", "foo_zero"});
        verify(statement).executeQuery();
        verify(dataSource.getConnection(), times(8)).prepareStatement(anyString());
    }
    
    @Test
    void assertLoadZeroColumnCandidatesAcrossSchemas() throws SQLException {
        DataSource dataSource = mockCrossSchemaDataSource();
        PreparedStatement statement = mock(PreparedStatement.class, RETURNS_DEEP_STUBS);
        when(statement.executeQuery().next()).thenReturn(true, true, false);
        when(statement.executeQuery().getString("table_schema")).thenReturn("foo_schema_0", "foo_schema_32");
        when(statement.executeQuery().getString("table_name")).thenReturn("foo_0", "foo_998");
        when(dataSource.getConnection().prepareStatement(contains("SELECT n.nspname AS table_schema, c.relname AS table_name"))).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.chars().filter(each -> '?' == each).count() > 65535) {
                throw new SQLException("Too many bind parameters");
            }
            return statement;
        });
        Collection<String> tableNames = IntStream.range(0, 999).mapToObj(each -> "foo_" + each).collect(Collectors.toList());
        tableNames.add("tbl");
        clearInvocations(dataSource.getConnection(), statement);
        Collection<SchemaMetaData> actual = metaDataLoader.load(new MetaDataLoaderMaterial(tableNames, "foo_ds", dataSource, databaseType, "foo_schema_0"));
        assertThat(actual.size(), is(33));
        assertThat(actual.stream().mapToInt(each -> each.getTables().size()).sum(), is(3));
        Map<String, SchemaMetaData> actualSchemas = actual.stream().collect(Collectors.toMap(SchemaMetaData::getName, each -> each));
        TableMetaData actualFirstTable = actualSchemas.get("foo_schema_0").getTables().iterator().next();
        assertThat(actualFirstTable.getName(), is("foo_0"));
        assertTrue(actualFirstTable.getColumns().isEmpty());
        TableMetaData actualLastTable = actualSchemas.get("foo_schema_32").getTables().iterator().next();
        assertThat(actualLastTable.getName(), is("foo_998"));
        assertTrue(actualLastTable.getColumns().isEmpty());
        TableMetaData actualOrdinaryTable = actualSchemas.get("foo_schema_1").getTables().iterator().next();
        assertThat(actualOrdinaryTable.getName(), is("tbl"));
        assertThat(actualOrdinaryTable.getColumns().size(), is(1));
        assertThat(actualOrdinaryTable.getColumns().iterator().next().getName(), is("id"));
        ArgumentCaptor<Integer> indices = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String[]> params = ArgumentCaptor.forClass(String[].class);
        verify(statement, times(2)).setObject(indices.capture(), params.capture());
        assertThat(indices.getAllValues(), is(Arrays.asList(1, 2)));
        String[] actualSchemaNames = params.getAllValues().get(0);
        String[] actualTableNames = params.getAllValues().get(1);
        assertThat(actualSchemaNames.length, is(32967));
        assertThat(actualTableNames.length, is(32967));
        Set<String> expectedTables = IntStream.range(0, 32967).mapToObj(each -> "foo_schema_" + each / 999 + ".foo_" + each % 999).collect(Collectors.toCollection(() -> new HashSet<>(32967, 1F)));
        for (int i = 0; i < actualTableNames.length; i++) {
            assertTrue(expectedTables.remove(actualSchemaNames[i] + "." + actualTableNames[i]));
        }
        assertTrue(expectedTables.isEmpty());
        verify(statement).executeQuery();
        verify(dataSource.getConnection(), times(8)).prepareStatement(anyString());
    }
    
    private DataSource mockCrossSchemaDataSource() throws SQLException {
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        Iterator<String> schemaNames = IntStream.range(0, 33).mapToObj(each -> "foo_schema_" + each).iterator();
        ResultSet schemaResultSet = mock(ResultSet.class);
        when(schemaResultSet.next()).thenAnswer(invocation -> schemaNames.hasNext());
        when(schemaResultSet.getString("TABLE_SCHEM")).thenAnswer(invocation -> schemaNames.next());
        when(result.getConnection().getMetaData().getSchemas()).thenReturn(schemaResultSet);
        int[] grantIndex = {-1};
        ResultSet roleTableGrants = mock(ResultSet.class);
        when(roleTableGrants.next()).thenAnswer(invocation -> ++grantIndex[0] < 32968);
        when(roleTableGrants.getString("table_schema")).thenAnswer(invocation -> "foo_schema_" + (32967 == grantIndex[0] ? 1 : grantIndex[0] / 999));
        when(roleTableGrants.getString("table_name")).thenAnswer(invocation -> 32967 == grantIndex[0] ? "tbl" : "foo_" + grantIndex[0] % 999);
        when(result.getConnection().prepareStatement(contains(LOAD_ALL_ROLE_TABLE_GRANTS_SQL)).executeQuery()).thenReturn(roleTableGrants);
        ResultSet columnResultSet = mock(ResultSet.class);
        when(columnResultSet.next()).thenReturn(true, false);
        when(columnResultSet.getString("table_schema")).thenReturn("foo_schema_1");
        when(columnResultSet.getString("table_name")).thenReturn("tbl");
        when(columnResultSet.getString("column_name")).thenReturn("id");
        when(columnResultSet.getString("udt_name")).thenReturn("int4");
        when(result.getConnection().prepareStatement(contains("SELECT table_name, column_name, ordinal_position")).executeQuery()).thenReturn(columnResultSet);
        return result;
    }
}
