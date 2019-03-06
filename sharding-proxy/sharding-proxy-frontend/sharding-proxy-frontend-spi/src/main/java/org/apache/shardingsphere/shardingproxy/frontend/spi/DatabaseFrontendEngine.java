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
import org.apache.shardingsphere.shardingproxy.frontend.context.FrontendContext;
import org.apache.shardingsphere.shardingproxy.frontend.engine.CommandExecuteEngine;
import org.apache.shardingsphere.shardingproxy.transport.api.payload.PacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.spi.DatabasePacketCodecEngine;

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
     * Get database packet codec engine.
     * 
     * @return database packet codec engine
     */
    DatabasePacketCodecEngine getCodecEngine();
    
    /**
     * Create packet payload.
     *
     * @param message message
     * @return packet payload
     */
    PacketPayload createPacketPayload(ByteBuf message);
    
    /**
     * Get frontend context.
     * 
     * @return frontend context
     */
    FrontendContext getFrontendContext();
    
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
     * Get command execute engine.
     * 
     * @return command execute engine
     */
    CommandExecuteEngine getCommandExecuteEngine();
    
    /**
     * Release resource.
     * 
     * @param backendConnection backend connection
     */
    void release(BackendConnection backendConnection);
}
