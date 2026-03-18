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

package org.apache.shardingsphere.transaction;

import lombok.Getter;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.spi.ShardingSphereDistributedTransactionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Connection transaction.
 */
public final class ConnectionTransaction {
    
    @Getter
    private final TransactionType transactionType;
    
    @Getter
    private final ShardingSphereDistributedTransactionManager distributedTransactionManager;
    
    private final TransactionConnectionContext transactionContext;
    
    public ConnectionTransaction(final TransactionRule rule, final TransactionConnectionContext transactionContext) {
        transactionType = transactionContext.getTransactionType().isPresent() ? TransactionType.valueOf(transactionContext.getTransactionType().get()) : rule.getDefaultType();
        this.transactionContext = transactionContext;
        if (transactionContext.getTransactionManager().isPresent()) {
            distributedTransactionManager = (ShardingSphereDistributedTransactionManager) transactionContext.getTransactionManager().get();
        } else {
            distributedTransactionManager = TransactionType.LOCAL == transactionType ? null : rule.getResource().getTransactionManager(rule.getDefaultType());
        }
    }
    
    /**
     * Whether in distributed transaction.
     *
     * @param transactionContext transaction connection context
     * @return in distributed transaction or not
     */
    public boolean isInDistributedTransaction(final TransactionConnectionContext transactionContext) {
        return transactionContext.isTransactionStarted() && isInDistributedTransaction();
    }
    
    /**
     * Whether in distributed transaction.
     *
     * @return in distributed transaction or not
     */
    public boolean isInDistributedTransaction() {
        return null != distributedTransactionManager && distributedTransactionManager.isInTransaction();
    }
    
    /**
     * Judge is local transaction or not.
     *
     * @return is local transaction or not
     */
    public boolean isLocalTransaction() {
        return TransactionType.LOCAL == transactionType;
    }
    
    /**
     * Whether hold transaction.
     *
     * @param autoCommit is auto commit
     * @return hold transaction or not
     */
    public boolean isHoldTransaction(final boolean autoCommit) {
        return TransactionType.LOCAL == transactionType && !autoCommit || TransactionType.XA == transactionType && isInDistributedTransaction();
    }
    
    /**
     * Get connection in transaction.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @param transactionConnectionContext transaction connection context
     * @return connection in transaction
     * @throws SQLException SQL exception
     */
    public Optional<Connection> getConnection(final String databaseName, final String dataSourceName, final TransactionConnectionContext transactionConnectionContext) throws SQLException {
        return isInDistributedTransaction(transactionConnectionContext) ? Optional.of(distributedTransactionManager.getConnection(databaseName, dataSourceName)) : Optional.empty();
    }
    
    /**
     * Begin transaction.
     */
    public void begin() {
        distributedTransactionManager.begin();
    }
    
    /**
     * Commit transaction.
     */
    public void commit() {
        distributedTransactionManager.commit(transactionContext.isExceptionOccur());
    }
    
    /**
     * Rollback transaction.
     */
    public void rollback() {
        distributedTransactionManager.rollback();
    }
    
    /**
     * Get distributed transaction operation type.
     *
     * @param autoCommit is auto commit
     * @return got distributed transaction operation type
     */
    public Optional<DistributedTransactionOperationType> getDistributedTransactionOperationType(final boolean autoCommit) {
        if (!autoCommit && !distributedTransactionManager.isInTransaction()) {
            return Optional.of(DistributedTransactionOperationType.BEGIN);
        }
        if (autoCommit && distributedTransactionManager.isInTransaction()) {
            return Optional.of(DistributedTransactionOperationType.COMMIT);
        }
        return Optional.empty();
    }
    
    public enum DistributedTransactionOperationType {
        
        BEGIN, COMMIT
    }
}
