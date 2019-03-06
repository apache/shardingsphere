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

package org.apache.shardingsphere.shardingproxy.frontend.spi;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.frontend.api.CommandExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.api.FrontendContextConfiguration;
import org.apache.shardingsphere.shardingproxy.frontend.api.QueryCommandExecutor;
import org.apache.shardingsphere.shardingproxy.transport.api.packet.CommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.api.packet.CommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.api.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxy.transport.api.payload.PacketPayload;

import java.sql.SQLException;

/**
 * Database frontend engine.
 * 
 * @author zhangliang 
 */
public interface DatabaseFrontendEngine {
    
    /**
     * Get database type.
     * 
     * @return database type
     */
    String getDatabaseType();
    
    /**
     * Create packet payload.
     *
     * @param message message
     * @return packet payload
     */
    PacketPayload createPacketPayload(ByteBuf message);
    
    /**
     * Get frontend context configuration.
     * 
     * @return frontend context configuration
     */
    FrontendContextConfiguration getContextConfiguration();
    
    /**
     * Handshake.
     * 
     * @param context channel handler context
     * @param backendConnection backend connection
     */
    void handshake(ChannelHandlerContext context, BackendConnection backendConnection);
    
    /**
     * Auth.
     * 
     * @param context channel handler context
     * @param message message
     * @param backendConnection backend connection
     * @return auth finish or not
     */
    boolean auth(ChannelHandlerContext context, ByteBuf message, BackendConnection backendConnection);
    
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
     * @param backendConnection backend connection
     * @return command packet
     * @throws SQLException SQL exception
     */
    CommandPacket getCommandPacket(PacketPayload payload, CommandPacketType type, BackendConnection backendConnection) throws SQLException;
    
    /**
     * Get command executor.
     * 
     * @param type command packet type
     * @param packet command packet
     * @param backendConnection backend connection
     * @return command executor
     */
    CommandExecutor getCommandExecutor(CommandPacketType type, CommandPacket packet, BackendConnection backendConnection);
    
    /**
     * Get error packet.
     * 
     * @param cause cause of error
     * @return error packet
     */
    DatabasePacket getErrorPacket(Exception cause);
    
    /**
     * Write query data.
     * 
     * @param context channel handler context
     * @param backendConnection backend connection
     * @param queryCommandExecutor query command executor
     * @param headerPackagesCount count of header packages
     * @throws SQLException SQL exception
     */
    void writeQueryData(ChannelHandlerContext context, BackendConnection backendConnection, QueryCommandExecutor queryCommandExecutor, int headerPackagesCount) throws SQLException;
    
    /**
     * Release resource.
     * 
     * @param backendConnection backend connection
     */
    void release(BackendConnection backendConnection);
}
