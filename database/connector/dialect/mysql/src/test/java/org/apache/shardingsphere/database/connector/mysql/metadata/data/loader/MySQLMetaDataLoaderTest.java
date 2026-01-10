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

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLMetaDataLoaderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final DialectMetaDataLoader dialectMetaDataLoader = DatabaseTypedSPILoader.getService(DialectMetaDataLoader.class, databaseType);
    
    @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
    @Test
    void assertLoadWithoutTables() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement("SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_KEY, EXTRA, COLLATION_NAME, ORDINAL_POSITION, COLUMN_TYPE, IS_NULLABLE "
                + "FROM information_schema.columns WHERE TABLE_SCHEMA=? ORDER BY ORDINAL_POSITION")
                .executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement("SELECT TABLE_NAME, INDEX_NAME, NON_UNIQUE, COLUMN_NAME FROM information_schema.statistics "
                + "WHERE TABLE_SCHEMA=? and TABLE_NAME IN ('tbl') ORDER BY NON_UNIQUE, INDEX_NAME, SEQ_IN_INDEX").executeQuery()).thenReturn(indexResultSet);
        DataTypeRegistry.load(dataSource, "MySQL");
        assertTableMetaDataMap(dialectMetaDataLoader.load(new MetaDataLoaderMaterial(Collections.emptyList(), "foo_ds", dataSource, databaseType, "sharding_db")));
    }
    
    @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
    @Test
    void assertLoadWithTables() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement("SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_KEY, EXTRA, COLLATION_NAME, ORDINAL_POSITION, COLUMN_TYPE, IS_NULLABLE "
                + "FROM information_schema.columns WHERE TABLE_SCHEMA=? AND TABLE_NAME IN ('tbl') ORDER BY ORDINAL_POSITION")
                .executeQuery()).thenReturn(resultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement("SELECT TABLE_NAME, INDEX_NAME, NON_UNIQUE, COLUMN_NAME FROM information_schema.statistics WHERE TABLE_SCHEMA=? and TABLE_NAME IN ('tbl') "
                + "ORDER BY NON_UNIQUE, INDEX_NAME, SEQ_IN_INDEX")
                .executeQuery()).thenReturn(indexResultSet);
        DataTypeRegistry.load(dataSource, "MySQL");
        assertTableMetaDataMap(dialectMetaDataLoader.load(new MetaDataLoaderMaterial(Collections.singletonList("tbl"), "foo_ds", dataSource, databaseType, "sharding_db")));
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
        when(result.getString("COLLATION_NAME")).thenReturn("utf8", "utf8_general_ci");
        when(result.getString("COLUMN_TYPE")).thenReturn("int", "varchar");
        when(result.getString("IS_NULLABLE")).thenReturn("NO", "YES", "YES", "YES", "YES", "YES", "YES", "YES", "YES");
        return result;
    }
    
    private ResultSet mockIndexMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("INDEX_NAME")).thenReturn("id");
        when(result.getString("TABLE_NAME")).thenReturn("tbl");
        when(result.getString("COLUMN_NAME")).thenReturn("id");
        when(result.getString("NON_UNIQUE")).thenReturn("0");
        return result;
    }
    
    private void assertTableMetaDataMap(final Collection<SchemaMetaData> schemaMetaDataList) {
        assertThat(schemaMetaDataList.size(), is(1));
        TableMetaData actualTableMetaData = schemaMetaDataList.iterator().next().getTables().iterator().next();
        assertThat(actualTableMetaData.getColumns().size(), is(9));
        Iterator<ColumnMetaData> columnsIterator = actualTableMetaData.getColumns().iterator();
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("id", Types.INTEGER, true, true, "", true, true, false, false));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("name", Types.VARCHAR, false, false, "", false, false, false, true));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("doc", Types.LONGVARCHAR, false, false, "", false, true, false, true));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("geo", Types.BINARY, false, false, "", false, true, false, true));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("t_year", Types.DATE, false, false, "", false, true, false, true));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("pg", Types.BINARY, false, false, "", false, true, false, true));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("mpg", Types.BINARY, false, false, "", false, true, false, true));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("pt", Types.BINARY, false, false, "", false, true, false, true));
        assertColumnMetaData(columnsIterator.next(), new ColumnMetaData("mpt", Types.BINARY, false, false, "", false, true, false, true));
        assertThat(actualTableMetaData.getIndexes().size(), is(1));
        Iterator<IndexMetaData> indexesIterator = actualTableMetaData.getIndexes().iterator();
        IndexMetaData expected = new IndexMetaData("id", Collections.singletonList("id"));
        expected.setUnique(true);
        assertIndexMetaData(indexesIterator.next(), expected);
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
}
