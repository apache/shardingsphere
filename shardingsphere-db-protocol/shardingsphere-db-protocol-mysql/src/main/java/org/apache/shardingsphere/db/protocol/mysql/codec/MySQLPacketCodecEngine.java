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

package org.apache.shardingsphere.db.protocol.mysql.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.db.protocol.CommonConstants;
import org.apache.shardingsphere.db.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.util.exception.external.sql.UnknownSQLException;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Database packet codec for MySQL.
 */
public final class MySQLPacketCodecEngine implements DatabasePacketCodecEngine<MySQLPacket> {
    
    private static final int MAX_PACKET_LENGTH = 0xFFFFFF;
    
    private static final int PAYLOAD_LENGTH = 3;
    
    private static final int SEQUENCE_LENGTH = 1;
    
    private final List<ByteBuf> pendingMessages = new LinkedList<>();
    
    @Override
    public boolean isValidHeader(final int readableBytes) {
        return readableBytes >= PAYLOAD_LENGTH + SEQUENCE_LENGTH;
    }
    
    @Override
    public void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {
        int payloadLength = in.markReaderIndex().readUnsignedMediumLE();
        int remainPayloadLength = SEQUENCE_LENGTH + payloadLength;
        if (in.readableBytes() < remainPayloadLength) {
            in.resetReaderIndex();
            return;
        }
        ByteBuf message = in.readRetainedSlice(SEQUENCE_LENGTH + payloadLength);
        if (MAX_PACKET_LENGTH == payloadLength) {
            pendingMessages.add(message);
        } else if (pendingMessages.isEmpty()) {
            out.add(message);
        } else {
            aggregateMessages(context, message, out);
        }
    }
    
    private void aggregateMessages(final ChannelHandlerContext context, final ByteBuf lastMessage, final List<Object> out) {
        CompositeByteBuf result = context.alloc().compositeBuffer(pendingMessages.size() + 1);
        Iterator<ByteBuf> pendingMessagesIterator = pendingMessages.iterator();
        result.addComponent(true, pendingMessagesIterator.next());
        while (pendingMessagesIterator.hasNext()) {
            result.addComponent(true, pendingMessagesIterator.next().skipBytes(1));
        }
        if (lastMessage.readableBytes() > 1) {
            result.addComponent(true, lastMessage.skipBytes(1));
        }
        out.add(result);
        pendingMessages.clear();
    }
    
    @Override
    public void encode(final ChannelHandlerContext context, final MySQLPacket message, final ByteBuf out) {
        MySQLPacketPayload payload = new MySQLPacketPayload(prepareMessageHeader(out).markWriterIndex(), context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get());
        try {
            message.write(payload);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            out.resetWriterIndex();
            SQLException unknownSQLException = new UnknownSQLException(ex).toSQLException();
            new MySQLErrPacket(1, unknownSQLException.getErrorCode(), unknownSQLException.getSQLState(), unknownSQLException.getMessage()).write(payload);
        } finally {
            updateMessageHeader(out, message.getSequenceId());
        }
    }
    
    private ByteBuf prepareMessageHeader(final ByteBuf out) {
        return out.writeInt(0);
    }
    
    private void updateMessageHeader(final ByteBuf byteBuf, final int sequenceId) {
        byteBuf.setMediumLE(0, byteBuf.readableBytes() - PAYLOAD_LENGTH - SEQUENCE_LENGTH);
        byteBuf.setByte(3, sequenceId);
    }
    
    @Override
    public MySQLPacketPayload createPacketPayload(final ByteBuf message, final Charset charset) {
        return new MySQLPacketPayload(message, charset);
    }
}
