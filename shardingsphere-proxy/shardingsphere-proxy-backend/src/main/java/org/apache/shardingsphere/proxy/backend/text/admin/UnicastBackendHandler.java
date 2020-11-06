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

package org.apache.shardingsphere.proxy.backend.text.admin;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistsException;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryData;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Map;

/**
 * Backend handler for unicast.
 */
@RequiredArgsConstructor
public final class UnicastBackendHandler implements TextProtocolBackendHandler {
    
    private final DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory = DatabaseCommunicationEngineFactory.getInstance();
    
    private final String sql;
    
    private final SQLStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Override
    public BackendResponse execute() throws SQLException {
        if (null == backendConnection.getSchemaName()) {
            Map<String, ShardingSphereMetaData> metaDataMap = ProxyContext.getInstance().getSchemaContexts().getMetaDataMap();
            if (metaDataMap.isEmpty()) {
                throw new NoDatabaseSelectedException();
            }
            if (!metaDataMap.values().iterator().next().isComplete()) {
                throw new RuleNotExistsException();
            }
            // TODO we should remove set default ShardingSphere schema after parser can recognize all DAL broadcast SQL.
            backendConnection.setCurrentSchema(metaDataMap.keySet().iterator().next());
        }
        databaseCommunicationEngine = databaseCommunicationEngineFactory.newTextProtocolInstance(sqlStatement, sql, backendConnection);
        return databaseCommunicationEngine.execute();
    }
    
    @Override
    public boolean next() throws SQLException {
        return databaseCommunicationEngine.next();
    }
    
    @Override
    public QueryData getQueryData() throws SQLException {
        return databaseCommunicationEngine.getQueryData();
    }
}
