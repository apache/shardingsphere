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

package org.apache.shardingsphere.proxy.backend.text.transaction;

import io.vertx.core.Future;
import org.apache.shardingsphere.proxy.backend.communication.TransactionManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.JDBCBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.communication.vertx.VertxBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.vertx.transaction.VertxLocalTransactionManager;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.ReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLBeginTransactionStatement;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Do transaction operation.
 */
public final class TransactionBackendHandler implements TextProtocolBackendHandler {
    
    private final TCLStatement tclStatement;
    
    private final TransactionOperationType operationType;
    
    private final TransactionManager backendTransactionManager;

    private final ConnectionSession connectionSession;
    
    public TransactionBackendHandler(final TCLStatement tclStatement, final TransactionOperationType operationType, final ConnectionSession connectionSession) {
        this.tclStatement = tclStatement;
        this.operationType = operationType;
        this.connectionSession = connectionSession;
        if (connectionSession.getBackendConnection() instanceof JDBCBackendConnection) {
            backendTransactionManager = new JDBCBackendTransactionManager((JDBCBackendConnection) connectionSession.getBackendConnection());
        } else {
            backendTransactionManager = new VertxLocalTransactionManager((VertxBackendConnection) connectionSession.getBackendConnection());
        }
    }
    
    @Override
    public Future<ResponseHeader> executeFuture() {
        VertxLocalTransactionManager transactionManager = (VertxLocalTransactionManager) backendTransactionManager;
        Future<Void> future = determineFuture(transactionManager);
        return future.compose(unused -> Future.succeededFuture(new UpdateResponseHeader(tclStatement)));
    }
    
    private Future<Void> determineFuture(final VertxLocalTransactionManager transactionManager) {
        switch (operationType) {
            case BEGIN:
                if (tclStatement instanceof MySQLBeginTransactionStatement && connectionSession.getTransactionStatus().isInTransaction()) {
                    return transactionManager.commit().compose(unused -> transactionManager.begin());
                }
                return transactionManager.begin();
            case SAVEPOINT:
                return transactionManager.setSavepoint(((SavepointStatement) tclStatement).getSavepointName());
            case ROLLBACK_TO_SAVEPOINT:
                return transactionManager.rollbackTo(((RollbackStatement) tclStatement).getSavepointName().get());
            case RELEASE_SAVEPOINT:
                return transactionManager.releaseSavepoint(((ReleaseSavepointStatement) tclStatement).getSavepointName());
            case COMMIT:
                return transactionManager.commit();
            case ROLLBACK:
                return transactionManager.rollback();
            default:
                return Future.failedFuture(new UnsupportedOperationException());
        }
    }
    
    @Override
    public ResponseHeader execute() throws SQLException {
        switch (operationType) {
            case BEGIN:
                if (tclStatement instanceof MySQLBeginTransactionStatement && connectionSession.getTransactionStatus().isInTransaction()) {
                    backendTransactionManager.commit();
                }
                backendTransactionManager.begin();
                break;
            case SAVEPOINT:
                backendTransactionManager.setSavepoint(((SavepointStatement) tclStatement).getSavepointName());
                break;
            case ROLLBACK_TO_SAVEPOINT:
                if (((RollbackStatement) tclStatement).getSavepointName().isPresent()) {
                    backendTransactionManager.rollbackTo(((RollbackStatement) tclStatement).getSavepointName().get());
                    break;
                }
                backendTransactionManager.rollback();
                break;
            case RELEASE_SAVEPOINT:
                backendTransactionManager.releaseSavepoint(((ReleaseSavepointStatement) tclStatement).getSavepointName());
                break;
            case COMMIT:
                backendTransactionManager.commit();
                break;
            case ROLLBACK:
                backendTransactionManager.rollback();
                break;
            default:
                throw new SQLFeatureNotSupportedException(operationType.name());
        }
        return new UpdateResponseHeader(tclStatement);
    }
}
