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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.AbstractDatabaseMetadataExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.NoResourceShowExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.ShowConnectionIdExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.ShowCreateDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.ShowCurrentDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.ShowCurrentUserExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.ShowDatabasesExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.ShowFunctionStatusExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.ShowProcedureStatusExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.ShowProcessListExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.ShowTablesExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.ShowTransactionExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.ShowVersionExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.UnicastResourceShowExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.UseDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor.information.SelectInformationSchemataExecutor;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
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
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLAdminExecutorCreatorTest extends ProxyContextRestorer {
    
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
    public void assertCreateWithMySQLShowDatabasesStatement() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLShowDatabasesStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "", "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowDatabasesExecutor.class));
    }
    
    @Test
    public void assertCreateWithMySQLShowProcessListStatement() {
        ProxyContext.init(mock(ContextManager.class, RETURNS_DEEP_STUBS));
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLShowProcessListStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "", "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowProcessListExecutor.class));
    }
    
    @Test
    public void assertCreateWithMySQLShowCreateDatabaseStatement() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLShowCreateDatabaseStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "", "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowCreateDatabaseExecutor.class));
    }
    
    @Test
    public void assertCreateWithSetStatement() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLSetStatement());
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "", "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MySQLSetVariableAdminExecutor.class));
    }
    
    @Test
    public void assertCreateWithSelectStatementForShowConnectionId() {
        MySQLSelectStatement mySQLSelectStatement = mock(MySQLSelectStatement.class);
        when(mySQLSelectStatement.getFrom()).thenReturn(null);
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "CONNECTION_ID()")));
        when(mySQLSelectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mySQLSelectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select CONNECTION_ID()", "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowConnectionIdExecutor.class));
    }
    
    @Test
    public void assertCreateWithSelectStatementForShowVersion() {
        MySQLSelectStatement mySQLSelectStatement = mock(MySQLSelectStatement.class);
        when(mySQLSelectStatement.getFrom()).thenReturn(null);
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "version()")));
        when(mySQLSelectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mySQLSelectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select version()", "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowVersionExecutor.class));
    }
    
    @Test
    public void assertCreateWithSelectStatementForCurrentUser() {
        MySQLSelectStatement mySQLSelectStatement = mock(MySQLSelectStatement.class);
        when(mySQLSelectStatement.getFrom()).thenReturn(null);
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "CURRENT_USER()")));
        when(mySQLSelectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mySQLSelectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select CURRENT_USER()", "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowCurrentUserExecutor.class));
    }
    
    @Test
    public void assertCreateWithSelectStatementForTransactionReadOnly() {
        initProxyContext(Collections.emptyMap());
        MySQLSelectStatement mySQLSelectStatement = mock(MySQLSelectStatement.class);
        when(mySQLSelectStatement.getFrom()).thenReturn(null);
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "@@session.transaction_read_only")));
        when(mySQLSelectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mySQLSelectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select @@session.transaction_read_only", "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowTransactionExecutor.class));
    }
    
    @Test
    public void assertCreateWithSelectStatementForTransactionIsolation() {
        initProxyContext(Collections.emptyMap());
        MySQLSelectStatement mySQLSelectStatement = mock(MySQLSelectStatement.class);
        when(mySQLSelectStatement.getFrom()).thenReturn(null);
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "@@session.transaction_isolation")));
        when(mySQLSelectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mySQLSelectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select @@session.transaction_isolation", "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowTransactionExecutor.class));
    }
    
    @Test
    public void assertCreateWithSelectStatementForShowDatabase() {
        initProxyContext(Collections.emptyMap());
        MySQLSelectStatement mySQLSelectStatement = mock(MySQLSelectStatement.class);
        when(mySQLSelectStatement.getFrom()).thenReturn(null);
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "DATABASE()")));
        when(mySQLSelectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mySQLSelectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select DATABASE()", "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowCurrentDatabaseExecutor.class));
    }
    
    @Test
    public void assertCreateWithOtherSelectStatementForNoResource() {
        initProxyContext(Collections.emptyMap());
        MySQLSelectStatement mySQLSelectStatement = mock(MySQLSelectStatement.class);
        when(mySQLSelectStatement.getFrom()).thenReturn(null);
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "CURRENT_DATE()")));
        when(mySQLSelectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mySQLSelectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select CURRENT_DATE()", null);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(NoResourceShowExecutor.class));
    }
    
    @Test
    public void assertCreateWithOtherSelectStatementForDatabaseName() {
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(1, 1);
        ShardingSphereResource resource = new ShardingSphereResource(Collections.singletonMap("ds", new MockedDataSource()));
        ShardingSphereDatabase database = new ShardingSphereDatabase("db_0", mock(DatabaseType.class), resource, mock(ShardingSphereRuleMetaData.class), Collections.emptyMap());
        result.put("db_0", database);
        initProxyContext(result);
        MySQLSelectStatement mySQLSelectStatement = mock(MySQLSelectStatement.class);
        when(mySQLSelectStatement.getFrom()).thenReturn(null);
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "CURRENT_DATE()")));
        when(mySQLSelectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mySQLSelectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select CURRENT_DATE()", "test_db");
        assertThat(actual, is(Optional.empty()));
    }
    
    @Test
    public void assertCreateWithOtherSelectStatementForNullDatabaseName() {
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(1, 1);
        ShardingSphereResource resource = new ShardingSphereResource(Collections.singletonMap("ds_0", new MockedDataSource()));
        ShardingSphereDatabase database = new ShardingSphereDatabase("db_0", mock(DatabaseType.class), resource, mock(ShardingSphereRuleMetaData.class), Collections.emptyMap());
        result.put("db_0", database);
        initProxyContext(result);
        MySQLSelectStatement mySQLSelectStatement = mock(MySQLSelectStatement.class);
        when(mySQLSelectStatement.getFrom()).thenReturn(null);
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(0, 10, "CURRENT_DATE()")));
        when(mySQLSelectStatement.getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mySQLSelectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select CURRENT_DATE()", null);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(UnicastResourceShowExecutor.class));
    }
    
    @Test
    public void assertCreateWithSelectStatementFromInformationSchemaOfDefaultExecutorTables() {
        initProxyContext(Collections.emptyMap());
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("ENGINES")));
        tableSegment.setOwner(new OwnerSegment(7, 8, new IdentifierValue("information_schema")));
        MySQLSelectStatement mySQLSelectStatement = mock(MySQLSelectStatement.class);
        when(mySQLSelectStatement.getFrom()).thenReturn(tableSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mySQLSelectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select ENGINE from ENGINES", "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(AbstractDatabaseMetadataExecutor.DefaultDatabaseMetadataExecutor.class));
    }
    
    @Test
    public void assertCreateWithSelectStatementFromInformationSchemaOfSchemaTable() {
        initProxyContext(Collections.emptyMap());
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("SCHEMATA")));
        tableSegment.setOwner(new OwnerSegment(7, 8, new IdentifierValue("information_schema")));
        MySQLSelectStatement mySQLSelectStatement = mock(MySQLSelectStatement.class);
        when(mySQLSelectStatement.getFrom()).thenReturn(tableSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mySQLSelectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select SCHEMA_NAME from SCHEMATA", "");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(SelectInformationSchemataExecutor.class));
    }
    
    @Test
    public void assertCreateWithSelectStatementFromInformationSchemaOfOtherTable() {
        initProxyContext(Collections.emptyMap());
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("CHARACTER_SETS")));
        tableSegment.setOwner(new OwnerSegment(7, 8, new IdentifierValue("information_schema")));
        MySQLSelectStatement mySQLSelectStatement = mock(MySQLSelectStatement.class);
        when(mySQLSelectStatement.getFrom()).thenReturn(tableSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mySQLSelectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select CHARACTER_SET_NAME from CHARACTER_SETS", "");
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertCreateWithSelectStatementFromPerformanceSchema() {
        initProxyContext(Collections.emptyMap());
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("accounts")));
        tableSegment.setOwner(new OwnerSegment(7, 8, new IdentifierValue("performance_schema")));
        MySQLSelectStatement mySQLSelectStatement = mock(MySQLSelectStatement.class);
        when(mySQLSelectStatement.getFrom()).thenReturn(tableSegment);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mySQLSelectStatement);
        Optional<DatabaseAdminExecutor> actual = new MySQLAdminExecutorCreator().create(sqlStatementContext, "select * from accounts", "");
        assertFalse(actual.isPresent());
    }
    
    private void initProxyContext(final Map<String, ShardingSphereDatabase> databases) {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(databases, mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
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
