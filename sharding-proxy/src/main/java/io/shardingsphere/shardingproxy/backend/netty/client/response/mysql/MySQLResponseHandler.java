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

package io.shardingsphere.shardingproxy.backend.netty.client.response.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.SimpleChannelPool;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.metadata.datasource.DataSourceMetaData;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.shardingproxy.backend.netty.client.BackendNettyClientManager;
import io.shardingsphere.shardingproxy.backend.netty.client.response.ResponseHandler;
import io.shardingsphere.shardingproxy.backend.netty.result.collector.QueryResultCollector;
import io.shardingsphere.shardingproxy.backend.netty.result.executor.QueryResultExecutor;
import io.shardingsphere.shardingproxy.frontend.common.executor.UserExecutorGroup;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.runtime.RuntimeContext;
import io.shardingsphere.shardingproxy.transport.mysql.constant.CapabilityFlag;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ServerInfo;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.ColumnDefinition41Packet;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.TextResultSetRowPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.handshake.HandshakePacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.handshake.HandshakeResponse41Packet;
import io.shardingsphere.shardingproxy.util.ChannelUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Response handler for MySQL.
 *
 * @author wangkai
 * @author linjiaqi
 */
@RequiredArgsConstructor
public final class MySQLResponseHandler extends ResponseHandler {
    
    private static final GlobalRegistry GLOBAL_REGISTRY = GlobalRegistry.getInstance();
    
    private static final RuntimeContext RUNTIME_CONTEXT = RuntimeContext.getInstance();
    
    private final DataSourceParameter dataSourceParameter;
    
    private final DataSourceMetaData dataSourceMetaData;
    
    private final Map<String, MySQLQueryResult> backendChannelResultMap;
    
    private final String logicSchemaName;
    
    private final String dataSourceName;
    
    public MySQLResponseHandler(final String dataSourceName, final String schema) {
        dataSourceParameter = GLOBAL_REGISTRY.getLogicSchema(schema).getDataSources().get(dataSourceName);
        dataSourceMetaData = GLOBAL_REGISTRY.getLogicSchema(schema).getMetaData().getDataSource().getActualDataSourceMetaData(dataSourceName);
        backendChannelResultMap = new HashMap<>();
        this.dataSourceName = dataSourceName;
        this.logicSchemaName = schema;
    }
    
    @Override
    protected int getHeader(final ByteBuf byteBuf) {
        MySQLPacketPayload payload = new MySQLPacketPayload(byteBuf);
        payload.getByteBuf().markReaderIndex();
        payload.readInt1();
        int result = payload.readInt1();
        payload.getByteBuf().resetReaderIndex();
        return result;
    }
    
    @Override
    protected void auth(final ChannelHandlerContext context, final ByteBuf byteBuf) {
        try (MySQLPacketPayload payload = new MySQLPacketPayload(byteBuf)) {
            HandshakePacket handshakePacket = new HandshakePacket(payload);
            byte[] authResponse = securePasswordAuthentication(
                    (null == dataSourceParameter.getPassword() ? "" : dataSourceParameter.getPassword()).getBytes(), handshakePacket.getAuthPluginData().getAuthPluginData());
            HandshakeResponse41Packet handshakeResponse41Packet = new HandshakeResponse41Packet(
                    handshakePacket.getSequenceId() + 1, CapabilityFlag.calculateHandshakeCapabilityFlagsLower(), 16777215, ServerInfo.CHARSET,
                    dataSourceParameter.getUsername(), authResponse, dataSourceMetaData.getSchemeName());
            context.writeAndFlush(handshakeResponse41Packet);
        }
    }
    
