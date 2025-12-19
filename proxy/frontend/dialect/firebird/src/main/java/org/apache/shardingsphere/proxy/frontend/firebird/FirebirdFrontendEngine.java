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

import lombok.Getter;
import org.apache.shardingsphere.database.exception.core.exception.transaction.InTransactionException;
import org.apache.shardingsphere.database.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.database.protocol.firebird.codec.FirebirdPacketCodecEngine;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdConnectionProtocolVersion;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.authentication.FirebirdAuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.command.FirebirdCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

/**
 * Frontend engine for Firebird.
 */
@Getter
public final class FirebirdFrontendEngine implements DatabaseProtocolFrontendEngine {
    
    private final AuthenticationEngine authenticationEngine = new FirebirdAuthenticationEngine();
    
    private final FirebirdCommandExecuteEngine commandExecuteEngine = new FirebirdCommandExecuteEngine();
    
    private final DatabasePacketCodecEngine codecEngine = new FirebirdPacketCodecEngine();
    
    @Override
    public void release(final ConnectionSession connectionSession) {
        FirebirdStatementIdGenerator.getInstance().unregisterConnection(connectionSession.getConnectionId());
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(connectionSession.getConnectionId());
        FirebirdConnectionProtocolVersion.getInstance().unsetProtocolVersion(connectionSession.getConnectionId());
        FirebirdFetchStatementCache.getInstance().unregisterConnection(connectionSession.getConnectionId());
    }
    
    @Override
    public void handleException(final ConnectionSession connectionSession, final Exception exception) {
        if (connectionSession.getTransactionStatus().isInTransaction() && !connectionSession.getConnectionContext().getTransactionContext().isExceptionOccur()
                && !(exception instanceof InTransactionException)) {
            connectionSession.getConnectionContext().getTransactionContext().setExceptionOccur(true);
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "Firebird";
    }
}
