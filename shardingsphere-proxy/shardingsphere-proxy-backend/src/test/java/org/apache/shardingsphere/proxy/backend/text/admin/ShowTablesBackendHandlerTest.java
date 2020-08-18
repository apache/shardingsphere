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

package org.apache.shardingsphere.proxy.backend.text.admin;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.StandardSchemaContexts;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.query.QueryData;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ShowTablesBackendHandlerTest {
    
    private ShowTablesBackendHandler tablesBackendHandler;
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(backendConnection.getUserName()).thenReturn("root");
        tablesBackendHandler = new ShowTablesBackendHandler("show tables", mock(SQLStatement.class), backendConnection);
        Map<String, SchemaContext> schemaContextMap = getSchemaContextMap();
        when(backendConnection.getSchema()).thenReturn(schemaContextMap.values().iterator().next());
        Field schemaContexts = ProxySchemaContexts.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        schemaContexts.set(ProxySchemaContexts.getInstance(),
                new StandardSchemaContexts(schemaContextMap, getAuthentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
    }
    
    private Map<String, SchemaContext> getSchemaContextMap() {
        Map<String, SchemaContext> result = new HashMap<>(10);
        for (int i = 0; i < 10; i++) {
            SchemaContext context = mock(SchemaContext.class);
            when(context.isComplete()).thenReturn(false);
            result.put("schema_" + i, context);
        }
        return result;
    }
    
    private Authentication getAuthentication() {
        ProxyUser proxyUser = new ProxyUser("root", Arrays.asList("schema_0", "schema_1"));
        Authentication result = new Authentication();
        result.getUsers().put("root", proxyUser);
        return result;
    }
    
    @Test
    public void assertExecuteShowTablesBackendHandler() {
        QueryResponse actual = (QueryResponse) tablesBackendHandler.execute();
        assertThat(actual, instanceOf(QueryResponse.class));
        assertThat(actual.getQueryHeaders().size(), is(1));
    }
    
    @Test
    public void assertShowTablesUsingStream() throws SQLException {
        tablesBackendHandler.execute();
        while (tablesBackendHandler.next()) {
            QueryData queryData = tablesBackendHandler.getQueryData();
            assertThat(queryData.getColumnTypes().size(), is(1));
            assertThat(queryData.getColumnTypes().iterator().next(), is(Types.VARCHAR));
            assertThat(queryData.getData().size(), is(1));
        }
    }
}
