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

package org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.table;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgreSQLTablePropertiesLoaderTest {
    
    @Test
    void assertLoad() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getCatalog()).thenReturn("foo_db");
        ResultSet fetchDatabaseIdResultSet = mockFetchDatabaseIdResultSet();
        when(connection.createStatement().executeQuery("\n" + "SELECT oid AS did, datlastsysoid FROM pg_catalog.pg_database WHERE datname = 'foo_db';" + "\n"))
                .thenReturn(fetchDatabaseIdResultSet);
        ResultSet fetchSchemaIdResultSet = mockFetchSchemaIdResultSet();
        when(connection.createStatement().executeQuery("\n" + "SELECT oid AS scid FROM pg_catalog.pg_namespace WHERE nspname = 'foo_schema';" + "\n"))
                .thenReturn(fetchSchemaIdResultSet);
        Map<String, Object> actual = new PostgreSQLTablePropertiesLoader(connection, "foo_tbl", "foo_schema", 12, 0).load();
        assertThat(actual.size(), is(7));
        assertThat(actual.get("did"), is(1));
        assertThat(actual.get("datlastsysoid"), is(10));
        assertThat(actual.get("scid"), is(20));
        assertThat(actual.get("autovacuum_enabled"), is("x"));
        assertThat(actual.get("toast_autovacuum_enabled"), is("x"));
        assertThat(actual.get("autovacuum_custom"), is(false));
        assertThat(actual.get("toast_autovacuum"), is(false));
    }
    
    private ResultSet mockFetchDatabaseIdResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(2);
        when(result.next()).thenReturn(true);
        when(result.getMetaData().getColumnName(1)).thenReturn("did");
        when(result.getObject(1)).thenReturn(1);
        when(result.getMetaData().getColumnName(2)).thenReturn("datlastsysoid");
        when(result.getObject(2)).thenReturn(10);
        return result;
    }
    
    private ResultSet mockFetchSchemaIdResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(1);
        when(result.next()).thenReturn(true);
        when(result.getMetaData().getColumnName(1)).thenReturn("scid");
        when(result.getObject(1)).thenReturn(20);
        return result;
    }
}
