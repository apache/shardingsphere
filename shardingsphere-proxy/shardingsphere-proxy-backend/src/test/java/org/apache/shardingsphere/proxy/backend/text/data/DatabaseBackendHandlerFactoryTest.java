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

package org.apache.shardingsphere.proxy.backend.text.data;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.data.impl.BroadcastDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.data.impl.SchemaAssignedDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.data.impl.UnicastDatabaseBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseBackendHandlerFactoryTest {
    
    @Test
    public void assertNewInstanceReturnedBroadcastDatabaseBackendHandler() {
        String sql = "SET a=1";
        SQLStatementContext<SetStatement> context = mock(SQLStatementContext.class);
        when(context.getSqlStatement()).thenReturn(mock(SetStatement.class));
        DatabaseBackendHandler actual = DatabaseBackendHandlerFactory.newInstance(context, sql, mock(JDBCConnectionSession.class));
        assertThat(actual, instanceOf(BroadcastDatabaseBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceReturnedUnicastDatabaseBackendHandlerWithDAL() {
        String sql = "DESC tbl";
        SQLStatementContext<DALStatement> context = mock(SQLStatementContext.class);
        when(context.getSqlStatement()).thenReturn(mock(DALStatement.class));
        DatabaseBackendHandler actual = DatabaseBackendHandlerFactory.newInstance(context, sql, mock(JDBCConnectionSession.class));
        assertThat(actual, instanceOf(UnicastDatabaseBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceReturnedUnicastDatabaseBackendHandlerWithQueryWithoutFrom() {
        String sql = "SELECT 1";
        SQLStatementContext<SelectStatement> context = mock(SQLStatementContext.class);
        when(context.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        DatabaseBackendHandler actual = DatabaseBackendHandlerFactory.newInstance(context, sql, mock(JDBCConnectionSession.class));
        assertThat(actual, instanceOf(UnicastDatabaseBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceReturnedSchemaAssignedDatabaseBackendHandler() {
        String sql = "SELECT 1 FROM user WHERE id = 1";
        SQLStatementContext<SQLStatement> context = mock(SQLStatementContext.class);
        when(context.getSqlStatement()).thenReturn(mock(SQLStatement.class));
        DatabaseBackendHandler actual = DatabaseBackendHandlerFactory.newInstance(context, sql, mock(JDBCConnectionSession.class));
        assertThat(actual, instanceOf(SchemaAssignedDatabaseBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithDCLStatement() {
        String sql = "CREATE USER test IDENTIFIED BY '123456'";
        SQLStatementContext<DCLStatement> context = mock(SQLStatementContext.class);
        when(context.getSqlStatement()).thenReturn(mock(DCLStatement.class));
        DatabaseBackendHandler actual = DatabaseBackendHandlerFactory.newInstance(context, sql, mock(JDBCConnectionSession.class));
        assertThat(actual, instanceOf(BroadcastDatabaseBackendHandler.class));
    }
}
