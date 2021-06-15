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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.sync;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLReadyForQueryPacket;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * Command sync executor for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLComSyncExecutor implements QueryCommandExecutor {
    
    private final PostgreSQLConnectionContext connectionContext;
    
    private final BackendConnection backendConnection;
    
    @Override
    public Collection<DatabasePacket<?>> execute() {
        connectionContext.clearContext();
        return Collections.singleton(new PostgreSQLReadyForQueryPacket(backendConnection.getTransactionStatus().isInTransaction()));
    }
    
    @Override
    public ResponseType getResponseType() {
        return ResponseType.UPDATE;
    }
    
    @Override
    public boolean next() throws SQLException {
        return false;
    }
    
    @Override
    public DatabasePacket<?> getQueryRowPacket() {
        throw new UnsupportedOperationException("PostgreSQLComSyncExecutor returns no query row packet.");
    }
}
