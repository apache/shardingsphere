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

package io.shardingsphere.proxy.backend.netty.mysql;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.shardingsphere.proxy.backend.netty.CommandResponsePacketsHandler;
import io.shardingsphere.proxy.backend.constant.AuthType;
import io.shardingsphere.proxy.config.DataSourceConfig;
import io.shardingsphere.proxy.transport.mysql.constant.CapabilityFlag;
import io.shardingsphere.proxy.transport.mysql.constant.PacketHeader;
import io.shardingsphere.proxy.transport.mysql.constant.ServerInfo;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.ColumnDefinition41Packet;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.text.TextResultSetRowPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import io.shardingsphere.proxy.transport.mysql.packet.handshake.HandshakePacket;
import io.shardingsphere.proxy.transport.mysql.packet.handshake.HandshakeResponse41Packet;
import io.shardingsphere.proxy.util.MySQLResultCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Backend handler for MySQL.
 *
 * @author wangkai
 * @author linjiaqi
 */
@Slf4j
@RequiredArgsConstructor
public class MySQLBackendHandler extends CommandResponsePacketsHandler {
    
    private final DataSourceConfig dataSourceConfig;
    
    private final Map<Integer, MySQLQueryResult> resultMap = Maps.newHashMap();
    
    private AuthType authType = AuthType.UN_AUTH;
    
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        MySQLPacketPayload payload = new MySQLPacketPayload((ByteBuf) message);
        payload.getByteBuf().markReaderIndex();
        payload.readInt1();
        int header = payload.readInt1();
        payload.getByteBuf().resetReaderIndex();
        if (AuthType.UN_AUTH == authType) {
            auth(context, payload);
            authType = AuthType.AUTHING;
        } else if (AuthType.AUTHING == authType) {
            if (PacketHeader.OK.getValue() == header) {
                okPacket(context, payload);
                authType = AuthType.AUTH_SUCCESS;
            } else {
                errPacket(context, payload);
                authType = AuthType.AUTH_FAILED;
            }
        } else if (AuthType.AUTH_FAILED == authType) {
            log.error("mysql auth failed, cannot handle channel read message");
        } else {
            if (PacketHeader.EOF.getValue() == header) {
                eofPacket(context, payload);
            } else if (PacketHeader.OK.getValue() == header) {
                okPacket(context, payload);
            } else if (PacketHeader.ERR.getValue() == header) {
                errPacket(context, payload);
            } else {
                commonPacket(context, payload);
            }
        }
    }
    
    @Override
    protected void auth(final ChannelHandlerContext context, final MySQLPacketPayload payload) {
        try {
            HandshakePacket handshakePacket = new HandshakePacket(payload);
            int capabilityFlags = CapabilityFlag.calculateHandshakeCapabilityFlagsLower();
            byte[] authResponse = securePasswordAuthentication(dataSourceConfig.getPassword().getBytes(), handshakePacket.getAuthPluginData().getAuthPluginData());
            HandshakeResponse41Packet handshakeResponse41Packet = new HandshakeResponse41Packet(handshakePacket.getSequenceId() + 1, capabilityFlags, 16777215, 
                    ServerInfo.CHARSET, dataSourceConfig.getUsername(), authResponse, dataSourceConfig.getDatabase());
            MySQLResultCache.getInstance().putConnection(context.channel().id().asShortText(), handshakePacket.getConnectionId());
            context.writeAndFlush(handshakeResponse41Packet);
        } finally {
            payload.close();
        }
    }
    
    @Override
    protected void okPacket(final ChannelHandlerContext context, final MySQLPacketPayload payload) {
        int connectionId = MySQLResultCache.getInstance().getConnection(context.channel().id().asShortText());
        try {
            MySQLQueryResult mysqlQueryResult = new MySQLQueryResult();
            mysqlQueryResult.setGenericResponse(new OKPacket(payload));
            resultMap.put(connectionId, mysqlQueryResult);
            setResponse(context);
        } finally {
            resultMap.remove(connectionId);
            payload.close();
        }
    }
    
    @Override
    protected void errPacket(final ChannelHandlerContext context, final MySQLPacketPayload payload) {
        int connectionId = MySQLResultCache.getInstance().getConnection(context.channel().id().asShortText());
        try {
            MySQLQueryResult mysqlQueryResult = new MySQLQueryResult();
            mysqlQueryResult.setGenericResponse(new ErrPacket(payload));
            resultMap.put(connectionId, mysqlQueryResult);
            setResponse(context);
        } finally {
            resultMap.remove(connectionId);
            payload.close();
        }
    }
    
    @Override
    protected void eofPacket(final ChannelHandlerContext context, final MySQLPacketPayload payload) {
        int connectionId = MySQLResultCache.getInstance().getConnection(context.channel().id().asShortText());
        MySQLQueryResult mysqlQueryResult = resultMap.get(connectionId);
        if (mysqlQueryResult.isColumnFinished()) {
            mysqlQueryResult.setRowFinished(new EofPacket(payload));
            resultMap.remove(connectionId);
            payload.close();
        } else {
            mysqlQueryResult.setColumnFinished(new EofPacket(payload));
            setResponse(context);
        }
    }
    
    @Override
    protected void commonPacket(final ChannelHandlerContext context, final MySQLPacketPayload payload) {
        int connectionId = MySQLResultCache.getInstance().getConnection(context.channel().id().asShortText());
        MySQLQueryResult mysqlQueryResult = resultMap.get(connectionId);
        if (mysqlQueryResult == null) {
            mysqlQueryResult = new MySQLQueryResult(payload);
            resultMap.put(connectionId, mysqlQueryResult);
        } else if (mysqlQueryResult.needColumnDefinition()) {
            mysqlQueryResult.addColumnDefinition(new ColumnDefinition41Packet(payload));
        } else {
            mysqlQueryResult.addTextResultSetRow(new TextResultSetRowPacket(payload, mysqlQueryResult.getColumnCount()));
        }
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        //TODO delete connection map.
        super.channelInactive(ctx);
    }
    
    private byte[] securePasswordAuthentication(final byte[] password, final byte[] authPluginData) {
        try {
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
        } catch (final NoSuchAlgorithmException ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }
    
    private void setResponse(final ChannelHandlerContext context) {
        int connectionId = MySQLResultCache.getInstance().getConnection(context.channel().id().asShortText());
        if (MySQLResultCache.getInstance().getFuture(connectionId) != null) {
            MySQLResultCache.getInstance().getFuture(connectionId).setResponse(resultMap.get(connectionId));
        }
    }
}
