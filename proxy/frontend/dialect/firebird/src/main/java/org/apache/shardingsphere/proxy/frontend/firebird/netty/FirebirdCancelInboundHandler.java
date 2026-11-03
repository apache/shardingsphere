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

package org.apache.shardingsphere.proxy.frontend.firebird.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.shardingsphere.database.protocol.firebird.codec.FirebirdPacketCodecEngine;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.proxy.frontend.event.WriteCompleteEvent;
import org.apache.shardingsphere.proxy.frontend.firebird.command.admin.FirebirdCancellationRegistry;
import org.firebirdsql.gds.ISCConstants;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Reads cancellation packets while a Firebird command is executing.
 */
public final class FirebirdCancelInboundHandler extends ChannelInboundHandlerAdapter {
    
    private final Queue<ByteBuf> deferredInput = new ArrayDeque<>();
    
    private final FirebirdPacketCodecEngine codecEngine;
    
    private ChannelHandlerContext rawContext;
    
    private boolean executing;
    
    private boolean closeAfterResponse;
    
    private int queuedCommands;
    
    private FragmentKind fragmentKind = FragmentKind.NONE;
    
    private int fragmentBytes;
    
    private boolean discardAfterFragment;
    
    private boolean replayingInput;
    
    @HighFrequencyInvocation
    public FirebirdCancelInboundHandler(final FirebirdPacketCodecEngine codecEngine) {
        this.codecEngine = codecEngine;
    }
    
    /**
     * Create a raw input observer to install before packet framing.
     *
     * @return input observer for this channel
     */
    @HighFrequencyInvocation
    public ChannelInboundHandlerAdapter newRawHandler() {
        return new ChannelInboundHandlerAdapter() {
            
            @Override
            public void handlerAdded(final ChannelHandlerContext context) {
                rawContext = context;
            }
            
            @Override
            @HighFrequencyInvocation
            public void channelRead(final ChannelHandlerContext context, final Object message) {
                if (message instanceof ByteBuf && !observeRawInput(context, (ByteBuf) message)) {
                    return;
                }
                context.fireChannelRead(message);
            }
            
            @Override
            public void channelInactive(final ChannelHandlerContext context) {
                releaseDeferredInput();
                context.fireChannelInactive();
            }
            
            @Override
            public void handlerRemoved(final ChannelHandlerContext context) {
                releaseDeferredInput();
                rawContext = null;
            }
        };
    }
    
    @HighFrequencyInvocation
    private boolean observeRawInput(final ChannelHandlerContext context, final ByteBuf input) {
        if (discardAfterFragment) {
            ReferenceCountUtil.release(input);
            return false;
        }
        if (!deferredInput.isEmpty()) {
            deferredInput.add(input);
            if (!executing) {
                replayDeferredInput();
            }
            return false;
        }
        if (FragmentKind.CANCEL_BODY == fragmentKind) {
            return observeCancelBody(context, input);
        }
        if (!executing || !input.isReadable()) {
            return true;
        }
        return observeActiveInput(context, input);
    }
    
    private boolean observeCancelBody(final ChannelHandlerContext context, final ByteBuf input) {
        if (input.readableBytes() < 8 - fragmentBytes) {
            fragmentBytes += input.readableBytes();
            ReferenceCountUtil.release(input);
            return false;
        }
        discardAfterFragment = true;
        ReferenceCountUtil.release(input);
        if (!executing) {
            context.close();
        } else {
            closeAfterResponse = true;
        }
        return false;
    }
    
    private boolean observeActiveInput(final ChannelHandlerContext context, final ByteBuf input) {
        if (codecEngine.hasIncompletePacket() || 0 != queuedCommands || FragmentKind.NONE != fragmentKind) {
            deferredInput.add(input);
            return false;
        }
        if (input.readableBytes() < 4) {
            fragmentKind = FragmentKind.HEADER;
        } else if (FirebirdCommandPacketType.CANCEL.getValue() == input.getInt(input.readerIndex())) {
            return observeCancelInput(context, input);
        }
        if (FragmentKind.NONE == fragmentKind) {
            fragmentKind = FragmentKind.OTHER;
        }
        deferredInput.add(input);
        return false;
    }
    
    private boolean observeCancelInput(final ChannelHandlerContext context, final ByteBuf input) {
        if (input.readableBytes() < 8) {
            fragmentKind = FragmentKind.CANCEL_BODY;
            fragmentBytes = input.readableBytes();
            ReferenceCountUtil.release(input);
            return false;
        }
        fragmentKind = FragmentKind.COMPLETE_CANCEL;
        if (8 == input.readableBytes()) {
            return true;
        }
        observeCompleteCancel(context, input);
        return false;
    }
    
    private void observeCompleteCancel(final ChannelHandlerContext context, final ByteBuf input) {
        try {
            while (input.readableBytes() >= 8 && FirebirdCommandPacketType.CANCEL.getValue() == input.getInt(input.readerIndex())) {
                fragmentKind = FragmentKind.COMPLETE_CANCEL;
                context.fireChannelRead(input.readRetainedSlice(8));
                if (closeAfterResponse) {
                    break;
                }
            }
        } finally {
            if (input.isReadable() && context.channel().isActive()) {
                deferredInput.add(input);
            } else {
                ReferenceCountUtil.release(input);
            }
        }
    }
    
