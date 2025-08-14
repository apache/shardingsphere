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

package org.apache.shardingsphere.proxy.frontend.firebird.authentication;

import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.proxy.frontend.connection.ConnectionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.test.infra.framework.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ConnectionIdGenerator.class, FirebirdTransactionIdGenerator.class, FirebirdStatementIdGenerator.class})
class FirebirdAuthenticationEngineTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext context;
    
    @Mock
    private ConnectionIdGenerator idGenerator;
    
    @Test
    void assertHandshake() {
        when(ConnectionIdGenerator.getInstance()).thenReturn(idGenerator);
        when(idGenerator.nextId()).thenReturn(1);
        FirebirdAuthenticationEngine engine = new FirebirdAuthenticationEngine();
        assertThat(engine.handshake(context), is(1));
        verify(FirebirdTransactionIdGenerator.getInstance()).registerConnection(1);
        verify(FirebirdStatementIdGenerator.getInstance()).registerConnection(1);
    }
    
    // TODO Implement tests for authenticate() method for the following cases: CONNECT, ATTACH, Cont_Auth (when implemented), Unknown Option
}
