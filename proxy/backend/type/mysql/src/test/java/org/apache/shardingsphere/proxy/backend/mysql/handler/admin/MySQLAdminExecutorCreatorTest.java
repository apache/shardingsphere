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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.AbstractDatabaseMetaDataExecutor.DefaultDatabaseMetaDataExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.MySQLSetVariableAdminExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.MySQLSystemVariableQueryExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.NoResourceShowExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowConnectionIdExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowCreateDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowCurrentDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowCurrentUserExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowDatabasesExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowFunctionStatusExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowProcedureStatusExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowProcessListExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowTablesExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.ShowVersionExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.UnicastResourceShowExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.UseDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.information.SelectInformationSchemataExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLSetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowFunctionStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowProcedureStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowProcessListStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class MySQLAdminExecutorCreatorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private SQLStatementContext sqlStatementContext;
    
    @Test
    void assertCreateWithMySQLShowFunctionStatus() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLShowFunctionStatusStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowFunctionStatusExecutor.class));
    }
    
    @Test
    void assertCreateWithShowProcedureStatus() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLShowProcedureStatusStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowProcedureStatusExecutor.class));
    }
    
    @Test
    void assertCreateWithShowTables() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLShowTablesStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowTablesExecutor.class));
    }
    
    @Test
    void assertCreateWithOtherSQLStatementContext() {
        assertThat(new MySQLAdminExecutorCreator().create(sqlStatementContext), is(Optional.empty()));
    }
    
    @Test
    void assertCreateWithUse() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLUseStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "use db", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(UseDatabaseExecutor.class));
    }
    
    @Test
    void assertCreateWithMySQLShowDatabasesStatement() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLShowDatabasesStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowDatabasesExecutor.class));
    }
    
    @Test
    void assertCreateWithMySQLShowProcessListStatement() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLShowProcessListStatement(false));
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowProcessListExecutor.class));
    }
    
    @Test
    void assertCreateWithMySQLShowCreateDatabaseStatement() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLShowCreateDatabaseStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowCreateDatabaseExecutor.class));
    }
    
    @Test
    void assertCreateWithSetStatement() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLSetStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MySQLSetVariableAdminExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementForShowConnectionId() {
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "CONNECTION_ID()")));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select CONNECTION_ID()", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowConnectionIdExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementForShowVersion() {
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "version()")));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select version()", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowVersionExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementForCurrentUser() {
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "CURRENT_USER()")));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select CURRENT_USER()", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowCurrentUserExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementForTransactionReadOnly() {
        initProxyContext(Collections.emptyMap());
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        VariableSegment variableSegment = new VariableSegment(0, 0, "transaction_read_only");
        variableSegment.setScope("SESSION");
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "@@session.transaction_read_only", variableSegment)));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select @@session.transaction_read_only", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MySQLSystemVariableQueryExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementForTransactionIsolation() {
        initProxyContext(Collections.emptyMap());
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        VariableSegment variableSegment = new VariableSegment(0, 0, "transaction_isolation");
        variableSegment.setScope("SESSION");
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "@@session.transaction_isolation", variableSegment)));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select @@session.transaction_isolation", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MySQLSystemVariableQueryExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementForShowDatabase() {
        initProxyContext(Collections.emptyMap());
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "DATABASE()")));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select DATABASE()", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowCurrentDatabaseExecutor.class));
    }
    
    @Test
    void assertCreateWithOtherSelectStatementForNoResource() {
        initProxyContext(Collections.emptyMap());
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "CURRENT_DATE()")));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select CURRENT_DATE()", null, Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(NoResourceShowExecutor.class));
    }
    
    @Test
    void assertCreateWithOtherSelectStatementForDatabaseName() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.singletonMap("ds", new MockedDataSource()));
        ShardingSphereDatabase database = new ShardingSphereDatabase("db_0", databaseType, resourceMetaData, mock(RuleMetaData.class), Collections.emptyMap());
        Map<String, ShardingSphereDatabase> result = Collections.singletonMap("db_0", database);
        initProxyContext(result);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Collections.singleton("db_0"));
        when(ProxyContext.getInstance().getContextManager().getDatabase("db_0")).thenReturn(database);
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "CURRENT_DATE()")));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select CURRENT_DATE()", "test_db", Collections.emptyList());
        assertThat(actual, is(Optional.empty()));
    }
    
    @Test
    void assertCreateWithOtherSelectStatementForNullDatabaseName() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.singletonMap("ds_0", new MockedDataSource()));
        ShardingSphereDatabase database = new ShardingSphereDatabase("db_0", databaseType, resourceMetaData, mock(RuleMetaData.class), Collections.emptyMap());
        Map<String, ShardingSphereDatabase> result = Collections.singletonMap("db_0", database);
        initProxyContext(result);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Collections.singleton("db_0"));
        when(ProxyContext.getInstance().getContextManager().getDatabase("db_0")).thenReturn(database);
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "CURRENT_DATE()")));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select CURRENT_DATE()", null, Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(UnicastResourceShowExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementFromInformationSchemaOfDefaultExecutorTables() {
        initProxyContext(Collections.emptyMap());
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("ENGINES")));
        tableSegment.setOwner(new OwnerSegment(7, 8, new IdentifierValue("information_schema")));
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(tableSegment));
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select ENGINE from ENGINES", "information_schema", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(DefaultDatabaseMetaDataExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectStatementFromInformationSchemaOfSchemaTable() {
        initProxyContext(Collections.emptyMap());
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("SCHEMATA")));
        tableSegment.setOwner(new OwnerSegment(7, 8, new IdentifierValue("information_schema")));
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(tableSegment));
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select SCHEMA_NAME from SCHEMATA", "information_schema", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(SelectInformationSchemataExecutor.class));
        when(ProxyContext.getInstance().getContextManager().getDatabase("information_schema").isComplete()).thenReturn(true);
        actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select SCHEMA_NAME from SCHEMATA", "information_schema", Collections.emptyList());
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertCreateWithSelectStatementFromInformationSchemaOfOtherTable() {
        initProxyContext(Collections.emptyMap());
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("CHARACTER_SETS")));
        tableSegment.setOwner(new OwnerSegment(7, 8, new IdentifierValue("information_schema")));
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(tableSegment));
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select CHARACTER_SET_NAME from CHARACTER_SETS", "", Collections.emptyList());
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertCreateWithSelectStatementFromPerformanceSchema() {
        initProxyContext(Collections.emptyMap());
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("accounts")));
        tableSegment.setOwner(new OwnerSegment(7, 8, new IdentifierValue("performance_schema")));
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(tableSegment));
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select * from accounts", "", Collections.emptyList());
        assertFalse(actual.isPresent());
    }
    
    private void initProxyContext(final Map<String, ShardingSphereDatabase> databases) {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = MetaDataContextsFactory.create(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties())));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    @Test
    void assertCreateWithDMLStatement() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLDeleteStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "delete from t", "", Collections.emptyList());
        assertThat(actual, is(Optional.empty()));
    }
}
