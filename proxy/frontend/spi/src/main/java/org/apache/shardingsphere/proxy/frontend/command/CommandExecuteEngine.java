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

package org.apache.shardingsphere.proxy.frontend.command;

import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacketType;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Command execute engine.
 */
public interface CommandExecuteEngine {
    
    /**
     * Get command packet type.
     *
     * @param packetPayload packet payload
     * @return command packet type
     */
    CommandPacketType getCommandPacketType(PacketPayload packetPayload);
    
    /**
     * Get command packet.
     *
     * @param payload packet payload
     * @param type command packet type
     * @param connectionSession connection session
     * @return command packet
     */
    CommandPacket getCommandPacket(PacketPayload payload, CommandPacketType type, ConnectionSession connectionSession);
    
    /**
     * Get command executor.
     *
     * @param type command packet type
     * @param packet command packet
     * @param connectionSession connection session
     * @return command executor
     * @throws SQLException SQL exception
     */
    CommandExecutor getCommandExecutor(CommandPacketType type, CommandPacket packet, ConnectionSession connectionSession) throws SQLException;
    
    /**
     * Get error packet.
     *
     * @param cause cause of error
     * @return error packet
     */
    DatabasePacket getErrorPacket(Exception cause);
    
    /**
     * Get other packet.
     *
     * @param connectionSession connection session
     * @return other packet
     */
    default Optional<DatabasePacket> getOtherPacket(final ConnectionSession connectionSession) {
        return Optional.empty();
    }
    
    /**
     * Write query data.
     *
     * @param context channel handler context
     * @param databaseConnectionManager database connection manager
     * @param queryCommandExecutor query command executor
     * @param headerPackagesCount count of header packages
     * @throws SQLException SQL exception
     */
    void writeQueryData(ChannelHandlerContext context,
                        ProxyDatabaseConnectionManager databaseConnectionManager, QueryCommandExecutor queryCommandExecutor, int headerPackagesCount) throws SQLException;
}
