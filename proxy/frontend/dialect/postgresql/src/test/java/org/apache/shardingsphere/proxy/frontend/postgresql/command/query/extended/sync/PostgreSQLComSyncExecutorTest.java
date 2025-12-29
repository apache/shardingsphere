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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.sync;

import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLReadyForQueryPacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLComSyncExecutorTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    void assertNewInstance() {
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus());
        PostgreSQLComSyncExecutor actual = new PostgreSQLComSyncExecutor(connectionSession);
        assertThat(actual.execute().iterator().next(), is(PostgreSQLReadyForQueryPacket.NOT_IN_TRANSACTION));
    }
    
    @Test
    void assertExecuteInTransaction() {
        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setInTransaction(true);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        PostgreSQLComSyncExecutor actual = new PostgreSQLComSyncExecutor(connectionSession);
        assertThat(actual.execute().iterator().next(), is(PostgreSQLReadyForQueryPacket.IN_TRANSACTION));
    }
}
