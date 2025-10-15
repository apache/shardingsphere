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

package org.apache.shardingsphere.proxy.backend.handler.data.type;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnectorFactory;
import org.apache.shardingsphere.proxy.backend.handler.data.DatabaseProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Unicast database proxy backend handler.
 */
@RequiredArgsConstructor
public final class UnicastDatabaseProxyBackendHandler implements DatabaseProxyBackendHandler {
    
    private final QueryContext queryContext;
    
    private final ContextManager contextManager;
    
    private final ConnectionSession connectionSession;
    
    private DatabaseProxyConnector databaseProxyConnector;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        String originalDatabaseName = connectionSession.getCurrentDatabaseName();
        String unicastDatabaseName = null == originalDatabaseName ? getFirstDatabaseName() : originalDatabaseName;
        ShardingSpherePreconditions.checkState(contextManager.getDatabase(unicastDatabaseName).containsDataSource(), () -> new EmptyStorageUnitException(unicastDatabaseName));
        try {
            connectionSession.setCurrentDatabaseName(unicastDatabaseName);
            databaseProxyConnector = DatabaseProxyConnectorFactory.newInstance(queryContext, connectionSession.getDatabaseConnectionManager(), false);
            return databaseProxyConnector.execute();
        } finally {
            connectionSession.setCurrentDatabaseName(originalDatabaseName);
        }
    }
    
    private String getFirstDatabaseName() {
        Collection<String> databaseNames = contextManager.getAllDatabaseNames();
        ShardingSpherePreconditions.checkNotEmpty(databaseNames, NoDatabaseSelectedException::new);
        AuthorityRule authorityRule = queryContext.getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        Optional<ShardingSpherePrivileges> privileges = authorityRule.findPrivileges(connectionSession.getConnectionContext().getGrantee());
        Stream<String> storageUnitContainedDatabaseNames = databaseNames.stream().filter(each -> contextManager.getDatabase(each).containsDataSource());
        Optional<String> result = privileges.map(optional -> storageUnitContainedDatabaseNames.filter(optional::hasPrivileges).findFirst()).orElseGet(storageUnitContainedDatabaseNames::findFirst);
        ShardingSpherePreconditions.checkState(result.isPresent(), EmptyStorageUnitException::new);
        return result.get();
    }
    
    @Override
    public boolean next() throws SQLException {
        return databaseProxyConnector.next();
    }
    
    @Override
    public QueryResponseRow getRowData() throws SQLException {
        return databaseProxyConnector.getRowData();
    }
    
    @Override
    public void close() throws SQLException {
        if (null != databaseProxyConnector) {
            databaseProxyConnector.close();
        }
    }
}
