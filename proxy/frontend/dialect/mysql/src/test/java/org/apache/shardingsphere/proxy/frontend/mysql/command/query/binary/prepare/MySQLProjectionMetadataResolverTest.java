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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare;

import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCharacterSets;
import org.apache.shardingsphere.database.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinitionFlag;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, MySQLPreparedStatementMetadataFactory.class, DatabaseTypeFactory.class})
class MySQLProjectionMetadataResolverTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("projectionArguments")
    void assertResolveProjectionPackets(final String name, final int jdbcType, final String typeName, final DatabaseType storageType, final boolean federationEnabled,
                                        final MySQLBinaryColumnType expectedColumnType, final int expectedFlags, final int expectedStorageLookups) throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.of("foo_db"));
        when(selectStatementContext.getProjectionsContext().getExpandProjections()).thenReturn(Collections.nCopies(2, mock(Projection.class)));
        PreparedStatement preparedStatement = mock(PreparedStatement.class, RETURNS_DEEP_STUBS);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(preparedStatement.getMetaData()).thenReturn(resultSetMetaData);
        mockResultSetMetaData(resultSetMetaData, jdbcType, typeName);
        if (Types.TIMESTAMP == jdbcType && "DATETIME".equals(typeName) && !federationEnabled) {
            Connection connection = mock(Connection.class);
            DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
            when(preparedStatement.getConnection()).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(databaseMetaData);
            when(DatabaseTypeFactory.get(databaseMetaData)).thenReturn(storageType);
        }
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        MySQLServerPreparedStatement serverPreparedStatement = mock(MySQLServerPreparedStatement.class);
        when(MySQLPreparedStatementMetadataFactory.load(connectionSession, serverPreparedStatement)).thenReturn(preparedStatement);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        when(database.getProtocolType()).thenReturn(databaseType);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        RuleMetaData globalRuleMetaData = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData();
        SQLFederationRule sqlFederationRule = mock(SQLFederationRule.class);
        doReturn(sqlFederationRule).when(globalRuleMetaData).getSingleRule(SQLFederationRule.class);
        when(sqlFederationRule.isSqlFederationEnabled()).thenReturn(federationEnabled);
        int expectedCharacterSet = MySQLCharacterSets.UTF8MB4_UNICODE_CI.getId();
        Collection<MySQLPacket> actual = MySQLProjectionMetadataResolver.resolveProjectionPackets(connectionSession, serverPreparedStatement, selectStatementContext, expectedCharacterSet);
        assertThat(actual.size(), is(2));
        for (MySQLPacket each : actual) {
            MySQLPacketPayload payload = createPayload((MySQLColumnDefinition41Packet) each);
            skipColumnDefinitionStrings(payload);
            payload.readIntLenenc();
            assertThat(payload.readInt2(), is(expectedCharacterSet));
            payload.skipReserved(4);
            assertThat(payload.readInt1(), is(expectedColumnType.getValue()));
            assertThat(payload.readInt2(), is(expectedFlags));
        }
        verify(preparedStatement, times(expectedStorageLookups)).getConnection();
        verify(preparedStatement).close();
    }
    
    private void mockResultSetMetaData(final ResultSetMetaData resultSetMetaData, final int jdbcType, final String typeName) throws SQLException {
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        for (int columnIndex = 1; columnIndex <= 2; columnIndex++) {
            when(resultSetMetaData.getColumnName(columnIndex)).thenReturn("content" + columnIndex);
            when(resultSetMetaData.getColumnLabel(columnIndex)).thenReturn("content" + columnIndex);
            when(resultSetMetaData.getColumnType(columnIndex)).thenReturn(jdbcType);
            when(resultSetMetaData.getColumnTypeName(columnIndex)).thenReturn(typeName);
            when(resultSetMetaData.getColumnDisplaySize(columnIndex)).thenReturn(1024);
            when(resultSetMetaData.isNullable(columnIndex)).thenReturn(ResultSetMetaData.columnNullable);
            when(resultSetMetaData.getTableName(columnIndex)).thenReturn("");
        }
    }
    
    private MySQLPacketPayload createPayload(final MySQLColumnDefinition41Packet packet) {
        ByteBuf byteBuf = Unpooled.buffer();
        packet.write(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8));
        return new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
    }
    
    private void skipColumnDefinitionStrings(final MySQLPacketPayload payload) {
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
    }
    
    private static Stream<Arguments> projectionArguments() {
        DatabaseType mysql = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        DatabaseType sqlserver = mock(DatabaseType.class);
        return Stream.of(
                Arguments.of("character-large-object", Types.CLOB, "BLOB SUB_TYPE TEXT", mysql, false, MySQLBinaryColumnType.BLOB, MySQLColumnDefinitionFlag.BLOB.getValue(), 0),
                Arguments.of("mysql-datetime", Types.TIMESTAMP, "DATETIME", mysql, false, MySQLBinaryColumnType.DATETIME, MySQLColumnDefinitionFlag.UNSIGNED.getValue(), 1),
                Arguments.of("federation-enabled", Types.TIMESTAMP, "DATETIME", mysql, true, MySQLBinaryColumnType.TIMESTAMP, MySQLColumnDefinitionFlag.UNSIGNED.getValue(), 0),
                Arguments.of("mysql-trunk-only-datetime", Types.TIMESTAMP, "DATETIME", TypedSPILoader.getService(DatabaseType.class, "H2"), false, MySQLBinaryColumnType.TIMESTAMP,
                        MySQLColumnDefinitionFlag.UNSIGNED.getValue(), 1),
                Arguments.of("sqlserver-datetime", Types.TIMESTAMP, "DATETIME", sqlserver, false, MySQLBinaryColumnType.TIMESTAMP, MySQLColumnDefinitionFlag.UNSIGNED.getValue(), 1),
                Arguments.of("timestamp", Types.TIMESTAMP, "TIMESTAMP", mysql, false, MySQLBinaryColumnType.TIMESTAMP, MySQLColumnDefinitionFlag.UNSIGNED.getValue(), 0),
                Arguments.of("missing-type-name", Types.TIMESTAMP, "", mysql, false, MySQLBinaryColumnType.TIMESTAMP, MySQLColumnDefinitionFlag.UNSIGNED.getValue(), 0));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("datetimeColumnArguments")
    void assertResolveDatetimeColumnIndexes(final String name, final int jdbcType, final String typeName, final Set<Integer> expected) throws SQLException {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        MySQLServerPreparedStatement serverPreparedStatement = mock(MySQLServerPreparedStatement.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        for (int columnIndex = 1; columnIndex <= 2; columnIndex++) {
            when(resultSetMetaData.getColumnType(columnIndex)).thenReturn(jdbcType);
            if (Types.TIMESTAMP == jdbcType) {
                when(resultSetMetaData.getColumnTypeName(columnIndex)).thenReturn(typeName);
            }
        }
        when(MySQLPreparedStatementMetadataFactory.loadMetadataWithoutExecution(connectionSession, serverPreparedStatement)).thenReturn(resultSetMetaData);
        Set<Integer> actual = MySQLProjectionMetadataResolver.resolveDatetimeColumnIndexes(connectionSession, serverPreparedStatement);
        assertThat(actual, is(expected));
    }
    
    private static Stream<Arguments> datetimeColumnArguments() {
        return Stream.of(
                Arguments.of("mysql-datetime", Types.TIMESTAMP, "DATETIME", ImmutableSet.of(1, 2)),
                Arguments.of("case-insensitive", Types.TIMESTAMP, "datetime", ImmutableSet.of(1, 2)),
                Arguments.of("timestamp", Types.TIMESTAMP, "TIMESTAMP", Collections.emptySet()),
                Arguments.of("date", Types.DATE, "DATETIME", Collections.emptySet()),
                Arguments.of("integer", Types.INTEGER, "INT", Collections.emptySet()));
    }
    
    @Test
    void assertResolveDatetimeColumnIndexesWithNullMetadata() throws SQLException {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        MySQLServerPreparedStatement serverPreparedStatement = mock(MySQLServerPreparedStatement.class);
        when(MySQLPreparedStatementMetadataFactory.loadMetadataWithoutExecution(connectionSession, serverPreparedStatement)).thenReturn(null);
        assertTrue(MySQLProjectionMetadataResolver.resolveDatetimeColumnIndexes(connectionSession, serverPreparedStatement).isEmpty());
    }
    
    @Test
    void assertResolveDatetimeColumnIndexesWithSQLException() throws SQLException {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        MySQLServerPreparedStatement serverPreparedStatement = mock(MySQLServerPreparedStatement.class);
        when(MySQLPreparedStatementMetadataFactory.loadMetadataWithoutExecution(connectionSession, serverPreparedStatement)).thenThrow(SQLException.class);
        assertThrows(SQLException.class, () -> MySQLProjectionMetadataResolver.resolveDatetimeColumnIndexes(connectionSession, serverPreparedStatement));
    }
    
    @Test
    void assertResolveProjectionPacketsWithNullMetadata() throws SQLException {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        MySQLServerPreparedStatement serverPreparedStatement = mock(MySQLServerPreparedStatement.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(MySQLPreparedStatementMetadataFactory.load(connectionSession, serverPreparedStatement)).thenReturn(preparedStatement);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getProjectionsContext().getExpandProjections()).thenReturn(Collections.singletonList(mock(Projection.class)));
        Collection<MySQLPacket> actual = MySQLProjectionMetadataResolver.resolveProjectionPackets(connectionSession, serverPreparedStatement, selectStatementContext, 45);
        MySQLPacketPayload payload = createPayload((MySQLColumnDefinition41Packet) actual.iterator().next());
        skipColumnDefinitionStrings(payload);
        payload.readIntLenenc();
        assertThat(payload.readInt2(), is(45));
        payload.skipReserved(4);
        assertThat(payload.readInt1(), is(MySQLBinaryColumnType.VAR_STRING.getValue()));
        verify(preparedStatement).close();
    }
}
