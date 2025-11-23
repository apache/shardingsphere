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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.free;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFreeStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;

import java.util.Collection;
import java.util.Collections;

/**
 * Firebird free statement command executor.
 */
@RequiredArgsConstructor
public final class FirebirdFreeStatementCommandExecutor implements CommandExecutor {
    
    private final FirebirdFreeStatementPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() {
        switch (packet.getOption()) {
            case FirebirdFreeStatementPacket.DROP:
            case FirebirdFreeStatementPacket.UNPREPARE:
                connectionSession.getServerPreparedStatementRegistry().removePreparedStatement(packet.getStatementId());
                break;
            case FirebirdFreeStatementPacket.CLOSE:
                connectionSession.getConnectionContext().clearCursorContext();
                ProxyBackendHandler proxyBackendHandler = FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(connectionSession.getConnectionId(), packet.getStatementId());
                connectionSession.getDatabaseConnectionManager().unmarkResourceInUse(proxyBackendHandler);
                FirebirdFetchStatementCache.getInstance().unregisterStatement(connectionSession.getConnectionId(), packet.getStatementId());
                break;
            default:
                throw new FirebirdProtocolException("Unknown DSQL option type %d", packet.getOption());
        }
        return Collections.singleton(new FirebirdGenericResponsePacket());
    }
}
