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
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLErrorCode;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.ConnectionScopeBinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.close.PostgreSQLCloseCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.close.PostgreSQLComClosePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * Command close executor for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLComCloseExecutor implements CommandExecutor {
    
    private final PostgreSQLComClosePacket packet;
    
    private final BackendConnection backendConnection;
    
    @Override
    public Collection<DatabasePacket<?>> execute() throws SQLException {
        switch (packet.getType()) {
            case PREPARED_STATEMENT:
                return closePreparedStatement();
            case PORTAL:
                return closePortal();
            default:
                throw new UnsupportedOperationException(packet.getType().name());
        }
    }
    
    private Collection<DatabasePacket<?>> closePreparedStatement() {
        ConnectionScopeBinaryStatementRegistry binaryStatementRegistry = PostgreSQLBinaryStatementRegistry.getInstance().get(backendConnection.getConnectionId());
        if (null != binaryStatementRegistry) {
            binaryStatementRegistry.closeStatement(packet.getName());
        }
        return Collections.singletonList(new PostgreSQLCloseCompletePacket());
    }
    
    private Collection<DatabasePacket<?>> closePortal() {
        PostgreSQLErrorResponsePacket packet = PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.ERROR, PostgreSQLErrorCode.FEATURE_NOT_SUPPORTED,
                "Not implemented: Close portal").build();
        return Collections.singletonList(packet);
    }
}
