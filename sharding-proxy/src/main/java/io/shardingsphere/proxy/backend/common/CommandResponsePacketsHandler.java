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

package io.shardingsphere.proxy.backend.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;

/**
 * SQL execute backend handler.
 *
 * @author wangkai
 * @author linjiaqi
 */
public abstract class CommandResponsePacketsHandler extends ChannelInboundHandlerAdapter {
    
    protected abstract void auth(ChannelHandlerContext context, MySQLPacketPayload mysqlPacketPayload);
    
    protected abstract void eofPacket(ChannelHandlerContext context, MySQLPacketPayload mysqlPacketPayload);
    
    protected abstract void okPacket(ChannelHandlerContext context, MySQLPacketPayload mysqlPacketPayload);
    
    protected abstract void errPacket(ChannelHandlerContext context, MySQLPacketPayload mysqlPacketPayload);
    
    protected abstract void commonPacket(MySQLPacketPayload mysqlPacketPayload);
}
