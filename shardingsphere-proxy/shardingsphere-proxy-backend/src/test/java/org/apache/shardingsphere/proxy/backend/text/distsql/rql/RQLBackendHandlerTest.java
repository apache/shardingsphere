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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql;

import org.apache.shardingsphere.distsql.parser.statement.rql.RQLStatement;
import org.apache.shardingsphere.infra.context.manager.ContextManager;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.junit.Before;
import org.junit.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RQLBackendHandlerTest {
    
    @Before
    public void setUp() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singleton("test"));
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
        when(metaDataContexts.getMetaData("test")).thenReturn(shardingSphereMetaData);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.getInstance().init(contextManager);
    }
    
    @Test
    public void assertExecute() {
        DistSQLResultSet resultSet = mock(DistSQLResultSet.class);
        when(resultSet.getColumnNames()).thenReturn(Arrays.asList("foo", "bar"));
        RQLBackendHandler handler = new RQLBackendHandler(mock(RQLStatement.class), mock(BackendConnection.class), resultSet);
        ResponseHeader responseHeader = handler.execute("test", mock(RQLStatement.class));
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
        DistSQLResultSet resultSet = mock(DistSQLResultSet.class);
        when(resultSet.getRowData()).thenReturn(Arrays.asList("foo_value", "bar_value"));
        RQLBackendHandler handler = new RQLBackendHandler(mock(RQLStatement.class), mock(BackendConnection.class), resultSet);
        handler.execute("test", mock(RQLStatement.class));
        Collection<Object> rowData = handler.getRowData();
        assertThat(rowData.size(), is(2));
        assertTrue(rowData.contains("foo_value"));
        assertTrue(rowData.contains("bar_value"));
    }
}
