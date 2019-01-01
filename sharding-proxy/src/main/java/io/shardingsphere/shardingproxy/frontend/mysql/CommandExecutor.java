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

package io.shardingsphere.shardingproxy.frontend.mysql;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.frontend.common.FrontendHandler;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.runtime.RuntimeContext;
import io.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandPacketFactory;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.quit.ComQuitPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.QueryCommandPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import io.shardingsphere.spi.root.RootInvokeHook;
import io.shardingsphere.spi.root.SPIRootInvokeHook;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Command executor.
 *
 * @author zhangyonglun
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class CommandExecutor implements Runnable {
    
    private static final GlobalRegistry GLOBAL_REGISTRY = GlobalRegistry.getInstance();
    
    private static final RuntimeContext RUNTIME_CONTEXT = RuntimeContext.getInstance();
    
    private final ChannelHandlerContext context;
    
    private final ByteBuf message;
    
    private final FrontendHandler frontendHandler;
    
    private int currentSequenceId;
    
    @Getter
    private String commandPacketId;
    
    private final RootInvokeHook rootInvokeHook = new SPIRootInvokeHook();
    
    private CommandPacket commandPacket;
    
    @Override
    public void run() {
        rootInvokeHook.start();
        int connectionSize = 0;
        try (MySQLPacketPayload payload = new MySQLPacketPayload(message);
             BackendConnection backendConnection = frontendHandler.getBackendConnection()) {
            backendConnection.getStateHandler().waitUntilConnectionReleasedIfNecessary();
            commandPacketId = UUID.randomUUID().toString();
            if (GLOBAL_REGISTRY.getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.PROXY_BACKEND_USE_NIO)) {
                RUNTIME_CONTEXT.getLocalFrontendChannel().set(context.channel());
                RUNTIME_CONTEXT.getCommandPacketId().set(commandPacketId);
                commandPacket = getCommandPacket(payload, backendConnection);
                frontendHandler.getCommandSequencer().addCommandPacketId(commandPacketId);
                RUNTIME_CONTEXT.getUniqueCommandExecutor().put(commandPacketId, this);
                commandPacket.execute();
                if (commandPacket instanceof ComQuitPacket) {
                    release();
                }
                return;
            }
            commandPacket = getCommandPacket(payload, backendConnection);
            Optional<CommandResponsePackets> responsePackets = commandPacket.execute();
            if (!responsePackets.isPresent()) {
                return;
            }
            writeResult(responsePackets.get());
            connectionSize = backendConnection.getConnectionSize();
        } catch (final SQLException ex) {
            writeErrPacket(ex);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            context.write(new ErrPacket(1, ServerErrorCode.ER_STD_UNKNOWN_EXCEPTION, ex.getMessage()));
        } finally {
            flush();
            rootInvokeHook.finish(connectionSize);
        }
    }
    
    /**
     * Output result to client.
     *
     * @param responsePackets the response packets of executing SQL
     */
    public void writeResult(final CommandResponsePackets responsePackets) {
        if (GLOBAL_REGISTRY.getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.PROXY_BACKEND_USE_NIO)) {
            this.frontendHandler.getCommandSequencer().reorderAndWriteResponse(this, responsePackets);
        } else {
            doWriteResult(responsePackets);
        }
    }
    
    /**
     * Write and flush response packets to client.
     *
     * @param responsePackets command response packets
     */
    protected void doWriteResult(final CommandResponsePackets responsePackets) {
        if (!context.channel().isActive()) {
            return;
        }
        for (DatabasePacket each : responsePackets.getPackets()) {
            context.channel().write(each);
        }
        if (commandPacket instanceof QueryCommandPacket && !(responsePackets.getHeadPacket() instanceof OKPacket) && !(responsePackets.getHeadPacket() instanceof ErrPacket)) {
            try {
                writeMoreResults((QueryCommandPacket) commandPacket, responsePackets.getPackets().size());
            } catch (SQLException ex) {
                writeErrPacket(ex);
            }
        }
        release();
    }
    
    /**
     * Flush data to client.
     */
    public void flush() {
        context.flush();
    }
    
    private void release() {
        RUNTIME_CONTEXT.getUniqueCommandExecutor().remove(commandPacketId);
    }
    
    /**
     * Output error packet to client.
     *
     * @param ex SQL exception
     */
    public void writeErrPacket(final SQLException ex) {
        context.write(new ErrPacket(++currentSequenceId, ex));
    }
    
    private CommandPacket getCommandPacket(final MySQLPacketPayload payload, final BackendConnection backendConnection) throws SQLException {
        int sequenceId = payload.readInt1();
        return CommandPacketFactory.newInstance(sequenceId, payload, backendConnection);
    }
    
    private void writeMoreResults(final QueryCommandPacket queryCommandPacket, final int headPacketsCount) throws SQLException {
        if (!context.channel().isActive()) {
            return;
        }
        currentSequenceId = headPacketsCount;
        int count = 0;
        int proxyFrontendFlushThreshold = GlobalRegistry.getInstance().getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.PROXY_FRONTEND_FLUSH_THRESHOLD);
        while (queryCommandPacket.next()) {
            count++;
            while (!context.channel().isWritable() && context.channel().isActive()) {
                synchronized (frontendHandler) {
                    try {
                        frontendHandler.wait();
                    } catch (final InterruptedException ignored) {
                    }
                }
            }
            DatabasePacket resultValue = queryCommandPacket.getResultValue();
            currentSequenceId = resultValue.getSequenceId();
            context.write(resultValue);
            if (proxyFrontendFlushThreshold == count) {
                flush();
                count = 0;
            }
        }
        EofPacket eofPacket = new EofPacket(++currentSequenceId);
        context.write(eofPacket);
    }
}
