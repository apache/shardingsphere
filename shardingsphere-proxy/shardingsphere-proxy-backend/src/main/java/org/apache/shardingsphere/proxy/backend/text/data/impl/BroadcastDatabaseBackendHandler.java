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

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.vertx.VertxDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.data.DatabaseBackendHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Backend handler for broadcast.
 */
@RequiredArgsConstructor
public final class BroadcastDatabaseBackendHandler implements DatabaseBackendHandler {
    
    private final DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory = DatabaseCommunicationEngineFactory.getInstance();
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final String sql;
    
    private final ConnectionSession connectionSession;
    
    @SuppressWarnings("rawtypes")
    @Override
    public Future<ResponseHeader> executeFuture() {
        List<String> databaseNames = getDatabaseNamesWithDataSource().orElseThrow(ResourceNotExistedException::new);
        String originalDatabase = connectionSession.getDatabaseName();
        List<Future> futures = new ArrayList<>(databaseNames.size());
        for (String each : databaseNames) {
            connectionSession.setCurrentDatabase(each);
            futures.add(databaseCommunicationEngineFactory.<VertxDatabaseCommunicationEngine>newTextProtocolInstance(sqlStatementContext, sql, connectionSession.getBackendConnection()).execute());
        }
        return CompositeFuture.all(futures)
                .compose(unused -> Future.succeededFuture((ResponseHeader) new UpdateResponseHeader(sqlStatementContext.getSqlStatement())))
                .eventually(unused -> {
                    connectionSession.setCurrentDatabase(originalDatabase);
                    return Future.succeededFuture();
                });
    }
    
    @Override
    public ResponseHeader execute() throws SQLException {
        List<String> databaseNames = getDatabaseNamesWithDataSource().orElseThrow(ResourceNotExistedException::new);
        String originalDatabase = connectionSession.getDatabaseName();
        try {
            for (String each : databaseNames) {
                connectionSession.setCurrentDatabase(each);
                databaseCommunicationEngineFactory.newTextProtocolInstance(sqlStatementContext, sql, connectionSession.getBackendConnection()).execute();
            }
        } finally {
            connectionSession.setCurrentDatabase(originalDatabase);
        }
        return new UpdateResponseHeader(sqlStatementContext.getSqlStatement());
    }
    
    private Optional<List<String>> getDatabaseNamesWithDataSource() {
        List<String> result = ProxyContext.getInstance().getAllDatabaseNames().stream().filter(each -> ProxyContext.getInstance().getDatabase(each).hasDataSource()).collect(Collectors.toList());
        return Optional.of(result).filter(each -> !each.isEmpty());
    }
}
