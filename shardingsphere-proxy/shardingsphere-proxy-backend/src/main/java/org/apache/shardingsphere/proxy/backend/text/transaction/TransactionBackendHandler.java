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

import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.BackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.ReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackToSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Do transaction operation.
 */
public final class TransactionBackendHandler implements TextProtocolBackendHandler {
    
    private final TCLStatement tclStatement;
    
    private final TransactionOperationType operationType;
    
    private final BackendTransactionManager backendTransactionManager;
    
    public TransactionBackendHandler(final TCLStatement tclStatement, final TransactionOperationType operationType, final BackendConnection backendConnection) {
        this.tclStatement = tclStatement;
        this.operationType = operationType;
        backendTransactionManager = new BackendTransactionManager(backendConnection);
    }
    
    @Override
    public ResponseHeader execute() throws SQLException {
        switch (operationType) {
            case BEGIN:
                backendTransactionManager.begin();
                break;
            case SAVEPOINT:
                backendTransactionManager.setSavepoint(((SavepointStatement) tclStatement).getSavepointName());
                break;
            case ROLLBACK_TO_SAVEPOINT:
                backendTransactionManager.rollbackTo(((RollbackToSavepointStatement) tclStatement).getSavepointName());
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
