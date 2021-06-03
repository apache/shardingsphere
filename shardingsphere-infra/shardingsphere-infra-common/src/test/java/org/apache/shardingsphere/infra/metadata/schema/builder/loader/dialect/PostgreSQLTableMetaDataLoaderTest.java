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

package org.apache.shardingsphere.infra.metadata.schema.builder.loader.dialect;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PostgreSQLTableMetaDataLoaderTest {
    
    private static final String BASIC_TABLE_META_DATA_SQL = "SELECT table_name, column_name, data_type, udt_name, column_default FROM information_schema.columns WHERE table_schema = ?";
    
    private static final String TABLE_META_DATA_SQL_WITH_EXISTED_TABLES = BASIC_TABLE_META_DATA_SQL + " AND table_name NOT IN ('existed_tbl')";
    
    private static final String PRIMARY_KEY_META_DATA_SQL = "SELECT tc.table_name, kc.column_name FROM information_schema.table_constraints tc"
        + " JOIN information_schema.key_column_usage kc"
        + " ON kc.table_schema = tc.table_schema AND kc.table_name = tc.table_name AND kc.constraint_name = tc.constraint_name"
        + " WHERE tc.constraint_type = 'PRIMARY KEY' AND kc.ordinal_position IS NOT NULL AND kc.table_schema = ?";
    
    private static final String BASIC_INDEX_META_DATA_SQL = "SELECT tablename, indexname FROM pg_indexes WHERE schemaname = ?";
    
    @BeforeClass
    public static void setUp() {
        ShardingSphereServiceLoader.register(DialectTableMetaDataLoader.class);
    }
    
    @Test
    public void assertLoadWithoutExistedTables() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(BASIC_TABLE_META_DATA_SQL).executeQuery()).thenReturn(resultSet);
        ResultSet primaryKeyResultSet = mockPrimaryKeyMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(PRIMARY_KEY_META_DATA_SQL).executeQuery()).thenReturn(primaryKeyResultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(BASIC_INDEX_META_DATA_SQL).executeQuery()).thenReturn(indexResultSet);
        assertTableMetaDataMap(getTableMetaDataLoader().load(dataSource, Collections.emptyList()));
    }
    
    @Test
    public void assertLoadWithExistedTables() throws SQLException {
        DataSource dataSource = mockDataSource();
        ResultSet resultSet = mockTableMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(TABLE_META_DATA_SQL_WITH_EXISTED_TABLES).executeQuery()).thenReturn(resultSet);
        ResultSet primaryKeyResultSet = mockPrimaryKeyMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(PRIMARY_KEY_META_DATA_SQL).executeQuery()).thenReturn(primaryKeyResultSet);
        ResultSet indexResultSet = mockIndexMetaDataResultSet();
        when(dataSource.getConnection().prepareStatement(BASIC_INDEX_META_DATA_SQL).executeQuery()).thenReturn(indexResultSet);
        assertTableMetaDataMap(getTableMetaDataLoader().load(dataSource, Collections.singletonList("existed_tbl")));
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
        when(result.getString("TYPE_NAME")).thenReturn("int4", "varchar");
        when(result.getInt("DATA_TYPE")).thenReturn(Types.INTEGER, Types.VARCHAR);
        return result;
    }
    
    private ResultSet mockTableMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("table_name")).thenReturn("tbl");
        when(result.getString("column_name")).thenReturn("id", "name");
        when(result.getString("data_type")).thenReturn("integer", "character varying");
        when(result.getString("udt_name")).thenReturn("int4", "varchar");
        when(result.getString("column_default")).thenReturn("nextval('id_seq'::regclass)", "");
        return result;
    }
    
    private ResultSet mockPrimaryKeyMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("table_name")).thenReturn("tbl");
        when(result.getString("column_name")).thenReturn("id");
        return result;
    }
    
    private ResultSet mockIndexMetaDataResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("tablename")).thenReturn("tbl");
        when(result.getString("indexname")).thenReturn("id");
        return result;
    }
    
    private DialectTableMetaDataLoader getTableMetaDataLoader() {
        for (DialectTableMetaDataLoader each : ShardingSphereServiceLoader.newServiceInstances(DialectTableMetaDataLoader.class)) {
            if ("PostgreSQL".equals(each.getDatabaseType())) {
                return each;
            }
        }
        throw new IllegalStateException("Can not find PostgreSQLTableMetaDataLoader");
    }
    
    private void assertTableMetaDataMap(final Map<String, TableMetaData> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get("tbl").getColumns().size(), is(2));
        assertThat(actual.get("tbl").getColumnMetaData(0), is(new ColumnMetaData("id", Types.INTEGER, true, true, true)));
        assertThat(actual.get("tbl").getColumnMetaData(1), is(new ColumnMetaData("name", Types.VARCHAR, false, false, true)));
        assertThat(actual.get("tbl").getIndexes().size(), is(1));
        assertThat(actual.get("tbl").getIndexes().get("id"), is(new IndexMetaData("id")));
    }
}