    @Override
    @HighFrequencyInvocation
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        Integer connectionId = context.channel().attr(FirebirdConstant.CURRENT_CONNECTION).get();
        if (null == connectionId || !(message instanceof ByteBuf)) {
            context.fireChannelRead(message);
            return;
        }
        ByteBuf buffer = (ByteBuf) message;
        if (discardAfterFragment) {
            ReferenceCountUtil.release(message);
            return;
        }
        FragmentKind observed = fragmentKind;
        fragmentKind = FragmentKind.NONE;
        if (isImmediateCancel(buffer, observed)) {
            handleImmediateCancel(context, buffer, connectionId);
            return;
        }
        dispatchCommand(context, message, connectionId);
    }
    
    private boolean isImmediateCancel(final ByteBuf buffer, final FragmentKind observed) {
        if (replayingInput || FragmentKind.HEADER == observed || FragmentKind.OTHER == observed) {
            return false;
        }
        if (executing && 0 != queuedCommands) {
            return false;
        }
        return buffer.readableBytes() >= 8 && FirebirdCommandPacketType.CANCEL.getValue() == buffer.getInt(buffer.readerIndex());
    }
    
    private void handleImmediateCancel(final ChannelHandlerContext context, final ByteBuf buffer, final int connectionId) {
        int kind = buffer.getInt(buffer.readerIndex() + 4);
        FirebirdCancellationRegistry.getInstance().apply(connectionId, kind);
        ReferenceCountUtil.release(buffer);
        if (ISCConstants.fb_cancel_abort == kind && executing) {
            closeAfterResponse = true;
        } else if (ISCConstants.fb_cancel_abort == kind) {
            context.close();
        }
    }
    
    private void dispatchCommand(final ChannelHandlerContext context, final Object message, final int connectionId) {
        if (executing) {
            queuedCommands++;
        } else {
            FirebirdCancellationRegistry.getInstance().beginCommand(connectionId);
        }
        context.fireChannelRead(message);
        if (!executing) {
            if (context.channel().config().isAutoRead()) {
                FirebirdCancellationRegistry.getInstance().endCommand(connectionId);
            } else {
                executing = true;
            }
        }
    }
    
    @Override
    @HighFrequencyInvocation
    public void channelReadComplete(final ChannelHandlerContext context) {
        context.fireChannelReadComplete();
        if (!executing || 0 != queuedCommands) {
            return;
        }
        if (!deferredInput.isEmpty() || discardAfterFragment || context.channel().config().isAutoRead()) {
            return;
        }
        context.read();
    }
    
    @Override
    @HighFrequencyInvocation
    public void userEventTriggered(final ChannelHandlerContext context, final Object event) {
        if (!(event instanceof WriteCompleteEvent)) {
            context.fireUserEventTriggered(event);
            return;
        }
        finishCommand(context);
        context.fireUserEventTriggered(event);
        if (closeAfterResponse) {
            context.close();
            return;
        }
        updateExecutionStatus(context);
        if (!executing && !deferredInput.isEmpty()) {
            replayDeferredInput();
        }
    }
    
    private void finishCommand(final ChannelHandlerContext context) {
        Integer connectionId = context.channel().attr(FirebirdConstant.CURRENT_CONNECTION).get();
        if (null != connectionId) {
            FirebirdCancellationRegistry.getInstance().endCommand(connectionId);
            if (queuedCommands > 0) {
                FirebirdCancellationRegistry.getInstance().beginCommand(connectionId);
            }
        }
    }
    
    private void updateExecutionStatus(final ChannelHandlerContext context) {
        if (context.channel().config().isAutoRead()) {
            executing = false;
        } else if (queuedCommands > 0) {
            queuedCommands--;
            if (0 == queuedCommands && deferredInput.isEmpty()) {
                context.read();
            }
        }
    }
    
    @HighFrequencyInvocation
    private void replayDeferredInput() {
        if (null == rawContext || replayingInput) {
            return;
        }
        boolean replayed = false;
        while (!executing && null != rawContext && rawContext.channel().isActive() && !deferredInput.isEmpty()) {
            if (FragmentKind.NONE == fragmentKind) {
                fragmentKind = FragmentKind.OTHER;
            }
            replayingInput = true;
            try {
                rawContext.fireChannelRead(deferredInput.remove());
                replayed = true;
            } finally {
                replayingInput = false;
            }
        }
        if (replayed && null != rawContext) {
            rawContext.fireChannelReadComplete();
        }
    }
    
    private void releaseDeferredInput() {
        while (!deferredInput.isEmpty()) {
            ReferenceCountUtil.release(deferredInput.remove());
        }
    }
    
    private enum FragmentKind {
        NONE, HEADER, CANCEL_BODY, COMPLETE_CANCEL, OTHER
    }
}
