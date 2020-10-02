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
import org.apache.shardingsphere.infra.context.schema.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.query.QueryData;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.rdl.parser.engine.ShardingSphereSQLParserEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

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

@RunWith(MockitoJUnitRunner.class)
public final class ShowDatabasesBackendHandlerTest {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    private ShowDatabasesBackendHandler showDatabasesBackendHandler;
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(backendConnection.getUsername()).thenReturn("root");
        showDatabasesBackendHandler = new ShowDatabasesBackendHandler(backendConnection);
        Field schemaContexts = ProxyContext.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        schemaContexts.set(ProxyContext.getInstance(), new StandardSchemaContexts(getSchemas(), 
                mock(ShardingSphereSQLParserEngine.class), mock(ExecutorKernel.class), getAuthentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
    }
    
    private Map<String, ShardingSphereSchema> getSchemas() {
        Map<String, ShardingSphereSchema> result = new HashMap<>(10);
        for (int i = 0; i < 10; i++) {
            result.put(String.format(SCHEMA_PATTERN, i), mock(ShardingSphereSchema.class));
        }
        return result;
    }
    
    private Authentication getAuthentication() {
        ProxyUser proxyUser = new ProxyUser("root", Arrays.asList(String.format(SCHEMA_PATTERN, 0), String.format(SCHEMA_PATTERN, 1)));
        Authentication result = new Authentication();
        result.getUsers().put("root", proxyUser);
        return result;
    }
    
    @Test
    public void assertExecuteShowDatabaseBackendHandler() {
        QueryResponse actual = (QueryResponse) showDatabasesBackendHandler.execute();
        assertThat(actual, instanceOf(QueryResponse.class));
        assertThat(actual.getQueryHeaders().size(), is(1));
    }
    
    @Test
    public void assertShowDatabaseUsingStream() throws SQLException {
        showDatabasesBackendHandler.execute();
        while (showDatabasesBackendHandler.next()) {
            QueryData queryData = showDatabasesBackendHandler.getQueryData();
            assertThat(queryData.getColumnTypes().size(), is(1));
            assertThat(queryData.getColumnTypes().iterator().next(), is(Types.VARCHAR));
            assertThat(queryData.getData().size(), is(1));
        }
    }
}
