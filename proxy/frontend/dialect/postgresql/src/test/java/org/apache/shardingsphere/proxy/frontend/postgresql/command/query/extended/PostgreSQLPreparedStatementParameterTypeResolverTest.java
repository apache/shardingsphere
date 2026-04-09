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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class PostgreSQLPreparedStatementParameterTypeResolverTest {
    
    private static final String SQL = "SELECT id FROM foo_tbl WHERE id=?";
    
    private static final List<Object> PARAMETERS = Collections.singletonList(1);
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(databaseType, new CacheOption(2000, 65535L), new CacheOption(128, 1024L));
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    void assertResolveParameterTypesWithConnectionSession() throws SQLException {
        SQLStatement sqlStatement = sqlParserEngine.parse(SQL, false);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        final PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement(
                SQL, sqlStatementContext, new HintValueContext(), new ArrayList<>(Collections.singletonList(PostgreSQLBinaryColumnType.UNSPECIFIED)), Collections.singletonList(0));
        when(connectionSession.getCurrentDatabaseName()).thenReturn("postgres");
        when(connectionSession.getUsedDatabaseName()).thenReturn("postgres");
        when(connectionSession.getConnectionContext()).thenReturn(mock(ConnectionContext.class));
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        prepareJDBCBackendConnectionWithParamTypes();
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(connectionSession, preparedStatement, PARAMETERS);
        assertThat(preparedStatement.getParameterTypes(), is(Collections.singletonList(PostgreSQLBinaryColumnType.INT4)));
    }
    
    @Test
    void assertResolveParameterTypesWithPreparedStatement() throws SQLException {
        SQLStatement sqlStatement = sqlParserEngine.parse("SELECT id FROM foo_tbl WHERE id=? AND k=?", false);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        final PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement("SELECT id FROM foo_tbl WHERE id=? AND k=?",
                sqlStatementContext, new HintValueContext(), Arrays.asList(PostgreSQLBinaryColumnType.UNSPECIFIED, PostgreSQLBinaryColumnType.INT4), Arrays.asList(0, 1));
        ParameterMetaData parameterMetaData = mock(ParameterMetaData.class);
        when(parameterMetaData.getParameterType(1)).thenReturn(Types.SMALLINT);
        when(parameterMetaData.getParameterTypeName(1)).thenReturn("int2");
        PreparedStatement actualPreparedStatement = mock(PreparedStatement.class);
        when(actualPreparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(preparedStatement, actualPreparedStatement);
        assertThat(preparedStatement.getParameterTypes(), is(Arrays.asList(PostgreSQLBinaryColumnType.INT2, PostgreSQLBinaryColumnType.INT4)));
    }
    
    @Test
    void assertResolveParameterTypesWithConnectionSessionWithoutUnspecified() throws SQLException {
        SQLStatement sqlStatement = sqlParserEngine.parse(SQL, false);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement(
                SQL, sqlStatementContext, new HintValueContext(), Collections.singletonList(PostgreSQLBinaryColumnType.INT4), Collections.singletonList(0));
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(connectionSession, preparedStatement, PARAMETERS);
        verifyNoInteractions(connectionSession);
    }
    
    @Test
    void assertResolveParameterTypesWithPreparedStatementWithoutUnspecified() throws SQLException {
        SQLStatement sqlStatement = sqlParserEngine.parse(SQL, false);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement(
                SQL, sqlStatementContext, new HintValueContext(), Collections.singletonList(PostgreSQLBinaryColumnType.INT4), Collections.singletonList(0));
        PreparedStatement actualPreparedStatement = mock(PreparedStatement.class);
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(preparedStatement, actualPreparedStatement);
        verifyNoInteractions(actualPreparedStatement);
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build())));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        lenient().when(result.getMetaDataContexts().getMetaData().containsDatabase("postgres")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().containsDatabase(new IdentifierValue("postgres"))).thenReturn(true);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getProtocolType()).thenReturn(databaseType);
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getStorageType()).thenReturn(databaseType);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(database.getDefaultSchemaName()).thenReturn("public");
        when(database.getAllSchemas()).thenReturn(Collections.singleton(schema));
        lenient().when(database.containsSchema("public")).thenReturn(true);
        when(database.containsSchema(new IdentifierValue("public"))).thenReturn(true);
        when(database.getSchema("public")).thenReturn(schema);
        when(database.getSchema(new IdentifierValue("public"))).thenReturn(schema);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("k", Types.INTEGER, true, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList());
        lenient().when(schema.containsTable("foo_tbl")).thenReturn(true);
        when(schema.containsTable(new IdentifierValue("foo_tbl"))).thenReturn(true);
        lenient().when(schema.getTable("foo_tbl")).thenReturn(table);
        when(schema.getTable(new IdentifierValue("foo_tbl"))).thenReturn(table);
        when(result.getMetaDataContexts().getMetaData().getDatabase("postgres")).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().getDatabase(new IdentifierValue("postgres"))).thenReturn(database);
        when(result.getDatabase("postgres")).thenReturn(database);
        return result;
    }
    
    private void prepareJDBCBackendConnectionWithParamTypes() throws SQLException {
        ParameterMetaData parameterMetaData = mock(ParameterMetaData.class);
        when(parameterMetaData.getParameterType(1)).thenReturn(Types.INTEGER);
        when(parameterMetaData.getParameterTypeName(1)).thenReturn("int4");
        PreparedStatement preparedStatement = mock(PreparedStatement.class, RETURNS_DEEP_STUBS);
        when(preparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(databaseConnectionManager.getConnections(any(), nullable(String.class), anyInt(), anyInt(), any())).thenReturn(Collections.singletonList(connection));
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
    }
}
