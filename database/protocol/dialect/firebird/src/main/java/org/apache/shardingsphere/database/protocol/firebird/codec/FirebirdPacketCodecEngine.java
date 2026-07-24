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

package org.apache.shardingsphere.database.protocol.firebird.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.database.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdErrorPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchColumnDescriptor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchCreateCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchSendMessageCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdParseBatchBlr;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Database packet codec for Firebird.
 */
public final class FirebirdPacketCodecEngine implements DatabasePacketCodecEngine {
    
    private static final int MESSAGE_TYPE_LENGTH = 4;
    
    private static final int ALLOCATE_STATEMENT_REQUEST_PAYLOAD_LENGTH = MESSAGE_TYPE_LENGTH + 4;
    
    private static final int FREE_STATEMENT_REQUEST_PAYLOAD_LENGTH = MESSAGE_TYPE_LENGTH + 8;
    
    private final List<ByteBuf> pendingMessages = new LinkedList<>();
    
    private final Map<Integer, List<FirebirdBatchColumnDescriptor>> deferredBatchFormats = new HashMap<>(1, 1F);
    
    private FirebirdCommandPacketType pendingPacketType;
    
    @Override
    public boolean isValidHeader(final int readableBytes) {
        return readableBytes >= MESSAGE_TYPE_LENGTH;
    }
    
    @Override
    public void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {
        try {
            if (pendingMessages.isEmpty()) {
                int type = in.getInt(in.readerIndex());
                pendingPacketType = FirebirdCommandPacketType.valueOf(type);
                if (pendingPacketType == FirebirdCommandPacketType.ALLOCATE_STATEMENT) {
                    handleMultiPacket(context, in, out, ALLOCATE_STATEMENT_REQUEST_PAYLOAD_LENGTH);
                    return;
                } else if (pendingPacketType == FirebirdCommandPacketType.FREE_STATEMENT) {
                    handleMultiPacket(context, in, out, FREE_STATEMENT_REQUEST_PAYLOAD_LENGTH);
                    return;
                }
            }
            addToBuffer(context, in, out);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            for (Object each : out) {
                if (each instanceof ByteBuf) {
                    ((ByteBuf) each).release();
                }
            }
            out.clear();
            resetState();
            in.skipBytes(in.readableBytes());
            context.channel().writeAndFlush(FirebirdErrorPacketFactory.newInstance(ex)).addListener(ChannelFutureListener.CLOSE);
        }
    }
    
    private void resetState() {
        for (ByteBuf each : pendingMessages) {
            each.release();
        }
        pendingMessages.clear();
        pendingPacketType = null;
    }
    
    private void handleMultiPacket(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out, final int firstPacketLength) {
        out.add(in.readRetainedSlice(firstPacketLength));
        if (in.readableBytes() > MESSAGE_TYPE_LENGTH) {
            decode(context, in, out);
        }
    }
    
    private void addToBuffer(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {
        ByteBuf buffer = mergePendingMessages(context, in);
        boolean shouldRelease = buffer != in;
        try {
            processPackets(context, buffer, out);
        } finally {
            if (shouldRelease) {
                buffer.release();
            }
        }
    }
    
    private ByteBuf mergePendingMessages(final ChannelHandlerContext context, final ByteBuf in) {
        if (pendingMessages.isEmpty()) {
            return in;
        }
        CompositeByteBuf result = context.alloc().compositeBuffer(pendingMessages.size() + 1);
        result.addComponents(true, pendingMessages);
        pendingMessages.clear();
        result.addComponent(true, in.readRetainedSlice(in.readableBytes()));
        return result;
    }
    
    private void processPackets(final ChannelHandlerContext context, final ByteBuf buffer, final List<Object> out) {
        Charset charset = context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get();
        while (buffer.isReadable()) {
            if (!isValidHeader(buffer.readableBytes())) {
                pendingMessages.add(buffer.readRetainedSlice(buffer.readableBytes()));
                return;
            }
            int readerIndex = buffer.readerIndex();
            FirebirdCommandPacketType commandType = (pendingPacketType != null) ? pendingPacketType : FirebirdCommandPacketType.valueOf(buffer.getInt(readerIndex));
            if (FirebirdCommandPacketType.VOID == commandType) {
                buffer.skipBytes(MESSAGE_TYPE_LENGTH);
                continue;
            }
            int packetLength = findPacketLength(context, buffer, commandType, charset);
            if (packetLength < 0) {
                pendingPacketType = commandType;
                pendingMessages.add(buffer.readRetainedSlice(buffer.readableBytes()));
                if (FirebirdCommandPacketType.BATCH_MSG != commandType) {
                    deferredBatchFormats.clear();
                }
                return;
            }
            if (FirebirdCommandPacketType.BATCH_CREATE == commandType && buffer.readableBytes() > packetLength) {
                rememberBatchFormat(buffer, packetLength, charset);
            }
            pendingPacketType = null;
            out.add(buffer.readRetainedSlice(packetLength));
        }
        deferredBatchFormats.clear();
    }
    
    private int findPacketLength(final ChannelHandlerContext context, final ByteBuf buffer, final FirebirdCommandPacketType commandType, final Charset charset) {
        int readerIndex = buffer.readerIndex();
        int readableBytes = buffer.readableBytes();
        ByteBuf slice = buffer.retainedSlice(readerIndex, readableBytes);
        try {
            FirebirdPacketPayload payload = new FirebirdPacketPayload(slice, charset);
            List<FirebirdBatchColumnDescriptor> columnDescriptors = getDeferredBatchFormat(buffer, commandType);
            int expectedLength = null == columnDescriptors
                    ? FirebirdCommandPacketFactory.getExpectedLength(commandType, payload,
                            context.channel().attr(FirebirdConstant.CONNECTION_PROTOCOL_VERSION).get(), context.channel().attr(FirebirdConstant.CURRENT_CONNECTION).get())
                    : FirebirdBatchSendMessageCommandPacket.getLength(payload, columnDescriptors);
            if (expectedLength < 0) {
                return -1;
            }
            return 0 == expectedLength ? readableBytes : readableBytes >= expectedLength ? expectedLength : -1;
        } catch (final IndexOutOfBoundsException ex) {
            return -1;
        } finally {
            slice.release();
        }
    }
    
    private List<FirebirdBatchColumnDescriptor> getDeferredBatchFormat(final ByteBuf buffer, final FirebirdCommandPacketType commandType) {
        return FirebirdCommandPacketType.BATCH_MSG == commandType && buffer.readableBytes() >= 8
                ? deferredBatchFormats.get(buffer.getInt(buffer.readerIndex() + MESSAGE_TYPE_LENGTH))
                : null;
    }
    
    private void rememberBatchFormat(final ByteBuf buffer, final int packetLength, final Charset charset) {
        FirebirdBatchCreateCommandPacket packet = new FirebirdBatchCreateCommandPacket(
                new FirebirdPacketPayload(buffer.slice(buffer.readerIndex(), packetLength), charset));
        ByteBuf batchBlr = packet.getBatchBlr();
        deferredBatchFormats.put(packet.getStatementHandle(), FirebirdParseBatchBlr.parseForFraming(batchBlr, batchBlr.readableBytes()).getFields());
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
            FirebirdErrorPacketFactory.newInstance(ex).write(payload);
        }
    }
    
    @Override
    public FirebirdPacketPayload createPacketPayload(final ByteBuf message, final Charset charset) {
        return new FirebirdPacketPayload(message, charset);
    }
}
