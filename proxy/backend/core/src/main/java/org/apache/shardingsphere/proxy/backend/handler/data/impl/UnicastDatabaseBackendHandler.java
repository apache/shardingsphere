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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnectorFactory;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnector;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.StorageUnitNotExistedException;
import org.apache.shardingsphere.proxy.backend.handler.data.DatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Database backend handler with unicast schema.
 */
@RequiredArgsConstructor
public final class UnicastDatabaseBackendHandler implements DatabaseBackendHandler {
    
    private final DatabaseConnectorFactory databaseConnectorFactory = DatabaseConnectorFactory.getInstance();
    
    private final QueryContext queryContext;
    
    private final ConnectionSession connectionSession;
    
    private DatabaseConnector databaseConnector;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        String originDatabase = connectionSession.getDefaultDatabaseName();
        String databaseName = null == originDatabase ? getFirstDatabaseName() : originDatabase;
        ShardingSpherePreconditions.checkState(ProxyContext.getInstance().getDatabase(databaseName).containsDataSource(), () -> new StorageUnitNotExistedException(databaseName));
        try {
            connectionSession.setCurrentDatabase(databaseName);
            databaseConnector = databaseConnectorFactory.newInstance(queryContext, connectionSession.getDatabaseConnectionManager(), false);
            return databaseConnector.execute();
        } finally {
            connectionSession.setCurrentDatabase(databaseName);
        }
    }
    
    private String getFirstDatabaseName() {
        Collection<String> databaseNames = ProxyContext.getInstance().getAllDatabaseNames();
        if (databaseNames.isEmpty()) {
            throw new NoDatabaseSelectedException();
        }
        AuthorityRule authorityRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        Optional<ShardingSpherePrivileges> privileges = authorityRule.findPrivileges(connectionSession.getGrantee());
        Stream<String> databaseStream = databaseNames.stream().filter(each -> ProxyContext.getInstance().getDatabase(each).containsDataSource());
        Optional<String> result = privileges.map(optional -> databaseStream.filter(optional::hasPrivileges).findFirst()).orElseGet(databaseStream::findFirst);
        ShardingSpherePreconditions.checkState(result.isPresent(), StorageUnitNotExistedException::new);
        return result.get();
    }
    
    @Override
    public boolean next() throws SQLException {
        return databaseConnector.next();
    }
    
    @Override
    public QueryResponseRow getRowData() throws SQLException {
        return databaseConnector.getRowData();
    }
    
    @Override
    public void close() throws SQLException {
        if (null != databaseConnector) {
            databaseConnector.close();
        }
    }
}
