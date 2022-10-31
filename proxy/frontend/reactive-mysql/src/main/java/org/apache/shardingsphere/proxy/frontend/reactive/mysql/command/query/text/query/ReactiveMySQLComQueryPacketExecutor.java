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

package org.apache.shardingsphere.proxy.frontend.reactive.mysql.command.query.text.query;

import io.vertx.core.Future;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.MySQLTextResultSetRowPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.mysql.command.ServerStatusFlagCalculator;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.builder.ResponsePacketBuilder;
import org.apache.shardingsphere.proxy.frontend.reactive.command.executor.ReactiveCommandExecutor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Reactive COM_QUERY command packet executor for MySQL.
 */
public final class ReactiveMySQLComQueryPacketExecutor implements ReactiveCommandExecutor {
    
    private final ConnectionSession connectionSession;
    
    private final ProxyBackendHandler proxyBackendHandler;
    
    private final int characterSet;
    
    private ResponseType responseType;
    
    private int currentSequenceId;
    
    public ReactiveMySQLComQueryPacketExecutor(final MySQLComQueryPacket packet, final ConnectionSession connectionSession) throws SQLException {
        this.connectionSession = connectionSession;
        proxyBackendHandler = ProxyBackendHandlerFactory.newInstance(DatabaseTypeFactory.getInstance("MySQL"), packet.getSql(), connectionSession);
        characterSet = connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).get().getId();
    }
    
    @Override
    public Future<Collection<DatabasePacket<?>>> executeFuture() {
        return proxyBackendHandler.executeFuture().compose(responseHeader -> {
            List<DatabasePacket<?>> result = new LinkedList<>(
                    responseHeader instanceof QueryResponseHeader ? processQuery((QueryResponseHeader) responseHeader) : processUpdate((UpdateResponseHeader) responseHeader));
            try {
                if (ResponseType.QUERY == responseType) {
                    while (proxyBackendHandler.next()) {
                        result.add(new MySQLTextResultSetRowPacket(++currentSequenceId, proxyBackendHandler.getRowData().getData()));
                    }
                    result.add(new MySQLEofPacket(++currentSequenceId, ServerStatusFlagCalculator.calculateFor(connectionSession)));
                }
                return Future.succeededFuture(result);
            } catch (final SQLException ex) {
                return Future.failedFuture(ex);
            }
        });
    }
    
    private Collection<DatabasePacket<?>> processQuery(final QueryResponseHeader queryResponseHeader) {
        responseType = ResponseType.QUERY;
        Collection<DatabasePacket<?>> result = ResponsePacketBuilder.buildQueryResponsePackets(queryResponseHeader, characterSet, ServerStatusFlagCalculator.calculateFor(connectionSession));
        currentSequenceId = result.size();
        return result;
    }
    
    private Collection<DatabasePacket<?>> processUpdate(final UpdateResponseHeader updateResponseHeader) {
        responseType = ResponseType.UPDATE;
        return ResponsePacketBuilder.buildUpdateResponsePackets(updateResponseHeader, ServerStatusFlagCalculator.calculateFor(connectionSession));
    }
    
    @Override
    public Future<Void> closeFuture() {
        try {
            proxyBackendHandler.close();
            return Future.succeededFuture();
        } catch (final SQLException ex) {
            return Future.failedFuture(ex);
        }
    }
}
