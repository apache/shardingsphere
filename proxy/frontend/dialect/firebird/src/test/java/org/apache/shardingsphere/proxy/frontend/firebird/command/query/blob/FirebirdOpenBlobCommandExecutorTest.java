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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdOpenBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdOpenBlobCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    @Mock
    private FirebirdOpenBlobCommandPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setup() {
        FirebirdStatementIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdStatementIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecute() {
        FirebirdOpenBlobCommandExecutor executor = new FirebirdOpenBlobCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        DatabasePacket response = actual.iterator().next();
        assertThat(response, instanceOf(FirebirdGenericResponsePacket.class));
        assertThat(((FirebirdGenericResponsePacket) response).getHandle(), is(1));
    }
}
