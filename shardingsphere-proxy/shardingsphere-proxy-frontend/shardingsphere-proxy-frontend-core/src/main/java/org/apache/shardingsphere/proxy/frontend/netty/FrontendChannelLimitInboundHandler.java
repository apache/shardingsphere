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

import org.apache.shardingsphere.proxy.frontend.connection.ConnectionLimitContext;
import org.apache.shardingsphere.proxy.frontend.exception.FrontendConnectionLimitException;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FrontendChannelLimitInboundHandler extends ChannelDuplexHandler {

    private final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine;
    
    public FrontendChannelLimitInboundHandler(final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine) {
        this.databaseProtocolFrontendEngine = databaseProtocolFrontendEngine;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        if (ConnectionLimitContext.INSTANCE.connect()) {
            ctx.fireChannelActive();
        } else {
            log.info("Close channel {}, The server connections greater than {}", ctx.channel().remoteAddress(), ConnectionLimitContext.INSTANCE.getConnectionLimit());
            ctx.writeAndFlush(databaseProtocolFrontendEngine.getCommandExecuteEngine().getErrorPacket(new FrontendConnectionLimitException("The number of connections exceeds the limit")));
            ctx.close();
        }
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        ConnectionLimitContext.INSTANCE.disconnect();
    }
}
