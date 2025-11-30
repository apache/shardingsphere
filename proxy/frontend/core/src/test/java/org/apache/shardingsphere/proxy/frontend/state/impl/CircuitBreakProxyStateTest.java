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

package org.apache.shardingsphere.proxy.frontend.state.impl;

import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.exception.kernel.connection.CircuitBreakException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CircuitBreakProxyStateTest {
    
    @Test
    void assertExecute() {
        ChannelHandlerContext channelHandlerContext = mock(ChannelHandlerContext.class);
        DatabaseProtocolFrontendEngine engine = mock(DatabaseProtocolFrontendEngine.class, RETURNS_DEEP_STUBS);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        DatabasePacket errorPacket = mock(DatabasePacket.class);
        when(engine.getCommandExecuteEngine().getErrorPacket(any(CircuitBreakException.class))).thenReturn(errorPacket);
        DatabasePacket otherPacket = mock(DatabasePacket.class);
        when(engine.getCommandExecuteEngine().getOtherPacket(connectionSession)).thenReturn(Optional.of(otherPacket));
        new CircuitBreakProxyState().execute(channelHandlerContext, null, engine, connectionSession);
        verify(channelHandlerContext).writeAndFlush(errorPacket);
        verify(channelHandlerContext).writeAndFlush(otherPacket);
    }
}
