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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.MySQLBaseBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.PlaceholderBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.netty.MySQLBinlogEventPacketDecoder;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.netty.MySQLCommandPacketDecoder;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.netty.MySQLNegotiateHandler;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.netty.MySQLNegotiatePackageDecoder;
import org.apache.shardingsphere.database.protocol.codec.PacketCodec;
import org.apache.shardingsphere.database.protocol.mysql.codec.MySQLPacketCodecEngine;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.netty.MySQLSequenceIdInboundHandler;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.binlog.MySQLComBinlogDumpCommandPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.binlog.MySQLComRegisterSlaveCommandPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.proxy.frontend.netty.ChannelAttrInitializer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * MySQL binlog client.
 */
@RequiredArgsConstructor
@Slf4j
public final class MySQLBinlogClient {
    
    private final ConnectInfo connectInfo;
    
    private final boolean decodeWithTX;
    
    private final ArrayBlockingQueue<List<MySQLBaseBinlogEvent>> blockingEventQueue = new ArrayBlockingQueue<>(2500);
    
    private EventLoopGroup eventLoopGroup;
    
    private Channel channel;
    
    private Promise<Object> responseCallback;
    
    private MySQLServerVersion serverVersion;
    
    private volatile boolean running = true;
    
    /**
     * Connect to MySQL.
     */
    public synchronized void connect() {
        eventLoopGroup = new NioEventLoopGroup(1);
        responseCallback = new DefaultPromise<>(eventLoopGroup.next());
        channel = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.AUTO_READ, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    
                    @Override
                    protected void initChannel(final SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast(new ChannelAttrInitializer());
                        socketChannel.pipeline().addLast(new PacketCodec(new MySQLPacketCodecEngine()));
                        socketChannel.pipeline().addLast(new MySQLSequenceIdInboundHandler(socketChannel));
                        socketChannel.pipeline().addLast(new MySQLNegotiatePackageDecoder());
                        socketChannel.pipeline().addLast(new MySQLCommandPacketDecoder());
                        socketChannel.pipeline().addLast(new MySQLNegotiateHandler(connectInfo.getUsername(), connectInfo.getPassword(), responseCallback));
                        socketChannel.pipeline().addLast(new MySQLCommandResponseHandler());
                    }
                }).connect(connectInfo.getHost(), connectInfo.getPort()).channel();
        serverVersion = waitExpectedResponse(MySQLServerVersion.class).orElse(null);
        running = true;
    }
    
    /**
     * Execute command.
     *
     * @param queryString query string
     * @return true if execute successfully, otherwise false
     */
    public synchronized boolean execute(final String queryString) {
        responseCallback = new DefaultPromise<>(eventLoopGroup.next());
        MySQLComQueryPacket comQueryPacket = new MySQLComQueryPacket(queryString);
        resetSequenceID();
        channel.writeAndFlush(comQueryPacket);
        return waitExpectedResponse(MySQLOKPacket.class).isPresent();
    }
    
    /**
     * Execute update.
     *
     * @param queryString query string
     * @return affected rows
     * @throws PipelineInternalException if could not get MySQL OK packet
     */
    public synchronized int executeUpdate(final String queryString) {
        responseCallback = new DefaultPromise<>(eventLoopGroup.next());
        MySQLComQueryPacket comQueryPacket = new MySQLComQueryPacket(queryString);
        resetSequenceID();
        channel.writeAndFlush(comQueryPacket);
        Optional<MySQLOKPacket> packet = waitExpectedResponse(MySQLOKPacket.class);
        ShardingSpherePreconditions.checkState(packet.isPresent(), () -> new PipelineInternalException("Could not get MySQL OK packet"));
        return (int) packet.get().getAffectedRows();
    }
    
    /**
     * Execute query.
     *
     * @param queryString query string
     * @return result set
     * @throws PipelineInternalException if getting MySQL packet failed
     */
    public synchronized InternalResultSet executeQuery(final String queryString) {
        responseCallback = new DefaultPromise<>(eventLoopGroup.next());
        MySQLComQueryPacket comQueryPacket = new MySQLComQueryPacket(queryString);
        resetSequenceID();
        channel.writeAndFlush(comQueryPacket);
        Optional<InternalResultSet> result = waitExpectedResponse(InternalResultSet.class);
        ShardingSpherePreconditions.checkState(result.isPresent(), () -> new PipelineInternalException("Could not get MySQL FieldCount/ColumnDefinition/TextResultSetRow packet"));
        return result.get();
    }
    
    /**
     * Start dump binlog.
     *
     * @param binlogFileName binlog file name
     * @param binlogPosition binlog position
     */
    public synchronized void subscribe(final String binlogFileName, final long binlogPosition) {
        initDumpConnectSession();
        configureHeartbeat();
        registerSlave();
        dumpBinlog(binlogFileName, binlogPosition, queryChecksumLength());
        log.info("subscribe binlog file: {}, position: {}", binlogFileName, binlogPosition);
    }
    
    private void initDumpConnectSession() {
        if (serverVersion.greaterThanOrEqualTo(5, 6, 0)) {
            execute("SET @MASTER_BINLOG_CHECKSUM= @@GLOBAL.BINLOG_CHECKSUM");
        }
    }
    
    private void configureHeartbeat() {
        execute("SET @master_heartbeat_period=" + TimeUnit.SECONDS.toNanos(15L));
    }
    
    private void registerSlave() {
        responseCallback = new DefaultPromise<>(eventLoopGroup.next());
        InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
        MySQLComRegisterSlaveCommandPacket packet = new MySQLComRegisterSlaveCommandPacket(
                connectInfo.getServerId(), localAddress.getHostName(), connectInfo.getUsername(), connectInfo.getPassword(), localAddress.getPort());
        resetSequenceID();
        channel.writeAndFlush(packet);
        waitExpectedResponse(MySQLOKPacket.class);
    }
    
    private int queryChecksumLength() {
        if (!serverVersion.greaterThanOrEqualTo(5, 6, 0)) {
            return 0;
        }
        InternalResultSet resultSet = executeQuery("SELECT @@GLOBAL.BINLOG_CHECKSUM");
        String checksumType = resultSet.getFieldValues().get(0).getData().iterator().next().toString();
        switch (checksumType.toUpperCase()) {
            case "NONE":
                return 0;
            case "CRC32":
                return 4;
            default:
                throw new UnsupportedSQLOperationException(checksumType);
        }
    }
    
    private void dumpBinlog(final String binlogFileName, final long binlogPosition, final int checksumLength) {
        responseCallback = null;
        channel.pipeline().remove(MySQLCommandPacketDecoder.class);
        channel.pipeline().remove(MySQLCommandResponseHandler.class);
        String tableKey = String.join(":", connectInfo.getHost(), String.valueOf(connectInfo.getPort()));
        channel.pipeline().addLast(new MySQLBinlogEventPacketDecoder(checksumLength, GlobalTableMapEventMapping.getTableMapEventMap(tableKey), decodeWithTX));
        channel.pipeline().addLast(new MySQLBinlogEventHandler(new PlaceholderBinlogEvent(binlogFileName, binlogPosition, 0L)));
        resetSequenceID();
        channel.writeAndFlush(new MySQLComBinlogDumpCommandPacket((int) binlogPosition, connectInfo.getServerId(), binlogFileName));
    }
    
    private void resetSequenceID() {
        channel.attr(MySQLConstants.SEQUENCE_ID_ATTRIBUTE_KEY).get().set(0);
    }
    
    /**
     * Poll binlog event.
     *
     * @return binlog event
     */
    public synchronized List<MySQLBaseBinlogEvent> poll() {
        if (!running) {
            return Collections.emptyList();
        }
        try {
            List<MySQLBaseBinlogEvent> result = blockingEventQueue.poll(100L, TimeUnit.MILLISECONDS);
            return null == result ? Collections.emptyList() : result;
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> Optional<T> waitExpectedResponse(final Class<T> type) {
        try {
            Object response = responseCallback.get(5L, TimeUnit.SECONDS);
            if (null == response) {
                return Optional.empty();
            }
            if (type.equals(response.getClass())) {
                return Optional.of((T) response);
            }
            if (response instanceof MySQLErrPacket) {
                throw new PipelineInternalException(((MySQLErrPacket) response).getErrorMessage());
            }
            throw new PipelineInternalException("unexpected response type");
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new PipelineInternalException(ex);
        } catch (final ExecutionException | TimeoutException ex) {
            throw new PipelineInternalException(ex);
        }
    }
    
    /**
     * Close netty channel.
     *
     * @return channel future
     */
    public Optional<ChannelFuture> closeChannel() {
        if (null == channel || !channel.isOpen()) {
            return Optional.empty();
        }
        running = false;
        ChannelFuture future = channel.close();
        if (null != eventLoopGroup) {
            eventLoopGroup.shutdownGracefully();
        }
        return Optional.of(future);
    }
    
    private class MySQLCommandResponseHandler extends ChannelInboundHandlerAdapter {
        
        @Override
        public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
            if (null != responseCallback) {
                responseCallback.setSuccess(msg);
            }
        }
        
        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
            if (null != responseCallback) {
                responseCallback.setFailure(cause);
                log.error("MySQLCommandResponseHandler protocol resolution error", cause);
            }
        }
    }
    
    private class MySQLBinlogEventHandler extends ChannelInboundHandlerAdapter {
        
        private final AtomicReference<MySQLBaseBinlogEvent> lastBinlogEvent;
        
        private final AtomicBoolean reconnectRequested = new AtomicBoolean(false);
        
        MySQLBinlogEventHandler(final MySQLBaseBinlogEvent lastBinlogEvent) {
            this.lastBinlogEvent = new AtomicReference<>(lastBinlogEvent);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
            if (!running) {
                return;
            }
            if (msg instanceof List) {
                List<MySQLBaseBinlogEvent> records = (List<MySQLBaseBinlogEvent>) msg;
                if (records.isEmpty()) {
                    log.warn("The records is empty");
                    return;
                }
                lastBinlogEvent.set(records.get(records.size() - 1));
                blockingEventQueue.put(records);
                return;
            }
            if (msg instanceof MySQLBaseBinlogEvent) {
                lastBinlogEvent.set((MySQLBaseBinlogEvent) msg);
                blockingEventQueue.put(Collections.singletonList(lastBinlogEvent.get()));
            }
        }
        
        @Override
        public void channelInactive(final ChannelHandlerContext ctx) {
            log.warn("MySQL binlog channel inactive, channel: {}, running: {}", ctx.channel(), running);
            if (!running) {
                return;
            }
            tryReconnect();
        }
        
        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
            log.error("MySQLBinlogEventHandler protocol resolution error, channel: {}, lastBinlogEvent: {}", ctx.channel(), JsonUtils.toJsonString(lastBinlogEvent.get()), cause);
        }
        
        private void tryReconnect() {
            if (reconnectRequested.compareAndSet(false, true)) {
                CompletableFuture.runAsync(this::reconnect).whenComplete((result, ex) -> reconnectRequested.set(false));
            }
        }
        
        @SneakyThrows(InterruptedException.class)
        private synchronized void reconnect() {
            for (int reconnectTimes = 0; reconnectTimes < 3; reconnectTimes++) {
                try {
                    connect();
                    log.info("Reconnect times {}", reconnectTimes);
                    subscribe(lastBinlogEvent.get().getFileName(), lastBinlogEvent.get().getPosition());
                    break;
                    // CHECKSTYLE:OFF
                } catch (final RuntimeException ex) {
                    // CHECKSTYLE:ON
                    log.error("Reconnect failed, reconnect times: {}, lastBinlogEvent: {}", reconnectTimes, JsonUtils.toJsonString(lastBinlogEvent.get()), ex);
                    wait(1000L << reconnectTimes);
                }
            }
        }
    }
}
