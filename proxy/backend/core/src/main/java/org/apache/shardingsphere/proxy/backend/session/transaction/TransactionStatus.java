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

package org.apache.shardingsphere.proxy.backend.session.transaction;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.exception.SwitchTypeInTransactionException;

/**
 * Transaction status.
 */
@Getter
public final class TransactionStatus {
    
    @Setter
    private volatile boolean inTransaction;
    
    private volatile TransactionType transactionType;
    
    @Setter
    private volatile boolean exceptionOccur;
    
    public TransactionStatus(final TransactionType initialTransactionType) {
        transactionType = initialTransactionType;
    }
    
    /**
     * Change transaction type of current channel.
     *
     * @param transactionType transaction type
     */
    public void setTransactionType(final TransactionType transactionType) {
        ShardingSpherePreconditions.checkState(!inTransaction, SwitchTypeInTransactionException::new);
        this.transactionType = transactionType;
    }
    
    /**
     * Judge whether in connection held transaction.
     * 
     * @return is in connection held transaction or not
     */
    public boolean isInConnectionHeldTransaction() {
        return inTransaction && TransactionType.BASE != transactionType;
    }
}
