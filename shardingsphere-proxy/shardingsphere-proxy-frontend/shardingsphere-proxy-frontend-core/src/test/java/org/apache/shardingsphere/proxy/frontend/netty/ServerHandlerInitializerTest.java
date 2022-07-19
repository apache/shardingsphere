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

package org.apache.shardingsphere.proxy.frontend.netty;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.apache.shardingsphere.db.protocol.codec.PacketCodec;
import org.apache.shardingsphere.db.protocol.netty.ChannelAttrInitializer;
import org.apache.shardingsphere.test.fixture.database.type.MockedDatabaseType;
import org.junit.Test;
import org.mockito.MockedConstruction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ServerHandlerInitializerTest {
    
    @Test
    public void assertInitChannel() {
        SocketChannel channel = mock(SocketChannel.class);
        ChannelPipeline pipeline = mock(ChannelPipeline.class);
        when(channel.pipeline()).thenReturn(pipeline);
        ServerHandlerInitializer initializer = new ServerHandlerInitializer(new MockedDatabaseType());
        try (MockedConstruction<FrontendChannelInboundHandler> ignored = mockConstruction(FrontendChannelInboundHandler.class)) {
            initializer.initChannel(channel);
        }
        verify(pipeline).addLast(any(ChannelAttrInitializer.class));
        verify(pipeline).addLast(any(PacketCodec.class));
        verify(pipeline).addLast(any(FrontendChannelLimitationInboundHandler.class));
        verify(pipeline).addLast(any(FrontendChannelInboundHandler.class));
    }
}
