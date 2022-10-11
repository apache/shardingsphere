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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.bind;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLPreparedStatement;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLBindCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PortalContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.JDBCPortal;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * Command bind executor for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLComBindExecutor implements CommandExecutor {
    
    private final PortalContext portalContext;
    
    private final PostgreSQLComBindPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket<?>> execute() throws SQLException {
        PostgreSQLPreparedStatement preparedStatement = connectionSession.getPreparedStatementRegistry().getPreparedStatement(packet.getStatementId());
        JDBCBackendConnection backendConnection = (JDBCBackendConnection) connectionSession.getBackendConnection();
        JDBCPortal portal = new JDBCPortal(packet.getPortal(), preparedStatement, packet.readParameters(preparedStatement.getParameterTypes()), packet.readResultFormats(), backendConnection);
        portalContext.add(portal);
        portal.bind();
        return Collections.singletonList(PostgreSQLBindCompletePacket.getInstance());
    }
}
