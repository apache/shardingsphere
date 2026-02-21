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

package org.apache.shardingsphere.database.connector.mysql.metadata.data.loader;

import org.apache.shardingsphere.database.connector.core.GlobalDataSourceRegistry;
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

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLMetaDataLoaderTest {
    
    private static final String TABLE_METADATA_SQL = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_KEY, EXTRA, COLLATION_NAME, ORDINAL_POSITION, COLUMN_TYPE, "
            + "IS_NULLABLE FROM information_schema.columns WHERE TABLE_SCHEMA=? ORDER BY ORDINAL_POSITION";
    
    private static final String TABLE_METADATA_SQL_WITH_TABLE = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_KEY, EXTRA, COLLATION_NAME, ORDINAL_POSITION, "
            + "COLUMN_TYPE, IS_NULLABLE FROM information_schema.columns WHERE TABLE_SCHEMA=? AND TABLE_NAME IN ('tbl') ORDER BY ORDINAL_POSITION";
    
    private static final String INDEX_METADATA_SQL = "SELECT TABLE_NAME, INDEX_NAME, NON_UNIQUE, COLUMN_NAME FROM information_schema.statistics "
            + "WHERE TABLE_SCHEMA=? and TABLE_NAME IN ('tbl') ORDER BY NON_UNIQUE, INDEX_NAME, SEQ_IN_INDEX";
    
    private static final String CONSTRAINT_METADATA_SQL = "SELECT CONSTRAINT_NAME, TABLE_NAME, REFERENCED_TABLE_NAME FROM information_schema.KEY_COLUMN_USAGE "
            + "WHERE TABLE_NAME IN ('tbl') AND REFERENCED_TABLE_SCHEMA IS NOT NULL";
    
    private static final String VIEW_METADATA_SQL = "SELECT TABLE_NAME FROM information_schema.VIEWS WHERE TABLE_SCHEMA=? AND TABLE_NAME IN ('tbl')";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final DialectMetaDataLoader dialectMetaDataLoader = DatabaseTypedSPILoader.getService(DialectMetaDataLoader.class, databaseType);
    
    @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
    @Test
    void assertLoadWithEmptyColumnMetaData() throws SQLException {
        DataSource dataSource = mockDataSource();
        when(dataSource.getConnection().getCatalog()).thenReturn("sharding_db");
        DataTypeRegistry.load(dataSource, "MySQL");
        Collection<SchemaMetaData> actual = dialectMetaDataLoader.load(new MetaDataLoaderMaterial(Collections.singletonList("tbl"), "foo_ds", dataSource, databaseType, "sharding_db"));
        assertThat(actual.size(), is(1));
        assertTrue(actual.iterator().next().getTables().isEmpty());
    }
    
    @SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed", "resource"})
    @ParameterizedTest(name = "{0}")
    @MethodSource("loadArguments")
    void assertLoad(final String name, final Collection<String> actualTableNames, final String tableMetaDataSQL,
                    final boolean emptyCatalog, final boolean includeView, final boolean includeConstraints, final boolean includeCompositeIndex) throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet tableMetaDataResultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(tableMetaDataSQL).executeQuery()).thenReturn(tableMetaDataResultSet);
        if (includeCompositeIndex) {
            ResultSet compositeIndexMetaDataResultSet = mockCompositeIndexMetaDataResultSet();
            when(dataSource.getConnection().prepareStatement(INDEX_METADATA_SQL).executeQuery()).thenReturn(compositeIndexMetaDataResultSet);
        } else {
            ResultSet simpleIndexMetaDataResultSet = mockSimpleIndexMetaDataResultSet();
            when(dataSource.getConnection().prepareStatement(INDEX_METADATA_SQL).executeQuery()).thenReturn(simpleIndexMetaDataResultSet);
        }
        ResultSet viewResultSet = includeView ? mockSingleViewResultSet() : mock(ResultSet.class);
        when(dataSource.getConnection().prepareStatement(VIEW_METADATA_SQL).executeQuery()).thenReturn(viewResultSet);
        ResultSet constraintMetaDataResultSet = includeConstraints ? mockConstraintMetaDataResultSet() : mock(ResultSet.class);
        when(dataSource.getConnection().prepareStatement(CONSTRAINT_METADATA_SQL).executeQuery()).thenReturn(constraintMetaDataResultSet);
        Map<String, String> cachedDatabaseTables = GlobalDataSourceRegistry.getInstance().getCachedDatabaseTables();
        String previous = cachedDatabaseTables.put("tbl", "fallback_db");
        try {
            when(dataSource.getConnection().getCatalog()).thenReturn(emptyCatalog ? "" : "sharding_db");
            DataTypeRegistry.load(dataSource, "MySQL");
            Collection<SchemaMetaData> actual = dialectMetaDataLoader.load(new MetaDataLoaderMaterial(actualTableNames, "foo_ds", dataSource, databaseType, "sharding_db"));
            assertTableMetaData(actual, includeConstraints, includeCompositeIndex);
        } finally {
            if (null == previous) {
                cachedDatabaseTables.remove("tbl");
            } else {
                cachedDatabaseTables.put("tbl", previous);
            }
        }
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
        when(result.next()).thenReturn(true, true, true, true, false);
        when(result.getString("TYPE_NAME")).thenReturn("int", "varchar");
        when(result.getInt("DATA_TYPE")).thenReturn(Types.INTEGER, Types.VARCHAR);
        return result;
    }
    
    private ResultSet mockTableMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, true, true, true, true, true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        when(result.getString("COLUMN_NAME")).thenReturn("id", "name", "doc", "geo", "t_year", "pg", "mpg", "pt", "mpt");
        when(result.getString("DATA_TYPE")).thenReturn("int", "varchar", "json", "geometry", "year", "polygon", "multipolygon", "point", "multipoint");
        when(result.getString("COLUMN_KEY")).thenReturn("PRI", "", "", "", "", "", "", "", "");
        when(result.getString("EXTRA")).thenReturn("auto_increment", "INVISIBLE", "", "", "", "", "", "", "");
        when(result.getString("COLLATION_NAME")).thenReturn("utf8", "utf8_general_ci", null, null, null, null, null, null, null);
        when(result.getString("COLUMN_TYPE")).thenReturn("int", "varchar", "json", "geometry", "year", "polygon", "multipolygon", "point", "multipoint");
        when(result.getString("IS_NULLABLE")).thenReturn("NO", "YES", "YES", "YES", "YES", "YES", "YES", "YES", "YES");
        return result;
    }
    
    private ResultSet mockSimpleIndexMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("INDEX_NAME")).thenReturn("id");
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        when(result.getString("COLUMN_NAME")).thenReturn("id");
        when(result.getString("NON_UNIQUE")).thenReturn("0");
        return result;
    }
    
    private ResultSet mockCompositeIndexMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, false);
        when(result.getString("INDEX_NAME")).thenReturn("idx_composite", "idx_composite", "idx_ignored");
        when(result.getString("TABLE_NAME")).thenReturn("tbl", "tbl", "tbl");
        when(result.getString("COLUMN_NAME")).thenReturn("id", "name", "");
        when(result.getString("NON_UNIQUE")).thenReturn("0", "0", "1");
        return result;
    }
    
    private ResultSet mockConstraintMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("CONSTRAINT_NAME")).thenReturn("fk_order", "fk_item");
        when(result.getString("TABLE_NAME")).thenReturn("tbl", "tbl");
        when(result.getString("REFERENCED_TABLE_NAME")).thenReturn("t_order", "t_item");
        return result;
    }
    
    private ResultSet mockSingleViewResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString(1)).thenReturn("tbl");
        return result;
    }
    
    private void assertTableMetaData(final Collection<SchemaMetaData> schemaMetaDataList, final boolean expectedConstraintExists, final boolean expectedCompositeIndexExists) {
        assertThat(schemaMetaDataList.size(), is(1));
        Collection<TableMetaData> tables = schemaMetaDataList.iterator().next().getTables();
        assertThat(tables.size(), is(1));
        TableMetaData actualTableMetaData = tables.iterator().next();
        assertThat(actualTableMetaData.getName(), is("tbl"));
        assertThat(actualTableMetaData.getColumns().size(), is(9));
        Iterator<ColumnMetaData> columnsIterator = actualTableMetaData.getColumns().iterator();
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("id", Types.INTEGER, true, true, true, true, false, false));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("name", Types.VARCHAR, false, false, false, false, false, true));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("doc", Types.LONGVARCHAR, false, false, false, true, false, true));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("geo", Types.BINARY, false, false, false, true, false, true));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("t_year", Types.DATE, false, false, false, true, false, true));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("pg", Types.BINARY, false, false, false, true, false, true));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("mpg", Types.BINARY, false, false, false, true, false, true));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("pt", Types.BINARY, false, false, false, true, false, true));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("mpt", Types.BINARY, false, false, false, true, false, true));
        assertIndexMetaData(actualTableMetaData.getIndexes(), expectedCompositeIndexExists);
        assertConstraintMetaData(actualTableMetaData.getConstraints(), expectedConstraintExists);
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
    
    private void assertIndexMetaData(final Collection<IndexMetaData> actualIndexMetaData, final boolean expectedCompositeIndexExists) {
        assertThat(actualIndexMetaData.size(), is(1));
        IndexMetaData actualIndexMetaDataItem = actualIndexMetaData.iterator().next();
        assertThat(actualIndexMetaDataItem.getName(), is(expectedCompositeIndexExists ? "idx_composite" : "id"));
        assertThat(actualIndexMetaDataItem.getColumns(), is(expectedCompositeIndexExists ? Arrays.asList("id", "name") : Collections.singletonList("id")));
        assertTrue(actualIndexMetaDataItem.isUnique());
    }
    
    private void assertConstraintMetaData(final Collection<ConstraintMetaData> actualConstraintMetaData, final boolean expectedConstraintExists) {
        if (expectedConstraintExists) {
            assertThat(actualConstraintMetaData.size(), is(2));
            Iterator<ConstraintMetaData> constraints = actualConstraintMetaData.iterator();
            assertThat(constraints.next().getName(), is("fk_order"));
            assertThat(constraints.next().getName(), is("fk_item"));
        } else {
            assertTrue(actualConstraintMetaData.isEmpty());
        }
    }
    
    private static Stream<Arguments> loadArguments() {
        return Stream.of(
                Arguments.of("load without requested table names", Collections.emptyList(), TABLE_METADATA_SQL, false, false, false, false),
                Arguments.of("load with requested table names", Collections.singletonList("tbl"), TABLE_METADATA_SQL_WITH_TABLE, false, false, false, false),
                Arguments.of("load with empty catalog fallback and view constraints", Collections.singletonList("tbl"), TABLE_METADATA_SQL_WITH_TABLE, true, true, true, true));
    }
}
