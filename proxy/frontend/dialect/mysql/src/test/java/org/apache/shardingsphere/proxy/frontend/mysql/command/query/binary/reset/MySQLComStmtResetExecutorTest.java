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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.reset;

import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.reset.MySQLComStmtResetPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLComStmtResetExecutorTest {
    
    @Test
    void assertExecute() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus());
        MySQLServerPreparedStatement preparedStatement = new MySQLServerPreparedStatement("", mock(SQLStatementContext.class), new HintValueContext(), Collections.emptyList());
        preparedStatement.getLongData().put(0, new byte[0]);
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(1, preparedStatement);
        MySQLComStmtResetPacket packet = mock(MySQLComStmtResetPacket.class);
        when(packet.getStatementId()).thenReturn(1);
        MySQLComStmtResetExecutor executor = new MySQLComStmtResetExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), isA(MySQLOKPacket.class));
        assertTrue(preparedStatement.getLongData().isEmpty());
    }
}
