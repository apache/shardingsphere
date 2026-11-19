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
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdErrorPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchBlobStream;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchBlobStreamCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchColumnDescriptor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchCreateCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchMessageCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry.BatchCreatePhase;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchSetBpbCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdParseBatchBlr;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdAllocateStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFreeStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Database packet codec for Firebird.
 */
@Slf4j
public final class FirebirdPacketCodecEngine implements DatabasePacketCodecEngine {
    
    private static final int MESSAGE_TYPE_LENGTH = 4;
    
    private final List<ByteBuf> pendingMessages = new LinkedList<>();
    
    private final Map<Integer, List<FirebirdBatchColumnDescriptor>> deferredBatchFormats = new HashMap<>(1, 1F);
    
    private final Map<Integer, FirebirdBatchBlobStream> batchBlobStreams = new HashMap<>(1, 1F);
    
    private final BatchCreateHandler batchCreateHandler = new BatchCreateHandler();
    
    private FirebirdCommandPacketType pendingPacketType;
    
    private boolean batchCreatePending;
    
    @HighFrequencyInvocation
    @Override
    public boolean isValidHeader(final int readableBytes) {
        return readableBytes >= MESSAGE_TYPE_LENGTH || readableBytes > 0 && (batchCreatePending || !pendingMessages.isEmpty());
    }
    
