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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;

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
    void assertExecuteWithDeleteRecords() throws ReflectiveOperationException {
        when(packet.getHandle()).thenReturn(1);
        when(packet.getInfoItems()).thenReturn(Collections.emptyList());
        FirebirdServerPreparedStatement preparedStatement = new FirebirdServerPreparedStatement(
                "DELETE FROM tbl", mock(SQLStatementContext.class), mock(HintValueContext.class), FirebirdSQLInfoReturnValue.DELETE);
        preparedStatement.setRecordCount(3);
        FirebirdServerPreparedStatement anotherPreparedStatement = new FirebirdServerPreparedStatement(
                "DELETE FROM another_tbl", mock(SQLStatementContext.class), mock(HintValueContext.class), FirebirdSQLInfoReturnValue.DELETE);
        anotherPreparedStatement.setRecordCount(7);
        ServerPreparedStatementRegistry registry = new ServerPreparedStatementRegistry();
        registry.addPreparedStatement(1, preparedStatement);
        registry.addPreparedStatement(2, anotherPreparedStatement);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
        FirebirdSQLInfoExecutor executor = new FirebirdSQLInfoExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
        FirebirdGenericResponsePacket responsePacket = (FirebirdGenericResponsePacket) actual.iterator().next();
        assertThat(responsePacket.getData(), isA(FirebirdSQLInfoReturnPacket.class));
        FirebirdSQLInfoReturnPacket returnPacket = (FirebirdSQLInfoReturnPacket) responsePacket.getData();
        assertThat(Plugins.getMemberAccessor().get(FirebirdSQLInfoReturnPacket.class.getDeclaredField("statementType"), returnPacket), is(FirebirdSQLInfoReturnValue.DELETE));
        assertThat(Plugins.getMemberAccessor().get(FirebirdSQLInfoReturnPacket.class.getDeclaredField("recordCount"), returnPacket), is(3));
    }
    
    @Test
    void assertExecuteWithoutStatement() {
        when(packet.getInfoItems()).thenReturn(Collections.emptyList());
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        Collection<DatabasePacket> actual = new FirebirdSQLInfoExecutor(packet, connectionSession).execute();
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getData(), isA(FirebirdSQLInfoReturnPacket.class));
    }
}
