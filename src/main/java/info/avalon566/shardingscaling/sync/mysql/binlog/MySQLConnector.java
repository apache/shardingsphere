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
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.command.BinlogDumpCommandPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.command.QueryCommandPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.command.RegisterSlaveCommandPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.response.ErrorPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.response.OkPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.response.InternalResultSet;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutionException;

/**
 * @author avalon566
 */
public final class MySQLConnector {

    private Logger LOGGER = LoggerFactory.getLogger(MySQLConnector.class);

    private final int serverId;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
    private Channel channel;
    private Promise<Object> responseCallback;

    public MySQLConnector(int serverId, String host, int port, String username, String password) {
        this.serverId = serverId;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public synchronized void connect() {
        responseCallback = new DefaultPromise<>(eventLoopGroup.next());
        channel = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, Integer.MAX_VALUE, 0, 3, 1, 4, true));
                        socketChannel.pipeline().addLast(MySQLLengthFieldBasedFrameEncoder.class.getSimpleName(), new MySQLLengthFieldBasedFrameEncoder());
                        socketChannel.pipeline().addLast(new MySQLCommandPacketDecoder());
                        socketChannel.pipeline().addLast(new MySQLNegotiateHandler(username, password, responseCallback));
                        socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                if (null != responseCallback) {
                                    responseCallback.setSuccess(msg);
                                }
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                if (null != responseCallback) {
                                    responseCallback.setFailure(cause);
                                }
                            }
                        });
                    }
                })
                .option(ChannelOption.AUTO_READ, true)
                .connect(host, port).channel();
        waitExpectedResponse(OkPacket.class);
    }

    public synchronized boolean execute(String queryString) {
        responseCallback = new DefaultPromise<Object>(eventLoopGroup.next());
        var queryCommandPacket = new QueryCommandPacket();
        queryCommandPacket.setQueryString(queryString);
        channel.writeAndFlush(queryCommandPacket);
        return null != waitExpectedResponse(OkPacket.class);
    }

    public synchronized int executeUpdate(String queryString) {
        responseCallback = new DefaultPromise<>(eventLoopGroup.next());
        var queryCommandPacket = new QueryCommandPacket();
        queryCommandPacket.setQueryString(queryString);
        channel.writeAndFlush(queryCommandPacket);
        return (int) waitExpectedResponse(OkPacket.class).getAffectedRows();
    }

    public synchronized InternalResultSet executeQuery(String queryString) {
        responseCallback = new DefaultPromise<>(eventLoopGroup.next());
        var queryCommandPacket = new QueryCommandPacket();
        queryCommandPacket.setQueryString(queryString);
        channel.writeAndFlush(queryCommandPacket);
        return waitExpectedResponse(InternalResultSet.class);
    }

    public synchronized void dump(String binlogFileName, long binlogPosition) {
        initDumpConnectSession();
        registerSlave();
        responseCallback = null;
        BinlogDumpCommandPacket binlogDumpCmd = new BinlogDumpCommandPacket();
        binlogDumpCmd.setBinlogFileName(binlogFileName);
        binlogDumpCmd.setBinlogPosition(binlogPosition);
        binlogDumpCmd.setSlaveServerId(serverId);
        channel.pipeline().remove(MySQLCommandPacketDecoder.class);
        channel.pipeline().addAfter(
                MySQLLengthFieldBasedFrameEncoder.class.getSimpleName(),
                MySQLBinlogEventPacketDecoder.class.getSimpleName(),
                new MySQLBinlogEventPacketDecoder());
        channel.writeAndFlush(binlogDumpCmd);
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initDumpConnectSession() {
        execute("set @master_binlog_checksum= @@global.binlog_checksum");
    }

    private void registerSlave() {
        responseCallback = new DefaultPromise<>(eventLoopGroup.next());
        RegisterSlaveCommandPacket cmd = new RegisterSlaveCommandPacket();
        var localAddress = (InetSocketAddress)channel.localAddress();
        cmd.reportHost = localAddress.getHostName();
        cmd.reportPort = (short) localAddress.getPort();
        cmd.reportPasswd = password;
        cmd.reportUser = username;
        cmd.serverId = 123456;
        channel.writeAndFlush(cmd);
        waitExpectedResponse(OkPacket.class);
    }

    private <T> T waitExpectedResponse(Class<T> type) {
        try {
            var response = responseCallback.get();
            if(null == response) {
                return null;
            }
            if (type.equals(response.getClass())) {
                return (T) response;
            }
            if (response instanceof ErrorPacket) {
                throw new RuntimeException(((ErrorPacket) response).getMessage());
            }
            throw new RuntimeException("unexpected response type");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
