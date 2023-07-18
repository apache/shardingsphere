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

package org.apache.shardingsphere.proxy.backend.connector;

import org.apache.shardingsphere.dialect.exception.transaction.TableModifyInTransactionException;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.TruncateStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLTruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCursorStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLTruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProxySQLExecutorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.XA);
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        when(databaseConnectionManager.getConnectionSession().getDatabaseName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getDatabase(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
        when(metaData.getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(0);
        when(metaData.getGlobalRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singletonList(mock(SQLFederationRule.class))));
        ContextManager contextManager = new ContextManager(new MetaDataContexts(mock(MetaDataPersistService.class), metaData), mock(InstanceContext.class));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteDDLInXATransaction() {
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createMySQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        assertThrows(TableModifyInTransactionException.class,
                () -> new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext));
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteTruncateInMySQLXATransaction() {
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createMySQLTruncateStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        assertThrows(TableModifyInTransactionException.class,
                () -> new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext));
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteTruncateInMySQLLocalTransaction() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createMySQLTruncateStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteDMLInXATransaction() {
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createMySQLInsertStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteDDLInBaseTransaction() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.BASE);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createMySQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteDDLNotInXATransaction() {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(false);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createMySQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteDDLInPostgreSQLTransaction() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createPostgreSQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        assertThrows(TableModifyInTransactionException.class,
                () -> new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext));
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteTruncateInPostgreSQLTransaction() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createPostgreSQLTruncateStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteCursorInPostgreSQLTransaction() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createCursorStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteDMLInPostgreSQLTransaction() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createPostgreSQLInsertStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteDDLInMySQLTransaction() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createMySQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    private QueryContext mockQueryContext() {
        QueryContext result = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatementContext().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(result.getSqlStatementContext().getTablesContext().getSchemaName()).thenReturn(Optional.of(DefaultDatabase.LOGIC_NAME));
        return result;
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteDDLNotInPostgreSQLTransaction() {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(false);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createPostgreSQLCreateTableStatementContext(), "", Collections.emptyList()), Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
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
    
    private SQLStatementContext createPostgreSQLTruncateStatementContext() {
        PostgreSQLTruncateStatement sqlStatement = new PostgreSQLTruncateStatement();
        sqlStatement.getTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        return new TruncateStatementContext(sqlStatement);
    }
    
    private CursorStatementContext createCursorStatementContext() {
        CursorStatementContext result = mock(CursorStatementContext.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = createSelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        OpenGaussCursorStatement sqlStatement = new OpenGaussCursorStatement();
        sqlStatement.setSelect(selectStatement);
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        when(result.getSelectStatementContext()).thenReturn(selectStatementContext);
        when(result.getSqlStatement()).thenReturn(sqlStatement);
        return result;
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData(final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
    }
    
    private SelectStatement createSelectStatement() {
        SelectStatement result = new MySQLSelectStatement();
        result.setFrom(new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("tbl"))));
        result.setProjections(new ProjectionsSegment(0, 0));
        return result;
    }
    
    private InsertStatementContext createMySQLInsertStatementContext() {
        MySQLInsertStatement sqlStatement = new MySQLInsertStatement();
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        return new InsertStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), sqlStatement, DefaultDatabase.LOGIC_NAME);
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
        return new InsertStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), sqlStatement, DefaultDatabase.LOGIC_NAME);
    }
}
