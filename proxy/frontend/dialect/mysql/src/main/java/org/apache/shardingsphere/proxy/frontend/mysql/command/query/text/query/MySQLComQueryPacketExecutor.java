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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.query;

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.MySQLTextResultSetRowPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.MultiStatementsUpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.mysql.command.ServerStatusFlagCalculator;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.builder.ResponsePacketBuilder;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * COM_QUERY command packet executor for MySQL.
 */
public final class MySQLComQueryPacketExecutor implements QueryCommandExecutor {
    
    private final ConnectionSession connectionSession;
    
    private final ProxyBackendHandler proxyBackendHandler;
    
    private final int characterSet;
    
    @Getter
    private volatile ResponseType responseType;
    
    public MySQLComQueryPacketExecutor(final MySQLComQueryPacket packet, final ConnectionSession connectionSession) throws SQLException {
        this.connectionSession = connectionSession;
        proxyBackendHandler = MySQLComQueryBackendHandlerFactory.newInstance(packet, connectionSession);
        characterSet = connectionSession.getAttributeMap().attr(MySQLConstants.CHARACTER_SET_ATTRIBUTE_KEY).get().getId();
    }
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        ResponseHeader responseHeader = proxyBackendHandler.execute();
        if (responseHeader instanceof QueryResponseHeader) {
            return processQuery((QueryResponseHeader) responseHeader);
        }
        responseType = ResponseType.UPDATE;
        if (responseHeader instanceof MultiStatementsUpdateResponseHeader) {
            return processMultiStatementsUpdate((MultiStatementsUpdateResponseHeader) responseHeader);
        }
        return processUpdate((UpdateResponseHeader) responseHeader);
    }
    
    private Collection<DatabasePacket> processQuery(final QueryResponseHeader queryResponseHeader) {
        responseType = ResponseType.QUERY;
        return ResponsePacketBuilder.buildQueryResponsePackets(queryResponseHeader, characterSet, ServerStatusFlagCalculator.calculateFor(connectionSession, true));
    }
    
    private Collection<DatabasePacket> processUpdate(final UpdateResponseHeader updateResponseHeader) {
        return ResponsePacketBuilder.buildUpdateResponsePackets(updateResponseHeader, ServerStatusFlagCalculator.calculateFor(connectionSession, true));
    }
    
    private Collection<DatabasePacket> processMultiStatementsUpdate(final MultiStatementsUpdateResponseHeader responseHeader) {
        Collection<DatabasePacket> result = new LinkedList<>();
        int index = 0;
        for (UpdateResponseHeader each : responseHeader.getUpdateResponseHeaders()) {
            boolean lastPacket = ++index == responseHeader.getUpdateResponseHeaders().size();
            result.addAll(ResponsePacketBuilder.buildUpdateResponsePackets(each, ServerStatusFlagCalculator.calculateFor(connectionSession, lastPacket)));
        }
        return result;
    }
    
    @Override
    public boolean next() throws SQLException {
        return proxyBackendHandler.next();
    }
    
    @Override
    public MySQLPacket getQueryRowPacket() throws SQLException {
        return new MySQLTextResultSetRowPacket(proxyBackendHandler.getRowData().getData());
    }
    
    @Override
    public void close() throws SQLException {
        proxyBackendHandler.close();
    }
}
