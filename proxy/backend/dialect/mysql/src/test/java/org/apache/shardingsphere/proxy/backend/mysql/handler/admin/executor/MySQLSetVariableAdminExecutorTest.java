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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.mysql.exception.CollationCharsetMismatchException;
import org.apache.shardingsphere.database.exception.mysql.exception.ErrorGlobalVariableException;
import org.apache.shardingsphere.database.exception.mysql.exception.UnknownCharsetException;
import org.apache.shardingsphere.database.exception.mysql.exception.UnknownCollationException;
import org.apache.shardingsphere.database.exception.mysql.exception.UnknownSystemVariableException;
import org.apache.shardingsphere.database.exception.mysql.exception.WrongValueForVariableException;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCharacterSets;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.StandardDatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.RequiredSessionVariableRecorder;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.variable.charset.MySQLSessionCharsetContext;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment.VariableType;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLSetVariableAdminExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertExecute() throws SQLException {
        SetStatement setStatement = prepareSetStatement();
        MySQLSetVariableAdminExecutor executor = new MySQLSetVariableAdminExecutor(setStatement);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getAttributeMap()).thenReturn(new DefaultAttributeMap());
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        ConnectionContext connectionContext = mockConnectionContext();
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        try (MockedConstruction<StandardDatabaseProxyConnector> mockConstruction = mockConstruction(StandardDatabaseProxyConnector.class)) {
            executor.execute(connectionSession, mockMetaData());
            verify(mockConstruction.constructed().get(0)).execute();
        }
        assertThat(connectionSession.getAttributeMap().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get(), is(StandardCharsets.UTF_8));
    }
    
    @Test
    void assertExecuteWithCharacterSetResults() throws SQLException {
        SetStatement setStatement = new SetStatement(databaseType, Collections.singletonList(
                new VariableAssignSegment(0, 0, new VariableSegment(0, 0, "character_set_results"), "'latin1'")));
        MySQLSetVariableAdminExecutor executor = new MySQLSetVariableAdminExecutor(setStatement);
        ConnectionSession connectionSession = mockConnectionSession();
        executor.execute(connectionSession, mock());
        assertThat(connectionSession.getAttributeMap().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get(), is(StandardCharsets.UTF_8));
        assertThat(connectionSession.getAttributeMap().attr(MySQLConstants.RESULT_CHARSET_ATTRIBUTE_KEY).get(), is(StandardCharsets.ISO_8859_1));
        assertThat(connectionSession.getAttributeMap().attr(MySQLConstants.CHARACTER_SET_ATTRIBUTE_KEY).get(), is(MySQLCharacterSets.LATIN1_SWEDISH_CI));
    }
    
    @Test
    void assertExecuteWithCharacterSetResultsAsNull() throws SQLException {
        SetStatement setStatement = new SetStatement(databaseType, Collections.singletonList(
                new VariableAssignSegment(0, 0, new VariableSegment(0, 0, "character_set_results"), "NULL")));
        MySQLSetVariableAdminExecutor executor = new MySQLSetVariableAdminExecutor(setStatement);
        ConnectionSession connectionSession = mockConnectionSession();
        executor.execute(connectionSession, mock());
        assertThat(MySQLSessionCharsetContext.get(connectionSession.getAttributeMap()).getResultCharacterSetName(), is(Optional.empty()));
        assertThat(connectionSession.getAttributeMap().attr(MySQLConstants.RESULT_CHARSET_ATTRIBUTE_KEY).get(), is(StandardCharsets.UTF_8));
        assertThat(connectionSession.getAttributeMap().attr(MySQLConstants.CHARACTER_SET_ATTRIBUTE_KEY).get(), is(MySQLConstants.DEFAULT_CHARSET));
    }
    
    @Test
    void assertExecuteWithCharacterSetResultsAsBinary() throws SQLException {
        SetStatement setStatement = new SetStatement(databaseType, Collections.singletonList(
                new VariableAssignSegment(0, 0, new VariableSegment(0, 0, "character_set_results"), "binary")));
        ConnectionSession connectionSession = mockConnectionSession();
        new MySQLSetVariableAdminExecutor(setStatement).execute(connectionSession, mock());
        assertThat(MySQLSessionCharsetContext.get(connectionSession.getAttributeMap()).getResultCharacterSetName(), is(Optional.of("binary")));
        assertThat(connectionSession.getAttributeMap().attr(MySQLConstants.RESULT_CHARSET_ATTRIBUTE_KEY).get(), is(StandardCharsets.UTF_8));
        assertThat(connectionSession.getAttributeMap().attr(MySQLConstants.CHARACTER_SET_ATTRIBUTE_KEY).get(), is(MySQLConstants.DEFAULT_CHARSET));
    }
    
    @Test
    void assertExecuteSetNamesWithCollation() throws SQLException {
        SetStatement setStatement = parseSetStatement("SET NAMES 'utf8' COLLATE 'utf8_bin'");
        ConnectionSession connectionSession = mockReplayableConnectionSession();
        new MySQLSetVariableAdminExecutor(setStatement).execute(connectionSession, mock());
        MySQLSessionCharsetContext actual = MySQLSessionCharsetContext.get(connectionSession.getAttributeMap());
        assertThat(actual.getClientCharacterSetName(), is("utf8"));
        assertThat(actual.getResultCharacterSetName(), is(Optional.of("utf8")));
        assertThat(actual.getConnectionCharacterSetName(), is("utf8"));
        assertThat(actual.getConnectionCollationName(), is("utf8_bin"));
        assertThat(connectionSession.getRequiredSessionVariableRecorder().toSetSQLs(databaseType.getType()), is(Collections.singletonList("SET collation_connection='utf8_bin'")));
    }
    
    @Test
    void assertExecuteSetCharacterSet() throws SQLException {
        SetStatement setStatement = parseSetStatement("SET CHARACTER SET latin1");
        ConnectionSession connectionSession = mockReplayableConnectionSession();
        new MySQLSetVariableAdminExecutor(setStatement).execute(connectionSession, mock());
        MySQLSessionCharsetContext actual = MySQLSessionCharsetContext.get(connectionSession.getAttributeMap());
        assertThat(actual.getClientCharacterSetName(), is("latin1"));
        assertThat(actual.getResultCharacterSetName(), is(Optional.of("latin1")));
        assertThat(actual.getConnectionCharacterSetName(), is("utf8mb4"));
        assertThat(actual.getConnectionCollationName(), is("utf8mb4_unicode_ci"));
        assertThat(connectionSession.getRequiredSessionVariableRecorder().toSetSQLs(databaseType.getType()),
                is(Collections.singletonList("SET collation_connection='utf8mb4_unicode_ci'")));
    }
    
    @Test
    void assertExecuteWithIndependentCharacterSets() throws SQLException {
        SetStatement setStatement = new SetStatement(databaseType, Arrays.asList(
                new VariableAssignSegment(0, 0, new VariableSegment(0, 0, "character_set_client"), "latin1"),
                new VariableAssignSegment(1, 1, new VariableSegment(1, 1, "character_set_results"), "utf8mb4"),
                new VariableAssignSegment(2, 2, new VariableSegment(2, 2, "collation_connection"), "latin1_bin")));
        ConnectionSession connectionSession = mockReplayableConnectionSession();
        new MySQLSetVariableAdminExecutor(setStatement).execute(connectionSession, mock());
        MySQLSessionCharsetContext actual = MySQLSessionCharsetContext.get(connectionSession.getAttributeMap());
        assertThat(actual.getClientCharacterSetName(), is("latin1"));
        assertThat(actual.getResultCharacterSetName(), is(Optional.of("utf8mb4")));
        assertThat(actual.getConnectionCharacterSetName(), is("latin1"));
        assertThat(actual.getConnectionCollationName(), is("latin1_bin"));
    }
    
    @Test
    void assertExecuteWithCharacterSetConnection() throws SQLException {
        SetStatement setStatement = new SetStatement(databaseType, Collections.singletonList(
                new VariableAssignSegment(0, 0, new VariableSegment(0, 0, "character_set_connection"), "latin1")));
        ConnectionSession connectionSession = mockReplayableConnectionSession();
        new MySQLSetVariableAdminExecutor(setStatement).execute(connectionSession, mock());
        MySQLSessionCharsetContext actual = MySQLSessionCharsetContext.get(connectionSession.getAttributeMap());
        assertThat(actual.getConnectionCharacterSetName(), is("latin1"));
        assertThat(actual.getConnectionCollationName(), is("latin1_swedish_ci"));
        assertThat(connectionSession.getRequiredSessionVariableRecorder().toSetSQLs(databaseType.getType()),
                is(Collections.singletonList("SET collation_connection='latin1_swedish_ci'")));
    }
    
    @Test
    void assertExecuteWithUtf8mb4CharacterSetConnection() throws SQLException {
        SetStatement setStatement = new SetStatement(databaseType, Collections.singletonList(
                new VariableAssignSegment(0, 0, new VariableSegment(0, 0, "character_set_connection"), "utf8mb4")));
        ConnectionSession connectionSession = mockReplayableConnectionSession();
        new MySQLSetVariableAdminExecutor(setStatement).execute(connectionSession, mock());
        assertThat(MySQLSessionCharsetContext.get(connectionSession.getAttributeMap()).getConnectionCollation(), is(MySQLCharacterSets.UTF8MB4_UNICODE_CI));
        assertThat(connectionSession.getRequiredSessionVariableRecorder().toSetSQLs(databaseType.getType()),
                is(Collections.singletonList("SET collation_connection='utf8mb4_unicode_ci'")));
    }
    
    @Test
    void assertExecuteSetNamesDefault() throws SQLException {
        SetStatement setStatement = parseSetStatement("SET NAMES DEFAULT");
        ConnectionSession connectionSession = mockReplayableConnectionSession();
        new MySQLSetVariableAdminExecutor(setStatement).execute(connectionSession, mock());
        MySQLSessionCharsetContext actual = MySQLSessionCharsetContext.get(connectionSession.getAttributeMap());
        assertThat(actual.getClientCharacterSet(), is(MySQLConstants.DEFAULT_CHARSET));
        assertThat(actual.getResultCharacterSet(), is(Optional.of(MySQLConstants.DEFAULT_CHARSET)));
        assertThat(actual.getConnectionCollation(), is(MySQLCharacterSets.UTF8MB4_UNICODE_CI));
        assertThat(connectionSession.getRequiredSessionVariableRecorder().toSetSQLs(databaseType.getType()),
                is(Collections.singletonList("SET collation_connection='utf8mb4_unicode_ci'")));
    }
    
    @Test
    void assertExecuteWithDefaultConnectionCollation() throws SQLException {
        SetStatement setStatement = new SetStatement(databaseType, Collections.singletonList(
                new VariableAssignSegment(0, 0, new VariableSegment(0, 0, "collation_connection"), "DEFAULT")));
        ConnectionSession connectionSession = mockReplayableConnectionSession();
        new MySQLSetVariableAdminExecutor(setStatement).execute(connectionSession, mock());
        assertThat(MySQLSessionCharsetContext.get(connectionSession.getAttributeMap()).getConnectionCollation(), is(MySQLCharacterSets.UTF8MB4_UNICODE_CI));
        assertThat(connectionSession.getRequiredSessionVariableRecorder().toSetSQLs(databaseType.getType()),
                is(Collections.singletonList("SET collation_connection='utf8mb4_unicode_ci'")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("quotedCharsetKeywordArguments")
    void assertExecuteWithQuotedCharsetKeyword(final String name, final String variableName, final String value) {
        SetStatement setStatement = new SetStatement(databaseType, Collections.singletonList(
                new VariableAssignSegment(0, 0, new VariableSegment(0, 0, variableName), value)));
        ConnectionSession connectionSession = mockConnectionSession();
        assertThrows(UnknownCharsetException.class, () -> new MySQLSetVariableAdminExecutor(setStatement).execute(connectionSession, mock()));
    }
    
    @Test
    void assertExecuteWithQuotedCollationDatabaseVariable() {
        SetStatement setStatement = new SetStatement(databaseType, Collections.singletonList(
                new VariableAssignSegment(0, 0, new VariableSegment(0, 0, "collation_connection"), "'@@collation_database'")));
        assertThrows(UnknownCollationException.class, () -> new MySQLSetVariableAdminExecutor(setStatement).execute(mockConnectionSession(), mock()));
    }
    
    @Test
    void assertExecuteAtomicallyWithImpermissibleClientCharacterSet() {
        SetStatement setStatement = new SetStatement(databaseType, Arrays.asList(
                new VariableAssignSegment(0, 0, new VariableSegment(0, 0, "character_set_results"), "utf8mb4"),
                new VariableAssignSegment(1, 1, new VariableSegment(1, 1, "character_set_client"), "ucs2")));
        ConnectionSession connectionSession = mockConnectionSession();
        MySQLSessionCharsetContext expected = MySQLSessionCharsetContext.create(MySQLCharacterSets.LATIN1_SWEDISH_CI);
        expected.apply(connectionSession.getAttributeMap());
        assertThrows(WrongValueForVariableException.class, () -> new MySQLSetVariableAdminExecutor(setStatement).execute(connectionSession, mock()));
        assertThat(MySQLSessionCharsetContext.get(connectionSession.getAttributeMap()), is(expected));
    }
    
    @Test
    void assertExecuteWithMismatchedSetNamesCollation() {
        SetStatement setStatement = parseSetStatement("SET NAMES latin1 COLLATE utf8mb4_bin");
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        assertThrows(CollationCharsetMismatchException.class, () -> new MySQLSetVariableAdminExecutor(setStatement).execute(connectionSession, mock()));
    }
    
    @Test
    void assertExecuteWithUserVariableEqualsOperator() throws SQLException {
        SetStatement setStatement = parseSetStatement("SET @test_var = 1");
        assertThat(setStatement.getVariableAssigns().iterator().next().getVariable().getVariableType(), is(VariableType.USER));
        MySQLSetVariableAdminExecutor executor = new MySQLSetVariableAdminExecutor(setStatement);
        ConnectionSession connectionSession = mockSessionVariableConnectionSession();
        RequiredSessionVariableRecorder recorder = connectionSession.getRequiredSessionVariableRecorder();
        executor.execute(connectionSession, mock());
        assertThat(recorder.toSetSQLs(databaseType.getType()), is(Collections.singletonList("SET @test_var=1")));
    }
    
    @Test
    void assertExecuteWithUserVariableAssignmentOperator() throws SQLException {
        SetStatement setStatement = parseSetStatement("SET @test_var := 1");
        assertThat(setStatement.getVariableAssigns().iterator().next().getVariable().getVariableType(), is(VariableType.USER));
        MySQLSetVariableAdminExecutor executor = new MySQLSetVariableAdminExecutor(setStatement);
        ConnectionSession connectionSession = mockSessionVariableConnectionSession();
        RequiredSessionVariableRecorder recorder = connectionSession.getRequiredSessionVariableRecorder();
        executor.execute(connectionSession, mock());
        assertThat(recorder.toSetSQLs(databaseType.getType()), is(Collections.singletonList("SET @test_var=1")));
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        return result;
    }
    
    private ConnectionSession mockConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getAttributeMap()).thenReturn(new DefaultAttributeMap());
        return result;
    }
    
    private ConnectionSession mockReplayableConnectionSession() {
        ConnectionSession result = mockSessionVariableConnectionSession();
        when(result.getAttributeMap()).thenReturn(new DefaultAttributeMap());
        return result;
    }
    
    private ConnectionSession mockSessionVariableConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getRequiredSessionVariableRecorder()).thenReturn(new RequiredSessionVariableRecorder());
        when(result.getDatabaseConnectionManager()).thenReturn(mock(ProxyDatabaseConnectionManager.class));
        return result;
    }
    
    private static Stream<Arguments> quotedCharsetKeywordArguments() {
        return Stream.of(
                Arguments.of("quoted NULL result", "character_set_results", "'NULL'"),
                Arguments.of("quoted DEFAULT client", "character_set_client", "'DEFAULT'"),
                Arguments.of("quoted DEFAULT connection", "character_set_connection", "'DEFAULT'"));
    }
    
    private SetStatement prepareSetStatement() {
        VariableSegment maxConnectionVariableSegment = new VariableSegment(0, 0, "max_connections", "global");
        VariableAssignSegment setGlobalMaxConnectionAssignSegment = new VariableAssignSegment(0, 0, maxConnectionVariableSegment, "151");
        VariableSegment characterSetClientSegment = new VariableSegment(0, 0, "character_set_client");
        VariableAssignSegment setCharacterSetClientVariableSegment = new VariableAssignSegment(0, 0, characterSetClientSegment, "'utf8mb4'");
        return new SetStatement(databaseType, Arrays.asList(setGlobalMaxConnectionAssignSegment, setCharacterSetClientVariableSegment));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        when(result.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(new SQLParserRule(new SQLParserRuleConfiguration(new CacheOption(1, 1L), new CacheOption(1, 1L))))));
        return result;
    }
    
    @Test
    void assertSetUnknownSystemVariable() {
        SetStatement setStatement = new SetStatement(databaseType, Collections.singletonList(new VariableAssignSegment(0, 0, new VariableSegment(0, 0, "unknown_var", "SESSION"), "1")));
        MySQLSetVariableAdminExecutor executor = new MySQLSetVariableAdminExecutor(setStatement);
        UnknownSystemVariableException actual = assertThrows(UnknownSystemVariableException.class, () -> executor.execute(mock(), mock()));
        assertThat(actual.getVariableName(), is("unknown_var"));
    }
    
    @Test
    void assertSetVariableWithIncorrectScope() {
        SetStatement setStatement = new SetStatement(databaseType, Collections.singletonList(new VariableAssignSegment(0, 0, new VariableSegment(0, 0, "max_connections"), "")));
        MySQLSetVariableAdminExecutor executor = new MySQLSetVariableAdminExecutor(setStatement);
        assertThrows(ErrorGlobalVariableException.class, () -> executor.execute(mock(), mock()));
    }
    
    private SetStatement parseSetStatement(final String sql) {
        SQLStatement result = new SQLParserRule(new SQLParserRuleConfiguration(new CacheOption(1, 1L), new CacheOption(1, 1L))).getSQLParserEngine(databaseType).parse(sql, false);
        return (SetStatement) result;
    }
}
