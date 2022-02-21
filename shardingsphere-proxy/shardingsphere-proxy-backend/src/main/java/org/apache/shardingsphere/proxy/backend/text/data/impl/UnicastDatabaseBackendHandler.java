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

import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.data.DatabaseBackendHandler;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Database backend handler with unicast schema.
 */
@RequiredArgsConstructor
public final class UnicastDatabaseBackendHandler implements DatabaseBackendHandler {
    
    private final DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory = DatabaseCommunicationEngineFactory.getInstance();
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final String sql;
    
    private final ConnectionSession connectionSession;
    
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Override
    public Future<ResponseHeader> executeFuture() {
        String originSchema = connectionSession.getSchemaName();
        String schemaName = null == originSchema ? getFirstSchemaName() : originSchema;
        if (!ProxyContext.getInstance().getMetaData(schemaName).hasDataSource()) {
            throw new RuleNotExistedException();
        }
        connectionSession.setCurrentSchema(schemaName);
        databaseCommunicationEngine = databaseCommunicationEngineFactory.newTextProtocolInstance(sqlStatementContext, sql, connectionSession.getBackendConnection());
        return ((Future<ResponseHeader>) databaseCommunicationEngine.execute()).eventually(unused -> {
            connectionSession.setCurrentSchema(originSchema);
            return Future.succeededFuture();
        });
    }
    
    @Override
    public ResponseHeader execute() throws SQLException {
        String originSchema = connectionSession.getDefaultSchemaName();
        String schemaName = null == originSchema ? getFirstSchemaName() : originSchema;
        if (!ProxyContext.getInstance().getMetaData(schemaName).hasDataSource()) {
            throw new RuleNotExistedException();
        }
        try {
            connectionSession.setCurrentSchema(schemaName);
            databaseCommunicationEngine = databaseCommunicationEngineFactory.newTextProtocolInstance(sqlStatementContext, sql, connectionSession.getBackendConnection());
            return (ResponseHeader) databaseCommunicationEngine.execute();
        } finally {
            connectionSession.setCurrentSchema(originSchema);
        }
    }
    
    private String getFirstSchemaName() {
        Collection<String> schemaNames = ProxyContext.getInstance().getAllSchemaNames();
        if (schemaNames.isEmpty()) {
            throw new NoDatabaseSelectedException();
        }
        Optional<String> result = schemaNames.stream().filter(each -> ProxyContext.getInstance().getMetaData(each).hasDataSource()).findFirst();
        if (!result.isPresent()) {
            throw new RuleNotExistedException();
        }
        return result.get();
    }
    
    @Override
    public boolean next() throws SQLException {
        return databaseCommunicationEngine.next();
    }
    
    @Override
    public Collection<Object> getRowData() throws SQLException {
        return databaseCommunicationEngine.getQueryResponseRow().getData();
    }
    
    @Override
    public void close() throws SQLException {
        if (databaseCommunicationEngine instanceof JDBCDatabaseCommunicationEngine) {
            ((JDBCDatabaseCommunicationEngine) databaseCommunicationEngine).close();
        }
    }
}
