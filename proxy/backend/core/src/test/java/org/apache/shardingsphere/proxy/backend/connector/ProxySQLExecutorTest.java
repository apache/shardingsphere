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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.TableAvailableSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableContextAvailable;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.dialect.exception.transaction.TableModifyInTransactionException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProxySQLExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final DatabaseType mysqlDatabaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final DatabaseType postgresqlDatabaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @Mock
    private TransactionRule transactionRule;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        when(databaseConnectionManager.getConnectionSession().getUsedDatabaseName()).thenReturn("foo_db");
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getDatabase("foo_db")).thenReturn(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
        when(metaData.getAllDatabases()).thenReturn(Collections.singleton(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS)));
        when(metaData.getAllDatabases().iterator().next().getProtocolType()).thenReturn(databaseType);
        when(metaData.getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(0);
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.XA);
        when(metaData.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Arrays.asList(mock(SQLFederationRule.class), transactionRule)));
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        when(computeNodeInstanceContext.getModeConfiguration()).thenReturn(mock(ModeConfiguration.class));
        ContextManager contextManager = new ContextManager(new MetaDataContexts(metaData,
                ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics())), computeNodeInstanceContext, mock(), mock(PersistRepository.class, RETURNS_DEEP_STUBS));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteDDLInXATransaction() {
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createCreateTableStatementContext(mysqlDatabaseType), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(),
                        mock(ShardingSphereMetaData.class)),
                Collections.emptyList(), mock(RouteContext.class));
        assertThrows(TableModifyInTransactionException.class,
                () -> new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext));
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        return result;
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteTruncateInMySQLXATransaction() {
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createTruncateStatementContext(mysqlDatabaseType), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class)),
                Collections.emptyList(), mock(RouteContext.class));
        assertThrows(TableModifyInTransactionException.class,
                () -> new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext));
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteTruncateInMySQLLocalTransaction() {
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createTruncateStatementContext(mysqlDatabaseType), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class)),
                Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteDMLInXATransaction() {
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(mockInsertStatementContext(mysqlDatabaseType), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class)),
                Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteDDLInBaseTransaction() {
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.BASE);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createCreateTableStatementContext(mysqlDatabaseType), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(),
                        mock(ShardingSphereMetaData.class)),
                Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteDDLNotInXATransaction() {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(false);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createCreateTableStatementContext(mysqlDatabaseType), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(),
                        mock(ShardingSphereMetaData.class)),
                Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteCreateTableInPostgreSQLTransaction() {
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createCreateTableStatementContext(postgresqlDatabaseType), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(),
                        mock(ShardingSphereMetaData.class)),
                Collections.emptyList(), mock(RouteContext.class));
        assertThrows(TableModifyInTransactionException.class,
                () -> new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext));
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteTruncateInPostgreSQLTransaction() {
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createTruncateStatementContext(postgresqlDatabaseType), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(),
                        mock(ShardingSphereMetaData.class)),
                Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteCursorInPostgreSQLTransaction() {
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(mockCursorStatementContext(), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class)),
                Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteDMLInPostgreSQLTransaction() {
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(mockInsertStatementContext(postgresqlDatabaseType), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(),
                        mock(ShardingSphereMetaData.class)),
                Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteDDLInMySQLTransaction() {
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createCreateTableStatementContext(mysqlDatabaseType), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(),
                        mock(ShardingSphereMetaData.class)),
                Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    private QueryContext mockQueryContext() {
        QueryContext result = mock(QueryContext.class);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, withSettings().extraInterfaces(TableContextAvailable.class).defaultAnswer(RETURNS_DEEP_STUBS));
        when(sqlStatementContext.getDatabaseType()).thenReturn(databaseType);
        when(((TableContextAvailable) sqlStatementContext).getTablesContext().getSchemaName()).thenReturn(Optional.of("foo_db"));
        when(result.getSqlStatementContext()).thenReturn(sqlStatementContext);
        return result;
    }
    
    @Test
    void assertCheckExecutePrerequisitesWhenExecuteDDLNotInPostgreSQLTransaction() {
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(false);
        ExecutionContext executionContext = new ExecutionContext(
                new QueryContext(createCreateTableStatementContext(postgresqlDatabaseType), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(),
                        mock(ShardingSphereMetaData.class)),
                Collections.emptyList(), mock(RouteContext.class));
        new ProxySQLExecutor(JDBCDriverType.STATEMENT, databaseConnectionManager, mock(DatabaseConnector.class), mockQueryContext()).checkExecutePrerequisites(executionContext);
    }
    
    private CreateTableStatementContext createCreateTableStatementContext(final DatabaseType databaseType) {
        CreateTableStatement sqlStatement = mock(CreateTableStatement.class);
        when(sqlStatement.getTable()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        return new CreateTableStatementContext(databaseType, sqlStatement);
    }
    
    private TableAvailableSQLStatementContext createTruncateStatementContext(final DatabaseType databaseType) {
        TruncateStatement sqlStatement = mock(TruncateStatement.class);
        return new TableAvailableSQLStatementContext(databaseType, sqlStatement, Collections.singletonList(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))));
    }
    
    private CursorStatementContext mockCursorStatementContext() {
        CursorStatementContext result = mock(CursorStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        when(result.getDatabaseType()).thenReturn(databaseType);
        return result;
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData(final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singleton(database), mock(ResourceMetaData.class), mock(RuleMetaData.class), mock(ConfigurationProperties.class));
    }
    
    private InsertStatementContext mockInsertStatementContext(final DatabaseType databaseType) {
        InsertStatement sqlStatement = mock(InsertStatement.class);
        when(sqlStatement.getTable()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        return new InsertStatementContext(databaseType, sqlStatement, Collections.emptyList(), createShardingSphereMetaData(database), "foo_db");
    }
}
