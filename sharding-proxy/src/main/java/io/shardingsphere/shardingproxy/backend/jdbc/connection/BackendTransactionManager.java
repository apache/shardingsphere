/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend.jdbc.connection;

import com.google.common.base.Preconditions;
import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.event.transaction.ShardingTransactionEvent;
import io.shardingsphere.core.event.transaction.xa.XATransactionEvent;
import io.shardingsphere.spi.transaction.ShardingTransactionHandler;
import io.shardingsphere.spi.transaction.ShardingTransactionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Proxy transaction manager.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public class BackendTransactionManager {
    
    private final BackendConnection connection;
    
    /**
     * Handle proxy transaction.
     *
     * @param operationType transaction operation type
     * @throws SQLException SQL Exception
     */
    public void doInTransaction(final TransactionOperationType operationType) throws SQLException {
        TransactionType transactionType = connection.getTransactionType();
        ShardingTransactionHandler<ShardingTransactionEvent> shardingTransactionHandler = ShardingTransactionHandlerRegistry.getInstance().getHandler(transactionType);
        if (null != transactionType && transactionType != TransactionType.LOCAL) {
            Preconditions.checkNotNull(shardingTransactionHandler, String.format("Cannot find transaction manager of [%s]", transactionType));
        }
        if (TransactionOperationType.BEGIN == operationType && ConnectionStatus.TRANSACTION != connection.getStatus()) {
            connection.setStatus(ConnectionStatus.TRANSACTION);
            connection.releaseConnections(false);
        }
        if (TransactionType.LOCAL == transactionType) {
            doLocalTransaction(operationType);
        } else if (TransactionType.XA == transactionType) {
            shardingTransactionHandler.doInTransaction(new XATransactionEvent(operationType));
            if (TransactionOperationType.BEGIN != operationType) {
                connection.setStatus(ConnectionStatus.TERMINATED);
            }
        }
    }
    
    private void doLocalTransaction(final TransactionOperationType operationType) throws SQLException {
        switch (operationType) {
            case BEGIN:
                setAutoCommit();
                break;
            case COMMIT:
                commit();
                break;
            case ROLLBACK:
                rollback();
                break;
            default:
        }
    }
    
    private void setAutoCommit() {
        recordMethodInvocation(Connection.class, "setAutoCommit", new Class[]{boolean.class}, new Object[]{false});
    }
    
    private void commit() throws SQLException {
        if (ConnectionStatus.TRANSACTION == connection.getStatus()) {
            Collection<SQLException> exceptions = new LinkedList<>();
            exceptions.addAll(commitConnections());
            connection.setStatus(ConnectionStatus.TERMINATED);
            throwSQLExceptionIfNecessary(exceptions);
        }
    }
    
    private void rollback() throws SQLException {
        if (ConnectionStatus.TRANSACTION == connection.getStatus()) {
            Collection<SQLException> exceptions = new LinkedList<>();
            exceptions.addAll(rollbackConnections());
            connection.setStatus(ConnectionStatus.TERMINATED);
            throwSQLExceptionIfNecessary(exceptions);
        }
    }
    
    private Collection<SQLException> commitConnections() {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : connection.getCachedConnections().values()) {
            try {
                each.commit();
            } catch (SQLException ex) {
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
            } catch (SQLException ex) {
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
    
    @SneakyThrows
    private void recordMethodInvocation(final Class<?> targetClass, final String methodName, final Class<?>[] argumentTypes, final Object[] arguments) {
        connection.getMethodInvocations().add(new MethodInvocation(targetClass.getMethod(methodName, argumentTypes), arguments));
    }
}
