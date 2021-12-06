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

package org.apache.shardingsphere.db.protocol.postgresql.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.db.protocol.CommonConstants;
import org.apache.shardingsphere.db.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLErrorCode;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Database packet codec for PostgreSQL.
 */
public final class PostgreSQLPacketCodecEngine implements DatabasePacketCodecEngine<PostgreSQLPacket> {
    
    private static final int MESSAGE_TYPE_LENGTH = 1;
    
    private static final int PAYLOAD_LENGTH = 4;
    
    @Override
    public boolean isValidHeader(final int readableBytes) {
        return readableBytes >= MESSAGE_TYPE_LENGTH + PAYLOAD_LENGTH;
    }
    
    @Override
    public void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {
        while (isValidHeader(in.readableBytes())) {
            int messageTypeLength = 0;
            if ('\0' == in.markReaderIndex().readByte()) {
                in.resetReaderIndex();
            } else {
                messageTypeLength = MESSAGE_TYPE_LENGTH;
            }
            int remainPayloadLength = in.readInt() - PAYLOAD_LENGTH;
            if (in.readableBytes() < remainPayloadLength) {
                in.resetReaderIndex();
                return;
            }
            in.resetReaderIndex();
            out.add(in.readRetainedSlice(messageTypeLength + PAYLOAD_LENGTH + remainPayloadLength));
        }
    }
    
    @Override
    public void encode(final ChannelHandlerContext context, final PostgreSQLPacket message, final ByteBuf out) {
        boolean isPostgreSQLIdentifierPacket = message instanceof PostgreSQLIdentifierPacket;
        if (isPostgreSQLIdentifierPacket) {
            prepareMessageHeader(out, ((PostgreSQLIdentifierPacket) message).getIdentifier().getValue());
        }
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(out, context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get());
        try {
            message.write(payload);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            payload.getByteBuf().resetWriterIndex();
            // TODO consider what severity to use
            PostgreSQLErrorResponsePacket errorResponsePacket = PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.ERROR, PostgreSQLErrorCode.SYSTEM_ERROR, ex.getMessage())
                    .build();
            isPostgreSQLIdentifierPacket = true;
            prepareMessageHeader(out, errorResponsePacket.getIdentifier().getValue());
            errorResponsePacket.write(payload);
        } finally {
            if (isPostgreSQLIdentifierPacket) {
                updateMessageLength(out);
            }
        }
    }
    
    private void prepareMessageHeader(final ByteBuf out, final char type) {
        out.writeByte(type);
        out.writeInt(0);
    }
    
    private void updateMessageLength(final ByteBuf out) {
        out.setInt(1, out.readableBytes() - MESSAGE_TYPE_LENGTH);
    }
    
    @Override
    public PostgreSQLPacketPayload createPacketPayload(final ByteBuf message, final Charset charset) {
        return new PostgreSQLPacketPayload(message, charset);
    }
}
