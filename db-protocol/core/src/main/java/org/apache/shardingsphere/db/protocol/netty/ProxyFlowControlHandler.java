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

package org.apache.shardingsphere.db.protocol.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.flow.FlowControlHandler;
import org.apache.shardingsphere.db.protocol.event.WriteCompleteEvent;

/**
 * Flow control handler for ShardingSphere-Proxy.
 */
public final class ProxyFlowControlHandler extends FlowControlHandler {
    
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object event) {
        if (event instanceof WriteCompleteEvent) {
            ctx.channel().config().setAutoRead(true);
        }
        ctx.fireUserEventTriggered(event);
    }
}
