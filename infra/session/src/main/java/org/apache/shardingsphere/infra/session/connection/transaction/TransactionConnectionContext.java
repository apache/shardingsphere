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

package org.apache.shardingsphere.infra.session.connection.transaction;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Transaction connection context.
 */
@Getter
public final class TransactionConnectionContext implements AutoCloseable {
    
    private volatile String transactionType;
    
    private volatile boolean inTransaction;
    
    @Setter
    private volatile long beginMillis;
    
    @Setter
    private volatile boolean exceptionOccur;
    
    @Setter
    private volatile String readWriteSplitReplicaRoute;
    
    private AtomicReference<TransactionManager> transactionManager;
    
    /**
     * Begin transaction.
     *
     * @param transactionType transaction type
     * @param transactionManager transaction manager
     */
    public void beginTransaction(final String transactionType, final TransactionManager transactionManager) {
        this.transactionType = transactionType;
        inTransaction = true;
        this.transactionManager = new AtomicReference<>(transactionManager);
    }
    
    /**
     * Judge is in distributed transaction or not.
     *
     * @return in distributed transaction or not
     */
    public boolean isDistributedTransactionStarted() {
        return isTransactionStarted() && ("XA".equals(transactionType) || "BASE".equals(transactionType));
    }
    
    /**
     * Get transaction type. 
     *
     * @return transaction type
     */
    public Optional<String> getTransactionType() {
        return Optional.ofNullable(transactionType);
    }
    
    /**
     * Get read write split replica route. 
     *
     * @return read write split replica route
     */
    public Optional<String> getReadWriteSplitReplicaRoute() {
        return Optional.ofNullable(readWriteSplitReplicaRoute);
    }
    
    /**
     * Get transaction manager.
     *
     * @return transaction manager
     */
    public Optional<TransactionManager> getTransactionManager() {
        return null == transactionManager ? Optional.empty() : Optional.ofNullable(transactionManager.get());
    }
    
    /**
     * Judge transaction is started or not.
     *
     * @return whether transaction is started or not
     */
    public boolean isTransactionStarted() {
        return inTransaction;
    }
    
    @Override
    public void close() {
        transactionType = null;
        inTransaction = false;
        beginMillis = 0L;
        exceptionOccur = false;
        readWriteSplitReplicaRoute = null;
        transactionManager = null;
    }
}
