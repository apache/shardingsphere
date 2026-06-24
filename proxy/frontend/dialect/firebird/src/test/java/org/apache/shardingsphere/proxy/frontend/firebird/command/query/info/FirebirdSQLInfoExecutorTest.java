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

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdSQLInfoExecutorTest {
    
    @Mock
    private FirebirdInfoPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Test
    void assertExecute() {
        when(packet.getInfoItems()).thenReturn(Collections.emptyList());
        FirebirdSQLInfoExecutor executor = new FirebirdSQLInfoExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
        FirebirdGenericResponsePacket responsePacket = (FirebirdGenericResponsePacket) actual.iterator().next();
        assertThat(responsePacket.getData(), isA(FirebirdSQLInfoReturnPacket.class));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("sqlStatementProvider")
    void assertExecuteWithRecordsInfo(final String name, final SQLStatement sqlStatement, final long expectedInsertCount, final long expectedUpdateCount, final long expectedDeleteCount) {
        when(packet.getInfoItems()).thenReturn(Collections.emptyList());
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
