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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdRollbackTransactionPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdRollbackTransactionCommandExecutorTest {
    
    @Mock
    private FirebirdRollbackTransactionPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Test
    void assertExecute() throws SQLException {
        try (MockedConstruction<ProxyBackendTransactionManager> mocked = mockConstruction(ProxyBackendTransactionManager.class, (mock, context) -> {
        })) {
            Collection<DatabasePacket> actual = new FirebirdRollbackTransactionCommandExecutor(packet, connectionSession).execute();
            assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
            verify(mocked.constructed().get(0)).rollback();
        }
    }
    
    @Test
    void assertExecuteWithAutoCommit() throws SQLException {
        when(connectionSession.isAutoCommit()).thenReturn(true);
        try (MockedConstruction<ProxyBackendTransactionManager> mocked = mockConstruction(ProxyBackendTransactionManager.class)) {
            Collection<DatabasePacket> actual = new FirebirdRollbackTransactionCommandExecutor(packet, connectionSession).execute();
            assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
            assertTrue(mocked.constructed().isEmpty());
        }
    }
}
