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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import org.apache.shardingsphere.proxy.frontend.event.WriteCompleteEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyFlowControlHandlerTest {
    
    @Test
    void assertUserEventTriggered() {
        AtomicBoolean eventReceived = new AtomicBoolean(false);
        EmbeddedChannel channel = new EmbeddedChannel(new ProxyFlowControlHandler(), new ChannelInboundHandlerAdapter() {
            
            @Override
            public void userEventTriggered(final ChannelHandlerContext ctx, final Object event) {
                eventReceived.set(event instanceof WriteCompleteEvent);
            }
        });
        channel.config().setAutoRead(false);
        channel.pipeline().fireUserEventTriggered(new WriteCompleteEvent());
        assertTrue(channel.config().isAutoRead());
        assertTrue(eventReceived.get());
    }
}
