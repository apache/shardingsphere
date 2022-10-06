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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.proxy.backend.communication.TransactionManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.transaction.ConnectionSavepointManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Local transaction manager.
 */
@RequiredArgsConstructor
public final class LocalTransactionManager implements TransactionManager<Void> {
    
    private final JDBCBackendConnection connection;
    
    @Override
    public Void begin() {
        connection.getConnectionPostProcessors().add(target -> {
            try {
                target.setAutoCommit(false);
            } catch (final SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        return null;
    }
    
    @Override
    public Void commit() throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        if (connection.getConnectionSession().getTransactionStatus().isRollbackOnly()) {
            exceptions.addAll(rollbackConnections());
        } else {
            exceptions.addAll(commitConnections());
        }
        throwSQLExceptionIfNecessary(exceptions);
        return null;
    }
    
    private Collection<SQLException> commitConnections() {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : connection.getCachedConnections().values()) {
            try {
                each.commit();
            } catch (final SQLException ex) {
                result.add(ex);
            } finally {
                ConnectionSavepointManager.getInstance().transactionFinished(each);
            }
        }
        return result;
    }
    
    @Override
    public Void rollback() throws SQLException {
        if (connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            Collection<SQLException> exceptions = new LinkedList<>(rollbackConnections());
            throwSQLExceptionIfNecessary(exceptions);
        }
        return null;
    }
    
    private Collection<SQLException> rollbackConnections() {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : connection.getCachedConnections().values()) {
            try {
                each.rollback();
            } catch (final SQLException ex) {
                result.add(ex);
            } finally {
                ConnectionSavepointManager.getInstance().transactionFinished(each);
            }
        }
        return result;
    }
    
    @Override
    public Void setSavepoint(final String savepointName) {
        return null;
    }
    
    @Override
    public Void rollbackTo(final String savepointName) {
        return null;
    }
    
    @Override
    public Void releaseSavepoint(final String savepointName) {
        return null;
    }
    
    private Void throwSQLExceptionIfNecessary(final Collection<SQLException> exceptions) throws SQLException {
        if (exceptions.isEmpty()) {
            return null;
        }
        SQLException parentException = null;
        for (SQLException ex : exceptions) {
            if (parentException != null) {
                ex.setNextException(parentException);
            }
            parentException = ex;
        }
        throw parentException;
    }
}
