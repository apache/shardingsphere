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
import org.apache.shardingsphere.database.connector.firebird.metadata.data.FirebirdNonFixedLengthColumnSizeRegistry;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdPrepareStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdPrepareStatementReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdReturnColumnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
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
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
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

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
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
    private ProxyDatabaseConnectionManager connectionManager;
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @Mock
    private ConnectionContext connectionContext;
    
    @BeforeEach
    void setUp() {
        FirebirdFetchStatementCache.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        when(connectionContext.getGrantee()).thenReturn(new Grantee("foo_user"));
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(connectionManager);
        when(packet.getSQL()).thenReturn("SELECT 1");
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        when(packet.isValidStatementHandle()).thenReturn(true);
        when(packet.getStatementId()).thenReturn(1);
        when(packet.nextItem()).thenReturn(true, false);
        when(packet.getCurrentItem()).thenReturn(FirebirdSQLInfoPacketType.STMT_TYPE);
        MetaDataContexts metaDataContexts = createMetaDataContexts();
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts()).thenReturn(metaDataContexts);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdFetchStatementCache.getInstance().unregisterStatement(CONNECTION_ID, 1);
        FirebirdFetchStatementCache.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobInfoRegistry.refreshTable("foo_db", "foo_tbl", Collections.emptyMap());
        FirebirdNonFixedLengthColumnSizeRegistry.refreshTable("foo_db", "foo_tbl", Collections.emptyMap());
    }
    
    private MetaDataContexts createMetaDataContexts() {
        SQLParserRule parserRule = new SQLParserRule(new SQLParserRuleConfiguration(new CacheOption(128, 1024L), new CacheOption(128, 1024L)));
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(parserRule));
        ShardingSphereColumn column = new ShardingSphereColumn("id", Types.INTEGER, false, false, true, true, false, true);
        ShardingSphereColumn blobColumn = new ShardingSphereColumn("content", Types.BLOB, false, false, true, true, false, true);
        ShardingSphereColumn nameColumn = new ShardingSphereColumn("foo_name", Types.VARCHAR, false, false, true, true, false, true);
        ShardingSphereColumn patternColumn = new ShardingSphereColumn("foo_pattern", Types.VARCHAR, false, false, true, true, false, true);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Arrays.asList(column, blobColumn, nameColumn, patternColumn), Collections.emptyList(), Collections.emptyList());
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
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        FirebirdGenericResponsePacket responsePacket = (FirebirdGenericResponsePacket) actual.iterator().next();
        FirebirdPrepareStatementReturnPacket returnPacket = (FirebirdPrepareStatementReturnPacket) responsePacket.getData();
        assertThat(returnPacket.getType(), is(FirebirdSQLInfoReturnValue.SELECT));
        FirebirdServerPreparedStatement preparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1);
        assertThat(preparedStatement.getSql(), is("SELECT 1"));
        assertThat(preparedStatement.getSqlStatementContext(), isA(SelectStatementContext.class));
    }
    
    @Test
    void assertExecuteDropDatabaseWithoutNameResolvesCurrentDatabase() throws Exception {
        when(packet.getSQL()).thenReturn("DROP DATABASE");
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        FirebirdGenericResponsePacket responsePacket = (FirebirdGenericResponsePacket) actual.iterator().next();
        FirebirdPrepareStatementReturnPacket returnPacket = (FirebirdPrepareStatementReturnPacket) responsePacket.getData();
        assertThat(returnPacket.getType(), is(FirebirdSQLInfoReturnValue.DDL));
        FirebirdServerPreparedStatement preparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1);
        assertThat(((DropDatabaseStatement) preparedStatement.getSqlStatementContext().getSqlStatement()).getDatabaseName(), is("foo_db"));
    }
    
    @Test
    void assertExecuteDropDatabaseWithoutNameAndCurrentDatabase() {
        when(packet.getSQL()).thenReturn("DROP DATABASE");
        when(connectionSession.getUsedDatabaseName()).thenReturn(null);
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        assertThrows(NoDatabaseSelectedException.class, executor::execute);
    }
    
    @Test
    void assertDescribeCountReturnsBigintType() throws Exception {
        when(packet.getSQL()).thenReturn("SELECT COUNT(*) FROM foo_tbl");
        when(packet.nextItem()).thenReturn(true, true, true, true, true, false);
        when(packet.getCurrentItem()).thenReturn(
                FirebirdSQLInfoPacketType.STMT_TYPE,
                FirebirdSQLInfoPacketType.SELECT,
                FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.DESCRIBE_END,
                FirebirdSQLInfoPacketType.DESCRIBE_END);
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        FirebirdGenericResponsePacket responsePacket = (FirebirdGenericResponsePacket) actual.iterator().next();
        FirebirdPrepareStatementReturnPacket returnPacket = (FirebirdPrepareStatementReturnPacket) responsePacket.getData();
        assertThat(returnPacket.getDescribeSelect().size(), is(1));
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class, RETURNS_DEEP_STUBS);
        FirebirdReturnColumnPacket columnPacket = returnPacket.getDescribeSelect().get(0);
        columnPacket.write(payload);
        verify(payload).writeInt4LE(FirebirdBinaryColumnType.INT64.getValue() + 1);
    }
    
    @Test
    void assertDescribeBlobColumnRegisteredInBlobInfoRegistryReturnsBlobTypeAndSubtype() throws Exception {
        FirebirdBlobInfoRegistry.refreshTable("foo_db", "foo_tbl", Collections.singletonMap("id", 7));
        FirebirdReturnColumnPacket columnPacket = describeSingleColumn("SELECT id FROM foo_tbl");
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class, RETURNS_DEEP_STUBS);
        columnPacket.write(payload);
        verify(payload).writeInt4LE(FirebirdBinaryColumnType.BLOB.getValue() + 1);
        verify(payload).writeInt4LE(7);
    }
    
    @Test
    void assertDescribeBlobTypeColumnWithoutRegistryEntryReturnsBlobTypeAndDefaultSubtype() throws Exception {
        FirebirdReturnColumnPacket columnPacket = describeSingleColumn("SELECT content FROM foo_tbl");
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class, RETURNS_DEEP_STUBS);
        columnPacket.write(payload);
        verify(payload).writeInt4LE(FirebirdBinaryColumnType.BLOB.getValue() + 1);
        verify(payload).writeInt4LE(FirebirdBinaryColumnType.BLOB.getSubtype());
    }
    
    @Test
    void assertDescribeDerivedTableSelect() throws Exception {
        when(packet.getSQL()).thenReturn("SELECT * FROM (SELECT id FROM foo_tbl) derived_tbl");
        when(packet.nextItem()).thenReturn(true, true, true, true, true, false);
        when(packet.getCurrentItem()).thenReturn(
                FirebirdSQLInfoPacketType.STMT_TYPE,
                FirebirdSQLInfoPacketType.SELECT,
                FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.DESCRIBE_END,
                FirebirdSQLInfoPacketType.DESCRIBE_END);
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        FirebirdGenericResponsePacket responsePacket = (FirebirdGenericResponsePacket) actual.iterator().next();
        FirebirdPrepareStatementReturnPacket returnPacket = (FirebirdPrepareStatementReturnPacket) responsePacket.getData();
        assertThat(returnPacket.getType(), is(FirebirdSQLInfoReturnValue.SELECT));
        assertThat(returnPacket.getDescribeSelect().size(), is(1));
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class, RETURNS_DEEP_STUBS);
        FirebirdReturnColumnPacket columnPacket = returnPacket.getDescribeSelect().get(0);
        columnPacket.write(payload);
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
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        FirebirdGenericResponsePacket responsePacket = (FirebirdGenericResponsePacket) actual.iterator().next();
        FirebirdPrepareStatementReturnPacket returnPacket = (FirebirdPrepareStatementReturnPacket) responsePacket.getData();
        assertThat(returnPacket.getDescribeSelect().size(), is(1));
        return returnPacket.getDescribeSelect().get(0);
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
    void assertDescribeLikeParameters() throws Exception {
        when(packet.getSQL()).thenReturn("SELECT id FROM foo_tbl WHERE content LIKE ? ESCAPE ?");
        when(packet.nextItem()).thenReturn(true, true, true, true, true, false);
        when(packet.getCurrentItem()).thenReturn(
                FirebirdSQLInfoPacketType.STMT_TYPE,
                FirebirdSQLInfoPacketType.BIND,
                FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.DESCRIBE_END,
                FirebirdSQLInfoPacketType.DESCRIBE_END);
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        FirebirdPrepareStatementReturnPacket actualReturnPacket = (FirebirdPrepareStatementReturnPacket) ((FirebirdGenericResponsePacket) actual.iterator().next()).getData();
        assertThat(actualReturnPacket.getDescribeBind().size(), is(2));
        for (FirebirdReturnColumnPacket each : actualReturnPacket.getDescribeBind()) {
            FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
            each.write(payload);
            verify(payload).writeInt4LE(FirebirdBinaryColumnType.BLOB.getValue() + 1);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("likeOperandParameterArguments")
    void assertDescribeLikeOperandParameters(final String name, final String sql, final List<int[]> expectedTypesAndLengths) throws Exception {
        Map<String, Integer> columnSizes = new HashMap<>(2, 1F);
        columnSizes.put("foo_name", 20);
        columnSizes.put("foo_pattern", 50);
        FirebirdNonFixedLengthColumnSizeRegistry.refreshTable("foo_db", "foo_tbl", columnSizes);
        when(packet.getSQL()).thenReturn(sql);
        when(packet.nextItem()).thenReturn(true, true, true, true, true, true, false);
        when(packet.getCurrentItem()).thenReturn(
                FirebirdSQLInfoPacketType.STMT_TYPE,
                FirebirdSQLInfoPacketType.BIND,
                FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.TYPE,
                FirebirdSQLInfoPacketType.LENGTH,
                FirebirdSQLInfoPacketType.LENGTH,
                FirebirdSQLInfoPacketType.DESCRIBE_END,
                FirebirdSQLInfoPacketType.DESCRIBE_END);
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        List<FirebirdReturnColumnPacket> actual = ((FirebirdPrepareStatementReturnPacket) ((FirebirdGenericResponsePacket) executor.execute().iterator().next()).getData()).getDescribeBind();
        assertThat(actual.size(), is(expectedTypesAndLengths.size()));
        for (int i = 0; i < expectedTypesAndLengths.size(); i++) {
            FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
            actual.get(i).write(payload);
            verify(payload).writeInt4LE(expectedTypesAndLengths.get(i)[0] + 1);
            verify(payload).writeInt4LE(expectedTypesAndLengths.get(i)[1]);
        }
    }
    
    @Test
    void assertExecuteWithValidStatementHandleWithoutFetchHandler() throws Exception {
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        executor.execute();
        verify(connectionSession).invalidatePreparedStatementCache(FirebirdStatementResourceCleaner.createPreparedStatementCacheKey(1));
    }
    
    private static Stream<Arguments> likeOperandParameterArguments() {
        int varying = FirebirdBinaryColumnType.VARYING.getValue();
        return Stream.of(
                Arguments.of("parameter in value takes literal pattern length", "SELECT id FROM foo_tbl WHERE (foo_name || ?) LIKE 'a%'", Collections.singletonList(new int[]{varying, 2})),
                Arguments.of("parameter in value takes column pattern length", "SELECT id FROM foo_tbl WHERE (foo_name || ?) LIKE foo_name", Collections.singletonList(new int[]{varying, 20})),
                Arguments.of("pattern of integer column", "SELECT id FROM foo_tbl WHERE id LIKE ?", Collections.singletonList(new int[]{varying, 30})),
                Arguments.of("value of integer column pattern", "SELECT id FROM foo_tbl WHERE ? LIKE id", Collections.singletonList(new int[]{varying, 30})),
                Arguments.of("pattern and escape of integer column", "SELECT id FROM foo_tbl WHERE id LIKE ? ESCAPE ?", Arrays.asList(new int[]{varying, 30}, new int[]{varying, 30})),
                Arguments.of("escape of column pattern", "SELECT id FROM foo_tbl WHERE foo_name LIKE foo_pattern ESCAPE ?", Collections.singletonList(new int[]{varying, 50})),
                Arguments.of("escape of other column pattern", "SELECT id FROM foo_tbl WHERE foo_pattern LIKE foo_name ESCAPE ?", Collections.singletonList(new int[]{varying, 20})),
                Arguments.of("escape of literal pattern", "SELECT id FROM foo_tbl WHERE foo_name LIKE 'a#_%' ESCAPE ?", Collections.singletonList(new int[]{varying, 4})),
                Arguments.of("pattern and escape of text column", "SELECT id FROM foo_tbl WHERE foo_name LIKE ? ESCAPE ?", Arrays.asList(new int[]{varying, 20}, new int[]{varying, 20})),
                Arguments.of("value and pattern parameters", "SELECT id FROM foo_tbl WHERE ? LIKE ?", Arrays.asList(new int[]{varying, 30}, new int[]{varying, 30})),
                Arguments.of("pattern before comparison", "SELECT id FROM foo_tbl WHERE foo_name LIKE ? AND id = ?",
                        Arrays.asList(new int[]{varying, 20}, new int[]{FirebirdBinaryColumnType.LONG.getValue(), 4})));
    }
}
