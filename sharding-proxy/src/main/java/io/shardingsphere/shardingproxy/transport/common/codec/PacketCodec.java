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

package io.shardingsphere.shardingproxy.transport.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Database packet codec.
 * 
 * @author zhangliang 
 */
@Slf4j
public abstract class PacketCodec<T extends DatabasePacket> extends ByteToMessageCodec<T> {
    
    @Override
    protected final void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {
        int readableBytes = in.readableBytes();
        if (!isValidHeader(readableBytes)) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Read from client {} : \n {}", context.channel().id().asShortText(), ByteBufUtil.prettyHexDump(in));
        }
        doDecode(context, in, out, readableBytes);
    }
    
    protected abstract boolean isValidHeader(int readableBytes);
    
    protected abstract void doDecode(ChannelHandlerContext context, ByteBuf in, List<Object> out, int readableBytes);
    
    @Override
    protected final void encode(final ChannelHandlerContext context, final T message, final ByteBuf out) {
        doEncode(context, message, out);
        if (log.isDebugEnabled()) {
            log.debug("Write to client {} : \n {}", context.channel().id().asShortText(), ByteBufUtil.prettyHexDump(out));
        }
    }
    
    protected abstract void doEncode(ChannelHandlerContext context, T message, ByteBuf out);
}
