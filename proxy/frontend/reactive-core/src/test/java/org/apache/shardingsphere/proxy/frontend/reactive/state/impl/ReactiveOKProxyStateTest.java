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

package org.apache.shardingsphere.proxy.frontend.reactive.state.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.reactive.command.ReactiveCommandExecuteTask;
import org.apache.shardingsphere.proxy.frontend.reactive.protocol.fixture.DummyReactiveDatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.reactive.spi.ReactiveDatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ReactiveOKProxyStateTest {
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertExecute() {
        ChannelHandlerContext channelHandlerContext = mock(ChannelHandlerContext.class);
        EventExecutor eventExecutor = mock(EventExecutor.class);
        when(channelHandlerContext.executor()).thenReturn(eventExecutor);
        Channel channel = mock(Channel.class);
        Attribute<ReactiveDatabaseProtocolFrontendEngine> attribute = mock(Attribute.class);
        when(channel.<ReactiveDatabaseProtocolFrontendEngine>attr(AttributeKey.valueOf(ReactiveDatabaseProtocolFrontendEngine.class.getName()))).thenReturn(attribute);
        when(channelHandlerContext.channel()).thenReturn(channel);
        DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine = mock(DatabaseProtocolFrontendEngine.class);
        when(databaseProtocolFrontendEngine.getType()).thenReturn("Dummy");
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        new ReactiveOKProxyState().execute(channelHandlerContext, null, databaseProtocolFrontendEngine, connectionSession);
        verify(attribute).setIfAbsent(any(DummyReactiveDatabaseProtocolFrontendEngine.class));
        verify(eventExecutor).execute(any(ReactiveCommandExecuteTask.class));
    }
}
