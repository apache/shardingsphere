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
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.parse.PostgreSQLComParseExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.postgresql.util.PGobject;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostgreSQLParameterTypeResolutionIntegrationTest {
    
    private static final String DATABASE_NAME = "test_db";
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    void assertDescribeFirstResolvesVarcharAndIntParametersViaBinder() {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(connectionSession.getUsedDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getCurrentDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        
        String sql = "INSERT INTO tbl (int_col, varchar_col) VALUES ($1, $2)";
        PostgreSQLServerPreparedStatement preparedStatement = parseViaExecutor(sql, contextManager);
        
        assertThat(preparedStatement.getParameterTypes().get(0), is(PostgreSQLBinaryColumnType.UNSPECIFIED));
        assertThat(preparedStatement.getParameterTypes().get(1), is(PostgreSQLBinaryColumnType.UNSPECIFIED));
        
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(connectionSession, preparedStatement);
        
        assertThat(preparedStatement.getParameterTypes().get(0), is(PostgreSQLBinaryColumnType.INT4));
        assertThat(preparedStatement.getParameterTypes().get(1), is(PostgreSQLBinaryColumnType.VARCHAR));
    }
    
    @Test
    void assertBindFirstDoesNotDecodeBeforeTypeIsResolved() {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(connectionSession.getUsedDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getCurrentDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        
        String sql = "INSERT INTO tbl (int_col) VALUES ($1)";
        PostgreSQLServerPreparedStatement preparedStatement = parseViaExecutor(sql, contextManager);
        
        assertFalse(preparedStatement.getParameterTypeStates().get(0).isResolved());
        
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(connectionSession, preparedStatement);
        assertTrue(preparedStatement.getParameterTypeStates().get(0).isResolved());
        
        Object decoded = preparedStatement.getParameterTypeStates().get(0).decode("42");
        assertThat(decoded, is(42));
    }
    
    @Test
    void assertRepeatedResolveReusesExistingState() {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(connectionSession.getUsedDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getCurrentDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        
        String sql = "INSERT INTO tbl (jsonb_col) VALUES ($1)";
        PostgreSQLServerPreparedStatement preparedStatement = parseViaExecutor(sql, contextManager);
        
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(connectionSession, preparedStatement);
        PostgreSQLBinaryColumnType resolvedAfterFirst = preparedStatement.getParameterTypes().get(0);
        assertThat(resolvedAfterFirst, is(PostgreSQLBinaryColumnType.JSONB));
        
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(connectionSession, preparedStatement);
        assertThat(preparedStatement.getParameterTypes().get(0), is(PostgreSQLBinaryColumnType.JSONB));
    }
    
    @Test
    void assertAlreadyResolvedStateSkipsJdbcWork() {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(connectionSession.getUsedDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getCurrentDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        
        String sql = "INSERT INTO tbl (int_col) VALUES ($1)";
        PostgreSQLServerPreparedStatement preparedStatement = parseViaExecutor(sql, contextManager);
        
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(connectionSession, preparedStatement);
        assertTrue(preparedStatement.isParameterTypesResolved());
        
        ConnectionSession freshSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(freshSession, preparedStatement);
        verifyNoInteractions(freshSession);
    }
    
    @Test
    void assertJsonColumnResolvesToOid114AndDecodesAsPGobjectJson() {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(connectionSession.getUsedDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getCurrentDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        
        String sql = "INSERT INTO tbl (json_col) VALUES ($1)";
        PostgreSQLServerPreparedStatement preparedStatement = parseViaExecutor(sql, contextManager);
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(connectionSession, preparedStatement);
        
        assertThat(preparedStatement.getParameterTypes().get(0), is(PostgreSQLBinaryColumnType.JSON));
        assertThat(preparedStatement.getParameterTypes().get(0).getValue(), is(114));
        assertTrue(preparedStatement.getParameterTypeStates().get(0).isResolved());
        
        Object decoded = preparedStatement.getParameterTypeStates().get(0).decode("{\"k\":1}");
        assertTrue(decoded instanceof PGobject);
        assertThat(((PGobject) decoded).getType(), is("json"));
        assertThat(((PGobject) decoded).getValue(), is("{\"k\":1}"));
    }
    
    @Test
    void assertJsonbColumnResolvesToOid3802AndDecodesAsPGobjectJsonbNotJson() {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(connectionSession.getUsedDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getCurrentDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        
        String sql = "INSERT INTO tbl (jsonb_col) VALUES ($1)";
        PostgreSQLServerPreparedStatement preparedStatement = parseViaExecutor(sql, contextManager);
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(connectionSession, preparedStatement);
        
        assertThat(preparedStatement.getParameterTypes().get(0), is(PostgreSQLBinaryColumnType.JSONB));
        assertThat(preparedStatement.getParameterTypes().get(0).getValue(), is(3802));
        assertTrue(preparedStatement.getParameterTypeStates().get(0).isResolved());
        
        Object decoded = preparedStatement.getParameterTypeStates().get(0).decode("{\"k\":1}");
        assertTrue(decoded instanceof PGobject);
        assertThat(((PGobject) decoded).getType(), is("jsonb"));
        assertThat(((PGobject) decoded).getValue(), is("{\"k\":1}"));
    }
    
    @Test
    void assertUuidColumnResolvesToOid2950AndDecodesAsString() {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(connectionSession.getUsedDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getCurrentDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        
        String sql = "INSERT INTO tbl (uuid_col) VALUES ($1)";
        PostgreSQLServerPreparedStatement preparedStatement = parseViaExecutor(sql, contextManager);
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(connectionSession, preparedStatement);
        
        assertThat(preparedStatement.getParameterTypes().get(0), is(PostgreSQLBinaryColumnType.UUID));
        assertThat(preparedStatement.getParameterTypes().get(0).getValue(), is(2950));
        
        Object decoded = preparedStatement.getParameterTypeStates().get(0).decode("550e8400-e29b-41d4-a716-446655440000");
        assertThat(decoded, is("550e8400-e29b-41d4-a716-446655440000"));
    }
    
    @Test
    void assertCustomEnumTypePreservesNativeTypeNameAndWireOid() {
        PostgreSQLPreparedStatementParameterType customType =
                PostgreSQLPreparedStatementParameterType.valueOf(Types.OTHER, "my_status_enum", 99999);
        
        assertTrue(customType.isResolved());
        assertTrue(customType.isNativeType());
        assertThat(customType.getWireOID(), is(99999));
        assertThat(customType.getNativeTypeName(), is("my_status_enum"));
        
        Object decoded = customType.decode("active");
        assertTrue(decoded instanceof PGobject);
        assertThat(((PGobject) decoded).getType(), is("my_status_enum"));
        assertThat(((PGobject) decoded).getValue(), is("active"));
    }
    
    @Test
    void assertTypesOtherWithoutWireOidRemainsUnresolved() {
        PostgreSQLPreparedStatementParameterType noOid =
                PostgreSQLPreparedStatementParameterType.valueOf(Types.OTHER, "some_type", null);
        
        assertFalse(noOid.isNativeType());
        assertFalse(noOid.isResolved());
    }
    
    @Test
    void assertCrossSchemaParameterResolvesAgainstCorrectOwnership() {
        ContextManager contextManager = mockCrossSchemaContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(connectionSession.getUsedDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getCurrentDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        
        String sql = "INSERT INTO tbl (int_col) VALUES ($1)";
        PostgreSQLServerPreparedStatement preparedStatement = parseViaExecutor(sql, contextManager);
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(connectionSession, preparedStatement);
        
        assertThat(preparedStatement.getParameterTypes().get(0), is(PostgreSQLBinaryColumnType.INT4));
    }
    
    private PostgreSQLServerPreparedStatement parseViaExecutor(final String sql, final ContextManager contextManager) {
        String statementId = "S_" + sql.hashCode();
        PostgreSQLComParsePacket parsePacket = mock(PostgreSQLComParsePacket.class);
        when(parsePacket.getSQL()).thenReturn(sql);
        when(parsePacket.getStatementId()).thenReturn(statementId);
        when(parsePacket.readParameterTypes()).thenReturn(Collections.emptyList());
        when(parsePacket.getHintValueContext()).thenReturn(new HintValueContext());
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        
        PostgreSQLComParseExecutor parseExecutor = new PostgreSQLComParseExecutor(parsePacket, connectionSession);
        
        parseExecutor.execute();
        return connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(statementId);
    }
    
    private ContextManager mockContextManager() {
        return mockContextManager(createSingleSchemaDatabase());
    }
    
    private ContextManager mockContextManager(final ShardingSphereDatabase database) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData())
                .thenReturn(new RuleMetaData(Arrays.asList(
                        new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()),
                        new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()))));
        when(result.getMetaDataContexts().getMetaData().getDatabase(DATABASE_NAME)).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().getDatabase(new IdentifierValue(DATABASE_NAME))).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().containsDatabase(DATABASE_NAME)).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().containsDatabase(new IdentifierValue(DATABASE_NAME))).thenReturn(true);
        return result;
    }
    
    private ContextManager mockCrossSchemaContextManager() {
        return mockContextManager(createCrossSchemaDatabase());
    }
    
    private ShardingSphereDatabase createSingleSchemaDatabase() {
        ShardingSphereTable table = new ShardingSphereTable("tbl", Arrays.asList(
                new ShardingSphereColumn("int_col", Types.INTEGER, false, false, false, true, false, false, "int4"),
                new ShardingSphereColumn("varchar_col", Types.VARCHAR, false, false, false, true, false, false, "varchar"),
                new ShardingSphereColumn("json_col", Types.OTHER, false, false, false, true, false, false, "json"),
                new ShardingSphereColumn("jsonb_col", Types.OTHER, false, false, false, true, false, false, "jsonb"),
                new ShardingSphereColumn("uuid_col", Types.OTHER, false, false, false, true, false, false, "uuid")),
                Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("public", DATABASE_TYPE, Collections.singletonList(table), Collections.emptyList());
        
        return new ShardingSphereDatabase(DATABASE_NAME, DATABASE_TYPE,
                new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()),
                Collections.singletonList(schema),
                new ConfigurationProperties(new Properties()));
    }
    
    private ShardingSphereDatabase createCrossSchemaDatabase() {
        ShardingSphereTable publicTable = new ShardingSphereTable("tbl", Collections.singletonList(
                new ShardingSphereColumn("int_col", Types.INTEGER, false, false, false, true, false, false, "int4")),
                Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema publicSchema = new ShardingSphereSchema("public", DATABASE_TYPE,
                Collections.singletonList(publicTable), Collections.emptyList());
        
        ShardingSphereTable otherTable = new ShardingSphereTable("tbl", Collections.singletonList(
                new ShardingSphereColumn("int_col", Types.VARCHAR, false, false, false, true, false, false, "varchar")),
                Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema otherSchema = new ShardingSphereSchema("other", DATABASE_TYPE,
                Collections.singletonList(otherTable), Collections.emptyList());
        
        return new ShardingSphereDatabase(DATABASE_NAME, DATABASE_TYPE,
                new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()),
                Arrays.asList(publicSchema, otherSchema),
                new ConfigurationProperties(new Properties()));
    }
}
