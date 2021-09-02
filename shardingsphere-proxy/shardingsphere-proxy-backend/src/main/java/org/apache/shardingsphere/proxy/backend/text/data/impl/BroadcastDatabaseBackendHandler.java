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

package org.apache.shardingsphere.proxy.backend.text.data.impl;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.data.DatabaseBackendHandler;

import java.sql.SQLException;

/**
 * Backend handler for broadcast.
 */
@RequiredArgsConstructor
public final class BroadcastDatabaseBackendHandler implements DatabaseBackendHandler {
    
    private final DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory = DatabaseCommunicationEngineFactory.getInstance();
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        String originalSchema = backendConnection.getSchemaName();
        try {
            for (String each : ProxyContext.getInstance().getAllSchemaNames()) {
                if (!ProxyContext.getInstance().getMetaData(each).hasDataSource()) {
                    continue;
                }
                backendConnection.setCurrentSchema(each);
                databaseCommunicationEngineFactory.newTextProtocolInstance(sqlStatementContext, sql, backendConnection).execute();
            }
        } finally {
            backendConnection.setCurrentSchema(originalSchema);
        }
        return new UpdateResponseHeader(sqlStatementContext.getSqlStatement());
    }
}
