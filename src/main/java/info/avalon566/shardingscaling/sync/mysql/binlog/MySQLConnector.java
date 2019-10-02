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

package info.avalon566.shardingscaling.sync.mysql.binlog;

import info.avalon566.shardingscaling.sync.mysql.binlog.codec.MySQLBinlogEventPacketDecoder;
import info.avalon566.shardingscaling.sync.mysql.binlog.codec.MySQLCommandPacketDecoder;
import info.avalon566.shardingscaling.sync.mysql.binlog.codec.MySQLLengthFieldBasedFrameEncoder;
import info.avalon566.shardingscaling.sync.mysql.binlog.event.AbstractBinlogEvent;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.command.BinlogDumpCommandPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.command.QueryCommandPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.command.RegisterSlaveCommandPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.response.ErrorPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.response.OkPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.response.InternalResultSet;
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
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;

/**
 * MySQL Connector.
 *
 * @author avalon566
 * @author yangyi
 */
@Slf4j
public final class MySQLConnector {

    private final int serverId;

    private final String host;

    private final int port;

    private final String username;

    private final String password;

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);

    private Channel channel;

    private Promise<Object> responseCallback;

    private ArrayBlockingQueue<AbstractBinlogEvent> blockingEventQueue = new ArrayBlockingQueue(1000);

    private ServerInfo serverInfo;

    class MySQLCommandResponHandler extends ChannelInboundHandlerAdapter {
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
                log.error("protocol resolution error", cause);
            }
        }
    }

    class MySQLBinlogEventHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof AbstractBinlogEvent) {
                blockingEventQueue.put((AbstractBinlogEvent) msg);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("protocol resolution error", cause);
        }
    }

    public MySQLConnector(final int serverId, final String host, final int port, final String username, final String password) {
        this.serverId = serverId;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * Connect to MySQL.
     */
    public synchronized void connect() {
        responseCallback = new DefaultPromise<>(eventLoopGroup.next());
        channel = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(final SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, Integer.MAX_VALUE, 0, 3, 1, 4, true));
                        socketChannel.pipeline().addLast(MySQLLengthFieldBasedFrameEncoder.class.getSimpleName(), new MySQLLengthFieldBasedFrameEncoder());
                        socketChannel.pipeline().addLast(new MySQLCommandPacketDecoder());
                        socketChannel.pipeline().addLast(new MySQLNegotiateHandler(username, password, responseCallback));
                        socketChannel.pipeline().addLast(new MySQLCommandResponHandler());
                    }
                })
                .option(ChannelOption.AUTO_READ, true)
                .connect(host, port).channel();
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
        var queryCommandPacket = new QueryCommandPacket();
        queryCommandPacket.setQueryString(queryString);
        channel.writeAndFlush(queryCommandPacket);
        return null != waitExpectedResponse(OkPacket.class);
    }

    /**
     * Execute update.
     *
     * @param queryString query string
     * @return affected rows
     */
    public synchronized int executeUpdate(final String queryString) {
        responseCallback = new DefaultPromise<>(eventLoopGroup.next());
        var queryCommandPacket = new QueryCommandPacket();
        queryCommandPacket.setQueryString(queryString);
        channel.writeAndFlush(queryCommandPacket);
        return (int) waitExpectedResponse(OkPacket.class).getAffectedRows();
    }

    /**
     * Execute query.
     *
     * @param queryString query string
     * @return result set
     */
    public synchronized InternalResultSet executeQuery(final String queryString) {
        responseCallback = new DefaultPromise<>(eventLoopGroup.next());
        var queryCommandPacket = new QueryCommandPacket();
        queryCommandPacket.setQueryString(queryString);
        channel.writeAndFlush(queryCommandPacket);
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
        responseCallback = null;
        BinlogDumpCommandPacket binlogDumpCmd = new BinlogDumpCommandPacket();
        binlogDumpCmd.setBinlogFileName(binlogFileName);
        binlogDumpCmd.setBinlogPosition(binlogPosition);
        binlogDumpCmd.setSlaveServerId(serverId);
        channel.pipeline().remove(MySQLCommandPacketDecoder.class);
        channel.pipeline().remove(MySQLCommandResponHandler.class);
        channel.pipeline().addLast(new MySQLBinlogEventPacketDecoder());
        channel.pipeline().addLast(new MySQLBinlogEventHandler());
        channel.writeAndFlush(binlogDumpCmd);
    }

    /**
     * Poll binlog event.
     *
     * @return binlog event
     */
    public synchronized AbstractBinlogEvent poll() {
        return blockingEventQueue.poll();
    }

    private void initDumpConnectSession() {
        if (serverInfo.getServerVersion().greaterThanOrEqualTo(5, 6, 0)) {
            execute("set @master_binlog_checksum= @@global.binlog_checksum");
        }
    }

    private void registerSlave() {
        responseCallback = new DefaultPromise<>(eventLoopGroup.next());
        RegisterSlaveCommandPacket cmd = new RegisterSlaveCommandPacket();
        var localAddress = (InetSocketAddress) channel.localAddress();
        cmd.setReportHost(localAddress.getHostName());
        cmd.setReportPort((short) localAddress.getPort());
        cmd.setReportPassword(password);
        cmd.setReportUser(username);
        cmd.setServerId(123456);
        channel.writeAndFlush(cmd);
        waitExpectedResponse(OkPacket.class);
    }

    private <T> T waitExpectedResponse(final Class<T> type) {
        try {
            var response = responseCallback.get();
            if (null == response) {
                return null;
            }
            if (type.equals(response.getClass())) {
                return (T) response;
            }
            if (response instanceof ErrorPacket) {
                throw new RuntimeException(((ErrorPacket) response).getMessage());
            }
            throw new RuntimeException("unexpected response type");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
