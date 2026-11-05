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

import io.netty.util.AttributeKey;
import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdObjectHandleGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdSQLInfoExecutorTest {
    
    private static final int CONNECTION_ID = 21416;
    
    @Mock
    private FirebirdInfoPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Test
    void assertExecute() {
        FirebirdServerPreparedStatement preparedStatement = mock(FirebirdServerPreparedStatement.class);
        when(preparedStatement.getSqlStatementContext()).thenReturn(mock(SQLStatementContext.class));
        ServerPreparedStatementRegistry registry = new ServerPreparedStatementRegistry();
        registry.addPreparedStatement(1, preparedStatement);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
        when(packet.getHandle()).thenReturn(1);
        FirebirdSQLInfoExecutor executor = new FirebirdSQLInfoExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
        FirebirdGenericResponsePacket responsePacket = (FirebirdGenericResponsePacket) actual.iterator().next();
        assertThat(responsePacket.getData(), isA(FirebirdSQLInfoReturnPacket.class));
    }
    
    @Test
    void assertExecuteWithStatementFlags() {
        when(packet.getInfoItems()).thenReturn(Collections.singletonList(FirebirdSQLInfoPacketType.STMT_FLAGS));
        when(packet.getHandle()).thenReturn(1);
        FirebirdServerPreparedStatement preparedStatement = mock(FirebirdServerPreparedStatement.class);
        when(preparedStatement.getSqlStatementContext()).thenReturn(mock(SQLStatementContext.class));
        when(preparedStatement.getStatementFlags()).thenReturn(3);
        ServerPreparedStatementRegistry registry = new ServerPreparedStatementRegistry();
        registry.addPreparedStatement(1, preparedStatement);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
        Collection<DatabasePacket> actual = new FirebirdSQLInfoExecutor(packet, connectionSession).execute();
        FirebirdSQLInfoReturnPacket actualPacket = (FirebirdSQLInfoReturnPacket) ((FirebirdGenericResponsePacket) actual.iterator().next()).getData();
        assertThat(actualPacket.getStatementFlags(), is(3));
    }
    
    @Test
    void assertExecuteWithUnpreparedStatementFlags() {
        FirebirdStatementIdGenerator actualGenerator = FirebirdStatementIdGenerator.getInstance();
        actualGenerator.registerConnection(CONNECTION_ID);
        try {
            int actualStatementId = actualGenerator.nextStatementId(CONNECTION_ID);
            assertStatementFlagsError(actualStatementId, ISCConstants.isc_unprepared_stmt);
        } finally {
            actualGenerator.unregisterConnection(CONNECTION_ID);
            FirebirdObjectHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        }
    }
    
    @Test
    void assertExecuteWithUnknownStatementFlags() {
        FirebirdStatementIdGenerator actualGenerator = FirebirdStatementIdGenerator.getInstance();
        actualGenerator.registerConnection(CONNECTION_ID);
        try {
            assertStatementFlagsError(1, ISCConstants.isc_bad_req_handle);
        } finally {
            actualGenerator.unregisterConnection(CONNECTION_ID);
            FirebirdObjectHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(ints = {-1, 65535})
    void assertExecuteWithLazyStatementFlags(final int wireHandle) {
        FirebirdStatementIdGenerator actualGenerator = FirebirdStatementIdGenerator.getInstance();
        actualGenerator.registerConnection(CONNECTION_ID);
        try {
            int statementId = actualGenerator.nextStatementId(CONNECTION_ID);
            FirebirdServerPreparedStatement preparedStatement = mock(FirebirdServerPreparedStatement.class);
            when(preparedStatement.getSqlStatementContext()).thenReturn(mock(SQLStatementContext.class));
            when(preparedStatement.getStatementFlags()).thenReturn(3);
            ServerPreparedStatementRegistry registry = new ServerPreparedStatementRegistry();
            registry.addPreparedStatement(statementId, preparedStatement);
            DefaultAttributeMap attributes = new DefaultAttributeMap();
            attributes.attr(AttributeKey.<Boolean>valueOf("FIREBIRD_LAZY_SEND")).set(true);
            when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
            when(connectionSession.getAttributeMap()).thenReturn(attributes);
            when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
            when(packet.getInfoItems()).thenReturn(Collections.singletonList(FirebirdSQLInfoPacketType.STMT_FLAGS));
            when(packet.getHandle()).thenReturn(wireHandle);
            FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdSQLInfoExecutor(packet, connectionSession).execute().iterator().next();
            assertThat(((FirebirdSQLInfoReturnPacket) actual.getData()).getStatementFlags(), is(3));
        } finally {
            actualGenerator.unregisterConnection(CONNECTION_ID);
            FirebirdObjectHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        }
    }
    
    @Test
    void assertLazyStatementFlagsRejectLastObjectOfAnotherType() {
        FirebirdStatementIdGenerator actualGenerator = FirebirdStatementIdGenerator.getInstance();
        actualGenerator.registerConnection(CONNECTION_ID);
        try {
            actualGenerator.nextStatementId(CONNECTION_ID);
            FirebirdObjectHandleGenerator.getInstance().nextHandle(CONNECTION_ID);
            DefaultAttributeMap attributes = new DefaultAttributeMap();
            attributes.attr(AttributeKey.<Boolean>valueOf("FIREBIRD_LAZY_SEND")).set(true);
            when(connectionSession.getAttributeMap()).thenReturn(attributes);
            assertStatementFlagsError(65535, ISCConstants.isc_bad_req_handle);
        } finally {
            actualGenerator.unregisterConnection(CONNECTION_ID);
            FirebirdObjectHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        }
    }
    
    @Test
    void assertLazyStatementFlagsRejectedWhenLazySendIsDisabled() {
        FirebirdStatementIdGenerator actualGenerator = FirebirdStatementIdGenerator.getInstance();
        actualGenerator.registerConnection(CONNECTION_ID);
        try {
            actualGenerator.nextStatementId(CONNECTION_ID);
            assertStatementFlagsError(65535, ISCConstants.isc_bad_req_handle);
        } finally {
            actualGenerator.unregisterConnection(CONNECTION_ID);
            FirebirdObjectHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        }
    }
    
    private void assertStatementFlagsError(final int statementId, final int errorCode) {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        FirebirdInfoPacket request = new FirebirdInfoPacket(statementId, 0, 1024);
        request.getInfoItems().add(FirebirdSQLInfoPacketType.STMT_FLAGS);
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdSQLInfoExecutor(request, connectionSession).execute().iterator().next();
        assertThat(actual.getStatusVector().getSegments().size(), is(1));
        assertThat(actual.getStatusVector().getSegments().get(0).getGdsCode(), is(errorCode));
        assertTrue(actual.getStatusVector().getSegments().get(0).getTypedArguments().isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("sqlStatementProvider")
    void assertExecuteWithRecordsInfo(final String name, final SQLStatement sqlStatement, final long expectedInsertCount, final long expectedUpdateCount, final long expectedDeleteCount) {
        when(packet.getHandle()).thenReturn(1);
        ServerPreparedStatementRegistry registry = new ServerPreparedStatementRegistry();
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        FirebirdServerPreparedStatement preparedStatement = new FirebirdServerPreparedStatement("DML", sqlStatementContext, new HintValueContext());
        preparedStatement.setAffectedRows(3L);
        registry.addPreparedStatement(1, preparedStatement);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
        FirebirdSQLInfoExecutor executor = new FirebirdSQLInfoExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        FirebirdGenericResponsePacket responsePacket = (FirebirdGenericResponsePacket) actual.iterator().next();
        FirebirdSQLInfoReturnPacket returnPacket = (FirebirdSQLInfoReturnPacket) responsePacket.getData();
        assertThat(returnPacket.getRecordsInfo().getInsertCount(), is(expectedInsertCount));
        assertThat(returnPacket.getRecordsInfo().getUpdateCount(), is(expectedUpdateCount));
        assertThat(returnPacket.getRecordsInfo().getDeleteCount(), is(expectedDeleteCount));
    }
    
    private static Stream<Arguments> sqlStatementProvider() {
        return Stream.of(
                Arguments.of("insert", mock(InsertStatement.class), 3L, 0L, 0L),
                Arguments.of("update", mock(UpdateStatement.class), 0L, 3L, 0L),
                Arguments.of("delete", mock(DeleteStatement.class), 0L, 0L, 3L));
    }
}