    @HighFrequencyInvocation
    @Override
    public void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {
        try {
            if (batchCreatePending) {
                pendingMessages.add(in.readRetainedSlice(in.readableBytes()));
                return;
            }
            if (pendingMessages.isEmpty()) {
                pendingPacketType = FirebirdCommandPacketType.valueOf(in.getInt(in.readerIndex()));
                if (FirebirdCommandPacketType.ALLOCATE_STATEMENT == pendingPacketType) {
                    handleMultiPacket(context, in, out, FirebirdAllocateStatementPacket.getLength());
                    return;
                }
                if (FirebirdCommandPacketType.FREE_STATEMENT == pendingPacketType) {
                    handleMultiPacket(context, in, out, FirebirdFreeStatementPacket.getLength());
                    return;
                }
            }
            addToBuffer(context, in, out);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            log.debug("Firebird decode failed: connectionId={}, protocolVersion={}, pendingPacketType={}, pendingMessages={}, readerIndex={}, readableBytes={}",
                    context.channel().attr(FirebirdConstant.CURRENT_CONNECTION).get(), context.channel().attr(FirebirdConstant.CONNECTION_PROTOCOL_VERSION).get(), pendingPacketType,
                    pendingMessages.size(), in.readerIndex(), in.readableBytes(), ex);
            out.forEach(ReferenceCountUtil::release);
            out.clear();
            resetState();
            in.skipBytes(in.readableBytes());
            if (ex instanceof InvalidBlobStreamException) {
                context.channel().close();
            } else {
                context.channel().writeAndFlush(FirebirdErrorPacketFactory.newInstance(ex)).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
    
    private void handleMultiPacket(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out, final int firstPacketLength) {
        out.add(in.readRetainedSlice(firstPacketLength));
        if (in.readableBytes() > MESSAGE_TYPE_LENGTH) {
            decode(context, in, out);
        }
    }
    
    private void addToBuffer(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {
        ByteBuf buffer = mergePendingMessages(context, in);
        try {
            processPackets(context, buffer, out);
        } finally {
            if (buffer != in) {
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
            FirebirdCommandPacketType commandType = null == pendingPacketType ? FirebirdCommandPacketType.valueOf(buffer.getInt(buffer.readerIndex())) : pendingPacketType;
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
            trackBatchBlobStream(buffer, commandType, packetLength, charset);
            pendingPacketType = null;
            out.add(buffer.readRetainedSlice(packetLength));
            if (FirebirdCommandPacketType.BATCH_CREATE == commandType) {
                batchCreatePending = true;
                if (buffer.isReadable()) {
                    pendingMessages.add(buffer.readRetainedSlice(buffer.readableBytes()));
                }
                return;
            }
        }
        deferredBatchFormats.clear();
    }
    
    private int findPacketLength(final ChannelHandlerContext context, final ByteBuf buffer, final FirebirdCommandPacketType commandType, final Charset charset) {
        int readableBytes = buffer.readableBytes();
        ByteBuf slice = buffer.retainedSlice();
        try {
            FirebirdPacketPayload payload = new FirebirdPacketPayload(slice, charset);
            int expectedLength = getExpectedLength(context, buffer, commandType, payload);
            if (expectedLength < 0) {
                return -1;
            }
            if (0 == expectedLength) {
                return readableBytes;
            }
            return readableBytes >= expectedLength ? expectedLength : -1;
        } catch (final IndexOutOfBoundsException ex) {
            return -1;
        } finally {
            slice.release();
        }
    }
    
    private int getExpectedLength(final ChannelHandlerContext context, final ByteBuf buffer, final FirebirdCommandPacketType commandType, final FirebirdPacketPayload payload) {
        if (FirebirdCommandPacketType.BATCH_BLOB_STREAM == commandType) {
            return getBatchBlobStreamLength(buffer, payload);
        }
        List<FirebirdBatchColumnDescriptor> columnDescriptors = getDeferredBatchFormat(buffer, commandType);
        return null == columnDescriptors
                ? FirebirdCommandPacketFactory.getExpectedLength(commandType, payload,
                        context.channel().attr(FirebirdConstant.CONNECTION_PROTOCOL_VERSION).get(), context.channel().attr(FirebirdConstant.CURRENT_CONNECTION).get())
                : FirebirdBatchMessageCommandPacket.getLength(payload, columnDescriptors);
    }
    
    private int getBatchBlobStreamLength(final ByteBuf buffer, final FirebirdPacketPayload payload) {
        int statementHandle = buffer.getInt(buffer.readerIndex() + MESSAGE_TYPE_LENGTH);
        FirebirdBatchBlobStream previous = batchBlobStreams.get(statementHandle);
        FirebirdBatchBlobStream blobStream = null == previous ? new FirebirdBatchBlobStream() : previous.copy();
        int result;
        try {
            result = FirebirdBatchBlobStreamCommandPacket.getLength(payload, blobStream);
        } catch (final SQLDialectException ex) {
            throw new InvalidBlobStreamException(ex);
        }
        if (result >= 0 && null != previous) {
            batchBlobStreams.put(statementHandle, blobStream);
        }
        return result;
    }
    
    private List<FirebirdBatchColumnDescriptor> getDeferredBatchFormat(final ByteBuf buffer, final FirebirdCommandPacketType commandType) {
        return FirebirdCommandPacketType.BATCH_MSG == commandType && buffer.readableBytes() >= 8
                ? deferredBatchFormats.get(buffer.getInt(buffer.readerIndex() + MESSAGE_TYPE_LENGTH))
                : null;
    }
    
    private void trackBatchBlobStream(final ByteBuf buffer, final FirebirdCommandPacketType commandType, final int packetLength, final Charset charset) {
        if (FirebirdCommandPacketType.BATCH_SET_BPB == commandType) {
            FirebirdBatchSetBpbCommandPacket packet = new FirebirdBatchSetBpbCommandPacket(new FirebirdPacketPayload(buffer.slice(buffer.readerIndex(), packetLength), charset));
            FirebirdBatchBlobStream blobStream = batchBlobStreams.get(packet.getStatementHandle());
            if (null == blobStream) {
                blobStream = new FirebirdBatchBlobStream();
            }
            blobStream.setDefaultBpb(packet.getBpb());
        }
    }
    
    private void resetState() {
        pendingMessages.forEach(ByteBuf::release);
        pendingMessages.clear();
        pendingPacketType = null;
        batchCreatePending = false;
    }
    
    @Override
    @HighFrequencyInvocation
    public void encode(final ChannelHandlerContext context, final DatabasePacket message, final ByteBuf out) {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(out, context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get());
        Integer connectionId = context.channel().attr(FirebirdConstant.CURRENT_CONNECTION).get();
        if (null != connectionId) {
            payload.setConnectionId(connectionId);
        }
        try {
            message.write(payload);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            log.debug("Firebird encode failed: connectionId={}, protocolVersion={}, packetClass={}, writerIndex={}", connectionId,
                    context.channel().attr(FirebirdConstant.CONNECTION_PROTOCOL_VERSION).get(), message.getClass().getName(), out.writerIndex(), ex);
            out.resetWriterIndex();
            FirebirdErrorPacketFactory.newInstance(ex).write(payload);
        }
    }
    
    @Override
    public FirebirdPacketPayload createPacketPayload(final ByteBuf message, final Charset charset) {
        return new FirebirdPacketPayload(message, charset);
    }
    
    /**
     * Create the lifecycle handler that coordinates batch framing with dispatched CREATE responses.
     *
     * @return handler to install before decoding, after inbound flow control and before command dispatch
     */
    public ChannelHandler createBatchCreateHandler() {
        return batchCreateHandler;
    }
    
    private final class BatchCreateHandler extends ChannelDuplexHandler {
        
        private int statementHandle;
        
        private boolean droppingStatement;
        
        private FirebirdCommandPacketType pendingCommandType;
        
        @HighFrequencyInvocation
        @Override
        public void channelRead(final ChannelHandlerContext context, final Object message) {
            ByteBuf packet = (ByteBuf) message;
            int commandType = packet.getInt(packet.readerIndex());
            if (FirebirdCommandPacketType.BATCH_CREATE.getValue() == commandType) {
                statementHandle = packet.getInt(packet.readerIndex() + MESSAGE_TYPE_LENGTH);
                boolean batchAlreadyOpened = null != FirebirdBatchRegistry.getInstance().getBatchStatement(context.channel().attr(FirebirdConstant.CURRENT_CONNECTION).get(), statementHandle);
                if (!batchAlreadyOpened && !pendingMessages.isEmpty()) {
                    try {
                        rememberBatchFormat(packet, context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get());
                        // CHECKSTYLE:OFF
                    } catch (final RuntimeException ex) {
                        // CHECKSTYLE:ON
                        packet.release();
                        resetState();
                        context.writeAndFlush(FirebirdErrorPacketFactory.newInstance(ex)).addListener(ChannelFutureListener.CLOSE);
                        return;
                    }
                }
                pendingCommandType = FirebirdCommandPacketType.BATCH_CREATE;
            } else if (FirebirdCommandPacketType.FREE_STATEMENT.getValue() == commandType) {
                statementHandle = packet.getInt(packet.readerIndex() + MESSAGE_TYPE_LENGTH);
                droppingStatement = FirebirdFreeStatementPacket.DROP == packet.getInt(packet.readerIndex() + 2 * MESSAGE_TYPE_LENGTH);
                pendingCommandType = FirebirdCommandPacketType.FREE_STATEMENT;
            } else if (FirebirdCommandPacketType.ALLOCATE_STATEMENT.getValue() == commandType) {
                pendingCommandType = FirebirdCommandPacketType.ALLOCATE_STATEMENT;
            }
            context.fireChannelRead(message);
        }
        
        private void rememberBatchFormat(final ByteBuf buffer, final Charset charset) {
            FirebirdBatchCreateCommandPacket packet = new FirebirdBatchCreateCommandPacket(new FirebirdPacketPayload(buffer.duplicate(), charset));
            ByteBuf batchBlr = packet.getBatchBlr();
            deferredBatchFormats.put(packet.getStatementHandle(), FirebirdParseBatchBlr.parse(batchBlr, batchBlr.readableBytes()).getFields());
        }
        
        @HighFrequencyInvocation
        @Override
        public void write(final ChannelHandlerContext context, final Object message, final ChannelPromise promise) {
            context.write(message, promise);
            if (null == pendingCommandType || !(message instanceof FirebirdGenericResponsePacket)) {
                return;
            }
            FirebirdCommandPacketType commandType = pendingCommandType;
            pendingCommandType = null;
            if (!context.channel().isActive()) {
                return;
            }
            FirebirdGenericResponsePacket response = (FirebirdGenericResponsePacket) message;
            if (FirebirdCommandPacketType.ALLOCATE_STATEMENT == commandType && null == response.getStatusVector()) {
                batchBlobStreams.put(response.getHandle(), new FirebirdBatchBlobStream());
            } else if (FirebirdCommandPacketType.FREE_STATEMENT == commandType && droppingStatement && null == response.getStatusVector()) {
                batchBlobStreams.remove(statementHandle);
            } else if (FirebirdCommandPacketType.BATCH_CREATE == commandType) {
                BatchCreatePhase phase = FirebirdBatchRegistry.getInstance().consumeBatchCreatePhase(context.channel().attr(FirebirdConstant.CURRENT_CONNECTION).get());
                if (BatchCreatePhase.API_INVOKED == phase) {
                    batchBlobStreams.put(statementHandle, new FirebirdBatchBlobStream());
                } else if (BatchCreatePhase.DEFAULTS_RESET == phase) {
                    resetBatchStreamDefaults(statementHandle);
                }
                context.executor().execute(() -> resume(context));
            }
        }
        
        private void resetBatchStreamDefaults(final int statementId) {
            FirebirdBatchBlobStream stream = batchBlobStreams.get(statementId);
            if (null != stream) {
                stream.resetDefaultBpb();
            }
        }
        
        @HighFrequencyInvocation
        private void resume(final ChannelHandlerContext context) {
            if (!batchCreatePending || !context.channel().isActive()) {
                return;
            }
            batchCreatePending = false;
            if (pendingMessages.isEmpty()) {
                deferredBatchFormats.clear();
                return;
            }
            ByteBuf[] messages = pendingMessages.toArray(new ByteBuf[pendingMessages.size()]);
            pendingMessages.clear();
            for (ByteBuf each : messages) {
                if (context.channel().isActive()) {
                    context.pipeline().fireChannelRead(each);
                } else {
                    each.release();
                }
            }
            context.pipeline().fireChannelReadComplete();
        }
        
        @Override
        public void channelInactive(final ChannelHandlerContext context) {
            clear();
            context.fireChannelInactive();
        }
        
        private void clear() {
            resetState();
            deferredBatchFormats.clear();
            batchBlobStreams.clear();
            pendingCommandType = null;
        }
        
        @Override
        public void handlerRemoved(final ChannelHandlerContext context) {
            clear();
        }
    }
    
    /**
     * Invalid stream portion of {@code op_batch_blob_stream}: Firebird fails to decode the packet in {@code xdr_blob_stream} and closes the connection without response.
     */
    private static final class InvalidBlobStreamException extends RuntimeException {
        
        private static final long serialVersionUID = 3197564018632745511L;
        
        InvalidBlobStreamException(final SQLDialectException cause) {
            super(cause);
        }
    }
}
