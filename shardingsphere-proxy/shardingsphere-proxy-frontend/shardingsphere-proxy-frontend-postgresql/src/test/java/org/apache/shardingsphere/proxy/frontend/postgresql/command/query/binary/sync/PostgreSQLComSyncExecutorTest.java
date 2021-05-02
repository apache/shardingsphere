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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.sync;

import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLReadyForQueryPacket;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLComSyncExecutorTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Test
    public void assertNewInstance() {
        when(backendConnection.getTransactionStatus()).thenReturn(new TransactionStatus(TransactionType.LOCAL));
        PostgreSQLComSyncExecutor actual = new PostgreSQLComSyncExecutor(backendConnection);
        assertThat(actual.execute().iterator().next(), is(instanceOf(PostgreSQLReadyForQueryPacket.class)));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    @SneakyThrows(SQLException.class)
    public void assertNextFalse() {
        PostgreSQLComSyncExecutor actual = new PostgreSQLComSyncExecutor(backendConnection);
        assertFalse(actual.next());
        actual.getQueryRowPacket();
    }
    
    @Test
    public void assertResponseType() {
        ResponseType actual = new PostgreSQLComSyncExecutor(backendConnection).getResponseType();
        assertThat(actual, is(ResponseType.UPDATE));
    }
}
