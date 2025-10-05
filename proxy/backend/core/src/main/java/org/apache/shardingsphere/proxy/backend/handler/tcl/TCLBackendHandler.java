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

package org.apache.shardingsphere.proxy.backend.handler.tcl;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.exception.core.exception.transaction.InTransactionException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.ReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * TCL backend handler.
 */
public final class TCLBackendHandler implements ProxyBackendHandler {
    
    private final TCLStatement tclStatement;
    
    private final TransactionOperationType operationType;
    
    private final ConnectionSession connectionSession;
    
    private final ProxyBackendTransactionManager transactionManager;
    
    private final DialectTransactionOption transactionOption;
    
    private final DialectSchemaOption schemaOption;
    
    public TCLBackendHandler(final TCLStatement tclStatement, final TransactionOperationType operationType, final ConnectionSession connectionSession) {
        this.tclStatement = tclStatement;
        this.operationType = operationType;
        this.connectionSession = connectionSession;
        transactionManager = new ProxyBackendTransactionManager(connectionSession.getDatabaseConnectionManager());
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(connectionSession.getProtocolType()).getDialectDatabaseMetaData();
        transactionOption = dialectDatabaseMetaData.getTransactionOption();
        schemaOption = dialectDatabaseMetaData.getSchemaOption();
    }
    
    @Override
    public ResponseHeader execute() throws SQLException {
        switch (operationType) {
            case BEGIN:
                handleBegin();
                break;
            case COMMIT:
                return handleCommit();
            case ROLLBACK:
                handleRollback();
                break;
            case SET_AUTOCOMMIT:
                handleSetAutoCommit();
                break;
            case SAVEPOINT:
                handleSavepoint();
                break;
            case ROLLBACK_TO_SAVEPOINT:
                handleRollbackToSavepoint();
                break;
            case RELEASE_SAVEPOINT:
                handleReleaseSavepoint();
                break;
            default:
                throw new SQLFeatureNotSupportedException(operationType.name());
        }
        return new UpdateResponseHeader(tclStatement);
    }
    
    private void handleBegin() throws SQLException {
        if (connectionSession.getTransactionStatus().isInTransaction()) {
            if (transactionOption.isSupportAutoCommitInNestedTransaction()) {
                transactionManager.commit();
            } else if (schemaOption.getDefaultSchema().isPresent()) {
                throw new InTransactionException();
            }
        }
        transactionManager.begin();
    }
    
    private UpdateResponseHeader handleCommit() throws SQLException {
        transactionManager.commit();
        SQLStatement sqlStatement = connectionSession.getConnectionContext().getTransactionContext().isExceptionOccur() && transactionOption.isReturnRollbackStatementWhenCommitFailed()
                ? new RollbackStatement(connectionSession.getProtocolType())
                : tclStatement;
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void handleRollback() throws SQLException {
        transactionManager.rollback();
    }
    
    private void handleSetAutoCommit() throws SQLException {
        if (transactionOption.isSupportAutoCommitInNestedTransaction() && connectionSession.getTransactionStatus().isInTransaction()
                && ((SetAutoCommitStatement) tclStatement).isAutoCommit()) {
            transactionManager.commit();
        }
        connectionSession.setAutoCommit(((SetAutoCommitStatement) tclStatement).isAutoCommit());
    }
    
    private void handleSavepoint() throws SQLException {
        ShardingSpherePreconditions.checkState(isValidSavepointStatus(), () -> new SQLFeatureNotSupportedException("SAVEPOINT can only be used in transaction blocks"));
        transactionManager.setSavepoint(((SavepointStatement) tclStatement).getSavepointName());
    }
    
    private void handleRollbackToSavepoint() throws SQLException {
        ShardingSpherePreconditions.checkState(isValidSavepointStatus(), () -> new SQLFeatureNotSupportedException("ROLLBACK TO SAVEPOINT can only be used in transaction blocks"));
        transactionManager.rollbackTo(((RollbackStatement) tclStatement).getSavepointName().orElse(""));
    }
    
    private void handleReleaseSavepoint() throws SQLException {
        ShardingSpherePreconditions.checkState(isValidSavepointStatus(), () -> new SQLFeatureNotSupportedException("RELEASE SAVEPOINT can only be used in transaction blocks"));
        transactionManager.releaseSavepoint(((ReleaseSavepointStatement) tclStatement).getSavepointName());
    }
    
    private boolean isValidSavepointStatus() {
        return connectionSession.getTransactionStatus().isInTransaction() || !schemaOption.getDefaultSchema().isPresent();
    }
}
