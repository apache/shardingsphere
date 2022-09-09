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
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobExecutionException;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.GlobalTableMapEventMapping;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.AbstractBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.netty.MySQLBinlogEventPacketDecoder;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.netty.MySQLCommandPacketDecoder;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.netty.MySQLNegotiateHandler;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.netty.MySQLNegotiatePackageDecoder;
import org.apache.shardingsphere.db.protocol.codec.PacketCodec;
import org.apache.shardingsphere.db.protocol.mysql.codec.MySQLPacketCodecEngine;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.binlog.MySQLComBinlogDumpCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.binlog.MySQLComRegisterSlaveCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.netty.ChannelAttrInitializer;
import org.apache.shardingsphere.infra.util.exception.external.sql.UnsupportedSQLOperationException;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

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
    
    private final ArrayBlockingQueue<AbstractBinlogEvent> blockingEventQueue = new ArrayBlockingQueue<>(10000);
    
    private ServerInfo serverInfo;
    
    private volatile boolean running = true;
    
    private final AtomicInteger reconnectTimes = new AtomicInteger();
    
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
                        socketChannel.pipeline().addLast(new MySQLNegotiatePackageDecoder());
                        socketChannel.pipeline().addLast(new MySQLCommandPacketDecoder());
                        socketChannel.pipeline().addLast(new MySQLNegotiateHandler(connectInfo.getUsername(), connectInfo.getPassword(), responseCallback));
                        socketChannel.pipeline().addLast(new MySQLCommandResponseHandler());
                    }
                }).connect(connectInfo.getHost(), connectInfo.getPort()).channel();
        serverInfo = waitExpectedResponse(ServerInfo.class);
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
        channel.writeAndFlush(comQueryPacket);
        return null != waitExpectedResponse(MySQLOKPacket.class);
    }
    
    /**
     * Execute update.
     *
     * @param queryString query string
     * @return affected rows
     */
    public synchronized int executeUpdate(final String queryString) {
        responseCallback = new DefaultPromise<>(eventLoopGroup.next());
        MySQLComQueryPacket comQueryPacket = new MySQLComQueryPacket(queryString);
        channel.writeAndFlush(comQueryPacket);
        return (int) Objects.requireNonNull(waitExpectedResponse(MySQLOKPacket.class)).getAffectedRows();
    }
    
    /**
     * Execute query.
     *
     * @param queryString query string
     * @return result set
     */
    public synchronized InternalResultSet executeQuery(final String queryString) {
        responseCallback = new DefaultPromise<>(eventLoopGroup.next());
        MySQLComQueryPacket comQueryPacket = new MySQLComQueryPacket(queryString);
        channel.writeAndFlush(comQueryPacket);
        return waitExpectedResponse(InternalResultSet.class);
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
        reconnectTimes.set(0);
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
        channel.writeAndFlush(packet);
        waitExpectedResponse(MySQLOKPacket.class);
    }
    
    private int queryChecksumLength() {
        if (!serverInfo.getServerVersion().greaterThanOrEqualTo(5, 6, 0)) {
            return 0;
        }
        InternalResultSet resultSet = executeQuery("SELECT @@GLOBAL.BINLOG_CHECKSUM");
        String checksumType = resultSet.getFieldValues().get(0).getData().iterator().next().toString();
        checksumType = null != checksumType ? checksumType.toUpperCase() : "";
        switch (checksumType) {
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
        channel.pipeline().addLast(new MySQLBinlogEventPacketDecoder(checksumLength, GlobalTableMapEventMapping.getTableMapEventMap(tableKey)));
        channel.pipeline().addLast(new MySQLBinlogEventHandler());
        channel.writeAndFlush(new MySQLComBinlogDumpCommandPacket((int) binlogPosition, connectInfo.getServerId(), binlogFileName));
    }
    
    /**
     * Poll binlog event.
     *
     * @return binlog event
     */
    public synchronized AbstractBinlogEvent poll() {
        if (!running) {
            throw new PipelineJobExecutionException("binlog sync channel already closed, can't poll event");
        }
        try {
            return blockingEventQueue.poll(100, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ignored) {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> T waitExpectedResponse(final Class<T> type) {
        try {
            Object response = responseCallback.get(5, TimeUnit.SECONDS);
            if (null == response) {
                return null;
            }
            if (type.equals(response.getClass())) {
                return (T) response;
            }
            if (response instanceof MySQLErrPacket) {
                throw new RuntimeException(((MySQLErrPacket) response).getErrorMessage());
            }
            throw new RuntimeException("unexpected response type");
        } catch (final InterruptedException | ExecutionException | TimeoutException ex) {
            throw new RuntimeException(ex);
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
            channel.close().sync();
        } catch (final InterruptedException ex) {
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
        
        private AbstractBinlogEvent lastBinlogEvent;
        
        @Override
        public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
            if (!running) {
                return;
            }
            if (msg instanceof AbstractBinlogEvent) {
                lastBinlogEvent = (AbstractBinlogEvent) msg;
                blockingEventQueue.put(lastBinlogEvent);
            }
        }
        
        @Override
        public void channelInactive(final ChannelHandlerContext ctx) {
            log.warn("channel inactive");
            if (!running) {
                return;
            }
            reconnect();
        }
        
        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
            running = false;
            String fileName = null == lastBinlogEvent ? null : lastBinlogEvent.getFileName();
            Long position = null == lastBinlogEvent ? null : lastBinlogEvent.getPosition();
            log.error("MySQLBinlogEventHandler protocol resolution error, file name:{}, position:{}", fileName, position, cause);
            reconnect();
        }
        
        private void reconnect() {
            if (reconnectTimes.get() > 3) {
                log.warn("exceeds the maximum number of retry times, last binlog event:{}", lastBinlogEvent);
                running = false;
                return;
            }
            int retryTimes = reconnectTimes.incrementAndGet();
            log.info("reconnect MySQL client, retry times={}", retryTimes);
            closeChannel();
            connect();
            subscribe(lastBinlogEvent.getFileName(), lastBinlogEvent.getPosition());
        }
    }
}
