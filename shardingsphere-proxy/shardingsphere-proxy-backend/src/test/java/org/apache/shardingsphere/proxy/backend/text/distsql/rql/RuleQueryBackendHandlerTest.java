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
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.junit.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RuleQueryBackendHandlerTest {
    
    @Test
    public void assertExecute() {
        RuleQueryResultSet resultSet = mock(RuleQueryResultSet.class);
        when(resultSet.getColumnNames()).thenReturn(Arrays.asList("foo", "bar"));
        RuleQueryBackendHandler handler = new RuleQueryBackendHandler(mock(RQLStatement.class), mock(BackendConnection.class), resultSet);
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
        RuleQueryResultSet resultSet = mock(RuleQueryResultSet.class);
        when(resultSet.getRowData()).thenReturn(Arrays.asList("foo_value", "bar_value"));
        RuleQueryBackendHandler handler = new RuleQueryBackendHandler(mock(RQLStatement.class), mock(BackendConnection.class), resultSet);
        handler.execute("test", mock(RQLStatement.class));
        Collection<Object> rowData = handler.getRowData();
        assertThat(rowData.size(), is(2));
        assertTrue(rowData.contains("foo_value"));
        assertTrue(rowData.contains("bar_value"));
    }
}
