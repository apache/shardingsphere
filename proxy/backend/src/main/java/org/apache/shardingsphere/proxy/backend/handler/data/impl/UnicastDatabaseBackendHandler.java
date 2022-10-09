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

package org.apache.shardingsphere.proxy.backend.handler.data.impl;

import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.data.DatabaseBackendHandler;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Database backend handler with unicast schema.
 */
@RequiredArgsConstructor
public final class UnicastDatabaseBackendHandler implements DatabaseBackendHandler {
    
    private final DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory = DatabaseCommunicationEngineFactory.getInstance();
    
    private final QueryContext queryContext;
    
    private final ConnectionSession connectionSession;
    
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Override
    public Future<ResponseHeader> executeFuture() {
        String originDatabase = connectionSession.getDatabaseName();
        String databaseName = null == originDatabase ? getFirstDatabaseName() : originDatabase;
        if (!ProxyContext.getInstance().getDatabase(databaseName).containsDataSource()) {
            throw new RuleNotExistedException();
        }
        connectionSession.setCurrentDatabase(databaseName);
        databaseCommunicationEngine = databaseCommunicationEngineFactory.newDatabaseCommunicationEngine(queryContext, connectionSession.getBackendConnection(), false);
        return databaseCommunicationEngine.executeFuture().eventually(unused -> {
            connectionSession.setCurrentDatabase(databaseName);
            return Future.succeededFuture();
        });
    }
    
    @Override
    public ResponseHeader execute() throws SQLException {
        String originDatabase = connectionSession.getDefaultDatabaseName();
        String databaseName = null == originDatabase ? getFirstDatabaseName() : originDatabase;
        if (!ProxyContext.getInstance().getDatabase(databaseName).containsDataSource()) {
            throw new RuleNotExistedException();
        }
        try {
            connectionSession.setCurrentDatabase(databaseName);
            databaseCommunicationEngine = databaseCommunicationEngineFactory.newDatabaseCommunicationEngine(queryContext, connectionSession.getBackendConnection(), false);
            return databaseCommunicationEngine.execute();
        } finally {
            connectionSession.setCurrentDatabase(databaseName);
        }
    }
    
    private String getFirstDatabaseName() {
        Collection<String> databaseNames = ProxyContext.getInstance().getAllDatabaseNames();
        if (databaseNames.isEmpty()) {
            throw new NoDatabaseSelectedException();
        }
        Optional<String> result = databaseNames.stream().filter(each -> ProxyContext.getInstance().getDatabase(each).containsDataSource()).findFirst();
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
    public QueryResponseRow getRowData() throws SQLException {
        return databaseCommunicationEngine.getRowData();
    }
    
    @Override
    public void close() throws SQLException {
        if (null != databaseCommunicationEngine) {
            databaseCommunicationEngine.close();
        }
    }
}
