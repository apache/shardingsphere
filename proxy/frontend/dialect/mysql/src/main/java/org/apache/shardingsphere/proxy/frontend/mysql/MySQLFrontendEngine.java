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

package org.apache.shardingsphere.proxy.frontend.mysql;

import io.netty.channel.Channel;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.database.protocol.mysql.codec.MySQLPacketCodecEngine;
import org.apache.shardingsphere.database.protocol.mysql.netty.MySQLSequenceIdInboundHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.mysql.authentication.MySQLAuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.mysql.command.MySQLCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLStatementIdGenerator;
import org.apache.shardingsphere.proxy.frontend.mysql.connection.MySQLConnectionIdRegistry;
import org.apache.shardingsphere.proxy.frontend.netty.FrontendChannelInboundHandler;
import org.apache.shardingsphere.proxy.frontend.netty.FrontendConstants;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

/**
 * Frontend engine for MySQL.
 */
@Getter
public final class MySQLFrontendEngine implements DatabaseProtocolFrontendEngine {
    
    private final AuthenticationEngine authenticationEngine = new MySQLAuthenticationEngine();
    
    private final MySQLCommandExecuteEngine commandExecuteEngine = new MySQLCommandExecuteEngine();
    
    private final DatabasePacketCodecEngine codecEngine = new MySQLPacketCodecEngine();
    
    @Override
    public void initChannel(final Channel channel) {
        channel.pipeline().addBefore(FrontendChannelInboundHandler.class.getSimpleName(), MySQLSequenceIdInboundHandler.class.getSimpleName(), new MySQLSequenceIdInboundHandler(channel));
    }
    
    @Override
    public void release(final ConnectionSession connectionSession) {
        MySQLStatementIdGenerator.getInstance()
                .unregisterConnection(connectionSession.getConnectionId());
        
        Long nativeConnectionId = connectionSession.getAttributeMap()
                .attr(FrontendConstants.NATIVE_CONNECTION_ID_ATTRIBUTE_KEY)
                .get();
        
        if (null != nativeConnectionId) {
            MySQLConnectionIdRegistry.getInstance().unregister(nativeConnectionId);
        }
    }
    
    @Override
    public void handleException(final ConnectionSession connectionSession, final Exception exception) {
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
    
    /**
     * Bind MySQL native connection ID with process ID after authentication.
     *
     * @param channel Netty channel
     * @param session connection session
     */
    @Override
    public void bindProcessAfterAuthentication(final Channel channel, final ConnectionSession session) {
        Long nativeConnectionId = channel
                .attr(FrontendConstants.NATIVE_CONNECTION_ID_ATTRIBUTE_KEY)
                .get();
        
        if (null != nativeConnectionId && null != session.getProcessId()) {
            MySQLConnectionIdRegistry.getInstance()
                    .register(nativeConnectionId, session.getProcessId());
        }
    }
    
}
