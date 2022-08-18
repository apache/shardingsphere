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
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.TruncateStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.dialect.exception.transaction.TableModifyInTransactionException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLTruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLTruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ProxySQLExecutorTest extends ProxyContextRestorer {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private JDBCBackendConnection backendConnection;
    
    @Before
    public void setUp() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.XA);
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData());
        ProxyContext.init(new ContextManager(metaDataContexts, mock(InstanceContext.class)));
    }
    
    @Test(expected = TableModifyInTransactionException.class)
    public void assertCheckExecutePrerequisitesWhenExecuteDDLInXATransaction() {
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createMySQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(JDBCDatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test(expected = TableModifyInTransactionException.class)
    public void assertCheckExecutePrerequisitesWhenExecuteTruncateInMySQLXATransaction() {
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createMySQLTruncateStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(JDBCDatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    public void assertCheckExecutePrerequisitesWhenExecuteTruncateInMySQLLocalTransaction() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createMySQLTruncateStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(JDBCDatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    public void assertCheckExecutePrerequisitesWhenExecuteDMLInXATransaction() {
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createMySQLInsertStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(JDBCDatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    public void assertCheckExecutePrerequisitesWhenExecuteDDLInBaseTransaction() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.BASE);
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createMySQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(JDBCDatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    public void assertCheckExecutePrerequisitesWhenExecuteDDLNotInXATransaction() {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(false);
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createMySQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(JDBCDatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test(expected = TableModifyInTransactionException.class)
    public void assertCheckExecutePrerequisitesWhenExecuteDDLInPostgreSQLTransaction() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createPostgreSQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(JDBCDatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    public void assertCheckExecutePrerequisitesWhenExecuteTruncateInPostgreSQLTransaction() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createPostgreSQLTruncateStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(JDBCDatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    public void assertCheckExecutePrerequisitesWhenExecuteDMLInPostgreSQLTransaction() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createPostgreSQLInsertStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(JDBCDatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    public void assertCheckExecutePrerequisitesWhenExecuteDDLInMySQLTransaction() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createMySQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(JDBCDatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    public void assertCheckExecutePrerequisitesWhenExecuteDDLNotInPostgreSQLTransaction() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(false);
        ExecutionContext executionContext = new ExecutionContext(
                new LogicSQL(createPostgreSQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, backendConnection, mock(JDBCDatabaseCommunicationEngine.class)).checkExecutePrerequisites(executionContext);
    }
    
    private CreateTableStatementContext createMySQLCreateTableStatementContext() {
        MySQLCreateTableStatement sqlStatement = new MySQLCreateTableStatement(false);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        return new CreateTableStatementContext(sqlStatement);
    }
    
    private TruncateStatementContext createMySQLTruncateStatementContext() {
        MySQLTruncateStatement sqlStatement = new MySQLTruncateStatement();
        sqlStatement.getTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        return new TruncateStatementContext(sqlStatement);
    }
    
    private SQLStatementContext<?> createPostgreSQLTruncateStatementContext() {
        PostgreSQLTruncateStatement sqlStatement = new PostgreSQLTruncateStatement();
        sqlStatement.getTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        return new TruncateStatementContext(sqlStatement);
    }
    
    private InsertStatementContext createMySQLInsertStatementContext() {
        MySQLInsertStatement sqlStatement = new MySQLInsertStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        return new InsertStatementContext(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), Collections.emptyList(), sqlStatement, DefaultDatabase.LOGIC_NAME);
    }
    
    private CreateTableStatementContext createPostgreSQLCreateTableStatementContext() {
        PostgreSQLCreateTableStatement sqlStatement = new PostgreSQLCreateTableStatement(false);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        return new CreateTableStatementContext(sqlStatement);
    }
    
    private InsertStatementContext createPostgreSQLInsertStatementContext() {
        PostgreSQLInsertStatement sqlStatement = new PostgreSQLInsertStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema("public")).thenReturn(mock(ShardingSphereSchema.class));
        return new InsertStatementContext(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), Collections.emptyList(), sqlStatement, DefaultDatabase.LOGIC_NAME);
    }
}
