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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowAllVariablesStatement;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.ShowDistSQLBackendHandler;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowAllVariablesBackendHandlerTest {
    
    private final BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Test
    public void assertShowPropsVariable() throws SQLException {
        backendConnection.setCurrentSchema("schema");
        ContextManager contextManager = mock(ContextManager.class);
        ProxyContext.getInstance().init(contextManager);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(metaDataContexts.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        ShowDistSQLBackendHandler backendHandler = new ShowDistSQLBackendHandler(new ShowAllVariablesStatement(), backendConnection);
        ResponseHeader actual = backendHandler.execute();
        assertThat(actual, instanceOf(QueryResponseHeader.class));
        assertThat(((QueryResponseHeader) actual).getQueryHeaders().size(), is(2));
        backendHandler.next();
        Collection<Object> rowData = backendHandler.getRowData();
        Iterator<Object> rowDataIterator = rowData.iterator();
        assertThat(rowDataIterator.next(), is("max_connections_size_per_query"));
        assertThat(rowDataIterator.next(), is("1"));
    }
}
