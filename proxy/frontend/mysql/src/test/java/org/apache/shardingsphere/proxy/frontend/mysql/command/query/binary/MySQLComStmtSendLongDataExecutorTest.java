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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary;

import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.MySQLComStmtSendLongDataPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.PreparedStatementRegistry;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MySQLComStmtSendLongDataExecutorTest {
    
    @Test
    public void assertExecute() {
        MySQLComStmtSendLongDataPacket packet = mock(MySQLComStmtSendLongDataPacket.class);
        when(packet.getStatementId()).thenReturn(1);
        when(packet.getParamId()).thenReturn(0);
        byte[] data = "data".getBytes(StandardCharsets.US_ASCII);
        when(packet.getData()).thenReturn(data);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getPreparedStatementRegistry()).thenReturn(new PreparedStatementRegistry());
        MySQLPreparedStatement preparedStatement = new MySQLPreparedStatement("insert into t (b) values (?)", null, mock(SQLStatementContext.class));
        connectionSession.getPreparedStatementRegistry().addPreparedStatement(1, preparedStatement);
        MySQLComStmtSendLongDataExecutor executor = new MySQLComStmtSendLongDataExecutor(packet, connectionSession);
        Collection<DatabasePacket<?>> actual = executor.execute();
        assertThat(actual, is(Collections.emptyList()));
        assertThat(preparedStatement.getLongData(), is(Collections.singletonMap(0, data)));
    }
}
