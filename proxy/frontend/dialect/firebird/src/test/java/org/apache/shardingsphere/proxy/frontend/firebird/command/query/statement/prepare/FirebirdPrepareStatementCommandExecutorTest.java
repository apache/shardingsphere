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
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
    }
    
    private MetaDataContexts createMetaDataContexts() {
        SQLParserRule parserRule = new SQLParserRule(new SQLParserRuleConfiguration(new CacheOption(128, 1024L), new CacheOption(128, 1024L)));
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(parserRule));
        ShardingSphereColumn column = new ShardingSphereColumn("id", Types.INTEGER, false, false, true, true, false, true);
        ShardingSphereColumn blobColumn = new ShardingSphereColumn("content", Types.BLOB, false, false, true, true, false, true);
        ShardingSphereColumn bigintColumn = new ShardingSphereColumn("foo_bigint", Types.BIGINT, false, false, true, true, false, true);
        ShardingSphereColumn smallintColumn = new ShardingSphereColumn("foo_smallint", Types.SMALLINT, false, false, true, true, false, true);
        ShardingSphereColumn dateColumn = new ShardingSphereColumn("foo_date", Types.DATE, false, false, true, true, false, true);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Arrays.asList(column, blobColumn, bigintColumn, smallintColumn, dateColumn), Collections.emptyList(), Collections.emptyList());
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
    void assertDescribeArithmeticOfIntegerColumns() throws Exception {
        List<FirebirdReturnColumnPacket> actual = describeSelect("SELECT id + 1, id * 2, id / 2, id || 'x' FROM foo_tbl", FirebirdSQLInfoPacketType.TYPE, FirebirdSQLInfoPacketType.FIELD);
        assertThat(actual.size(), is(4));
        assertArithmeticColumn(actual.get(0), FirebirdBinaryColumnType.INT64, "ADD");
        assertArithmeticColumn(actual.get(1), FirebirdBinaryColumnType.INT64, "MULTIPLY");
        assertArithmeticColumn(actual.get(2), FirebirdBinaryColumnType.INT64, "DIVIDE");
        assertArithmeticColumn(actual.get(3), FirebirdBinaryColumnType.VARYING, "CONCATENATION");
    }
    
    private List<FirebirdReturnColumnPacket> describeSelect(final String sql, final FirebirdSQLInfoPacketType... describeItems) throws SQLException {
        when(packet.getSQL()).thenReturn(sql);
        Boolean[] nextItems = new Boolean[describeItems.length + 4];
        Arrays.fill(nextItems, Boolean.TRUE);
        nextItems[nextItems.length - 1] = Boolean.FALSE;
        when(packet.nextItem()).thenReturn(Boolean.TRUE, nextItems);
        List<FirebirdSQLInfoPacketType> currentItems = new LinkedList<>();
        currentItems.add(FirebirdSQLInfoPacketType.SELECT);
        for (FirebirdSQLInfoPacketType each : describeItems) {
            currentItems.add(each);
            currentItems.add(each);
        }
        currentItems.add(FirebirdSQLInfoPacketType.DESCRIBE_END);
        currentItems.add(FirebirdSQLInfoPacketType.DESCRIBE_END);
        when(packet.getCurrentItem()).thenReturn(FirebirdSQLInfoPacketType.STMT_TYPE, currentItems.toArray(new FirebirdSQLInfoPacketType[0]));
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        return ((FirebirdPrepareStatementReturnPacket) ((FirebirdGenericResponsePacket) executor.execute().iterator().next()).getData()).getDescribeSelect();
    }
    
    private void assertArithmeticColumn(final FirebirdReturnColumnPacket columnPacket, final FirebirdBinaryColumnType expectedType, final String expectedName) {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        when(payload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        columnPacket.write(payload);
        verify(payload).writeInt4LE(expectedType.getValue() + 1);
        verify(payload).writeBytes(expectedName.getBytes(StandardCharsets.UTF_8));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("arithmeticExpressionArguments")
    void assertDescribeArithmeticExpression(final String name, final String sql, final FirebirdBinaryColumnType expectedType, final String expectedName) throws Exception {
        List<FirebirdReturnColumnPacket> actual = describeSelect(sql, FirebirdSQLInfoPacketType.TYPE, FirebirdSQLInfoPacketType.FIELD);
        assertThat(actual.size(), is(1));
        assertArithmeticColumn(actual.get(0), expectedType, expectedName);
    }
    
    @Test
    void assertDescribeArithmeticOfSystemTableColumn() throws Exception {
        ShardingSphereColumn fieldPositionColumn = new ShardingSphereColumn("RDB$FIELD_POSITION", Types.SMALLINT, false, false, false, true, false, true);
        ShardingSphereTable relationFieldsTable = new ShardingSphereTable("RDB$RELATION_FIELDS", Collections.singleton(fieldPositionColumn), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema systemSchema = new ShardingSphereSchema("system_tables", databaseType, Collections.singleton(relationFieldsTable), Collections.emptyList());
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase("foo_db");
        doReturn(true).when(database).containsSchema(new IdentifierValue("system_tables"));
        doReturn(systemSchema).when(database).getSchema(new IdentifierValue("system_tables"));
        List<FirebirdReturnColumnPacket> actual = describeSelect("SELECT RDB$FIELD_POSITION + 1 FROM RDB$RELATION_FIELDS", FirebirdSQLInfoPacketType.TYPE, FirebirdSQLInfoPacketType.FIELD);
        assertThat(actual.size(), is(1));
        assertArithmeticColumn(actual.get(0), FirebirdBinaryColumnType.INT64, "ADD");
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("derivedTableArguments")
    void assertDescribeDerivedTableColumnType(final String name, final String sql, final List<FirebirdBinaryColumnType> expectedTypes) throws Exception {
        List<FirebirdReturnColumnPacket> actual = describeSelect(sql, FirebirdSQLInfoPacketType.TYPE);
        assertThat(actual.size(), is(expectedTypes.size()));
        for (int i = 0; i < expectedTypes.size(); i++) {
            FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
            actual.get(i).write(payload);
            verify(payload).writeInt4LE(expectedTypes.get(i).getValue() + 1);
        }
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
    void assertExecuteWithValidStatementHandleWithoutFetchHandler() throws Exception {
        FirebirdPrepareStatementCommandExecutor executor = new FirebirdPrepareStatementCommandExecutor(packet, connectionSession);
        executor.execute();
        verify(connectionSession).invalidatePreparedStatementCache(FirebirdStatementResourceCleaner.createPreparedStatementCacheKey(1));
    }
    
    private static Stream<Arguments> arithmeticExpressionArguments() {
        return Stream.of(
                Arguments.of("subtract integer column", "SELECT id - 1 FROM foo_tbl", FirebirdBinaryColumnType.INT64, "SUBTRACT"),
                Arguments.of("add bigint and integer columns", "SELECT foo_bigint + id FROM foo_tbl", FirebirdBinaryColumnType.INT64, "ADD"),
                Arguments.of("subtract bigint columns", "SELECT foo_bigint - foo_bigint FROM foo_tbl", FirebirdBinaryColumnType.INT64, "SUBTRACT"),
                Arguments.of("add smallint column", "SELECT foo_smallint + 1 FROM foo_tbl", FirebirdBinaryColumnType.INT64, "ADD"),
                Arguments.of("add literals", "SELECT 1 + 2 FROM foo_tbl", FirebirdBinaryColumnType.INT64, "ADD"),
                Arguments.of("add literal beyond integer", "SELECT id + 2147483648 FROM foo_tbl", FirebirdBinaryColumnType.INT64, "ADD"),
                Arguments.of("multiply integer column by maximum integer", "SELECT id * 2147483647 FROM foo_tbl", FirebirdBinaryColumnType.INT64, "MULTIPLY"),
                Arguments.of("multiply smallint and integer columns", "SELECT foo_smallint * id FROM foo_tbl", FirebirdBinaryColumnType.INT64, "MULTIPLY"),
                Arguments.of("divide literals", "SELECT 6 / 3 FROM foo_tbl", FirebirdBinaryColumnType.INT64, "DIVIDE"),
                Arguments.of("nested addition", "SELECT id + 1 + 2 FROM foo_tbl", FirebirdBinaryColumnType.INT64, "ADD"),
                Arguments.of("multiplication inside subtraction", "SELECT (id * 2) - 1 FROM foo_tbl", FirebirdBinaryColumnType.INT64, "SUBTRACT"),
                Arguments.of("subtract date columns", "SELECT foo_date - foo_date FROM foo_tbl", FirebirdBinaryColumnType.LONG, "SUBTRACT"));
    }
    
    private static Stream<Arguments> derivedTableArguments() {
        return Stream.of(
                Arguments.of("literal over derived arithmetic", "SELECT 1 FROM (SELECT id + 1 AS x FROM foo_tbl) d", Collections.singletonList(FirebirdBinaryColumnType.LONG)),
                Arguments.of("derived arithmetic column", "SELECT x FROM (SELECT id + 1 AS x FROM foo_tbl) d", Collections.singletonList(FirebirdBinaryColumnType.INT64)),
                Arguments.of("all columns of derived arithmetic", "SELECT * FROM (SELECT id + 1 AS x FROM foo_tbl) d", Collections.singletonList(FirebirdBinaryColumnType.INT64)),
                Arguments.of("derived arithmetic column and literal", "SELECT x, 1 FROM (SELECT id + 1 AS x FROM foo_tbl) d",
                        Arrays.asList(FirebirdBinaryColumnType.INT64, FirebirdBinaryColumnType.LONG)),
                Arguments.of("arithmetic over derived arithmetic column", "SELECT x + 1 FROM (SELECT id + 1 AS x FROM foo_tbl) d", Collections.singletonList(FirebirdBinaryColumnType.INT64)));
    }
}
