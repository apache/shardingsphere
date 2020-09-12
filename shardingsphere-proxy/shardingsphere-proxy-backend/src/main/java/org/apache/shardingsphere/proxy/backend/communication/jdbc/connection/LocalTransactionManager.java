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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.connection;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Local transaction manager.
 */
@RequiredArgsConstructor
public final class LocalTransactionManager implements TransactionManager {
    
    private final BackendConnection connection;
    
    @Override
    public void begin() {
        recordMethodInvocation(Connection.class, "setAutoCommit", new Class[]{boolean.class}, new Object[]{false});
    }
    
    @Override
    public void commit() throws SQLException {
        if (connection.getStatusManager().isInTransaction()) {
            Collection<SQLException> exceptions = new LinkedList<>(commitConnections());
            throwSQLExceptionIfNecessary(exceptions);
        }
    }
    
    @Override
    public void rollback() throws SQLException {
        if (connection.getStatusManager().isInTransaction()) {
            Collection<SQLException> exceptions = new LinkedList<>(rollbackConnections());
            throwSQLExceptionIfNecessary(exceptions);
        }
    }
    
    private Collection<SQLException> commitConnections() {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : connection.getCachedConnections().values()) {
            try {
                each.commit();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        return result;
    }
    
    private Collection<SQLException> rollbackConnections() {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : connection.getCachedConnections().values()) {
            try {
                each.rollback();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        return result;
    }
    
    private void throwSQLExceptionIfNecessary(final Collection<SQLException> exceptions) throws SQLException {
        if (exceptions.isEmpty()) {
            return;
        }
        SQLException ex = new SQLException();
        for (SQLException each : exceptions) {
            ex.setNextException(each);
        }
        throw ex;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void recordMethodInvocation(final Class<?> targetClass, final String methodName, final Class<?>[] argumentTypes, final Object[] arguments) {
        connection.getMethodInvocations().add(new MethodInvocation(targetClass.getMethod(methodName, argumentTypes), arguments));
    }
}
