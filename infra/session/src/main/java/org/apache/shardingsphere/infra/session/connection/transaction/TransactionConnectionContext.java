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

/**
 * Transaction connection context.
 */
@Getter
public final class TransactionConnectionContext implements AutoCloseable {
    
    private volatile String transactionType;
    
    private volatile boolean inTransaction;
    
    @Setter
    private volatile long beginMills;
    
    @Setter
    private volatile String readWriteSplitReplicaRoute;
    
    /**
     * Begin transaction.
     *
     * @param transactionType transaction type 
     */
    public void beginTransaction(final String transactionType) {
        this.transactionType = transactionType;
        inTransaction = true;
    }
    
    /**
     * Judge is in distributed transaction or not.
     *
     * @return in distributed transaction or not
     */
    public boolean isInDistributedTransaction() {
        return inTransaction && ("XA".equals(transactionType) || "BASE".equals(transactionType));
    }
    
    @Override
    public void close() {
        inTransaction = false;
        beginMills = 0L;
        readWriteSplitReplicaRoute = null;
    }
}
