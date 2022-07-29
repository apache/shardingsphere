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

package org.apache.shardingsphere.proxy.backend.handler.admin.mysql;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.ShowFunctionStatusExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.ShowProcedureStatusExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.ShowTablesExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.UseDatabaseExecutor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowFunctionStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowProcedureStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

// TODO Cover more lines in MySQLAdminExecutorCreator
@RunWith(MockitoJUnitRunner.class)
public final class MySQLAdminExecutorCreatorTest {
    
    @SuppressWarnings("rawtypes")
    @Mock
    private SQLStatementContext sqlStatementContext;
    
    @Test
    public void assertCreateWithMySQLShowFunctionStatus() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLShowFunctionStatusStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowFunctionStatusExecutor.class));
    }
    
    @Test
    public void assertCreateWithShowProcedureStatus() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLShowProcedureStatusStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowProcedureStatusExecutor.class));
    }
    
    @Test
    public void assertCreateWithShowTables() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLShowTablesStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowTablesExecutor.class));
    }
    
    @Test
    public void assertCreateWithOtherSQLStatementContext() {
        assertThat(new MySQLAdminExecutorCreator().create(sqlStatementContext), is(Optional.empty()));
    }
    
    @Test
    public void assertCreateWithUse() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLUseStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "use db", "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(UseDatabaseExecutor.class));
    }
    
    @Test
    public void assertCreateWithDMLStatement() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLDeleteStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "delete from t", "");
        assertThat(actual, is(Optional.empty()));
    }
    
    @Test
    public void assertGetType() {
        assertThat(new MySQLAdminExecutorCreator().getType(), is("MySQL"));
    }
}
