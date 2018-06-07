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

package io.shardingsphere.proxy.backend.mysql;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Bytes;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.shardingsphere.proxy.backend.common.CommandResponsePacketsHandler;
import io.shardingsphere.proxy.backend.constant.AuthType;
import io.shardingsphere.proxy.config.DataScourceConfig;
import io.shardingsphere.proxy.transport.mysql.constant.CapabilityFlag;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.constant.ServerInfo;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.TextResultSetRowPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import io.shardingsphere.proxy.transport.mysql.packet.handshake.HandshakeResponse41Packet;
import io.shardingsphere.proxy.util.MySQLResultCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Backend handler.
 *
 * @author wangkai
 * @author linjiaqi
 */
@Slf4j
@RequiredArgsConstructor
public class MySQLBackendHandler extends CommandResponsePacketsHandler {
    private final DataScourceConfig dataScourceConfig;
    
    private AuthType authType = AuthType.UN_AUTH;
    
    private MySQLQueryResult mysqlQueryResult;
    
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        MySQLPacketPayload mysqlPacketPayload = new MySQLPacketPayload((ByteBuf) message);
        int sequenceId = mysqlPacketPayload.readInt1();
        mysqlPacketPayload.getByteBuf().markReaderIndex();
        int header = mysqlPacketPayload.readInt1() & 0xFF;
        
