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

package org.apache.shardingsphere.proxy.frontend.postgresql;

import lombok.Getter;
import org.apache.shardingsphere.db.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.db.protocol.postgresql.codec.PostgreSQLPacketCodecEngine;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLServerInfo;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.dialect.exception.transaction.InTransactionException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.context.FrontendContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.PostgreSQLAuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLPortalContextRegistry;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

/**
 * Frontend engine for PostgreSQL.
 */
@Getter
public final class PostgreSQLFrontendEngine implements DatabaseProtocolFrontendEngine {
    
    private final FrontendContext frontendContext = new PostgreSQLFrontendContext();
    
    private final AuthenticationEngine authenticationEngine = new PostgreSQLAuthenticationEngine();
    
    private final CommandExecuteEngine commandExecuteEngine = new PostgreSQLCommandExecuteEngine();
    
    private final DatabasePacketCodecEngine<PostgreSQLPacket> codecEngine = new PostgreSQLPacketCodecEngine();
    
    @Override
    public void setDatabaseVersion(final String databaseName, final String databaseVersion) {
        PostgreSQLServerInfo.setServerVersion(databaseVersion);
    }
    
    @Override
    public void release(final ConnectionSession connectionSession) {
        PostgreSQLPortalContextRegistry.getInstance().remove(connectionSession.getConnectionId());
    }
    
    @Override
    public void handleException(final ConnectionSession connectionSession, final Exception exception) {
        if (connectionSession.getTransactionStatus().isInTransaction() && !connectionSession.getTransactionStatus().isRollbackOnly() && !(exception instanceof InTransactionException)) {
            connectionSession.getTransactionStatus().setRollbackOnly(true);
        }
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}
