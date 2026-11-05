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

package org.apache.shardingsphere.proxy.frontend.firebird;

import io.netty.channel.Channel;
import lombok.Getter;
import org.apache.shardingsphere.database.exception.core.exception.transaction.InTransactionException;
import org.apache.shardingsphere.database.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.database.protocol.codec.PacketCodec;
import org.apache.shardingsphere.database.protocol.firebird.codec.FirebirdPacketCodecEngine;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.authentication.FirebirdAuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.command.FirebirdCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.command.admin.FirebirdCancellationRegistry;
import org.apache.shardingsphere.proxy.frontend.firebird.netty.FirebirdCancelInboundHandler;
import org.apache.shardingsphere.proxy.frontend.firebird.resource.FirebirdConnectionResourceManager;
import org.apache.shardingsphere.proxy.frontend.netty.ProxyFlowControlHandler;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.firebirdsql.gds.ISCConstants;

import java.sql.SQLException;

/**
 * Frontend engine for Firebird.
 */
@HighFrequencyInvocation
@Getter
public final class FirebirdFrontendEngine implements DatabaseProtocolFrontendEngine {
    
    private final AuthenticationEngine authenticationEngine = new FirebirdAuthenticationEngine();
    
    private final FirebirdCommandExecuteEngine commandExecuteEngine = new FirebirdCommandExecuteEngine();
    
    private final DatabasePacketCodecEngine codecEngine = new FirebirdPacketCodecEngine();
    
    @Override
    @HighFrequencyInvocation
    public void initChannel(final Channel channel) {
        FirebirdCancelInboundHandler cancelHandler = new FirebirdCancelInboundHandler((FirebirdPacketCodecEngine) codecEngine);
        channel.pipeline().addBefore(channel.pipeline().context(PacketCodec.class).name(), "FirebirdCancelRawHandler", cancelHandler.newRawHandler());
        channel.pipeline().addBefore(ProxyFlowControlHandler.class.getSimpleName(), FirebirdCancelInboundHandler.class.getSimpleName(), cancelHandler);
    }
    
    @Override
    public void release(final ConnectionSession connectionSession) {
        FirebirdCancellationRegistry.getInstance().unregister(connectionSession.getConnectionId());
        FirebirdConnectionResourceManager.getInstance().unregisterConnection(connectionSession.getConnectionId());
    }
    
    @Override
    public void handleException(final ConnectionSession connectionSession, final Exception exception) {
        if (connectionSession.getTransactionStatus().isInTransaction() && !connectionSession.getConnectionContext().getTransactionContext().isExceptionOccur()
                && !(exception instanceof InTransactionException) && !isCancelled(exception)) {
            connectionSession.getConnectionContext().getTransactionContext().setExceptionOccur(true);
        }
    }
    
    private boolean isCancelled(final Exception exception) {
        return exception instanceof SQLException && ISCConstants.isc_cancelled == ((SQLException) exception).getErrorCode();
    }
    
    @Override
    public String getDatabaseType() {
        return "Firebird";
    }
}