    @SneakyThrows
    private byte[] securePasswordAuthentication(final byte[] password, final byte[] authPluginData) {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        byte[] part1 = messageDigest.digest(password);
        messageDigest.reset();
        byte[] part2 = messageDigest.digest(part1);
        messageDigest.reset();
        messageDigest.update(authPluginData);
        byte[] result = messageDigest.digest(part2);
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (result[i] ^ part1[i]);
        }
        return result;
    }
    
    @Override
    protected void executeCommand(final ChannelHandlerContext context, final ByteBuf byteBuf, final int header) {
        switch (header) {
            case EofPacket.HEADER:
                eofPacket(context, byteBuf);
                break;
            case OKPacket.HEADER:
                okPacket(context, byteBuf);
                break;
            case ErrPacket.HEADER:
                errPacket(context, byteBuf);
                break;
            default:
                commandPacket(context, byteBuf);
        }
    }
    
    private void okPacket(final ChannelHandlerContext context, final ByteBuf byteBuf) {
        Channel backendChannel = context.channel();
        try (MySQLPacketPayload payload = new MySQLPacketPayload(byteBuf)) {
            MySQLQueryResult mysqlQueryResult = new MySQLQueryResult();
            mysqlQueryResult.setGenericResponse(new OKPacket(payload));
            backendChannelResultMap.put(ChannelUtils.getLongTextId(backendChannel), mysqlQueryResult);
            setResponse(context, mysqlQueryResult);
        } finally {
            release(backendChannel);
        }
    }
    
    private void errPacket(final ChannelHandlerContext context, final ByteBuf byteBuf) {
        Channel backendChannel = context.channel();
        try (MySQLPacketPayload payload = new MySQLPacketPayload(byteBuf)) {
            MySQLQueryResult mysqlQueryResult = new MySQLQueryResult();
            mysqlQueryResult.setGenericResponse(new ErrPacket(payload));
            backendChannelResultMap.put(ChannelUtils.getLongTextId(backendChannel), mysqlQueryResult);
            setResponse(context, mysqlQueryResult);
        } finally {
            release(backendChannel);
        }
    }
    
    private void eofPacket(final ChannelHandlerContext context, final ByteBuf byteBuf) {
        Channel backendChannel = context.channel();
        MySQLQueryResult mysqlQueryResult = backendChannelResultMap.get(ChannelUtils.getLongTextId(backendChannel));
        MySQLPacketPayload payload = new MySQLPacketPayload(byteBuf);
        if (mysqlQueryResult.isColumnFinished()) {
            mysqlQueryResult.setRowFinished(new EofPacket(payload));
            try {
                payload.close();
                triggerFinish(context);
            } finally {
                release(backendChannel);
            }
        } else {
            mysqlQueryResult.setColumnFinished(new EofPacket(payload));
            setResponse(context, mysqlQueryResult);
        }
    }
    
    private void setResponse(final ChannelHandlerContext context, final MySQLQueryResult queryResult) {
        final QueryResultCollector queryResultCollector = RUNTIME_CONTEXT.getBackendChannelQueryResultCollector().get(ChannelUtils.getLongTextId(context.channel()));
        if (queryResultCollector == null) {
            return;
        }
        queryResultCollector.setResponse(queryResult);
        triggerFinish(context);
    }
    
    private void triggerFinish(final ChannelHandlerContext context) {
        final QueryResultCollector queryResultCollector = RUNTIME_CONTEXT.getBackendChannelQueryResultCollector().get(ChannelUtils.getLongTextId(context.channel()));
        if (!queryResultCollector.isDone() || queryResultCollector.isBackendChannelExhausted()) {
            return;
        }
        if (queryResultCollector.getSqlStatement().getType() == SQLType.DQL) {
            UserExecutorGroup.getInstance().getExecutorService().execute(new QueryResultExecutor(queryResultCollector));
        } else {
            context.channel().eventLoop().execute(new QueryResultExecutor(queryResultCollector));
        }
    }
    
    private void commandPacket(final ChannelHandlerContext context, final ByteBuf byteBuf) {
        MySQLQueryResult mysqlQueryResult = backendChannelResultMap.get(ChannelUtils.getLongTextId(context.channel()));
        MySQLPacketPayload payload = new MySQLPacketPayload(byteBuf);
        if (null == mysqlQueryResult) {
            mysqlQueryResult = new MySQLQueryResult(payload);
            backendChannelResultMap.put(ChannelUtils.getLongTextId(context.channel()), mysqlQueryResult);
        } else if (mysqlQueryResult.needColumnDefinition()) {
            mysqlQueryResult.addColumnDefinition(new ColumnDefinition41Packet(payload));
        } else {
            mysqlQueryResult.addTextResultSetRow(new TextResultSetRowPacket(payload, mysqlQueryResult.getColumnCount()));
        }
    }
    
    private void release(final Channel backendChannel) {
        String backendLongTextId = ChannelUtils.getLongTextId(backendChannel);
        backendChannelResultMap.remove(backendLongTextId);
        RUNTIME_CONTEXT.getBackendChannelQueryResultCollector().remove(backendLongTextId);
        EventLoop eventLoop = backendChannel.eventLoop();
        ChannelPoolMap<String, SimpleChannelPool> channelPoolMap = BackendNettyClientManager.getInstance().getBackendNettyClient(logicSchemaName).getEventLoopChannelPoolMap()
                .get(eventLoop);
        if (channelPoolMap == null) {
            return;
        }
        ChannelPool channelPool = channelPoolMap.get(dataSourceName);
        channelPool.release(backendChannel);
    }
}
