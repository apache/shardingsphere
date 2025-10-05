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

package org.apache.shardingsphere.proxy.backend.handler.tcl.local.type;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.TransactionUtils;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionAccessType;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.transaction.exception.SwitchTypeInTransactionException;

/**
 * Set transaction proxy backend handler.
 */
public final class SetTransactionProxyBackendHandler implements ProxyBackendHandler {
    
    private final SetTransactionStatement sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    private final DialectDatabaseMetaData dialectDatabaseMetaData;
    
    public SetTransactionProxyBackendHandler(final SetTransactionStatement sqlStatement, final ConnectionSession connectionSession) {
        this.sqlStatement = sqlStatement;
        this.connectionSession = connectionSession;
        dialectDatabaseMetaData = new DatabaseTypeRegistry(connectionSession.getProtocolType()).getDialectDatabaseMetaData();
    }
    
    @Override
    public ResponseHeader execute() {
        ShardingSpherePreconditions.checkState(sqlStatement.containsScope() || !connectionSession.getTransactionStatus().isInTransaction(), SwitchTypeInTransactionException::new);
        setReadOnly();
        setTransactionIsolationLevel();
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void setReadOnly() {
        if (sqlStatement.isDesiredAccessMode(TransactionAccessType.READ_ONLY)) {
            connectionSession.setReadOnly(true);
        } else if (sqlStatement.isDesiredAccessMode(TransactionAccessType.READ_WRITE)) {
            connectionSession.setReadOnly(false);
        }
    }
    
    private void setTransactionIsolationLevel() {
        if (!sqlStatement.getIsolationLevel().isPresent()) {
            return;
        }
        connectionSession.setDefaultIsolationLevel(TransactionUtils.getTransactionIsolationLevel(dialectDatabaseMetaData.getTransactionOption().getDefaultIsolationLevel()));
        connectionSession.setIsolationLevel(sqlStatement.getIsolationLevel().get());
    }
}
