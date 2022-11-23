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

package org.apache.shardingsphere.infra.metadata.database.schema.loader.common;

import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaMetaDataLoaderTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSource dataSource;
    
    @Before
    public void setUp() throws SQLException {
        ResultSet tableResultSet = mockTableResultSet();
        when(dataSource.getConnection().getMetaData().getTables("catalog", "public", null, new String[]{"TABLE", "VIEW"})).thenReturn(tableResultSet);
        when(dataSource.getConnection().getCatalog()).thenReturn("catalog");
        when(dataSource.getConnection().getSchema()).thenReturn("public");
        ResultSet schemaResultSet = mockSchemaResultSet();
        when(dataSource.getConnection().getMetaData().getSchemas()).thenReturn(schemaResultSet);
    }
    
    private ResultSet mockTableResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn("tbl", "$tbl", "/tbl", "##tbl");
        return result;
    }
    
    private ResultSet mockSchemaResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, true, false);
        when(result.getString("TABLE_SCHEM")).thenReturn("information_schema", "public", "schema_1", "schema_2");
        return result;
    }
    
    @Test
    public void assertLoadSchemaTableNamesForPostgreSQL() throws SQLException {
        assertThat(SchemaMetaDataLoader.loadSchemaTableNames(DefaultDatabase.LOGIC_NAME, new PostgreSQLDatabaseType(), dataSource), is(createSchemaTableNames()));
    }
    
    @Test
    public void assertLoadSchemaTableNamesForOpenGauss() throws SQLException {
        assertThat(SchemaMetaDataLoader.loadSchemaTableNames(DefaultDatabase.LOGIC_NAME, new OpenGaussDatabaseType(), dataSource), is(createSchemaTableNames()));
    }
    
    @Test
    public void assertLoadSchemaTableNamesForMySQL() throws SQLException {
        Map<String, Collection<String>> schemaTableNames = Collections.singletonMap(DefaultDatabase.LOGIC_NAME, Collections.singletonList("tbl"));
        assertThat(SchemaMetaDataLoader.loadSchemaTableNames(DefaultDatabase.LOGIC_NAME, new MySQLDatabaseType(), dataSource), is(schemaTableNames));
    }
    
    private static Map<String, Collection<String>> createSchemaTableNames() {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        result.put("public", Collections.singletonList("tbl"));
        result.put("schema_1", Collections.emptyList());
        result.put("schema_2", Collections.emptyList());
        return result;
    }
    
    @Test
    public void assertLoadSchemaNamesForPostgreSQL() throws SQLException {
        assertThat(SchemaMetaDataLoader.loadSchemaNames(dataSource.getConnection(), new PostgreSQLDatabaseType()), is(Arrays.asList("public", "schema_1", "schema_2")));
    }
    
    @Test
    public void assertLoadSchemaNamesForOpenGauss() throws SQLException {
        assertThat(SchemaMetaDataLoader.loadSchemaNames(dataSource.getConnection(), new OpenGaussDatabaseType()), is(Arrays.asList("public", "schema_1", "schema_2")));
    }
    
    @Test
    public void assertLoadSchemaNamesForMySQL() throws SQLException {
        assertThat(SchemaMetaDataLoader.loadSchemaNames(dataSource.getConnection(), new MySQLDatabaseType()), is(Collections.singletonList("public")));
    }
}
