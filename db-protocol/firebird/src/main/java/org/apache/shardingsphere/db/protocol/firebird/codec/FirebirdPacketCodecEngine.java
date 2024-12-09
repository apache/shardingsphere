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

package org.apache.shardingsphere.db.protocol.firebird.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.db.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.db.protocol.constant.CommonConstants;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Database packet codec for Firebird.
 */
public final class FirebirdPacketCodecEngine implements DatabasePacketCodecEngine {

    private static final int ALLOCATE_STATEMENT_REQUEST_PAYLOAD_LENGTH = 4;

    private static final int MESSAGE_TYPE_LENGTH = 4;
    
    @Override
    public boolean isValidHeader(final int readableBytes) {
        return readableBytes >= MESSAGE_TYPE_LENGTH;
    }
    
    @Override
    public void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {
        if (isValidHeader(in.readableBytes())) {
            int type = in.getInt(in.readerIndex());
            FirebirdCommandPacketType commandPacketType = FirebirdCommandPacketType.valueOf(type);
            if (commandPacketType == FirebirdCommandPacketType.OP_ALLOCATE_STATEMENT) {
                handleAllocateStatement(in, out);
                return;
            }
            out.add(in.readRetainedSlice(in.readableBytes()));
        }
    }

    private void handleAllocateStatement(final ByteBuf in, final List<Object> out) {
        out.add(in.readRetainedSlice(MESSAGE_TYPE_LENGTH + ALLOCATE_STATEMENT_REQUEST_PAYLOAD_LENGTH));
        if (in.readableBytes() > MESSAGE_TYPE_LENGTH) {
            out.add(in.readRetainedSlice(in.readableBytes()));
        }
    }
    
    @Override
    public void encode(final ChannelHandlerContext context, final DatabasePacket message, final ByteBuf out) {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(out, context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get());
        try {
            message.write(payload);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            payload.getByteBuf().resetWriterIndex();
            // TODO send error packet
        }
    }

    @Override
    public FirebirdPacketPayload createPacketPayload(final ByteBuf message, final Charset charset) {
        return new FirebirdPacketPayload(message, charset);
    }
}
