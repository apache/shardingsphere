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

package org.apache.shardingsphere.proxy.frontend.mysql.command;

import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.database.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.MySQLCommandPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacketType;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.mysql.err.MySQLErrorPacketFactory;

import java.sql.SQLException;

public final class MySQLCommandExecuteEngine implements CommandExecuteEngine {
    
    @Override
    public MySQLCommandPacketType getCommandPacketType(final PacketPayload payload) {
        return MySQLCommandPacketType.valueOf(((MySQLPacketPayload) payload).readInt1());
    }
    
    @Override
    public MySQLCommandPacket getCommandPacket(
                                               final PacketPayload payload,
                                               final CommandPacketType type,
                                               final ConnectionSession connectionSession) {
        
        return MySQLCommandPacketFactory.newInstance(
                (MySQLCommandPacketType) type,
                (MySQLPacketPayload) payload,
                connectionSession);
    }
    
    @Override
    public CommandExecutor getCommandExecutor(
                                              final CommandPacketType type,
                                              final CommandPacket packet,
                                              final ConnectionSession connectionSession) throws SQLException {
        return MySQLCommandExecutorFactory.newInstance(
                (MySQLCommandPacketType) type,
                packet,
                connectionSession);
    }
    
    @Override
    public MySQLPacket getErrorPacket(final Exception cause) {
        return MySQLErrorPacketFactory.newInstance(cause);
    }
    
    @Override
    public void writeQueryData(
                               final ChannelHandlerContext context,
                               final ProxyDatabaseConnectionManager databaseConnectionManager,
                               final QueryCommandExecutor queryCommandExecutor,
                               final int headerPackagesCount) throws SQLException {
        
        if (ResponseType.QUERY != queryCommandExecutor.getResponseType()
                || !context.channel().isActive()) {
            return;
        }
        
        int count = 0;
        int flushThreshold = ProxyContext.getInstance()
                .getContextManager()
                .getMetaDataContexts()
                .getMetaData()
                .getProps()
                .<Integer>getValue(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD);
        
        while (queryCommandExecutor.next()) {
            count++;
            databaseConnectionManager.getConnectionResourceLock().doAwait(context);
            DatabasePacket row = queryCommandExecutor.getQueryRowPacket();
            context.write(row);
            if (flushThreshold == count) {
                context.flush();
                count = 0;
            }
        }
        
        context.write(new MySQLEofPacket(
                ServerStatusFlagCalculator.calculateFor(
                        databaseConnectionManager.getConnectionSession(), true)));
    }
}
