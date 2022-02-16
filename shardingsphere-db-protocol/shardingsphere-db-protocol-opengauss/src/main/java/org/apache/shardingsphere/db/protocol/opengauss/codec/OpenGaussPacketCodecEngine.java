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

package org.apache.shardingsphere.db.protocol.opengauss.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.db.protocol.CommonConstants;
import org.apache.shardingsphere.db.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.db.protocol.opengauss.packet.command.OpenGaussCommandPacketType;
import org.apache.shardingsphere.db.protocol.opengauss.packet.command.generic.OpenGaussErrorResponsePacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLErrorCode;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

/**
 * Database packet codec for openGauss.
 */
public final class OpenGaussPacketCodecEngine implements DatabasePacketCodecEngine<PostgreSQLPacket> {
    
    private static final int SSL_REQUEST_PAYLOAD_LENGTH = 8;
    
    private static final int SSL_REQUEST_CODE = (1234 << 16) + 5679;
    
    private static final int MESSAGE_TYPE_LENGTH = 1;
    
    private static final int PAYLOAD_LENGTH = 4;
    
    private boolean startupPhase = true;
    
    private final List<ByteBuf> pendingMessages = new LinkedList<>();
    
    @Override
    public boolean isValidHeader(final int readableBytes) {
        return readableBytes >= (startupPhase ? 0 : MESSAGE_TYPE_LENGTH) + PAYLOAD_LENGTH;
    }
    
    @Override
    public void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {
        while (isValidHeader(in.readableBytes())) {
            if (startupPhase) {
                handleStartupPhase(in, out);
                return;
            }
            int payloadLength = in.getInt(in.readerIndex() + 1);
            if (in.readableBytes() < MESSAGE_TYPE_LENGTH + payloadLength) {
                return;
            }
            byte type = in.getByte(in.readerIndex());
            CommandPacketType commandPacketType = OpenGaussCommandPacketType.valueOf(type);
            if (requireAggregation(commandPacketType)) {
                pendingMessages.add(in.readRetainedSlice(MESSAGE_TYPE_LENGTH + payloadLength));
            } else if (!pendingMessages.isEmpty()) {
                handlePendingMessages(context, in, out, payloadLength);
            } else {
                out.add(in.readRetainedSlice(MESSAGE_TYPE_LENGTH + payloadLength));
            }
        }
    }
    
    private void handleStartupPhase(final ByteBuf in, final List<Object> out) {
        int readerIndex = in.readerIndex();
        if (in.readableBytes() == SSL_REQUEST_PAYLOAD_LENGTH && SSL_REQUEST_PAYLOAD_LENGTH == in.getInt(readerIndex) && SSL_REQUEST_CODE == in.getInt(readerIndex + 4)) {
            out.add(in.readRetainedSlice(SSL_REQUEST_PAYLOAD_LENGTH));
            return;
        }
        if (in.readableBytes() == in.getInt(readerIndex)) {
            out.add(in.readRetainedSlice(in.readableBytes()));
            startupPhase = false;
        }
    }
    
    private boolean requireAggregation(final CommandPacketType commandPacketType) {
        return OpenGaussCommandPacketType.isExtendedProtocolPacketType(commandPacketType)
                && PostgreSQLCommandPacketType.SYNC_COMMAND != commandPacketType && PostgreSQLCommandPacketType.FLUSH_COMMAND != commandPacketType;
    }
    
    private void handlePendingMessages(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out, final int payloadLength) {
        CompositeByteBuf result = context.alloc().compositeBuffer(pendingMessages.size() + 1);
        result.addComponents(true, pendingMessages).addComponent(true, in.readRetainedSlice(MESSAGE_TYPE_LENGTH + payloadLength));
        out.add(result);
        pendingMessages.clear();
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
            OpenGaussErrorResponsePacket errorResponsePacket = new OpenGaussErrorResponsePacket(PostgreSQLMessageSeverityLevel.ERROR, PostgreSQLErrorCode.SYSTEM_ERROR.getErrorCode(), ex.getMessage());
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
