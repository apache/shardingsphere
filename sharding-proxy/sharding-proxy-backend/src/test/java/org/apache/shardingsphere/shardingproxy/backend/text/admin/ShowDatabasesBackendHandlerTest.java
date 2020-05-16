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

package org.apache.shardingsphere.shardingproxy.backend.text.admin;

import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.shardingproxy.backend.MockShardingSphereSchemasUtil;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShowDatabasesBackendHandlerTest {
    
    private ShowDatabasesBackendHandler showDatabasesBackendHandler;
    
    @Before
    public void setUp() {
        MockShardingSphereSchemasUtil.setSchemas("schema", 5);
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(backendConnection.getUserName()).thenReturn("root");
        ShardingProxyContext.getInstance().init(getAuthentication(), new Properties());
        showDatabasesBackendHandler = new ShowDatabasesBackendHandler(backendConnection);
    }
    
    private Authentication getAuthentication() {
        ProxyUser proxyUser = new ProxyUser("root", Arrays.asList("schema_0", "schema_1"));
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
