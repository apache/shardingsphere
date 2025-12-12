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

package org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.index;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgreSQLIndexSQLGeneratorTest {
    
    @Test
    void assertGenerate() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        ResultSet getNodesResultSet = mockGetNodesResultSet();
        when(connection.createStatement().executeQuery(
                contains("SELECT DISTINCT ON(cls.relname) cls.oid, cls.relname as name," + "\n" + "(SELECT (CASE WHEN count(i.inhrelid) > 0 THEN true ELSE false END)")))
                .thenReturn(getNodesResultSet);
        ResultSet getPropertiesResultSet = mockGetPropertiesResultSet();
        when(connection.createStatement().executeQuery(contains("SELECT DISTINCT ON(cls.relname) cls.oid, cls.relname as name, indrelid, indkey, indisclustered"))).thenReturn(getPropertiesResultSet);
        Map<String, Object> context = new HashMap<>(5, 1F);
        context.put("did", 1);
        context.put("datlastsysoid", 10);
        context.put("tid", 20);
        context.put("schema", "foo_schema");
        context.put("name", "foo_tbl");
        String actual = new PostgreSQLIndexSQLGenerator(connection, 10, 0).generate(context);
        String expected = "CREATE INDEX IF NOT EXISTS foo_tbl" + "\n" + "ON foo_schema.foo_tbl USING foo_am_name"
                + "\n" + "()" + "\n" + "WITH (FILLFACTOR=90)" + "\n" + "TABLESPACE default" + "\n" + "WHERE NULL;";
        assertThat(actual, is(expected));
    }
    
    private ResultSet mockGetNodesResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(3);
        when(result.next()).thenReturn(true, false);
        when(result.getMetaData().getColumnName(1)).thenReturn("oid");
        when(result.getObject(1)).thenReturn(1L);
        when(result.getMetaData().getColumnName(2)).thenReturn("name");
        when(result.getObject(2)).thenReturn("foo_tbl");
        when(result.getMetaData().getColumnName(3)).thenReturn("is_inherited");
        when(result.getObject(3)).thenReturn(false);
        return result;
    }
    
    private ResultSet mockGetPropertiesResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(23);
        when(result.next()).thenReturn(true, false);
        when(result.getMetaData().getColumnName(1)).thenReturn("oid");
        when(result.getObject(1)).thenReturn(1L);
        when(result.getMetaData().getColumnName(2)).thenReturn("name");
        when(result.getObject(2)).thenReturn("foo_tbl");
        when(result.getMetaData().getColumnName(3)).thenReturn("indrelid");
        when(result.getObject(3)).thenReturn(20);
        when(result.getMetaData().getColumnName(4)).thenReturn("indkey");
        when(result.getObject(4)).thenReturn("{1,2}");
        when(result.getMetaData().getColumnName(5)).thenReturn("indisclustered");
        when(result.getObject(5)).thenReturn(false);
        when(result.getMetaData().getColumnName(6)).thenReturn("indisvalid");
        when(result.getObject(6)).thenReturn(true);
        when(result.getMetaData().getColumnName(7)).thenReturn("indisunique");
        when(result.getObject(7)).thenReturn(false);
        when(result.getMetaData().getColumnName(8)).thenReturn("indisprimary");
        when(result.getObject(8)).thenReturn(false);
        when(result.getMetaData().getColumnName(9)).thenReturn("nspname");
        when(result.getObject(9)).thenReturn("foo_schema");
        when(result.getMetaData().getColumnName(10)).thenReturn("indnatts");
        when(result.getObject(10)).thenReturn(3);
        when(result.getMetaData().getColumnName(11)).thenReturn("spcoid");
        when(result.getObject(11)).thenReturn("10101");
        when(result.getMetaData().getColumnName(12)).thenReturn("spcname");
        when(result.getObject(12)).thenReturn("default");
        when(result.getMetaData().getColumnName(13)).thenReturn("tabname");
        when(result.getObject(13)).thenReturn("foo_tbl");
        when(result.getMetaData().getColumnName(14)).thenReturn("indclass");
        when(result.getObject(14)).thenReturn("{pg_am_oid}");
        when(result.getMetaData().getColumnName(15)).thenReturn("conoid");
        when(result.getObject(15)).thenReturn(23456);
        when(result.getMetaData().getColumnName(16)).thenReturn("description");
        when(result.getObject(16)).thenReturn("");
        when(result.getMetaData().getColumnName(17)).thenReturn("indconstraint");
        when(result.getObject(17)).thenReturn("NULL");
        when(result.getMetaData().getColumnName(18)).thenReturn("contype");
        when(result.getObject(18)).thenReturn("p");
        when(result.getMetaData().getColumnName(19)).thenReturn("condeferrable");
        when(result.getObject(19)).thenReturn(true);
        when(result.getMetaData().getColumnName(20)).thenReturn("condeferred");
        when(result.getObject(20)).thenReturn(false);
        when(result.getMetaData().getColumnName(21)).thenReturn("amname");
        when(result.getObject(21)).thenReturn("foo_am_name");
        when(result.getMetaData().getColumnName(22)).thenReturn("fillfactor");
        when(result.getObject(22)).thenReturn(90);
        when(result.getMetaData().getColumnName(23)).thenReturn("is_sys_idx");
        when(result.getObject(23)).thenReturn(true);
        return result;
    }
}
