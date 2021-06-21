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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl;

import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.RuleQueryResultSet;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class RuleQueryBackendHandlerTest {
    
    @Mock
    private RuleQueryResultSet resultSet;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private ShowDatabaseDiscoveryRulesStatement sqlStatement;
    
    @Mock
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private TransactionContexts transactionContexts;
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereRuleMetaData ruleMetaData;
    
    private RuleQueryBackendHandler handler;
    
    @Before
    public void setUp() {
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        mockRuleQueryResultSet();
        handler = new RuleQueryBackendHandler(sqlStatement, backendConnection, resultSet);
    }
    
    public void mockRuleQueryResultSet() {
        when(resultSet.getColumnNames()).thenReturn(Arrays.asList("foo", "bar"));
        when(resultSet.getRowData()).thenReturn(Arrays.asList("foo_value", "bar_value"));
    }
    
    @Test
    public void assertExecute() {
        ResponseHeader responseHeader = handler.execute("test", sqlStatement);
        assertThat(((QueryResponseHeader) responseHeader).getQueryHeaders().size(), is(2));
        assertQueryHeader(((QueryResponseHeader) responseHeader).getQueryHeaders().get(0), "foo");
        assertQueryHeader(((QueryResponseHeader) responseHeader).getQueryHeaders().get(1), "bar");
    }
    
    private void assertQueryHeader(final QueryHeader queryHeader, final String expectedColumnLabel) {
        assertThat(queryHeader.getSchema(), is("test"));
        assertThat(queryHeader.getTable(), is(""));
        assertThat(queryHeader.getColumnLabel(), is(expectedColumnLabel));
        assertThat(queryHeader.getColumnName(), is(expectedColumnLabel));
        assertThat(queryHeader.getColumnType(), is(Types.CHAR));
        assertThat(queryHeader.getColumnTypeName(), is("CHAR"));
        assertThat(queryHeader.getColumnLength(), is(255));
        assertThat(queryHeader.getDecimals(), is(0));
        assertFalse(queryHeader.isSigned());
        assertFalse(queryHeader.isPrimaryKey());
        assertFalse(queryHeader.isNotNull());
        assertFalse(queryHeader.isAutoIncrement());
    }
    
    @Test
    public void assertGetRowData() {
        handler.execute("test", sqlStatement);
        Collection<Object> rowData = handler.getRowData();
        assertThat(rowData.size(), is(2));
        assertTrue(rowData.contains("foo_value"));
        assertTrue(rowData.contains("bar_value"));
    }
}
