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

package org.apache.shardingsphere.proxy.backend.text.query;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.context.SchemaContext;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryData;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;

/**
 * Backend handler with query.
 */
@RequiredArgsConstructor
public final class QueryBackendHandler implements TextProtocolBackendHandler {
    
    private final DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory = DatabaseCommunicationEngineFactory.getInstance();
    
    private final String sql;
    
    private final SQLStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Override
    public BackendResponse execute() throws SQLException {
        SchemaContext context = ProxyContext.getInstance().getSchema(backendConnection.getSchemaName());
        if (null == context) {
            throw new NoDatabaseSelectedException();
        }
        if (!context.isComplete()) {
            return getDefaultQueryResponse(backendConnection.getSchemaName());
        }
        databaseCommunicationEngine = databaseCommunicationEngineFactory.newTextProtocolInstance(sqlStatement, sql, backendConnection);
        return databaseCommunicationEngine.execute();
    }
    
    private QueryResponse getDefaultQueryResponse(final String schemaName) {
        return new QueryResponse(Collections.singletonList(new QueryHeader(schemaName, "", "", "", 255, Types.VARCHAR, 0, false, false, false, false)));
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != databaseCommunicationEngine && databaseCommunicationEngine.next();
    }
    
    @Override
    public QueryData getQueryData() throws SQLException {
        return databaseCommunicationEngine.getQueryData();
    }
}
