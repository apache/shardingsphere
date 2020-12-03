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
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistsException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Backend handler with query.
 */
@RequiredArgsConstructor
public final class QueryBackendHandler implements TextProtocolBackendHandler {
    
    private final DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory = DatabaseCommunicationEngineFactory.getInstance();
    
    private final SQLStatement sqlStatement;

    private final String sql;
    
    private final BackendConnection backendConnection;
    
    private final boolean schemaSelectedRequired;
    
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        if (!schemaSelectedRequired && null == backendConnection.getSchemaName()) {
            // TODO should remove set default ShardingSphere schema after parser can recognize all DAL broadcast SQL.
            backendConnection.setCurrentSchema(getFirstSchemaName());
        }
        if (!ProxyContext.getInstance().getMetaData(backendConnection.getSchemaName()).isComplete()) {
            throw new RuleNotExistsException();
        }
        databaseCommunicationEngine = databaseCommunicationEngineFactory.newTextProtocolInstance(sqlStatement, sql, backendConnection);
        return databaseCommunicationEngine.execute();
    }
    
    private String getFirstSchemaName() {
        Map<String, ShardingSphereMetaData> metaDataMap = ProxyContext.getInstance().getMetaDataContexts().getMetaDataMap();
        if (metaDataMap.isEmpty()) {
            throw new NoDatabaseSelectedException();
        }
        return metaDataMap.keySet().iterator().next();
    }
    
    @Override
    public boolean next() throws SQLException {
        return databaseCommunicationEngine.next();
    }
    
    @Override
    public Collection<Object> getRowData() throws SQLException {
        return databaseCommunicationEngine.getQueryResponseRow().getData();
    }
}