        if (AuthType.UN_AUTH == authType) {
            auth(context, sequenceId, header, mysqlPacketPayload);
        } else if (AuthType.AUTHING == authType) {
            genericResponsePacket(context, sequenceId, header, mysqlPacketPayload);
        } else if (AuthType.AUTH_FAILED == authType) {
            log.error("mysql auth failed, cannot handle channel read message");
        } else {
            if (EofPacket.HEADER == header) {
                endOfFilePacket(context, sequenceId, header, mysqlPacketPayload);
            } else if (OKPacket.HEADER == header || ErrPacket.HEADER == header) {
                genericResponsePacket(context, sequenceId, header, mysqlPacketPayload);
            } else {
                executeCommandResponsePackets(context, sequenceId, header, mysqlPacketPayload);
            }
        }
    }
    
    @Override
    protected void auth(final ChannelHandlerContext context, final int sequenceId, final int header, final MySQLPacketPayload mysqlPacketPayload) {
        try {
            int protocolVersion = header;
            String serverVersion = mysqlPacketPayload.readStringNul();
            int connectionId = mysqlPacketPayload.readInt4();
            MySQLResultCache.getInstance().putConnection(context.channel().id().asShortText(), connectionId);
            byte[] authPluginDataPart1 = mysqlPacketPayload.readStringNul().getBytes();
            int capabilityFlagsLower = mysqlPacketPayload.readInt2();
            int charset = mysqlPacketPayload.readInt1();
            int statusFlag = mysqlPacketPayload.readInt2();
            int capabilityFlagsUpper = mysqlPacketPayload.readInt2();
            int authPluginDataLength = mysqlPacketPayload.readInt1();
            mysqlPacketPayload.skipReserved(10);
            byte[] authPluginDataPart2 = mysqlPacketPayload.readStringNul().getBytes();
            byte[] authPluginData = Bytes.concat(authPluginDataPart1, authPluginDataPart2);
            byte[] authResponse = securePasswordAuthentication(dataScourceConfig.getPassword().getBytes(), authPluginData);
            //TODO maxSizePactet（16MB） should be set.
            HandshakeResponse41Packet handshakeResponse41Packet = new HandshakeResponse41Packet(sequenceId + 1, CapabilityFlag.calculateHandshakeCapabilityFlagsLower(), 16777215, ServerInfo.CHARSET,
                    dataScourceConfig.getUsername(), authResponse, dataScourceConfig.getDatabase());
            context.writeAndFlush(handshakeResponse41Packet);
        } finally {
            authType = AuthType.AUTHING;
            mysqlPacketPayload.getByteBuf().release();
        }
    }
    
    @Override
    protected void genericResponsePacket(final ChannelHandlerContext context, final int sequenceId, final int header, final MySQLPacketPayload mysqlPacketPayload) {
        mysqlQueryResult = new MySQLQueryResult();
        switch (header) {
            case OKPacket.HEADER:
                if (authType == AuthType.AUTHING) {
                    authType = AuthType.AUTH_SUCCESS;
                }
                long affectedRows = mysqlPacketPayload.readIntLenenc();
                long lastInsertId = mysqlPacketPayload.readIntLenenc();
                int statusFlags = mysqlPacketPayload.readInt2();
                int warnings = mysqlPacketPayload.readInt2();
                String info = mysqlPacketPayload.readStringEOF();
                log.debug("OKPacket[affectedRows={},lastInsertId={},statusFlags={},warnings={},info={}]", affectedRows, lastInsertId, statusFlags, warnings, info);
                mysqlQueryResult.setGenericResponse(new OKPacket(sequenceId, affectedRows, lastInsertId, statusFlags, warnings, info));
                break;
            case ErrPacket.HEADER:
                if (authType == AuthType.AUTHING) {
                    authType = AuthType.AUTH_FAILED;
                }
                int errorCode = mysqlPacketPayload.readInt2();
                String sqlStateMarker = mysqlPacketPayload.readStringFix(1);
                String sqlState = mysqlPacketPayload.readStringFix(5);
                String errorMessage = mysqlPacketPayload.readStringEOF();
                log.debug("ErrPacket[errorCode={},sqlStateMarker={},sqlState={},errorMessage={}]", errorCode, sqlStateMarker, sqlState, errorMessage);
                mysqlQueryResult.setGenericResponse(new ErrPacket(sequenceId, errorCode, sqlStateMarker, sqlState, errorMessage));
                break;
            default:
                break;
        }
        
        try {
            int connectionId = MySQLResultCache.getInstance().getConnection(context.channel().id().asShortText());
            if (MySQLResultCache.getInstance().getFuture(connectionId) != null) {
                MySQLResultCache.getInstance().getFuture(connectionId).setResponse(mysqlQueryResult);
            }
        } finally {
            mysqlQueryResult = null;
            mysqlPacketPayload.getByteBuf().release();
        }
    }
    
    @Override
    protected void endOfFilePacket(final ChannelHandlerContext context, final int sequenceId, final int header, final MySQLPacketPayload mysqlPacketPayload) {
        int warnings = mysqlPacketPayload.readInt2();
        int statusFlags = mysqlPacketPayload.readInt2();
        log.debug("EofPacket[warnings={},statusFlags={}]", warnings, statusFlags);
        EofPacket eofPacket = new EofPacket(sequenceId, warnings, statusFlags);
        if (mysqlQueryResult.isColumnFinished()) {
            mysqlQueryResult.setRowFinished(eofPacket);
            mysqlQueryResult = null;
            mysqlPacketPayload.getByteBuf().release();
        } else {
            mysqlQueryResult.setColumnFinished(eofPacket);
            int connectionId = MySQLResultCache.getInstance().getConnection(context.channel().id().asShortText());
            if (MySQLResultCache.getInstance().getFuture(connectionId) != null) {
                MySQLResultCache.getInstance().getFuture(connectionId).setResponse(mysqlQueryResult);
            }
        }
    }
    
    @Override
    protected void executeCommandResponsePackets(final ChannelHandlerContext context, final int sequenceId, final int header, final MySQLPacketPayload mysqlPacketPayload) {
        if (mysqlQueryResult == null) {
            mysqlQueryResult = new MySQLQueryResult(sequenceId, header);
        } else if (mysqlQueryResult.needColumnDefinition()) {
            mysqlPacketPayload.getByteBuf().resetReaderIndex();
            String catalog = mysqlPacketPayload.readStringLenenc();
            String schema = mysqlPacketPayload.readStringLenenc();
            String table = mysqlPacketPayload.readStringLenenc();
            String orgTable = mysqlPacketPayload.readStringLenenc();
            String name = mysqlPacketPayload.readStringLenenc();
            String orgName = mysqlPacketPayload.readStringLenenc();
            // fixed value and charset
            mysqlPacketPayload.skipReserved(1 + 2);
            int columnLength = mysqlPacketPayload.readInt4();
            int columnType = mysqlPacketPayload.readInt1();
            // field flags
            mysqlPacketPayload.skipReserved(2);
            int decimals = mysqlPacketPayload.readInt1();
            // filler value 0x00 0x00
            mysqlPacketPayload.skipReserved(2);
            
            ColumnDefinition41Packet columnDefinition = new ColumnDefinition41Packet(sequenceId, schema, table, orgTable, name, orgName,
                    columnLength, ColumnType.valueOfJDBCType(columnType), decimals);
            mysqlQueryResult.addColumnDefinition(columnDefinition);
        } else {
            mysqlPacketPayload.getByteBuf().resetReaderIndex();
            List<Object> data = new ArrayList<>(mysqlQueryResult.getColumnCount());
            for (int i = 1; i <= mysqlQueryResult.getColumnCount(); i++) {
                data.add(mysqlPacketPayload.readStringLenenc());
            }
            
            TextResultSetRowPacket textResultSetRow = new TextResultSetRowPacket(sequenceId, data);
            mysqlQueryResult.addTextResultSetRow(textResultSetRow);
        }
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        //TODO delete connection map.
        super.channelInactive(ctx);
    }
    
    private byte[] securePasswordAuthentication(final byte[] password, final byte[] authPluginData) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] part1 = sha1.digest(password);
            sha1.reset();
            byte[] part2 = sha1.digest(part1);
            sha1.reset();
            sha1.update(authPluginData);
            byte[] authResponse = sha1.digest(part2);
            for (int i = 0; i < authResponse.length; i++) {
                authResponse[i] = (byte) (authResponse[i] ^ part1[i]);
            }
            return authResponse;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
