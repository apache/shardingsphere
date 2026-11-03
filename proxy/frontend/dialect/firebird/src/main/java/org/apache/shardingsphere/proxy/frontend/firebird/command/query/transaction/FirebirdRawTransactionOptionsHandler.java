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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.jdbc.FBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Applies a client transaction parameter buffer to backend connections.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
final class FirebirdRawTransactionOptionsHandler {
    
    private final byte[] rawTpb;
    
    private final Map<Connection, SavedParameters> previousParameters = new ConcurrentHashMap<>();
    
    /**
     * Start the backend transaction with the client transaction parameters.
     *
     * @param databaseName logical database name
     * @param dataSourceName storage unit name
     * @param connection backend connection
     * @throws SQLException SQL exception
     */
    void initialize(final String databaseName, final String dataSourceName, final Connection connection) throws SQLException {
        FBConnection firebird = connection.unwrap(FBConnection.class);
        previousParameters.put(connection, new SavedParameters(firebird, firebird.getManagedConnection().getTransactionParameters().deepCopy()));
        connection.setAutoCommit(false);
        firebird.setTransactionParameters(new FirebirdRawTransactionParameterBuffer(rawTpb));
        firebird.getLocalTransaction().begin();
    }
    
    /**
     * Restore the connection's parameters before returning it to the pool.
     *
     * @param connection backend connection
     * @throws SQLException if the transaction or parameters cannot be restored
     */
    void release(final Connection connection) throws SQLException {
        SavedParameters previous = previousParameters.remove(connection);
        if (null == previous || previous.connection.isClosed()) {
            return;
        }
        try {
            if (previous.connection.getLocalTransaction().inTransaction()) {
                connection.rollback();
            }
            previous.connection.setTransactionParameters(previous.parameters);
        } catch (final SQLException ex) {
            abortOnFailure(previous.connection, ex);
            throw ex;
        }
    }
    
    private void abortOnFailure(final FBConnection connection, final SQLException cause) {
        try {
            connection.getFbDatabase().cancelOperation(ISCConstants.fb_cancel_abort);
        } catch (final SQLException ex) {
            cause.addSuppressed(ex);
        }
    }
    
    @RequiredArgsConstructor
    private static final class SavedParameters {
        
        private final FBConnection connection;
        
        private final TransactionParameterBuffer parameters;
    }
}
