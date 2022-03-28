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
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.data.DatabaseBackendHandler;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Database backend handler with assigned schema.
 */
@RequiredArgsConstructor
public final class SchemaAssignedDatabaseBackendHandler implements DatabaseBackendHandler {
    
    private final DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory = DatabaseCommunicationEngineFactory.getInstance();
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final String sql;
    
    private final ConnectionSession connectionSession;
    
    private DatabaseCommunicationEngine<?> databaseCommunicationEngine;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        prepareDatabaseCommunicationEngine();
        return (ResponseHeader) databaseCommunicationEngine.execute();
    }
    
    @Override
    public Future<ResponseHeader> executeFuture() {
        try {
            prepareDatabaseCommunicationEngine();
            return (Future<ResponseHeader>) databaseCommunicationEngine.execute();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            return Future.failedFuture(ex);
        }
    }
    
    private void prepareDatabaseCommunicationEngine() throws RequiredResourceMissedException {
        boolean isSystemSchema = sqlStatementContext.getDatabaseType().containsSystemSchema(connectionSession.getSchemaName());
        if (!isSystemSchema && !ProxyContext.getInstance().getMetaData(connectionSession.getSchemaName()).hasDataSource()) {
            throw new RequiredResourceMissedException(connectionSession.getSchemaName());
        }
        if (!isSystemSchema && !ProxyContext.getInstance().getMetaData(connectionSession.getSchemaName()).isComplete()) {
            throw new RuleNotExistedException();
        }
        databaseCommunicationEngine = databaseCommunicationEngineFactory.newTextProtocolInstance(sqlStatementContext, sql, connectionSession.getBackendConnection());
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
