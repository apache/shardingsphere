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

import org.apache.shardingsphere.core.parse.sql.statement.dal.dialect.mysql.ShowTablesStatement;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.rule.ProxyUser;
import org.apache.shardingsphere.shardingproxy.backend.MockLogicSchemasUtil;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ShowTablesBackendHandlerTest {
    
    @Before
    public void setUp() {
        MockLogicSchemasUtil.setLogicSchemas("sharding_db", 2);
        MockLogicSchemasUtil.setLogicTablesOnLogicSchemas(Arrays.asList("t_user", "t_order", "t_order_item"), "sharding_db_0");
        ShardingProxyContext.getInstance().init(getAuthentication(), new Properties());
    }
    
    private BackendConnection getBackendConnection() {
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(backendConnection.getUserName()).thenReturn("root");
        return backendConnection;
    }
    
    private Authentication getAuthentication() {
        ProxyUser proxyUser = new ProxyUser("root", Collections.singletonList("sharding_db_0"));
        Authentication result = new Authentication();
        result.getUsers().put("root", proxyUser);
        return result;
    }
    
    @Test
    public void assertExecute() throws SQLException {
        ShowTablesBackendHandler showTablesBackendHandler = new ShowTablesBackendHandler(getShowTablesStatement(true, "sharding_db_0", "t_order"), getBackendConnection());
        assertThat(((QueryResponse) showTablesBackendHandler.execute()).getQueryHeaders().size(), is(2));
        showTablesBackendHandler.next();
        assertThat((String) showTablesBackendHandler.getQueryData().getData().get(0), is("t_order"));
    }
    
    @Test
    public void assertExecuteWithQuestionSymbol() throws SQLException {
        ShowTablesBackendHandler showTablesBackendHandler = new ShowTablesBackendHandler(getShowTablesStatement(true, "sharding_db_0", "?_orde?"), getBackendConnection());
        assertThat(((QueryResponse) showTablesBackendHandler.execute()).getQueryHeaders().size(), is(2));
        showTablesBackendHandler.next();
        assertThat((String) showTablesBackendHandler.getQueryData().getData().get(0), is("t_order"));
    }
    
    @Test
    public void assertExecuteWithPercentSymbol() throws SQLException {
        ShowTablesBackendHandler showTablesBackendHandler = new ShowTablesBackendHandler(getShowTablesStatement(true, "sharding_db_0", "t_order%"), getBackendConnection());
        assertThat(((QueryResponse) showTablesBackendHandler.execute()).getQueryHeaders().size(), is(2));
        showTablesBackendHandler.next();
        assertThat((String) showTablesBackendHandler.getQueryData().getData().get(0), is("t_order"));
        showTablesBackendHandler.next();
        assertThat((String) showTablesBackendHandler.getQueryData().getData().get(0), is("t_order_item"));
    }
    
    @Test
    public void assertExecuteWithOutPattern() throws SQLException {
        ShowTablesBackendHandler showTablesBackendHandler = new ShowTablesBackendHandler(getShowTablesStatement(true, "sharding_db_0", null), getBackendConnection());
        assertThat(((QueryResponse) showTablesBackendHandler.execute()).getQueryHeaders().size(), is(2));
        showTablesBackendHandler.next();
        assertThat((String) showTablesBackendHandler.getQueryData().getData().get(0), is("t_user"));
        showTablesBackendHandler.next();
        assertThat((String) showTablesBackendHandler.getQueryData().getData().get(0), is("t_order"));
        showTablesBackendHandler.next();
        assertThat((String) showTablesBackendHandler.getQueryData().getData().get(0), is("t_order_item"));
    }
    
    @Test
    public void assertExecuteWithOutFull() throws SQLException {
        ShowTablesBackendHandler showTablesBackendHandler = new ShowTablesBackendHandler(getShowTablesStatement(false, "sharding_db_0", "t_order%"), getBackendConnection());
        assertThat(((QueryResponse) showTablesBackendHandler.execute()).getQueryHeaders().size(), is(1));
        showTablesBackendHandler.next();
        assertThat((String) showTablesBackendHandler.getQueryData().getData().get(0), is("t_order"));
        showTablesBackendHandler.next();
        assertThat((String) showTablesBackendHandler.getQueryData().getData().get(0), is("t_order_item"));
    }
    
    @Test
    public void assertExecuteWithNoDatabaseSelectedException() {
        assertThat(new ShowTablesBackendHandler(getShowTablesStatement(false, null, "t_order%"), getBackendConnection()).execute(), instanceOf(ErrorResponse.class));
    }
    
    @Test
    public void assertExecuteWithUnknownDatabaseException() {
        assertThat(new ShowTablesBackendHandler(getShowTablesStatement(false, "illegal schema", "t_order%"), getBackendConnection()).execute(), instanceOf(ErrorResponse.class));
    }
    
    private ShowTablesStatement getShowTablesStatement(final boolean full, final String schema, final String pattern) {
        ShowTablesStatement showTablesStatement = new ShowTablesStatement();
        showTablesStatement.setFull(full);
        showTablesStatement.setSchema(schema);
        showTablesStatement.setPattern(pattern);
        return showTablesStatement;
    }
    
}
