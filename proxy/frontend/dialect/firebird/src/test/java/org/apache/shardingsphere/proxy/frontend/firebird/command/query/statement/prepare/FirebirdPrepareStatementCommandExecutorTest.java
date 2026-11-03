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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.prepare;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.firebird.metadata.data.FirebirdBlobInfoRegistry;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdPrepareStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdPrepareStatementReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdReturnColumnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementResourceCleaner;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.DropDatabaseStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FirebirdPrepareStatementCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    @Mock
    private FirebirdPrepareStatementPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @BeforeEach
    void setUp() {
        FirebirdFetchStatementCache.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        when(connectionSession.getConnectionContext().getGrantee()).thenReturn(new Grantee("foo_user"));
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getSQL()).thenReturn("SELECT 1");
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        when(packet.isValidStatementHandle()).thenReturn(true);
        when(packet.getStatementId()).thenReturn(1);
        when(packet.nextItem()).thenReturn(true, false);
        when(packet.getCurrentItem()).thenReturn(FirebirdSQLInfoPacketType.STMT_TYPE);
        MetaDataContexts metaDataContexts = createMetaDataContexts();
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts()).thenReturn(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager().getDatabase("foo_db")).thenReturn(metaDataContexts.getMetaData().getDatabase("foo_db"));
        when(ProxyContext.getInstance().getContextManager().getDatabase(null)).thenThrow(NoDatabaseSelectedException.class);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdFetchStatementCache.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobInfoRegistry.refreshTable("foo_db", "foo_tbl", Collections.emptyMap());
        FirebirdBlobInfoRegistry.refreshTable("foo_db", "foo_tbl_0", Collections.emptyMap());
    }
    
    private MetaDataContexts createMetaDataContexts() {
        SQLParserRule parserRule = new SQLParserRule(new SQLParserRuleConfiguration(new CacheOption(128, 1024L), new CacheOption(128, 1024L)));
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(parserRule));
        ShardingSphereColumn column = new ShardingSphereColumn("id", Types.INTEGER, false, false, true, true, false, true);
        ShardingSphereColumn blobColumn = new ShardingSphereColumn("content", Types.BLOB, false, false, true, true, false, true);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Arrays.asList(column, blobColumn), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(table), Collections.emptyList());
        ShardingSphereDatabase database = spy(new ShardingSphereDatabase(
                "foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.singleton(schema),
                new ConfigurationProperties(new Properties())));
        doReturn(Optional.of(schema)).when(database).findDefaultSchema();
        doReturn(null).when(database).getSchema("FOO_DB");
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singleton(database), new ResourceMetaData(Collections.emptyMap()), globalRuleMetaData, new ConfigurationProperties(new Properties()));
        return new MetaDataContexts(metaData, new ShardingSphereStatistics());
    }
    
    @Test
    void assertExecute() throws Exception {
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getType(), is(FirebirdSQLInfoReturnValue.SELECT));
        FirebirdServerPreparedStatement preparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1);
        assertThat(preparedStatement.getSql(), is("SELECT 1"));
        assertThat(preparedStatement.getSqlStatementContext(), isA(SelectStatementContext.class));
    }
    
    private FirebirdPrepareStatementReturnPacket executeAndGetReturnPacket() throws SQLException {
        return (FirebirdPrepareStatementReturnPacket) ((FirebirdGenericResponsePacket) new FirebirdPrepareStatementCommandExecutor(packet, connectionSession).execute().iterator().next()).getData();
    }
    
    @Test
    void assertExecuteDropDatabaseWithoutNameResolvesCurrentDatabase() throws Exception {
        when(packet.getSQL()).thenReturn("DROP DATABASE");
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getType(), is(FirebirdSQLInfoReturnValue.DDL));
        FirebirdServerPreparedStatement preparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1);
        assertThat(((DropDatabaseStatement) preparedStatement.getSqlStatementContext().getSqlStatement()).getDatabaseName(), is("foo_db"));
    }
    
    @Test
    void assertDescribeParametersOfUnionWithTwoBranches() throws Exception {
        FirebirdPrepareStatementReturnPacket actual = describeBind("SELECT id FROM foo_tbl WHERE content = ? UNION ALL SELECT id FROM foo_tbl WHERE id = ?");
        assertThat(actual.getDescribeBind().size(), is(2));
        assertParameterType(actual.getDescribeBind().get(0), FirebirdBinaryColumnType.BLOB);
        assertParameterType(actual.getDescribeBind().get(1), FirebirdBinaryColumnType.LONG);
    }
    
    private FirebirdPrepareStatementReturnPacket describeBind(final String sql) throws Exception {
        when(packet.getSQL()).thenReturn(sql);
        when(packet.nextItem()).thenReturn(true, true, true, true, true, false);
        when(packet.getCurrentItem()).thenReturn(
                FirebirdSQLInfoPacketType.STMT_TYPE,
                FirebirdSQLInfoPacketType.BIND,
                FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.DESCRIBE_END,
                FirebirdSQLInfoPacketType.DESCRIBE_END);
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        return (FirebirdPrepareStatementReturnPacket) ((FirebirdGenericResponsePacket) executor.execute().iterator().next()).getData();
    }
    
    private void assertParameterType(final FirebirdReturnColumnPacket actual, final FirebirdBinaryColumnType expected) {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        actual.write(payload);
        verify(payload).writeInt4LE(expected.getValue() + 1);
    }
    
    @Test
    void assertDescribeParametersOfUnionWithThreeBranches() throws Exception {
        String sql = "SELECT id FROM foo_tbl WHERE id = ? UNION ALL SELECT id FROM foo_tbl WHERE content = ? UNION ALL SELECT id FROM foo_tbl WHERE id = ?";
        FirebirdPrepareStatementReturnPacket actual = describeBind(sql);
        assertThat(actual.getDescribeBind().size(), is(3));
        assertParameterType(actual.getDescribeBind().get(0), FirebirdBinaryColumnType.LONG);
        assertParameterType(actual.getDescribeBind().get(1), FirebirdBinaryColumnType.BLOB);
        assertParameterType(actual.getDescribeBind().get(2), FirebirdBinaryColumnType.LONG);
    }
    
    @Test
    void assertExecuteDropDatabaseWithoutNameAndCurrentDatabase() {
        when(packet.getSQL()).thenReturn("DROP DATABASE");
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        assertThrows(NoDatabaseSelectedException.class, executor::execute);
    }
    
    @Test
    void assertDescribeCountReturnsBigintType() throws Exception {
        when(packet.getSQL()).thenReturn("SELECT COUNT(*) FROM foo_tbl");
        mockDescribeRequest(FirebirdSQLInfoPacketType.SELECT);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getDescribeSelect().size(), is(1));
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        FirebirdReturnColumnPacket actualColumnPacket = actualReturnPacket.getDescribeSelect().get(0);
        actualColumnPacket.write(payload);
        verify(payload).writeInt4LE(FirebirdBinaryColumnType.INT64.getValue() + 1);
    }
    
    @Test
    void assertDescribeBlobColumnRegisteredInBlobInfoRegistryReturnsBlobTypeAndSubtype() throws Exception {
        FirebirdBlobInfoRegistry.refreshTable("foo_db", "foo_tbl", Collections.singletonMap("id", 7));
        FirebirdReturnColumnPacket columnPacket = describeSingleColumn("SELECT id FROM foo_tbl");
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        columnPacket.write(payload);
        verify(payload).writeInt4LE(FirebirdBinaryColumnType.BLOB.getValue() + 1);
        verify(payload).writeInt4LE(7);
    }
    
    @Test
    void assertDescribeBlobColumnOfShardingTable() throws Exception {
        DataNodeRuleAttribute dataNodeRuleAttribute = mock(DataNodeRuleAttribute.class);
        when(dataNodeRuleAttribute.findFirstActualTable("foo_tbl")).thenReturn(Optional.of("foo_tbl_0"));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(dataNodeRuleAttribute));
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase("foo_db");
        doReturn(new RuleMetaData(Collections.singleton(rule))).when(database).getRuleMetaData();
        FirebirdBlobInfoRegistry.refreshTable("foo_db", "foo_tbl_0", Collections.singletonMap("id", 7));
        FirebirdReturnColumnPacket actualColumnPacket = describeSingleColumn("SELECT id FROM foo_tbl");
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        actualColumnPacket.write(payload);
        verify(payload).writeInt4LE(FirebirdBinaryColumnType.BLOB.getValue() + 1);
        verify(payload).writeInt4LE(7);
    }
    
    @Test
    void assertDescribeBlobTypeColumnWithoutRegistryEntryReturnsBlobTypeAndDefaultSubtype() throws Exception {
        FirebirdReturnColumnPacket columnPacket = describeSingleColumn("SELECT content FROM foo_tbl");
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        columnPacket.write(payload);
        verify(payload).writeInt4LE(FirebirdBinaryColumnType.BLOB.getValue() + 1);
        verify(payload).writeInt4LE(FirebirdBinaryColumnType.BLOB.getSubtype());
    }
    
    @Test
    void assertDescribeDerivedTableSelect() throws Exception {
        when(packet.getSQL()).thenReturn("SELECT * FROM (SELECT id FROM foo_tbl) derived_tbl");
        mockDescribeRequest(FirebirdSQLInfoPacketType.SELECT);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getType(), is(FirebirdSQLInfoReturnValue.SELECT));
        assertThat(actualReturnPacket.getDescribeSelect().size(), is(1));
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        FirebirdReturnColumnPacket actualColumnPacket = actualReturnPacket.getDescribeSelect().get(0);
        actualColumnPacket.write(payload);
        verify(payload).writeInt4LE(FirebirdBinaryColumnType.LONG.getValue() + 1);
    }
    
    private FirebirdReturnColumnPacket describeSingleColumn(final String sql) throws Exception {
        when(packet.getSQL()).thenReturn(sql);
        when(packet.nextItem()).thenReturn(true, true, true, true, true, true, false);
        when(packet.getCurrentItem()).thenReturn(
                FirebirdSQLInfoPacketType.STMT_TYPE,
                FirebirdSQLInfoPacketType.SELECT,
                FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.SUB_TYPE,
                FirebirdSQLInfoPacketType.SUB_TYPE,
                FirebirdSQLInfoPacketType.DESCRIBE_END,
                FirebirdSQLInfoPacketType.DESCRIBE_END);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getDescribeSelect().size(), is(1));
        return actualReturnPacket.getDescribeSelect().get(0);
    }
    
    @Test
    void assertDescribeBinaryExpressions() throws Exception {
        when(packet.getSQL()).thenReturn("SELECT 'foo' || 'bar', 1 + 2 FROM foo_tbl");
        when(packet.nextItem()).thenReturn(true, true, true, true, true, true, false);
        when(packet.getCurrentItem()).thenReturn(
                FirebirdSQLInfoPacketType.STMT_TYPE,
                FirebirdSQLInfoPacketType.SELECT,
                FirebirdSQLInfoPacketType.FIELD,
                FirebirdSQLInfoPacketType.FIELD,
                FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.DESCRIBE_END,
                FirebirdSQLInfoPacketType.DESCRIBE_END);
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = (FirebirdPrepareStatementReturnPacket) ((FirebirdGenericResponsePacket) executor.execute().iterator().next()).getData();
        assertThat(actualReturnPacket.getDescribeSelect().size(), is(2));
        FirebirdPacketPayload concatenationPayload = mock(FirebirdPacketPayload.class);
        when(concatenationPayload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        actualReturnPacket.getDescribeSelect().get(0).write(concatenationPayload);
        verify(concatenationPayload).writeBytes("CONCATENATION".getBytes(StandardCharsets.UTF_8));
        verify(concatenationPayload).writeInt4LE(FirebirdBinaryColumnType.VARYING.getValue() + 1);
        FirebirdPacketPayload arithmeticPayload = mock(FirebirdPacketPayload.class);
        when(arithmeticPayload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        actualReturnPacket.getDescribeSelect().get(1).write(arithmeticPayload);
        verify(arithmeticPayload).writeBytes("+".getBytes(StandardCharsets.UTF_8));
        verify(arithmeticPayload).writeInt4LE(FirebirdBinaryColumnType.LONG.getValue() + 1);
    }
    
    @Test
    void assertDescribeLiterals() throws Exception {
        when(packet.getSQL()).thenReturn("SELECT 'it''s', 1 AS one FROM foo_tbl");
        when(packet.nextItem()).thenReturn(true, true, true, true, true, true, true, false);
        when(packet.getCurrentItem()).thenReturn(
                FirebirdSQLInfoPacketType.STMT_TYPE,
                FirebirdSQLInfoPacketType.SELECT,
                FirebirdSQLInfoPacketType.FIELD,
                FirebirdSQLInfoPacketType.FIELD,
                FirebirdSQLInfoPacketType.ALIAS,
                FirebirdSQLInfoPacketType.ALIAS,
                FirebirdSQLInfoPacketType.LENGTH,
                FirebirdSQLInfoPacketType.LENGTH,
                FirebirdSQLInfoPacketType.DESCRIBE_END,
                FirebirdSQLInfoPacketType.DESCRIBE_END);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getDescribeSelect().size(), is(2));
        FirebirdPacketPayload stringPayload = mock(FirebirdPacketPayload.class);
        when(stringPayload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        actualReturnPacket.getDescribeSelect().get(0).write(stringPayload);
        verify(stringPayload, times(2)).writeBytes("CONSTANT".getBytes(StandardCharsets.UTF_8));
        verify(stringPayload).writeInt4LE(4);
        FirebirdPacketPayload numberPayload = mock(FirebirdPacketPayload.class);
        when(numberPayload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        actualReturnPacket.getDescribeSelect().get(1).write(numberPayload);
        verify(numberPayload).writeBytes("CONSTANT".getBytes(StandardCharsets.UTF_8));
        verify(numberPayload).writeBytes("one".getBytes(StandardCharsets.UTF_8));
    }
    
    @Test
    void assertExecuteWithValidStatementHandleCleansPreviousPreparedStatementResources() throws Exception {
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(1, new FirebirdServerPreparedStatement("SELECT 0", mock(SelectStatementContext.class), new HintValueContext()));
        FirebirdFetchStatementCache.getInstance().registerStatement(CONNECTION_ID, 1, proxyBackendHandler);
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        executor.execute();
        verify(connectionSession).invalidatePreparedStatementCache(FirebirdStatementResourceCleaner.createPreparedStatementCacheKey(1));
        assertThat(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1).getSql(), is("SELECT 1"));
        assertThat(FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(CONNECTION_ID, 1), is((ProxyBackendHandler) null));
    }
    
    @Test
    void assertExecuteWithValidStatementHandleWithoutFetchHandler() throws Exception {
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        executor.execute();
        verify(connectionSession).invalidatePreparedStatementCache(FirebirdStatementResourceCleaner.createPreparedStatementCacheKey(1));
    }
    
    @Test
    void assertDescribeLiteralWithoutDatabase() throws Exception {
        when(connectionSession.getCurrentDatabaseName()).thenReturn(null);
        mockDescribeRequest(FirebirdSQLInfoPacketType.SELECT);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getDescribeSelect().size(), is(1));
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        actualReturnPacket.getDescribeSelect().get(0).write(payload);
        verify(payload).writeInt4LE(FirebirdBinaryColumnType.LONG.getValue() + 1);
    }
    
    private void mockDescribeRequest(final FirebirdSQLInfoPacketType describeType) {
        when(packet.nextItem()).thenReturn(true, true, true, true, true, false);
        when(packet.getCurrentItem()).thenReturn(FirebirdSQLInfoPacketType.STMT_TYPE, describeType, FirebirdSQLInfoPacketType.TYPE, FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.DESCRIBE_END, FirebirdSQLInfoPacketType.DESCRIBE_END);
    }
    
    @Test
    void assertDescribeParameterWithoutDatabase() throws Exception {
        when(connectionSession.getCurrentDatabaseName()).thenReturn(null);
        when(packet.getSQL()).thenReturn("SELECT ?");
        mockDescribeRequest(FirebirdSQLInfoPacketType.BIND);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getDescribeBind().size(), is(1));
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        actualReturnPacket.getDescribeBind().get(0).write(payload);
        verify(payload).writeInt4LE(FirebirdBinaryColumnType.VARYING.getValue() + 1);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("describeTableArguments")
    void assertDescribeTable(final String name, final String sql, final boolean defaultSchemaPresent, final FirebirdBinaryColumnType columnType) throws Exception {
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getDatabase("foo_db");
        addSystemSchema(database);
        if (!defaultSchemaPresent) {
            database.dropSchema("foo_db");
            doReturn(Optional.empty()).when(database).findDefaultSchema();
        }
        when(packet.getSQL()).thenReturn(sql);
        mockDescribeRequest(FirebirdSQLInfoPacketType.SELECT);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getDescribeSelect().size(), is(1));
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        actualReturnPacket.getDescribeSelect().get(0).write(payload);
        verify(payload).writeInt4LE(columnType.getValue() + 1);
    }
    
    private void addSystemSchema(final ShardingSphereDatabase database) {
        ShardingSphereColumn column = new ShardingSphereColumn("RDB$RELATION_ID", Types.SMALLINT, false, false, false, true, false, true);
        ShardingSphereTable table = new ShardingSphereTable("RDB$DATABASE", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        database.addSchema(new ShardingSphereSchema("system_tables", databaseType, Collections.singleton(table), Collections.emptyList()));
    }
    
    private static Stream<Arguments> describeTableArguments() {
        return Stream.of(
                Arguments.of("Default schema table", "SELECT id FROM foo_tbl", true, FirebirdBinaryColumnType.LONG),
                Arguments.of("System table with default schema", "SELECT RDB$RELATION_ID FROM RDB$DATABASE", true, FirebirdBinaryColumnType.SHORT),
                Arguments.of("System table without default schema", "SELECT RDB$RELATION_ID FROM RDB$DATABASE", false, FirebirdBinaryColumnType.SHORT));
    }
    
    @Test
    void assertDescribeSystemTableParameterWithoutDefaultSchema() throws Exception {
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getDatabase("foo_db");
        addSystemSchema(database);
        database.dropSchema("foo_db");
        doReturn(Optional.empty()).when(database).findDefaultSchema();
        when(packet.getSQL()).thenReturn("SELECT RDB$RELATION_ID FROM RDB$DATABASE WHERE RDB$RELATION_ID = ?");
        mockDescribeRequest(FirebirdSQLInfoPacketType.BIND);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getDescribeBind().size(), is(1));
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        actualReturnPacket.getDescribeBind().get(0).write(payload);
        verify(payload).writeInt4LE(FirebirdBinaryColumnType.SHORT.getValue() + 1);
    }
    
    @Test
    void assertDescribeInsertParameter() throws Exception {
        when(packet.getSQL()).thenReturn("INSERT INTO foo_tbl (id) VALUES (?)");
        mockDescribeRequest(FirebirdSQLInfoPacketType.BIND);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getDescribeBind().size(), is(1));
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        actualReturnPacket.getDescribeBind().get(0).write(payload);
        verify(payload).writeInt4LE(FirebirdBinaryColumnType.LONG.getValue() + 1);
    }
    
    @Test
    void assertDescribeReturningShorthand() throws Exception {
        when(packet.getSQL()).thenReturn("INSERT INTO foo_tbl (id) VALUES (1) RETURNING *");
        mockDescribeRequest(FirebirdSQLInfoPacketType.SELECT);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getDescribeSelect().size(), is(2));
        FirebirdPacketPayload idPayload = mock(FirebirdPacketPayload.class);
        actualReturnPacket.getDescribeSelect().get(0).write(idPayload);
        verify(idPayload).writeInt4LE(FirebirdBinaryColumnType.LONG.getValue() + 1);
        FirebirdPacketPayload contentPayload = mock(FirebirdPacketPayload.class);
        actualReturnPacket.getDescribeSelect().get(1).write(contentPayload);
        verify(contentPayload).writeInt4LE(FirebirdBinaryColumnType.BLOB.getValue() + 1);
    }
    
    @Test
    void assertDescribeHintedOrdinaryTableWildcard() throws Exception {
        when(packet.getSQL()).thenReturn("SELECT * FROM foo_tbl");
        when(packet.getHintValueContext()).thenReturn(createDataSourceHintValueContext());
        mockDescribeRequest(FirebirdSQLInfoPacketType.SELECT);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getDescribeSelect().size(), is(2));
        FirebirdPacketPayload idPayload = mock(FirebirdPacketPayload.class);
        actualReturnPacket.getDescribeSelect().get(0).write(idPayload);
        verify(idPayload).writeInt4LE(FirebirdBinaryColumnType.LONG.getValue() + 1);
    }
    
    @Test
    void assertDescribeHintedQualifiedWildcard() throws Exception {
        when(packet.getSQL()).thenReturn("SELECT foo_tbl.* FROM foo_tbl");
        when(packet.getHintValueContext()).thenReturn(createDataSourceHintValueContext());
        mockDescribeRequest(FirebirdSQLInfoPacketType.SELECT);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getDescribeSelect().size(), is(2));
        FirebirdPacketPayload idPayload = mock(FirebirdPacketPayload.class);
        actualReturnPacket.getDescribeSelect().get(0).write(idPayload);
        verify(idPayload).writeInt4LE(FirebirdBinaryColumnType.LONG.getValue() + 1);
    }
    
    @Test
    void assertDescribeHintedJoinWithQualifiedWildcard() throws Exception {
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getDatabase("foo_db");
        addSystemSchema(database);
        ShardingSphereColumn idColumn = new ShardingSphereColumn("id", Types.INTEGER, false, false, true, true, false, true);
        ShardingSphereColumn blobColumn = new ShardingSphereColumn("content", Types.BLOB, false, false, true, true, false, true);
        ShardingSphereColumn nameColumn = new ShardingSphereColumn("name", Types.VARCHAR, false, false, true, true, false, true);
        ShardingSphereTable fooTable = new ShardingSphereTable("foo_tbl", Arrays.asList(idColumn, blobColumn), Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable barTable = new ShardingSphereTable("bar_tbl", Arrays.asList(idColumn, nameColumn), Collections.emptyList(), Collections.emptyList());
        database.addSchema(new ShardingSphereSchema("foo_db", databaseType, Arrays.asList(fooTable, barTable), Collections.emptyList()));
        when(database.findDefaultSchema()).thenCallRealMethod();
        when(packet.getSQL()).thenReturn("SELECT bar_tbl.* FROM foo_tbl JOIN bar_tbl ON foo_tbl.id = bar_tbl.id");
        when(packet.getHintValueContext()).thenReturn(createDataSourceHintValueContext());
        mockDescribeRequest(FirebirdSQLInfoPacketType.SELECT);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getDescribeSelect().size(), is(2));
        FirebirdPacketPayload idPayload = mock(FirebirdPacketPayload.class);
        actualReturnPacket.getDescribeSelect().get(0).write(idPayload);
        verify(idPayload).writeInt4LE(FirebirdBinaryColumnType.LONG.getValue() + 1);
        FirebirdPacketPayload namePayload = mock(FirebirdPacketPayload.class);
        actualReturnPacket.getDescribeSelect().get(1).write(namePayload);
        verify(namePayload).writeInt4LE(FirebirdBinaryColumnType.VARYING.getValue() + 1);
    }
    
    @Test
    void assertDescribeDerivedTableShorthand() throws Exception {
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getDatabase("foo_db");
        ShardingSphereColumn idColumn = new ShardingSphereColumn("id", Types.INTEGER, false, false, true, true, false, true);
        ShardingSphereTable originalTable = new ShardingSphereTable("foo_tbl", Arrays.asList(idColumn, new ShardingSphereColumn("content", Types.BLOB, false, false, true, true, false, true)),
                Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable derivedTable = new ShardingSphereTable("s", Collections.singleton(idColumn), Collections.emptyList(), Collections.emptyList());
        database.addSchema(new ShardingSphereSchema("foo_db", databaseType, Arrays.asList(originalTable, derivedTable), Collections.emptyList()));
        when(database.findDefaultSchema()).thenCallRealMethod();
        when(packet.getSQL()).thenReturn("SELECT s.* FROM (SELECT id FROM foo_tbl) s");
        when(packet.getHintValueContext()).thenReturn(createDataSourceHintValueContext());
        mockDescribeRequest(FirebirdSQLInfoPacketType.SELECT);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getDescribeSelect().size(), is(1));
    }
    
    @Test
    void assertDescribeUnqualifiedDerivedTableShorthand() throws Exception {
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getDatabase("foo_db");
        ShardingSphereColumn idColumn = new ShardingSphereColumn("id", Types.INTEGER, false, false, true, true, false, true);
        ShardingSphereTable originalTable = new ShardingSphereTable("foo_tbl", Arrays.asList(idColumn, new ShardingSphereColumn("content", Types.BLOB, false, false, true, true, false, true)),
                Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable derivedTable = new ShardingSphereTable("s", Collections.singleton(idColumn), Collections.emptyList(), Collections.emptyList());
        database.addSchema(new ShardingSphereSchema("foo_db", databaseType, Arrays.asList(originalTable, derivedTable), Collections.emptyList()));
        when(database.findDefaultSchema()).thenCallRealMethod();
        when(packet.getSQL()).thenReturn("SELECT * FROM (SELECT id FROM foo_tbl) s");
        when(packet.getHintValueContext()).thenReturn(createDataSourceHintValueContext());
        mockDescribeRequest(FirebirdSQLInfoPacketType.SELECT);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getDescribeSelect().size(), is(1));
    }
    
    private HintValueContext createDataSourceHintValueContext() {
        HintValueContext result = new HintValueContext();
        result.setDataSourceName("foo_ds");
        return result;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("describeWithSchemaReplacementArguments")
    void assertDescribeWithSchemaReplacement(final String name, final String sql, final FirebirdSQLInfoPacketType describeType) throws Exception {
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase("foo_db");
        ShardingSphereColumn idColumn = new ShardingSphereColumn("id", Types.INTEGER, false, false, true, true, false, true);
        ShardingSphereColumn codeColumn = new ShardingSphereColumn("code", Types.BIGINT, false, false, true, true, false, true);
        ShardingSphereTable originalTable = new ShardingSphereTable("foo_tbl", Arrays.asList(idColumn, codeColumn), Collections.emptyList(), Collections.emptyList());
        database.addSchema(new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(originalTable), Collections.emptyList()));
        when(database.findDefaultSchema()).thenCallRealMethod();
        ShardingSphereTable replacementTable = new ShardingSphereTable("foo_tbl", Collections.singleton(idColumn), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema replacementSchema = new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(replacementTable), Collections.emptyList());
        when(ProxyContext.getInstance().getContextManager().getDatabase("foo_db")).thenReturn(database).thenAnswer(invocation -> {
            database.addSchema(replacementSchema);
            return database;
        }).thenReturn(database);
        when(packet.getSQL()).thenReturn(sql);
        when(packet.nextItem()).thenReturn(true, true, true, true, true, true, false);
        when(packet.getCurrentItem()).thenReturn(FirebirdSQLInfoPacketType.STMT_TYPE, describeType, FirebirdSQLInfoPacketType.TYPE, FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.FIELD, FirebirdSQLInfoPacketType.FIELD, FirebirdSQLInfoPacketType.DESCRIBE_END, FirebirdSQLInfoPacketType.DESCRIBE_END);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        List<FirebirdReturnColumnPacket> actualColumns = FirebirdSQLInfoPacketType.BIND == describeType ? actualReturnPacket.getDescribeBind() : actualReturnPacket.getDescribeSelect();
        assertThat(actualColumns.size(), is(2));
        FirebirdPacketPayload idPayload = mock(FirebirdPacketPayload.class);
        when(idPayload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        actualColumns.get(0).write(idPayload);
        verify(idPayload).writeBytes("id".getBytes(StandardCharsets.UTF_8));
        verify(idPayload).writeInt4LE(FirebirdBinaryColumnType.LONG.getValue() + 1);
        FirebirdPacketPayload codePayload = mock(FirebirdPacketPayload.class);
        when(codePayload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        actualColumns.get(1).write(codePayload);
        verify(codePayload).writeBytes("code".getBytes(StandardCharsets.UTF_8));
        verify(codePayload).writeInt4LE(FirebirdBinaryColumnType.INT64.getValue() + 1);
    }
    
    private static Stream<Arguments> describeWithSchemaReplacementArguments() {
        return Stream.of(
                Arguments.of("Select columns", "SELECT id, code FROM foo_tbl", FirebirdSQLInfoPacketType.SELECT),
                Arguments.of("Bind parameters", "SELECT id, code FROM foo_tbl WHERE id = ? AND code = ?", FirebirdSQLInfoPacketType.BIND),
                Arguments.of("Returning shorthand", "INSERT INTO foo_tbl (id, code) VALUES (1, 2) RETURNING *", FirebirdSQLInfoPacketType.SELECT));
    }
    
    @Test
    void assertDescribeReturningOldAndNewShorthandColumnTypes() throws Exception {
        when(packet.getSQL()).thenReturn("UPDATE foo_tbl SET id = 1 RETURNING OLD.*, NEW.*");
        mockDescribeRequest(FirebirdSQLInfoPacketType.SELECT);
        FirebirdPrepareStatementReturnPacket actualReturnPacket = executeAndGetReturnPacket();
        assertThat(actualReturnPacket.getDescribeSelect().size(), is(4));
        FirebirdPacketPayload oldIdPayload = mock(FirebirdPacketPayload.class);
        actualReturnPacket.getDescribeSelect().get(0).write(oldIdPayload);
        verify(oldIdPayload).writeInt4LE(FirebirdBinaryColumnType.LONG.getValue() + 1);
        FirebirdPacketPayload oldContentPayload = mock(FirebirdPacketPayload.class);
        actualReturnPacket.getDescribeSelect().get(1).write(oldContentPayload);
        verify(oldContentPayload).writeInt4LE(FirebirdBinaryColumnType.BLOB.getValue() + 1);
        FirebirdPacketPayload newIdPayload = mock(FirebirdPacketPayload.class);
        actualReturnPacket.getDescribeSelect().get(2).write(newIdPayload);
        verify(newIdPayload).writeInt4LE(FirebirdBinaryColumnType.LONG.getValue() + 1);
        FirebirdPacketPayload newContentPayload = mock(FirebirdPacketPayload.class);
        actualReturnPacket.getDescribeSelect().get(3).write(newContentPayload);
        verify(newContentPayload).writeInt4LE(FirebirdBinaryColumnType.BLOB.getValue() + 1);
    }
    
    @Test
    void assertDescribeWithDatabaseRemoved() {
        when(packet.getSQL()).thenReturn("SELECT id FROM foo_tbl");
        mockDescribeRequest(FirebirdSQLInfoPacketType.SELECT);
        UnknownDatabaseException expected = new UnknownDatabaseException("foo_db");
        when(ProxyContext.getInstance().getContextManager().getDatabase("foo_db")).thenThrow(expected);
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        assertThat(assertThrows(UnknownDatabaseException.class, executor::execute), sameInstance(expected));
    }
    
    @Test
    void assertDescribeBlobWithDatabaseRemoved() {
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getDatabase("foo_db");
        when(packet.getSQL()).thenReturn("SELECT content FROM foo_tbl");
        mockDescribeRequest(FirebirdSQLInfoPacketType.SELECT);
        UnknownDatabaseException expected = new UnknownDatabaseException("foo_db");
        when(ProxyContext.getInstance().getContextManager().getDatabase("foo_db")).thenReturn(database).thenThrow(expected);
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        assertThat(assertThrows(UnknownDatabaseException.class, executor::execute), sameInstance(expected));
    }
    
    @Test
    void assertDescribeWithTableRemoved() {
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getDatabase("foo_db");
        addSystemSchema(database);
        when(packet.getSQL()).thenReturn("SELECT id FROM foo_tbl");
        mockDescribeRequest(FirebirdSQLInfoPacketType.SELECT);
        when(ProxyContext.getInstance().getContextManager().getDatabase("foo_db")).thenAnswer(invocation -> {
            database.getSchema("foo_db").removeTable("foo_tbl");
            return database;
        });
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        TableNotFoundException actual = assertThrows(TableNotFoundException.class, executor::execute);
        assertThat(actual.getMessage(), is("Table or view 'foo_tbl' does not exist."));
    }
}
