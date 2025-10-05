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

package org.apache.shardingsphere.proxy.backend.handler.tcl.type;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Rollback savepoint proxy backend handler.
 */
public final class RollbackSavepointProxyBackendHandler implements ProxyBackendHandler {
    
    private final TCLStatement sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    private final ProxyBackendTransactionManager transactionManager;
    
    private final DialectDatabaseMetaData dialectDatabaseMetaData;
    
    public RollbackSavepointProxyBackendHandler(final TCLStatement sqlStatement, final ConnectionSession connectionSession) {
        this.sqlStatement = sqlStatement;
        this.connectionSession = connectionSession;
        transactionManager = new ProxyBackendTransactionManager(connectionSession.getDatabaseConnectionManager());
        dialectDatabaseMetaData = new DatabaseTypeRegistry(connectionSession.getProtocolType()).getDialectDatabaseMetaData();
    }
    
    @Override
    public ResponseHeader execute() throws SQLException {
        ShardingSpherePreconditions.checkState(isValidSavepointStatus(), () -> new SQLFeatureNotSupportedException("ROLLBACK TO SAVEPOINT can only be used in transaction blocks"));
        transactionManager.rollbackTo(((RollbackStatement) sqlStatement).getSavepointName().orElse(""));
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private boolean isValidSavepointStatus() {
        return connectionSession.getTransactionStatus().isInTransaction() || !dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent();
    }
}
