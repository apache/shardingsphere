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

import org.apache.shardingsphere.infra.binder.context.statement.dal.ShowTablesStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.exception.HBaseOperationException;
import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseSupportedSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HBaseListResultSetTest extends AbstractHBaseQueryResultSetTest {
    
    @Test
    void assertGetRowData() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(HBaseSupportedSQLStatement.getShowTablesStatement());
        HBaseQueryResultSet resultSet = new HBaseListResultSet();
        ShowTablesStatementContext context = mock(ShowTablesStatementContext.class);
        when(context.getSqlStatement()).thenReturn((MySQLShowTablesStatement) sqlStatement);
        resultSet.init(context);
        assertTrue(resultSet.next());
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is("cluster_lj"));
        assertThat(actual.get(1), is(HBaseSupportedSQLStatement.HBASE_DATABASE_TABLE_NAME));
    }
    
    @Test
    void assertGetRowDataFromRemoteHBaseCluster() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement("show /*+ hbase */ tables from cluster_lj");
        HBaseQueryResultSet resultSet = new HBaseListResultSet();
        ShowTablesStatementContext context = mock(ShowTablesStatementContext.class);
        when(context.getSqlStatement()).thenReturn((MySQLShowTablesStatement) sqlStatement);
        resultSet.init(context);
        
        assertTrue(resultSet.next());
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is("cluster_lj"));
        assertThat(actual.get(1), is(HBaseSupportedSQLStatement.HBASE_DATABASE_TABLE_NAME));
    }
    
    @Test
    void assertGetRowDataByLike() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement("show /*+ hbase */ tables  like 't_test' ");
        HBaseQueryResultSet resultSet = new HBaseListResultSet();
        ShowTablesStatementContext context = mock(ShowTablesStatementContext.class);
        when(context.getSqlStatement()).thenReturn((MySQLShowTablesStatement) sqlStatement);
        resultSet.init(context);
        sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement("show /*+ hbase */ tables  like 't_test%' ");
        when(context.getSqlStatement()).thenReturn((MySQLShowTablesStatement) sqlStatement);
        resultSet.init(context);
        assertTrue(resultSet.next());
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(1), is(HBaseSupportedSQLStatement.HBASE_DATABASE_TABLE_NAME));
    }
    
    @Test
    void assertGetRowDataError() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement("show /*+ hbase */ tables from cluster_do_not_exists");
        HBaseQueryResultSet resultSet = new HBaseListResultSet();
        ShowTablesStatementContext context = mock(ShowTablesStatementContext.class);
        when(context.getSqlStatement()).thenReturn((MySQLShowTablesStatement) sqlStatement);
        assertThrows(HBaseOperationException.class, () -> resultSet.init(context));
    }
}
