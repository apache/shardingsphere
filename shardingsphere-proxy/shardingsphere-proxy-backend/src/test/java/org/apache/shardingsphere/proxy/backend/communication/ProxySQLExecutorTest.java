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

package org.apache.shardingsphere.proxy.backend.communication;

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.exception.TableModifyInTransactionException;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ProxySQLExecutorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private BackendConnection backendConnection;
    
    @Before
    public void setUp() {
        when(backendConnection.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.XA);
        when(backendConnection.getTransactionStatus().isInTransaction()).thenReturn(true);
    }
    
    @Test(expected = TableModifyInTransactionException.class)
    public void assertCheckExecutePrerequisitesWhenExecuteDDLInXATransaction() {
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createMySQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(DatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    public void assertCheckExecutePrerequisitesWhenExecuteDMLInXATransaction() {
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createMySQLInsertStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(DatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    public void assertCheckExecutePrerequisitesWhenExecuteDDLInBaseTransaction() {
        when(backendConnection.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.BASE);
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createMySQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(DatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    public void assertCheckExecutePrerequisitesWhenExecuteDDLNotInXATransaction() {
        when(backendConnection.getTransactionStatus().isInTransaction()).thenReturn(false);
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createMySQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(DatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test(expected = TableModifyInTransactionException.class)
    public void assertCheckExecutePrerequisitesWhenExecuteDDLInPostgreSQLTransaction() {
        when(backendConnection.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createPostgreSQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(DatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    public void assertCheckExecutePrerequisitesWhenExecuteDMLInPostgreSQLTransaction() {
        when(backendConnection.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createPostgreSQLInsertStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(DatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    public void assertCheckExecutePrerequisitesWhenExecuteDDLInMySQLTransaction() {
        when(backendConnection.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createMySQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(DatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    public void assertCheckExecutePrerequisitesWhenExecuteDDLNotInPostgreSQLTransaction() {
        when(backendConnection.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        when(backendConnection.getTransactionStatus().isInTransaction()).thenReturn(false);
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createPostgreSQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(DatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    private CreateTableStatementContext createMySQLCreateTableStatementContext() {
        MySQLCreateTableStatement sqlStatement = new MySQLCreateTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        return new CreateTableStatementContext(sqlStatement);
    }
    
    private InsertStatementContext createMySQLInsertStatementContext() {
        MySQLInsertStatement sqlStatement = new MySQLInsertStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getSchema()).thenReturn(mock(ShardingSphereSchema.class));
        return new InsertStatementContext(Collections.singletonMap("logic_db", metaData), Collections.emptyList(), sqlStatement, "logic_db");
    }
    
    private CreateTableStatementContext createPostgreSQLCreateTableStatementContext() {
        PostgreSQLCreateTableStatement sqlStatement = new PostgreSQLCreateTableStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        return new CreateTableStatementContext(sqlStatement);
    }
    
    private InsertStatementContext createPostgreSQLInsertStatementContext() {
        PostgreSQLInsertStatement sqlStatement = new PostgreSQLInsertStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getSchema()).thenReturn(mock(ShardingSphereSchema.class));
        return new InsertStatementContext(Collections.singletonMap("logic_db", metaData), Collections.emptyList(), sqlStatement, "logic_db");
    }
}
