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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.BinlogSyncChannelAlreadyClosedException;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.GlobalTableMapEventMapping;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.AbstractBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.netty.MySQLBinlogEventPacketDecoder;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.netty.MySQLCommandPacketDecoder;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.netty.MySQLNegotiateHandler;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.netty.MySQLNegotiatePackageDecoder;
import org.apache.shardingsphere.db.protocol.codec.PacketCodec;
import org.apache.shardingsphere.db.protocol.mysql.codec.MySQLPacketCodecEngine;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.netty.MySQLSequenceIdInboundHandler;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.binlog.MySQLComBinlogDumpCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.binlog.MySQLComRegisterSlaveCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.netty.ChannelAttrInitializer;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * MySQL Connector.
 */
@RequiredArgsConstructor
@Slf4j
public final class MySQLClient {
    
    private final ConnectInfo connectInfo;
    
    private EventLoopGroup eventLoopGroup;
    
    private Channel channel;
    
    private Promise<Object> responseCallback;
    
    private final ArrayBlockingQueue<List<AbstractBinlogEvent>> blockingEventQueue = new ArrayBlockingQueue<>(2500);
    
    private ServerInfo serverInfo;
    
    private volatile boolean running = true;
    
    private final AtomicInteger reconnectTimes = new AtomicInteger();
    
    private final boolean decodeWithTX;
    
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
                        socketChannel.attr(MySQLConstants.MYSQL_SEQUENCE_ID).set(new AtomicInteger());
                        socketChannel.pipeline().addLast(new ChannelAttrInitializer());
                        socketChannel.pipeline().addLast(new PacketCodec(new MySQLPacketCodecEngine()));
                        socketChannel.pipeline().addLast(new MySQLSequenceIdInboundHandler());
                        socketChannel.pipeline().addLast(new MySQLNegotiatePackageDecoder());
                        socketChannel.pipeline().addLast(new MySQLCommandPacketDecoder());
                        socketChannel.pipeline().addLast(new MySQLNegotiateHandler(connectInfo.getUsername(), connectInfo.getPassword(), responseCallback));
                        socketChannel.pipeline().addLast(new MySQLCommandResponseHandler());
                    }
                }).connect(connectInfo.getHost(), connectInfo.getPort()).channel();
        serverInfo = waitExpectedResponse(ServerInfo.class).orElse(null);
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
        MySQLComQueryPacket comQueryPacket = new MySQLComQueryPacket(queryString, true);
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
        MySQLComQueryPacket comQueryPacket = new MySQLComQueryPacket(queryString, false);
        resetSequenceID();
        channel.writeAndFlush(comQueryPacket);
        Optional<MySQLOKPacket> packet = waitExpectedResponse(MySQLOKPacket.class);
        if (!packet.isPresent()) {
            throw new PipelineInternalException("Could not get MySQL OK packet");
        }
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
        MySQLComQueryPacket comQueryPacket = new MySQLComQueryPacket(queryString, false);
        resetSequenceID();
        channel.writeAndFlush(comQueryPacket);
        Optional<InternalResultSet> result = waitExpectedResponse(InternalResultSet.class);
        if (!result.isPresent()) {
            throw new PipelineInternalException("Could not get MySQL FieldCount/ColumnDefinition/TextResultSetRow packet");
        }
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
        registerSlave();
        dumpBinlog(binlogFileName, binlogPosition, queryChecksumLength());
        log.info("subscribe binlog file: {}, position: {}", binlogFileName, binlogPosition);
    }
    
    private void initDumpConnectSession() {
        if (serverInfo.getServerVersion().greaterThanOrEqualTo(5, 6, 0)) {
            execute("SET @MASTER_BINLOG_CHECKSUM= @@GLOBAL.BINLOG_CHECKSUM");
        }
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
        if (!serverInfo.getServerVersion().greaterThanOrEqualTo(5, 6, 0)) {
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
        channel.pipeline().addLast(new MySQLBinlogEventHandler(getLastBinlogEvent(binlogFileName, binlogPosition)));
        resetSequenceID();
        channel.writeAndFlush(new MySQLComBinlogDumpCommandPacket((int) binlogPosition, connectInfo.getServerId(), binlogFileName));
    }
    
    private AbstractBinlogEvent getLastBinlogEvent(final String binlogFileName, final long binlogPosition) {
        PlaceholderEvent result = new PlaceholderEvent();
        result.setFileName(binlogFileName);
        result.setPosition(binlogPosition);
        return result;
    }
    
    private void resetSequenceID() {
        channel.attr(MySQLConstants.MYSQL_SEQUENCE_ID).get().set(0);
    }
    
    /**
     * Poll binlog event.
     *
     * @return binlog event
     */
    public synchronized List<AbstractBinlogEvent> poll() {
        ShardingSpherePreconditions.checkState(running, BinlogSyncChannelAlreadyClosedException::new);
        try {
            List<AbstractBinlogEvent> result = blockingEventQueue.poll(100L, TimeUnit.MILLISECONDS);
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
     */
    public void closeChannel() {
        if (null == channel || !channel.isOpen()) {
            return;
        }
        try {
            running = false;
            channel.close().sync();
            if (null != eventLoopGroup) {
                eventLoopGroup.shutdownGracefully();
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("close channel interrupted", ex);
        }
    }
    
    private final class MySQLCommandResponseHandler extends ChannelInboundHandlerAdapter {
        
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
    
    private final class MySQLBinlogEventHandler extends ChannelInboundHandlerAdapter {
        
        private final AtomicReference<AbstractBinlogEvent> lastBinlogEvent;
        
        MySQLBinlogEventHandler(final AbstractBinlogEvent lastBinlogEvent) {
            this.lastBinlogEvent = new AtomicReference<>(lastBinlogEvent);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
            if (!running) {
                return;
            }
            reconnectTimes.set(0);
            if (msg instanceof List) {
                List<AbstractBinlogEvent> records = (List<AbstractBinlogEvent>) msg;
                if (records.isEmpty()) {
                    log.warn("The records is empty");
                    return;
                }
                lastBinlogEvent.set(records.get(records.size() - 1));
                blockingEventQueue.put(records);
                return;
            }
            if (msg instanceof AbstractBinlogEvent) {
                lastBinlogEvent.set((AbstractBinlogEvent) msg);
                blockingEventQueue.put(Collections.singletonList(lastBinlogEvent.get()));
            }
        }
        
        @Override
        public void channelInactive(final ChannelHandlerContext ctx) {
            log.warn("MySQL binlog channel inactive");
            if (!running) {
                return;
            }
            reconnect();
        }
        
        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
            String fileName = null == lastBinlogEvent.get() ? null : lastBinlogEvent.get().getFileName();
            Long position = null == lastBinlogEvent.get() ? null : lastBinlogEvent.get().getPosition();
            log.error("MySQLBinlogEventHandler protocol resolution error, file name:{}, position:{}", fileName, position, cause);
        }
        
        private void reconnect() {
            closeChannel();
            if (reconnectTimes.incrementAndGet() > 3) {
                log.warn("Exceeds the maximum number of retry times, last binlog event:{}", lastBinlogEvent);
                return;
            }
            connect();
            log.info("Reconnect times {}", reconnectTimes.get());
            subscribe(lastBinlogEvent.get().getFileName(), lastBinlogEvent.get().getPosition());
        }
    }
}
