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

package org.apache.shardingsphere.proxy.backend.hbase.result.query;

import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.dal.ShowCreateTableStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.exception.HBaseOperationException;
import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseSupportedSQLStatement;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HBaseDescribeResultSetTest extends AbstractHBaseQueryResultSetTest {
    
    @Test
    void assertGetRowData() {
        HBaseQueryResultSet resultSet = new HBaseDescribeResultSet();
        ShowCreateTableStatementContext context = mock(ShowCreateTableStatementContext.class);
        when(context.getTablesContext()).thenReturn(mock(TablesContext.class, RETURNS_DEEP_STUBS));
        when(context.getTablesContext().getTableNames().iterator().next()).thenReturn(HBaseSupportedSQLStatement.HBASE_DATABASE_TABLE_NAME);
        resultSet.init(context);
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(9));
        assertThat(actual.get(0), is(HBaseSupportedSQLStatement.HBASE_DATABASE_TABLE_NAME));
        assertThat(actual.get(5), is(0));
        assertThat(actual.get(6), is(1));
        assertThat(actual.get(8), is(""));
    }
    
    @Test
    void assertGetRowDataWithTableIsNotExists() throws IOException {
        when(getAdmin().tableExists(any())).thenReturn(false);
        ShowCreateTableStatementContext context = mock(ShowCreateTableStatementContext.class);
        when(context.getTablesContext()).thenReturn(mock(TablesContext.class, RETURNS_DEEP_STUBS));
        when(context.getTablesContext().getTableNames().iterator().next()).thenReturn(HBaseSupportedSQLStatement.HBASE_DATABASE_TABLE_NAME);
        assertThrows(IllegalArgumentException.class, () -> new HBaseDescribeResultSet().init(context));
    }
    
    @Test
    void assertGetRowDataWithBackendError() throws IOException {
        when(getAdmin().getTableDescriptor(any())).thenThrow(IOException.class);
        ShowCreateTableStatementContext context = mock(ShowCreateTableStatementContext.class);
        when(context.getTablesContext()).thenReturn(mock(TablesContext.class, RETURNS_DEEP_STUBS));
        when(context.getTablesContext().getTableNames().iterator().next()).thenReturn(HBaseSupportedSQLStatement.HBASE_DATABASE_TABLE_NAME);
        assertThrows(HBaseOperationException.class, () -> new HBaseDescribeResultSet().init(context));
    }
}
