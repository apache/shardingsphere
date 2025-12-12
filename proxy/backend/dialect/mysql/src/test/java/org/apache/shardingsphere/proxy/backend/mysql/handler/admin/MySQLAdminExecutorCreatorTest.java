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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseMetaDataExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.MySQLSetVariableAdminExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.MySQLSystemVariableQueryExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowCreateDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowDatabasesExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowFunctionStatusExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowProcedureStatusExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowProcessListExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowTablesExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.MySQLUseDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.NoResourceShowExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.SelectInformationSchemataExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.ShowConnectionIdExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.ShowCurrentDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.ShowCurrentUserExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.ShowVersionExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.UnicastResourceShowExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLUseStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.function.MySQLShowFunctionStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.procedure.MySQLShowProcedureStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.process.MySQLShowProcessListStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowTablesStatement;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class MySQLAdminExecutorCreatorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertCreateWithMySQLShowFunctionStatus() {
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(new MySQLShowFunctionStatusStatement(databaseType, null));
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(MySQLShowFunctionStatusExecutor.class));
    }
    
    @Test
    void assertCreateWithShowProcedureStatus() {
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(new MySQLShowProcedureStatusStatement(databaseType, null));
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(MySQLShowProcedureStatusExecutor.class));
    }
    
    @Test
    void assertCreateWithShowTables() {
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(new MySQLShowTablesStatement(databaseType, null, null, false));
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(MySQLShowTablesExecutor.class));
    }
    
    @Test
    void assertCreateWithUse() {
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(new MySQLUseStatement(databaseType, null));
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "use db", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(MySQLUseDatabaseExecutor.class));
    }
    
    @Test
    void assertCreateWithShowDatabasesStatement() {
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(new MySQLShowDatabasesStatement(databaseType, null));
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(MySQLShowDatabasesExecutor.class));
    }
    
    @Test
    void assertCreateWithMySQLShowProcessListStatement() {
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(new MySQLShowProcessListStatement(databaseType, false));
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(MySQLShowProcessListExecutor.class));
    }
    
    @Test
    void assertCreateWithMySQLShowCreateDatabaseStatement() {
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(new MySQLShowCreateDatabaseStatement(databaseType, null));
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(MySQLShowCreateDatabaseExecutor.class));
    }
    
    @Test
    void assertCreateWithSetStatement() {
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(new SetStatement(databaseType, Collections.emptyList()));
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(MySQLSetVariableAdminExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementForShowConnectionId() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "CONNECTION_ID()")));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "SELECT CONNECTION_ID()", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(ShowConnectionIdExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementForShowVersion() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "version()")));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "SELECT version()", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(ShowVersionExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementForCurrentUser() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "CURRENT_USER()")));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "SELECT CURRENT_USER()", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(ShowCurrentUserExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementForTransactionReadOnly() {
        initProxyContext(Collections.emptyList());
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        VariableSegment variableSegment = new VariableSegment(0, 0, "transaction_read_only", "SESSION");
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "@@session.transaction_read_only", variableSegment)));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "SELECT @@session.transaction_read_only", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(MySQLSystemVariableQueryExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementForTransactionIsolation() {
        initProxyContext(Collections.emptyList());
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        VariableSegment variableSegment = new VariableSegment(0, 0, "transaction_isolation", "SESSION");
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "@@session.transaction_isolation", variableSegment)));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "SELECT @@session.transaction_isolation", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(MySQLSystemVariableQueryExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementForShowDatabase() {
        initProxyContext(Collections.emptyList());
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "DATABASE()")));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "SELECT DATABASE()", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(ShowCurrentDatabaseExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementForShowDatabaseWithSpace() {
        initProxyContext(Collections.emptyList());
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 11, "DATABASE ()")));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "SELECT DATABASE ()", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(ShowCurrentDatabaseExecutor.class));
    }
    
    @Test
    void assertCreateWithOtherSelectStatementForNoResource() {
        initProxyContext(Collections.emptyList());
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "CURRENT_DATE()")));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "SELECT CURRENT_DATE()", null, Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(NoResourceShowExecutor.class));
    }
    
    @Test
    void assertCreateWithOtherSelectStatementForDatabaseName() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.singletonMap("ds", new MockedDataSource()));
        ShardingSphereDatabase database = new ShardingSphereDatabase("db_0", databaseType, resourceMetaData, mock(RuleMetaData.class), Collections.emptyList());
        initProxyContext(Collections.singleton(database));
        when(ProxyContext.getInstance().getContextManager().getAllDatabaseNames()).thenReturn(Collections.singleton("db_0"));
        when(ProxyContext.getInstance().getContextManager().getDatabase("db_0")).thenReturn(database);
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "CURRENT_DATE()")));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "SELECT CURRENT_DATE()", "test_db", Collections.emptyList());
        assertThat(actual, is(Optional.empty()));
    }
    
    @Test
    void assertCreateWithOtherSelectStatementForNullDatabaseName() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.singletonMap("ds_0", new MockedDataSource()));
        ShardingSphereDatabase database = new ShardingSphereDatabase("db_0", databaseType, resourceMetaData, mock(RuleMetaData.class), Collections.emptyList());
        initProxyContext(Collections.singleton(database));
        when(ProxyContext.getInstance().getContextManager().getAllDatabaseNames()).thenReturn(Collections.singleton("db_0"));
        when(ProxyContext.getInstance().getContextManager().getDatabase("db_0")).thenReturn(database);
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "CURRENT_DATE()")));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "SELECT CURRENT_DATE()", null, Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(UnicastResourceShowExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementFromInformationSchemaOfDefaultExecutorTables() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("information_schema");
        when(database.getProtocolType()).thenReturn(databaseType);
        initProxyContext(Collections.singleton(database));
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("ENGINES")));
        tableSegment.setOwner(new OwnerSegment(7, 8, new IdentifierValue("information_schema")));
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(tableSegment));
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "SELECT ENGINE from ENGINES", "information_schema", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(DatabaseMetaDataExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementFromInformationSchemaOfSchemaTableWithUnCompletedDatabase() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("information_schema");
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.isComplete()).thenReturn(false);
        initProxyContext(Collections.singleton(database));
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("SCHEMATA")));
        tableSegment.setOwner(new OwnerSegment(7, 8, new IdentifierValue("information_schema")));
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(tableSegment));
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "SELECT SCHEMA_NAME FROM SCHEMATA", "information_schema", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(SelectInformationSchemataExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementFromInformationSchemaOfSchemaTableWithCompletedDatabase() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("information_schema");
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.isComplete()).thenReturn(true);
        initProxyContext(Collections.singleton(database));
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("SCHEMATA")));
        tableSegment.setOwner(new OwnerSegment(7, 8, new IdentifierValue("information_schema")));
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(tableSegment));
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "SELECT SCHEMA_NAME FROM SCHEMATA", "information_schema", Collections.emptyList());
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertCreateWithSelectStatementFromInformationSchemaOfOtherTable() {
        initProxyContext(Collections.emptyList());
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("CHARACTER_SETS")));
        tableSegment.setOwner(new OwnerSegment(7, 8, new IdentifierValue("information_schema")));
        SelectStatement selectStatement = mock(SelectStatement.class);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(tableSegment));
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "SELECT CHARACTER_SET_NAME FROM CHARACTER_SETS", "", Collections.emptyList());
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertCreateWithSelectStatementFromPerformanceSchema() {
        initProxyContext(Collections.emptyList());
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("accounts")));
        tableSegment.setOwner(new OwnerSegment(7, 8, new IdentifierValue("performance_schema")));
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(tableSegment));
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "SELECT * FROM accounts", "", Collections.emptyList());
        assertFalse(actual.isPresent());
    }
    
    private void initProxyContext(final Collection<ShardingSphereDatabase> databases) {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties()));
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics()));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    @Test
    void assertCreateWithDMLStatement() {
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(new DeleteStatement(databaseType));
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "DELETE FROM t", "", Collections.emptyList());
        assertThat(actual, is(Optional.empty()));
    }
}
