/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend.netty.client.response;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * SQL executed response handler.
 *
 * @author wangkai
 * @author linjiaqi
 */
public abstract class ResponseHandler extends ChannelInboundHandlerAdapter {
    
    private boolean authorized;
    
    @Override
    public final void channelRead(final ChannelHandlerContext context, final Object message) {
        ByteBuf byteBuf = (ByteBuf) message;
        int header = getHeader(byteBuf);
        
        if (!authorized) {
            auth(context, byteBuf);
            authorized = true;
        } else {
            executeCommand(context, byteBuf, header);
        }
    }
    
    protected abstract int getHeader(ByteBuf byteBuf);
    
    protected abstract void auth(ChannelHandlerContext context, ByteBuf byteBuf);
    
    protected abstract void executeCommand(ChannelHandlerContext context, ByteBuf byteBuf, int header);
    
    @Override
    public final void channelInactive(final ChannelHandlerContext context) throws Exception {
        //TODO delete connection map
        super.channelInactive(context);
    }
}
