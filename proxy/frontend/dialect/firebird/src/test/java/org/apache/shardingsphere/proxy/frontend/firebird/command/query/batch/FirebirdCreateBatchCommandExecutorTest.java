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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchCreateCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdCreateBatchCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final int STATEMENT_ID = 11;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private FirebirdBatchCreateCommandPacket packet;
    
    @Test
    void assertExecute() throws SQLException {
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        Collection<DatabasePacket> actual = new FirebirdCreateBatchCommandExecutor(packet, connectionSession).execute();
        assertThat(actual.size(), is(1));
        DatabasePacket actualPacket = actual.iterator().next();
        assertThat(actualPacket, isA(FirebirdGenericResponsePacket.class));
        assertThat(((FirebirdGenericResponsePacket) actualPacket).getHandle(), is(STATEMENT_ID));
        FirebirdBatchStatement actualBatchStatement = FirebirdBatchRegistry.getInstance().getBatchStatement(CONNECTION_ID, STATEMENT_ID);
        assertNotNull(actualBatchStatement);
        assertThat(actualBatchStatement.getStatementHandle(), is(STATEMENT_ID));
        FirebirdBatchRegistry.getInstance().unregisterConnection(CONNECTION_ID);
    }
}
