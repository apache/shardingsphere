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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.info;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdLastObjectRegistry;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdSQLInfoExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final int INVALID_OBJECT_HANDLE = 0xFFFF;
    
    @Mock
    private FirebirdInfoPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        FirebirdStatementIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdStatementIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdLastObjectRegistry.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("sqlStatementProvider")
    void assertExecuteWithRecordsInfo(final String name, final SQLStatement sqlStatement, final long expectedInsertCount, final long expectedUpdateCount, final long expectedDeleteCount) {
        int statementId = FirebirdStatementIdGenerator.getInstance().nextStatementId(CONNECTION_ID);
        when(packet.getHandle()).thenReturn(statementId);
        ServerPreparedStatementRegistry registry = createPreparedStatementRegistry(statementId, sqlStatement);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
        Collection<DatabasePacket> actual = new FirebirdSQLInfoExecutor(packet, connectionSession).execute();
        FirebirdSQLInfoReturnPacket actualReturnPacket = (FirebirdSQLInfoReturnPacket) ((FirebirdGenericResponsePacket) actual.iterator().next()).getData();
        assertThat(actualReturnPacket.getRecordsInfo().getInsertCount(), is(expectedInsertCount));
        assertThat(actualReturnPacket.getRecordsInfo().getUpdateCount(), is(expectedUpdateCount));
        assertThat(actualReturnPacket.getRecordsInfo().getDeleteCount(), is(expectedDeleteCount));
    }
    
    private ServerPreparedStatementRegistry createPreparedStatementRegistry(final int statementId, final SQLStatement sqlStatement) {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        FirebirdServerPreparedStatement preparedStatement = new FirebirdServerPreparedStatement("DML", sqlStatementContext, new HintValueContext());
        preparedStatement.setAffectedRows(3L);
        ServerPreparedStatementRegistry result = new ServerPreparedStatementRegistry();
        result.addPreparedStatement(statementId, preparedStatement);
        return result;
    }
    
    @Test
    void assertExecuteWithLastStatementHandle() {
        FirebirdSQLInfoReturnPacket actual = executeWithLastStatementHandle(INVALID_OBJECT_HANDLE);
        assertThat(actual.getRecordsInfo().getInsertCount(), is(3L));
    }
    
    private FirebirdSQLInfoReturnPacket executeWithLastStatementHandle(final int encodedHandle) {
        int statementId = FirebirdStatementIdGenerator.getInstance().nextStatementId(CONNECTION_ID);
        FirebirdLastObjectRegistry.getInstance().registerLazySendConnection(CONNECTION_ID);
        FirebirdLastObjectRegistry.getInstance().setLastStatement(CONNECTION_ID, statementId);
        ByteBuf byteBuf = Unpooled.buffer().writeInt(FirebirdCommandPacketType.INFO_SQL.getValue()).writeInt(encodedHandle).writeInt(0).writeInt(1)
                .writeByte(FirebirdSQLInfoPacketType.RECORDS.getCode()).writeZero(3).writeInt(100);
        FirebirdInfoPacket infoPacket = new FirebirdInfoPacket(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8), FirebirdSQLInfoPacketType::valueOf);
        ServerPreparedStatementRegistry registry = createPreparedStatementRegistry(statementId, mock(InsertStatement.class));
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
        Collection<DatabasePacket> responsePackets = new FirebirdSQLInfoExecutor(infoPacket, connectionSession).execute();
        return (FirebirdSQLInfoReturnPacket) ((FirebirdGenericResponsePacket) responsePackets.iterator().next()).getData();
    }
    
    @Test
    void assertExecuteWithSignExtendedLastStatementHandle() {
        FirebirdSQLInfoReturnPacket actual = executeWithLastStatementHandle(0xFFFFFFFF);
        assertThat(actual.getRecordsInfo().getInsertCount(), is(3L));
    }
    
    @Test
    void assertExecuteWithNotAllocatedStatement() {
        when(packet.getHandle()).thenReturn(0x7FFF);
        Collection<DatabasePacket> actual = new FirebirdSQLInfoExecutor(packet, connectionSession).execute();
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getErrorCode(), is(ISCConstants.isc_bad_req_handle));
    }
    
    @Test
    void assertExecuteWithLastNonStatementObjectHandle() {
        int statementId = FirebirdStatementIdGenerator.getInstance().nextStatementId(CONNECTION_ID);
        FirebirdLastObjectRegistry.getInstance().registerLazySendConnection(CONNECTION_ID);
        FirebirdLastObjectRegistry.getInstance().setLastStatement(CONNECTION_ID, statementId);
        FirebirdLastObjectRegistry.getInstance().setLastNonStatementObject(CONNECTION_ID);
        when(packet.getHandle()).thenReturn(INVALID_OBJECT_HANDLE);
        Collection<DatabasePacket> actual = new FirebirdSQLInfoExecutor(packet, connectionSession).execute();
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getErrorCode(), is(ISCConstants.isc_bad_req_handle));
    }
    
    @Test
    void assertExecuteWithUnpreparedStatement() {
        when(packet.getHandle()).thenReturn(FirebirdStatementIdGenerator.getInstance().nextStatementId(CONNECTION_ID));
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        Collection<DatabasePacket> actual = new FirebirdSQLInfoExecutor(packet, connectionSession).execute();
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getErrorCode(), is(ISCConstants.isc_unprepared_stmt));
    }
    
    private static Stream<Arguments> sqlStatementProvider() {
        return Stream.of(
                Arguments.of("insert", mock(InsertStatement.class), 3L, 0L, 0L),
                Arguments.of("update", mock(UpdateStatement.class), 0L, 3L, 0L),
                Arguments.of("delete", mock(DeleteStatement.class), 0L, 0L, 3L));
    }
}
