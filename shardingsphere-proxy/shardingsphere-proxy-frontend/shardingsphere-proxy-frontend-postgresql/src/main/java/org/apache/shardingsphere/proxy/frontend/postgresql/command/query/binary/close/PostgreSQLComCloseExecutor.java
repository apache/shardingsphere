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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.close;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.close.PostgreSQLCloseCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.close.PostgreSQLComClosePacket;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * Command close executor for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLComCloseExecutor implements CommandExecutor {
    
    private final PostgreSQLConnectionContext connectionContext;
    
    private final PostgreSQLComClosePacket packet;
    
    private final BackendConnection backendConnection;
    
    @Override
    public Collection<DatabasePacket<?>> execute() throws SQLException {
        switch (packet.getType()) {
            case PREPARED_STATEMENT:
                PostgreSQLBinaryStatementRegistry.getInstance().unregister(backendConnection.getConnectionId(), packet.getName());
                break;
            case PORTAL:
                closePortal();
                break;
            default:
                throw new UnsupportedOperationException(packet.getType().name());
        }
        return Collections.singletonList(new PostgreSQLCloseCompletePacket());
    }
    
    private void closePortal() throws SQLException {
        connectionContext.closePortal(packet.getName());
    }
}
