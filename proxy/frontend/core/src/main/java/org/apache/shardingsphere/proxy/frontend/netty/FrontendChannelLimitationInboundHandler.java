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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.exception.core.exception.connection.TooManyConnectionsException;
import org.apache.shardingsphere.proxy.frontend.connection.ConnectionLimitContext;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

/**
 * Frontend channel limitation inbound handler.
 */
@RequiredArgsConstructor
@Slf4j
public class FrontendChannelLimitationInboundHandler extends ChannelInboundHandlerAdapter {
    
    private final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine;
    
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        if (ConnectionLimitContext.getInstance().connectionAllowed()) {
            ctx.fireChannelActive();
            return;
        }
        log.debug("Closing channel {} due to the number of server connections has reached max connections {}", ctx.channel().remoteAddress(), ConnectionLimitContext.getInstance().getMaxConnections());
        // TODO This is not how actual databases does and should be refactored.
        ctx.writeAndFlush(databaseProtocolFrontendEngine.getCommandExecuteEngine().getErrorPacket(new TooManyConnectionsException()));
        ctx.close();
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        ctx.fireChannelInactive();
        ConnectionLimitContext.getInstance().connectionInactive();
    }
}
