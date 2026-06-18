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

package org.apache.shardingsphere.database.protocol.mysql.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.database.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.exception.generic.UnknownSQLException;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Database packet codec for MySQL.
 */
public final class MySQLPacketCodecEngine implements DatabasePacketCodecEngine {
    
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
        ByteBuf message = in.readRetainedSlice(remainPayloadLength);
        if (MAX_PACKET_LENGTH == payloadLength) {
            pendingMessages.add(message.skipBytes(SEQUENCE_LENGTH));
        } else if (pendingMessages.isEmpty()) {
            out.add(message);
        } else {
            aggregateMessages(context, message, out);
        }
    }
    
    private void aggregateMessages(final ChannelHandlerContext context, final ByteBuf lastMessage, final List<Object> out) {
        CompositeByteBuf result = context.alloc().compositeBuffer(SEQUENCE_LENGTH + pendingMessages.size() + 1);
        result.addComponent(true, lastMessage.readSlice(SEQUENCE_LENGTH));
        Iterator<ByteBuf> pendingMessagesIterator = pendingMessages.iterator();
        do {
            result.addComponent(true, pendingMessagesIterator.next());
        } while (pendingMessagesIterator.hasNext());
        if (lastMessage.readableBytes() > 0) {
            result.addComponent(true, lastMessage);
        }
        out.add(result);
        pendingMessages.clear();
    }
    
    @Override
    public void encode(final ChannelHandlerContext context, final DatabasePacket message, final ByteBuf out) {
        MySQLPacketPayload payload = new MySQLPacketPayload(prepareMessageHeader(out).markWriterIndex(), context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get());
        try {
            message.write(payload);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            out.resetWriterIndex();
            new MySQLErrPacket(new UnknownSQLException(ex).toSQLException()).write(payload);
        } finally {
            if (out.readableBytes() - PAYLOAD_LENGTH - SEQUENCE_LENGTH < MAX_PACKET_LENGTH) {
                updateMessageHeader(out, context.channel().attr(MySQLConstants.SEQUENCE_ID_ATTRIBUTE_KEY).get().getAndIncrement());
            } else {
                writeMultiPackets(context, out);
            }
        }
    }
    
    private ByteBuf prepareMessageHeader(final ByteBuf out) {
        return out.writeInt(0);
    }
    
    private void updateMessageHeader(final ByteBuf byteBuf, final int sequenceId) {
        byteBuf.setMediumLE(0, byteBuf.readableBytes() - PAYLOAD_LENGTH - SEQUENCE_LENGTH);
        byteBuf.setByte(3, sequenceId);
    }
    
    private void writeMultiPackets(final ChannelHandlerContext context, final ByteBuf byteBuf) {
        int packetCount = byteBuf.skipBytes(PAYLOAD_LENGTH + SEQUENCE_LENGTH).readableBytes() / MAX_PACKET_LENGTH + 1;
        CompositeByteBuf result = context.alloc().compositeBuffer(packetCount * 2);
        AtomicInteger sequenceId = context.channel().attr(MySQLConstants.SEQUENCE_ID_ATTRIBUTE_KEY).get();
        for (int i = 0; i < packetCount; i++) {
            ByteBuf header = context.alloc().ioBuffer(4, 4);
            int packetLength = Math.min(byteBuf.readableBytes(), MAX_PACKET_LENGTH);
            header.writeMediumLE(packetLength);
            header.writeByte(sequenceId.getAndIncrement());
            result.addComponent(true, header);
            if (packetLength > 0) {
                result.addComponent(true, byteBuf.readRetainedSlice(packetLength));
            }
        }
        context.write(result);
    }
    
    @Override
    public MySQLPacketPayload createPacketPayload(final ByteBuf message, final Charset charset) {
        return new MySQLPacketPayload(message, charset);
    }
}
